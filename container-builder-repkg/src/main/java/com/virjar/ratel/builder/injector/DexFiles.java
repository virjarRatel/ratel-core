package com.virjar.ratel.builder.injector;

import com.virjar.ratel.allcommon.ReflectUtil;
import com.virjar.ratel.builder.BuildParamMeta;
import com.virjar.ratel.builder.Util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;
import org.jf.dexlib2.dexbacked.OatFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.stream.Stream;

/**
 * 处理和缓存所有的Dex文件
 */
public class DexFiles {

    public DexFiles(ZipFile zipFile) throws IOException {
        dexFileMap = new TreeMap<>();
        init(zipFile);
    }

    private void init(ZipFile zipFile) throws IOException {
        Enumeration<ZipEntry> entries = zipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            String name = zipEntry.getName();
            if (name.equals("classes.dex")) {
                DexFile dexFile = new DexFile(zipFile, zipEntry, 0);
                dexFileMap.put(0, dexFile);
                continue;
            }
            Matcher matcher = Util.classesIndexPattern.matcher(name);
            if (matcher.matches()) {
                int index = NumberUtils.toInt(matcher.group(1));
                dexFileMap.put(index, new DexFile(zipFile, zipEntry, index));
            }

        }
    }

    private final TreeMap<Integer, DexFile> dexFileMap;

    public DexFile findClassInDex(String className) {
        for (DexFile dexFile : dexFileMap.values()) {
            if (dexFile.getClassDef(className) != null) {
                return dexFile;
            }
        }
        return null;
    }

    public void appendDex(byte[] bytes) {
        Integer lastDex = dexFileMap.lastKey();
        dexFileMap.put(lastDex + 1, new DexFile(lastDex + 1, bytes));
    }

    public Collection<DexFile> dexFiles() {
        return dexFileMap.values();
    }


    public class DexFile {
        private byte[] rawData;
        private DexBackedDexFile dexBackedDexFile;
        private Set<DexBackedClassDef> classesSet;
        private Map<String, DexBackedClassDef> classNameMap;
        private final int index;

        DexFile(ZipFile zipFile, ZipEntry zipEntry, int index) throws IOException {
            InputStream inputStream = zipFile.getInputStream(zipEntry);
            setRawData(IOUtils.toByteArray(inputStream));
            this.index = index;
        }

        DexFile(int index, byte[] data) {
            setRawData(data);
            this.index = index;
        }

        public void setRawData(byte[] rawData) {
            if (this.rawData != null) {
                this.rawData = null;
                this.dexBackedDexFile = null;
                // 覆盖dex的时候，执行一次gc，释放部分空间，避免oom
                System.gc();
                System.gc();
            }
            this.rawData = rawData;
            try {
                dexBackedDexFile = loadDexFile(rawData, Opcodes.getDefault());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            classesSet = new HashSet<>();
            classNameMap = new HashMap<>();
            for (DexBackedClassDef classDef : dexBackedDexFile.getClasses()) {
                classesSet.add(classDef);
                classNameMap.put(Util.descriptorToDot(classDef.getType()), classDef);
            }
        }

        public Set<? extends DexBackedClassDef> getClasses() {
            return classesSet;
        }

        public byte[] getRawData() {
            return rawData;
        }

        public int getIndex() {
            return index;
        }

        public DexBackedClassDef getClassDef(String name) {
            return classNameMap.get(name);
        }

        public DexBackedDexFile getDexBackedDexFile() {
            return dexBackedDexFile;
        }

        public DexFiles getDexFiles() {
            return DexFiles.this;
        }

        public void splitIfNeed(BuildParamMeta buildParamMeta, Set<String> mainClasses) {
            boolean needSplit = Stream.of("stringCount", "typeCount", "protoCount", "fieldCount", "methodCount", "classCount").anyMatch(fieldName -> {
                // 如果存在 overflow的可能，那么提前执行split
                // 任何一个字段超过80%的容量，即需要拆分class
                return ReflectUtil.getIntField(dexBackedDexFile, fieldName) > (65535 * 0.8);
            });

            if (!needSplit) {
                return;
            }
            System.out.println("need split dex because of dex maybe overflow");
            DexSplitterV2.splitDex(this, buildParamMeta, mainClasses);
        }
    }

    public static DexBackedDexFile loadDexFile(byte[] data, Opcodes opcodes) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            try {
                return DexBackedDexFile.fromInputStream(opcodes, inputStream);
            } catch (DexBackedDexFile.NotADexFile ex) {
                // just eat it
            }

            try {
                return DexBackedOdexFile.fromInputStream(opcodes, inputStream);
            } catch (DexBackedOdexFile.NotAnOdexFile ex) {
                // just eat it
            }

            // Note: DexBackedDexFile.fromInputStream and DexBackedOdexFile.fromInputStream will reset inputStream
            // back to the same position, if they fails

            OatFile oatFile = null;
            try {
                oatFile = OatFile.fromInputStream(inputStream);
            } catch (OatFile.NotAnOatFileException ex) {
                // just eat it
            }

            if (oatFile != null) {
                if (oatFile.isSupportedVersion() == OatFile.UNSUPPORTED) {
                    throw new DexFileFactory.UnsupportedOatVersionException(oatFile);
                }

                List<DexBackedDexFile> oatDexFiles = oatFile.getDexFiles();

                if (oatDexFiles.size() == 0) {
                    throw new DexFileFactory.DexFileNotFoundException("Oat file contains no dex files");
                }

                return oatDexFiles.get(0);
            }
        }

        throw new DexFileFactory.UnsupportedFileTypeException(" is not an dex, odex or oat file.");
    }
}
