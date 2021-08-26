/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.util.jar;

import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Hashtable;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * Non-public class used by {@link JarFile} and {@link JarInputStream} to manage
 * the verification of signed JARs. {@code JarFile} and {@code JarInputStream}
 * objects are expected to have a {@code JarVerifier} instance member which
 * can be used to carry out the tasks associated with verifying a signed JAR.
 * These tasks would typically include:
 * <ul>
 * <li>verification of all signed signature files
 * <li>confirmation that all signed data was signed only by the party or parties
 * specified in the signature block data
 * <li>verification that the contents of all signature files (i.e. {@code .SF}
 * files) agree with the JAR entries information found in the JAR manifest.
 * </ul>
 */
class StrictJarVerifier {

    /**
     * Stores and a hash and a message digest and verifies that massage digest
     * matches the hash.
     */
    static class VerifierEntry extends OutputStream {

        VerifierEntry(String name, MessageDigest digest, byte[] hash,
                Certificate[][] certChains, Hashtable<String, Certificate[][]> verifedEntries) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Updates a digest with one byte.
         */
        @Override
        public void write(int value) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Updates a digest with byte array.
         */
        @Override
        public void write(byte[] buf, int off, int nbytes) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Verifies that the digests stored in the manifest match the decrypted
         * digests from the .SF file. This indicates the validity of the
         * signing, not the integrity of the file, as its digest must be
         * calculated and verified when its contents are read.
         *
         * @throws SecurityException
         *             if the digest value stored in the manifest does <i>not</i>
         *             agree with the decrypted digest as recovered from the
         *             <code>.SF</code> file.
         */
        void verify() {
            throw new UnsupportedOperationException("STUB");
        }
    }
}
