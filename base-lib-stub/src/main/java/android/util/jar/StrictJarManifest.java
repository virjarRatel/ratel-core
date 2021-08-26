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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.Attributes;

/**
 * The {@code StrictJarManifest} class is used to obtain attribute information for a
 * {@code StrictJarFile} and its entries.
 *
 * @hide
 */
public class StrictJarManifest implements Cloneable {

    static final class Chunk {
        final int start;
        final int end;

        Chunk(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Creates a new {@code StrictJarManifest} instance.
     */
    public StrictJarManifest() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Creates a new {@code StrictJarManifest} instance using the attributes obtained
     * from the input stream.
     *
     * @param is
     *            {@code InputStream} to parse for attributes.
     * @throws IOException
     *             if an IO error occurs while creating this {@code StrictJarManifest}
     */
    public StrictJarManifest(InputStream is) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Creates a new {@code StrictJarManifest} instance. The new instance will have the
     * same attributes as those found in the parameter {@code StrictJarManifest}.
     *
     * @param man
     *            {@code StrictJarManifest} instance to obtain attributes from.
     */
    @SuppressWarnings("unchecked")
    public StrictJarManifest(StrictJarManifest man) {
        throw new UnsupportedOperationException("STUB");
    }

    StrictJarManifest(byte[] manifestBytes, boolean readChunks) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Resets the both the main attributes as well as the entry attributes
     * associated with this {@code StrictJarManifest}.
     */
    public void clear() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Returns the {@code Attributes} associated with the parameter entry
     * {@code name}.
     *
     * @param name
     *            the name of the entry to obtain {@code Attributes} from.
     * @return the Attributes for the entry or {@code null} if the entry does
     *         not exist.
     */
    public Attributes getAttributes(String name) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Returns a map containing the {@code Attributes} for each entry in the
     * {@code StrictJarManifest}.
     *
     * @return the map of entry attributes.
     */
    public Map<String, Attributes> getEntries() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Returns the main {@code Attributes} of the {@code JarFile}.
     *
     * @return main {@code Attributes} associated with the source {@code
     *         JarFile}.
     */
    public Attributes getMainAttributes() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Creates a copy of this {@code StrictJarManifest}. The returned {@code StrictJarManifest}
     * will equal the {@code StrictJarManifest} from which it was cloned.
     *
     * @return a copy of this instance.
     */
    @Override
    public Object clone() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Writes this {@code StrictJarManifest}'s name/attributes pairs to the given {@code OutputStream}.
     * The {@code MANIFEST_VERSION} or {@code SIGNATURE_VERSION} attribute must be set before
     * calling this method, or no attributes will be written.
     *
     * @throws IOException
     *             If an error occurs writing the {@code StrictJarManifest}.
     */
    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Merges name/attribute pairs read from the input stream {@code is} into this manifest.
     *
     * @param is
     *            The {@code InputStream} to read from.
     * @throws IOException
     *             If an error occurs reading the manifest.
     */
    public void read(InputStream is) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Returns the hash code for this instance.
     *
     * @return this {@code StrictJarManifest}'s hashCode.
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Determines if the receiver is equal to the parameter object. Two {@code
     * StrictJarManifest}s are equal if they have identical main attributes as well as
     * identical entry attributes.
     *
     * @param o
     *            the object to compare against.
     * @return {@code true} if the manifests are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("STUB");
    }
}
