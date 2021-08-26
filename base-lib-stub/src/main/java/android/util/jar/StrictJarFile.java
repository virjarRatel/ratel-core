/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package android.util.jar;

import java.io.FileDescriptor;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;

/**
 * A subset of the JarFile API implemented as a thin wrapper over
 * system/core/libziparchive.
 *
 * @hide for internal use only. Not API compatible (or as forgiving) as
 *        {@link java.util.jar.JarFile}
 */
public final class StrictJarFile {

    public StrictJarFile(String fileName)
            throws IOException, SecurityException {
        this(fileName, true, true);
    }

    public StrictJarFile(FileDescriptor fd)
            throws IOException, SecurityException {
        this(fd, true, true);
    }

    public StrictJarFile(FileDescriptor fd,
            boolean verify,
            boolean signatureSchemeRollbackProtectionsEnforced)
                    throws IOException, SecurityException {
        throw new UnsupportedOperationException("STUB");
    }

    public StrictJarFile(String fileName,
            boolean verify,
            boolean signatureSchemeRollbackProtectionsEnforced)
                    throws IOException, SecurityException {
        throw new UnsupportedOperationException("STUB");
    }

    public StrictJarManifest getManifest() {
        throw new UnsupportedOperationException("STUB");
    }

    public Iterator<ZipEntry> iterator() throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    public ZipEntry findEntry(String name) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Return all certificate chains for a given {@link ZipEntry} belonging to this jar.
     * This method MUST be called only after fully exhausting the InputStream belonging
     * to this entry.
     *
     * Returns {@code null} if this jar file isn't signed or if this method is
     * called before the stream is processed.
     */
    public Certificate[][] getCertificateChains(ZipEntry ze) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Return all certificates for a given {@link ZipEntry} belonging to this jar.
     * This method MUST be called only after fully exhausting the InputStream belonging
     * to this entry.
     *
     * Returns {@code null} if this jar file isn't signed or if this method is
     * called before the stream is processed.
     *
     * @deprecated Switch callers to use getCertificateChains instead
     */
    @Deprecated
    public Certificate[] getCertificates(ZipEntry ze) {
        throw new UnsupportedOperationException("STUB");
    }

    public InputStream getInputStream(ZipEntry ze) {
        throw new UnsupportedOperationException("STUB");
    }

    public void close() throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    static final class EntryIterator implements Iterator<ZipEntry> {

        EntryIterator(long nativeHandle, String prefix) throws IOException {
            throw new UnsupportedOperationException("STUB");
        }

        public ZipEntry next() {
            throw new UnsupportedOperationException("STUB");
        }

        public boolean hasNext() {
            throw new UnsupportedOperationException("STUB");
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static final class JarFileInputStream extends FilterInputStream {

        JarFileInputStream(InputStream is, long size, StrictJarVerifier.VerifierEntry e) {
            super(is);
            throw new UnsupportedOperationException("STUB");
        }

        @Override
        public int read() throws IOException {
            throw new UnsupportedOperationException("STUB");
        }

        @Override
        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            throw new UnsupportedOperationException("STUB");
        }

        @Override
        public int available() throws IOException {
            throw new UnsupportedOperationException("STUB");
        }

        @Override
        public long skip(long byteCount) throws IOException {
            throw new UnsupportedOperationException("STUB");
        }
    }

    /** @hide */
    public static class ZipInflaterInputStream extends InflaterInputStream {

        public ZipInflaterInputStream(InputStream is, Inflater inf, int bsize, ZipEntry entry) {
            super(is, inf, bsize);
            throw new UnsupportedOperationException("STUB");
        }

        @Override public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            throw new UnsupportedOperationException("STUB");
        }

        @Override public int available() throws IOException {
            throw new UnsupportedOperationException("STUB");
        }
    }

    /**
     * Wrap a stream around a FileDescriptor.  The file descriptor is shared
     * among all streams returned by getInputStream(), so we have to synchronize
     * access to it.  (We can optimize this by adding buffering here to reduce
     * collisions.)
     *
     * <p>We could support mark/reset, but we don't currently need them.
     *
     * @hide
     */
    public static class FDStream extends InputStream {

        public FDStream(FileDescriptor fd, long initialOffset, long endOffset) {
            throw new UnsupportedOperationException("STUB");
        }

        @Override public int available() throws IOException {
            throw new UnsupportedOperationException("STUB");
        }

        @Override public int read() throws IOException {
            throw new UnsupportedOperationException("STUB");
        }

        @Override public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            throw new UnsupportedOperationException("STUB");
        }

        @Override public long skip(long byteCount) throws IOException {
            throw new UnsupportedOperationException("STUB");
        }
    }

}
