package com.virjar.ratel.rdp;

/**
 * Copyright (C) 2018 Ryszard Wiśniewski <brut.alll@gmail.com>
 * Copyright (C) 2018 Connor Tumbleson <connor.tumbleson@gmail.com>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.antlr.runtime.RecognitionException;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.FileDataStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;


/**
 * @author Ryszard Wiśniewski <brut.alll@gmail.com>
 */
public class SmaliBuilder {
    public static void build(File smaliDir, File dexFile, int apiLevel, boolean printLog) throws IOException {
        new SmaliBuilder(smaliDir, dexFile, apiLevel).build(printLog);
    }

    public static void build(File smaliDir, File dexFile) throws IOException {
        new SmaliBuilder(smaliDir, dexFile, 0).build(true);
    }

    private SmaliBuilder(File smaliDir, File dexFile, int apiLevel) {
        mSmaliDir = smaliDir;
        mDexFile = dexFile;
        mApiLevel = apiLevel;
    }

    private void build(boolean printLog) throws IOException {

        DexBuilder dexBuilder;
        if (mApiLevel > 0) {
            dexBuilder = new DexBuilder(Opcodes.forApi(mApiLevel));
        } else {
            dexBuilder = new DexBuilder(Opcodes.getDefault());
        }

        for (File fileName : mSmaliDir.listFiles()) {
            buildFile(fileName, dexBuilder, printLog);
        }
        dexBuilder.writeTo(new FileDataStore(new File(mDexFile.getAbsolutePath())));

    }

    private void buildFile(File inFile, DexBuilder dexBuilder, boolean printLog)
            throws IOException {
        if (inFile.isDirectory()) {
            for (File fileName : inFile.listFiles()) {
                buildFile(fileName, dexBuilder, printLog);
            }
            return;
        }
        //File inFile = new File(mSmaliDir, fileName);
        //System.out.println("build smali file: " + inFile.getAbsolutePath());
        if (printLog) {
            System.out.println("build smali file: " + inFile.getAbsolutePath());
        }
        InputStream inStream = new FileInputStream(inFile);

        if (inFile.getName().endsWith(".smali")) {
            try {
                if (!SmaliMod.assembleSmaliFile(inFile, dexBuilder, false, false)) {
                    throw new IOException("Could not smali file: " + inFile);
                }
            } catch (RecognitionException ex) {
                throw new IOException(ex);
            }
        } else {
            LOGGER.warning("Unknown file type, ignoring: " + inFile);
        }
        inStream.close();
    }

    private final File mSmaliDir;
    private final File mDexFile;
    private int mApiLevel = 0;

    private final static Logger LOGGER = Logger.getLogger(SmaliBuilder.class.getName());
}

