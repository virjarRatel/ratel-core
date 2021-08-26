package org.jf.pxb.googlecode.dex2jar.reader.io;

import java.io.IOException;

public interface DataOut {
    void writeByte(int b) throws IOException;

    void writeBytes(byte[] bs) throws IOException;

    void writeInt(int i) throws IOException;

    void writeShort(int i) throws IOException;
}
