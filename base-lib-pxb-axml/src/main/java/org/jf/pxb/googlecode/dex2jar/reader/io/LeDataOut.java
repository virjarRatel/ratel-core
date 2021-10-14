package org.jf.pxb.googlecode.dex2jar.reader.io;

import java.io.IOException;
import java.io.OutputStream;

public class LeDataOut implements DataOut {

    private OutputStream os;

    public LeDataOut(OutputStream os) {
        super();
        this.os = os;
    }

    @Override
    public void writeByte(int v) throws IOException {
        os.write(v);
    }

    @Override
    public void writeBytes(byte[] bs) throws IOException {
        os.write(bs);
    }

    @Override
    public void writeInt(int v) throws IOException {
        os.write(v);
        os.write(v >> 8);
        os.write(v >> 16);
        os.write(v >>> 24);
    }

    @Override
    public void writeShort(int v) throws IOException {
        os.write(v);
        os.write(v >> 8);
    }

}
