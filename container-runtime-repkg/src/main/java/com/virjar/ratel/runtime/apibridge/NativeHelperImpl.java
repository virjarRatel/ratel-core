package com.virjar.ratel.runtime.apibridge;

import android.os.Process;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.NativeHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import external.org.apache.commons.io.FileUtils;
import external.org.apache.commons.lang3.StringUtils;

public class NativeHelperImpl implements NativeHelper {
    @Override
    public NativePrtInfo queryMethodNativeInfo(Method method) {
        if (!Modifier.isNative(method.getModifiers())) {
            Log.w(Constants.TAG, "queryNativeMethodPtr from none native method");
            return null;
        }
        if (Modifier.isAbstract(method.getModifiers())) {
            Log.w(Constants.TAG, "queryNativeMethodPtr from abstract method");
            return null;
        }
        long memoryAddress = RatelNative.methodNativePtr(method);

        NativePrtInfo nativePrtInfo = new NativePrtInfo();
        nativePrtInfo.ptr = memoryAddress;
        visitMaps(procmapsStruct -> {
            if (memoryAddress >= procmapsStruct.addr_start
                    && memoryAddress <= procmapsStruct.addr_end) {
                nativePrtInfo.soPath = procmapsStruct.pathname;
                nativePrtInfo.offset = memoryAddress - procmapsStruct.addr_start;
                return true;
            }
            return false;
        });
        return nativePrtInfo;
    }


    public long queryNativeMethodPtr(Method method) {
        if (!Modifier.isNative(method.getModifiers())) {
            Log.w(Constants.TAG, "queryNativeMethodPtr from none native method");
            return 0;
        }
        if (Modifier.isAbstract(method.getModifiers())) {
            Log.w(Constants.TAG, "queryNativeMethodPtr from abstract method");
            return 0;
        }
        return RatelNative.methodNativePtr(method);
    }


    private static List<String> readMapsFile(File mapsFile) {
        try {
            if (mapsFile.exists() && mapsFile.canRead()) {
                return FileUtils.readLines(mapsFile, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            Log.w(Constants.TAG, "read mapsFile: " + mapsFile.getAbsolutePath() + " failed", e);
        }
        return null;
    }

    @Override
    public byte[] dumpSo(Method method) {
        long memoryAddress = queryNativeMethodPtr(method);
        if (memoryAddress < 1024) {
            return null;
        }

        return new MapVisitor() {
            byte[] dumpData = null;

            @Override
            public boolean visit(ProcmapsStruct procmapsStruct) {
                if (memoryAddress >= procmapsStruct.addr_start
                        && memoryAddress <= procmapsStruct.addr_end) {
                    this.dumpData = dumpMemory(procmapsStruct.addr_start, procmapsStruct.length);
                    return true;
                }
                return false;
            }

            public byte[] doDump() {
                visitMaps(this);
                return dumpData;
            }
        }.doDump();
    }

    @Override
    public byte[] dumpMemory(long start, long size) {
        ByteBuffer byteBuffer = RatelNative.dumpMemory(start, size);
        if (byteBuffer == null) {
            return null;
        }
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] buffer = new byte[byteBuffer.capacity()];
        byteBuffer.get(buffer, 0, byteBuffer.capacity());
        return buffer;
    }

    @Override
    public void traceFile(String feature, int mode) {
        RatelNative.traceFilePath(feature);
    }

    @Override
    public void traceFile(String feature, int mode, FileOpCallback fileOpCallback) {
        Log.w(Constants.TAG, "not implement!!");
    }

    private static class ProcmapsStruct {
        long addr_start;    //< start address of the area
        long addr_end;    //< end address
        long length; //< size of the range

        String perm;        //< permissions rwxp
        boolean is_r;            //< rewrote of perm with short flags
        boolean is_w;
        boolean is_x;
        boolean is_p;

        long offset;    //< offset
        String dev;    //< dev major:minor
        int inode;        //< inode of the file that backs the area

        String pathname;        //< the path of the file that backs the area
    }

    private interface MapVisitor {
        boolean visit(ProcmapsStruct procmapsStruct);
    }


    private static void visitMaps(MapVisitor mapVisitor) {
        List<String> mapContent = readMapsFile(new File("/proc/self/maps"));

        if (mapContent == null || mapContent.isEmpty()) {
            mapContent = readMapsFile(new File("/proc/" + Process.myPid() + "/maps"));
        }

        if (mapContent == null || mapContent.isEmpty()) {
            return;
        }
//        String targetMapItem = null;
        // parse maps item
        for (String mapItem : mapContent) {
            if (StringUtils.isBlank(mapItem)) {
                continue;
            }
            //ab629000-ab62a000 r--p 0005a000 b3:46 214226                             /data/data/com.smile.gifmaker/app_ratel_env_mock/4/data/app_DvaPlugin/zstd_yxcorp/-513600515/so/libzcompress.so
            String[] map = StringUtils.split(mapItem);
            if (map.length == 0) {
                continue;
            }
            ProcmapsStruct procmapsStruct = new ProcmapsStruct();
            String[] space = StringUtils.split(map[0], "-");
            long start = Long.parseLong(space[0], 16);
            long end = Long.parseLong(space[1], 16);

            procmapsStruct.addr_start = start;
            procmapsStruct.addr_end = end;

            procmapsStruct.perm = map[1];

            procmapsStruct.offset = Long.parseLong(map[2], 16);
            procmapsStruct.dev = map[3];
            procmapsStruct.inode = Integer.parseInt(map[4]);
            if (map.length > 5) {
                procmapsStruct.pathname = map[5];
            }

            procmapsStruct.length = procmapsStruct.addr_end - procmapsStruct.addr_start;
            procmapsStruct.is_r = procmapsStruct.perm.charAt(0) == 'r';
            procmapsStruct.is_w = procmapsStruct.perm.charAt(0) == 'w';
            procmapsStruct.is_x = procmapsStruct.perm.charAt(0) == 'x';
            procmapsStruct.is_p = procmapsStruct.perm.charAt(0) == 'p';

            if (mapVisitor.visit(procmapsStruct)) {
                return;
            }
        }

    }
}
