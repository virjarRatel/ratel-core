package com.virjar.ratel.api.extension.socketmonitor.http;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import external.org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Created by virjar on 2018/4/26.<br>
 * common util for http stream decoder
 */

public class HttpStreamUtil {

    public static final int CR = 13; // <US-ASCII CR, carriage return (13)>
    public static final int LF = 10; // <US-ASCII LF, linefeed (10)>
    public static final int SP = 32; // <US-ASCII SP, space (32)>
    public static final int HT = 9;  // <US-ASCII HT, horizontal-tab (9)>

    public static final int BUFSIZE = 8192;

    /**
     * Find byte index separating header from body. It must be the last byte of
     * the first two sequential new lines.
     */
    public static int findHeaderEnd(final byte[] buf, int rlen) {
        int splitbyte = 0;
        while (splitbyte + 1 < rlen) {

            // RFC2616
            if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && splitbyte + 3 < rlen && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n') {
                return splitbyte + 4;
            }

            // tolerance
            if (buf[splitbyte] == '\n' && buf[splitbyte + 1] == '\n') {
                return splitbyte + 2;
            }
            splitbyte++;
        }
        return 0;
    }

    /**
     * 寻找回车换行结束，注意这个函数无法处理单纯的换行。如果寻找失败，返回-1
     *
     * @param buf
     * @param rlen
     * @return
     */
    public static int findLineEnd(final byte[] buf, int rlen) {
        int splitbyte = 0;
        while (splitbyte + 1 < rlen) {

            // RFC2616
            if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n') {
                //  if(splitbyte == 0) continue; //说明分包长度前面有\r\n
                return splitbyte + 2;
            }

            splitbyte++;
        }

        return -1;
    }

    public static String readLine(InputStream inputStream, int max) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        while (i < max) {
            int char1 = inputStream.read();
            if (char1 == -1) {
                throw new IllegalStateException("eof of stream");
            }
            i++;
            if (char1 == '\r') {
                int char2 = inputStream.read();
                i++;
                if (char2 == -1) {
                    throw new IllegalStateException("eof of stream");
                }
                if (char2 == '\n') {
                    return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
                }
                byteArrayOutputStream.write(char1);
                byteArrayOutputStream.write(char2);
            }
            if (char1 == '\n') {
                return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
            }
            byteArrayOutputStream.write(char1);
        }
        throw new IOException("Maximum line length limit exceeded");
    }

    public static long consume(InputStream inputStream, long n) throws IOException {
        long remaining = n;
        long nr;

        if (n <= 0) {
            return 0;
        }

        while (remaining > 0) {
            nr = inputStream.skip(remaining);
            if (nr <= 0) {
                break;
            }
            remaining -= nr;
        }

        return n - remaining;
    }

    public static int readFully(InputStream inputStream, byte[] buffer) throws IOException {
        return readFully(inputStream, buffer, buffer.length);
    }

    public static int readFully(InputStream inputStream, byte[] buffer, int length) throws IOException {

        if (length > buffer.length) {
            throw new IOException("buffer size must less than length");
        }
        if (length <= 0) {
            return 0;
        }

        int read = inputStream.read(buffer, 0, length);
        if (read == length) {
            return read;
        }
        while (read < length) {
            int nr = inputStream.read(buffer, read, length - read);
            if (nr < 0) {
                break;
            }
            read += nr;
        }
        return read;
    }
}
