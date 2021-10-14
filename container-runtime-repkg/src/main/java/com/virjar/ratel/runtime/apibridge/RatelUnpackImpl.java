package com.virjar.ratel.runtime.apibridge;

import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.RatelUnpack;
import com.virjar.ratel.api.inspect.Lists;
import com.virjar.ratel.hook.sandcompat.hookstub.DexMakerOptConfig;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.unpack.DexFixer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;

import external.com.android.dex.ClassDef;
import external.com.android.dex.Dex;
import external.com.android.dex.DexFormat;
import external.org.apache.commons.lang3.StringUtils;

public class RatelUnpackImpl implements RatelUnpack {
    private static File theWorkDir = null;

    @Override
    public File getWorkDir() {
        return theWorkDir;
    }

    @Override
    public void enableUnPack(File workDir, boolean dumpMethod) {
        if (theWorkDir != null) {
            Log.e(Constants.TAG, "the unpack init already");
            return;
        }
        if (workDir == null) {
            workDir = new File(RatelEnvironment.ratelResourceDir(), "unpack");
        }
        if (workDir.isFile()) {
            throw new IllegalStateException("the work dir:" + workDir.getAbsolutePath() + " must be directory");
        }
        theWorkDir = workDir;
        if (!theWorkDir.exists()) {
            theWorkDir.mkdirs();
        }
        String absolutePath = theWorkDir.getAbsolutePath();
        if (!absolutePath.endsWith("/")) {
            absolutePath = absolutePath + "/";
        }
        Log.i(Constants.TAG_UNPACK, "enableUnpackComponent for path:" + absolutePath);
        RatelNative.enableUnpackComponent(absolutePath, dumpMethod);
        if (!absolutePath.startsWith(RatelToolKit.whiteSdcardDirPath)) {
            RatelNative.whitelist(absolutePath);
        }
    }

    @Override
    public List<byte[]> findDumpedDex(String className) {
        File[] files = theWorkDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getAbsolutePath().endsWith(".dex");
            }
        });
        if (files == null) {
            return Collections.emptyList();
        }
        List<byte[]> ret = Lists.newLinkedList();
        for (File dexFile : files) {
            try {
                handleDexFileFix(dexFile, ret, className);
            } catch (IOException e) {
                Log.e(Constants.TAG_UNPACK, "handle dex file failed", e);
            }
        }
        return ret;
    }

    private static void handleDexFileFix(File dexFile, List<byte[]> ret, String className) throws IOException {
        if (!StringUtils.isBlank(className)) {
            Dex dex = new Dex(dexFile);
            boolean find = false;
            for (ClassDef classDef : dex.classDefs()) {
                if (className.equals(
                        DexMakerOptConfig.descriptorToDot(dex.typeNames().get(classDef.getTypeIndex())))
                ) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                return;
            }
        }

        Dex fixDex = DexFixer.fix(dexFile);
        if (fixDex == null) {
            return;
        }
        ret.add(fixDex.getBytes());
    }


    @Override
    public byte[] methodDex(Member method) {
        if (theWorkDir == null) {
            //如果没有开启脱壳组件，那么默认开启以下，否则底层无法拿到数据
            enableUnPack(null, false);
        }
        ByteBuffer byteBuffer = RatelNative.methodDex(method);
        if (byteBuffer == null) {
            //系统API可能无法拿到内存映射
            return null;
        }
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] buffer = new byte[byteBuffer.capacity()];
        byteBuffer.get(buffer, 0, byteBuffer.capacity());
        if (!DexFormat.isSupportedDexMagic(buffer)) {
            //如果不是合法dex，强行修复dex
            byte[] magic = new byte[]{0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00};
            System.arraycopy(magic, 0, buffer, 0, magic.length);
        }
        return buffer;
    }

}
