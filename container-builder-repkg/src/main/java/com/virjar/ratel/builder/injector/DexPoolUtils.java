package com.virjar.ratel.builder.injector;

import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.IOException;

public class DexPoolUtils {
    public static byte[] encodeDex(DexPool dexPool) {
        try {
            MemoryDataStore originDexFileDataStore = new MemoryDataStore();
            dexPool.writeTo(originDexFileDataStore);
            originDexFileDataStore.close();

            return originDexFileDataStore.getData();
        } catch (IOException e) {
            // not happen ,just eat it
            throw new IllegalStateException(e);
        }
    }
}
