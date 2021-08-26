package com.virjar.ratel.api.extension.socketmonitor.http;

import android.util.Log;

import com.virjar.ratel.api.extension.socketmonitor.SocketMonitor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import external.org.apache.commons.io.IOUtils;
import external.org.apache.commons.io.output.ByteArrayOutputStream;
import external.org.apache.commons.lang3.NumberUtils;
import external.org.apache.commons.lang3.StringUtils;

public class HttpBaseInfoDecoder {
    private Charset charset = null;
    private String contentType = null;
    private byte[] headData = null;
    private Map<String, String> headers = null;

    private ByteArrayOutputStream data;

    private boolean request;

    private long headerSplitByte = 0;
    private int contentLength = 0;

    private boolean isTrunked = false;
    private long trunckCursor = -1;
    private InputStream chunkAggregateStream = null;
    private List<ByteArrayOutputStream> trunckItemList = new ArrayList<>();

    private boolean isGzip = false;


    public HttpBaseInfoDecoder(ByteArrayOutputStream data, boolean request) {
        this.data = data;
        this.request = request;
    }

    public InputStream parse() throws IOException {
        decodeHeader();
        if (headers == null) {
            //header还没有解码成功
            return null;
        }
        InputStream targetInputStream = decodeChunk();
        if (targetInputStream == null) {
            targetInputStream = decodeContentLengthBody();
        }
        if (targetInputStream == null) {
            return null;
        }

        if (isGzip) {
            try {
                targetInputStream = new GZIPInputStream(targetInputStream);
            } catch (IOException e) {
                isGzip = false;
            }
        }

        if (StringUtils.startsWithIgnoreCase(headers.get("Content-Type".toLowerCase(Locale.US)), "image/")) {
            targetInputStream = new ByteArrayInputStream("this content is a image!".getBytes());
            charset = null;
        }

        synchronized (this) {
            if (headData == null) {
                //TODO 这里似乎在报错
                return null;
            }
            ByteArrayOutputStream ret = new ByteArrayOutputStream();
            ret.write(headData);
            IOUtils.copy(targetInputStream, ret);

            headData = null;
            headerSplitByte = 0;
            contentLength = 0;
            trunckCursor = -1;
            chunkAggregateStream = null;
            isGzip = false;
            trunckItemList = new ArrayList<>();
            headers = null;
            charset = null;
            contentType = null;

            return ret.toInputStream();
        }
    }


    private InputStream decodeContentLengthBody() throws IOException {
        if (contentLength == 0) {
            return new ByteArrayInputStream(new byte[0]);
        }

        if (data.size() < contentLength + headerSplitByte) {
            //这个时候数据还不完整
            return null;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.toByteArray());
        HttpStreamUtil.consume(byteArrayInputStream, headerSplitByte);

        byte[] bytes = new byte[contentLength];
        byteArrayInputStream.read(bytes);

        byte[] leftData = IOUtils.toByteArray(byteArrayInputStream);
        data.reset();
        if (leftData.length > 0) {
            data.write(leftData);
        }

        return new ByteArrayInputStream(bytes);
    }

    private InputStream decodeChunk() throws IOException {
        if (!isTrunked) {
            return null;
        }

        if (chunkAggregateStream != null) {
            return chunkAggregateStream;
        }

        if (trunckCursor <= 0) {
            trunckCursor = headerSplitByte;
        }

        InputStream inputStream = data.toInputStream();
        long skipTrunckCursor = HttpStreamUtil.consume(inputStream, trunckCursor);
        if (skipTrunckCursor != trunckCursor) {
            Log.w(SocketMonitor.TAG, "跳过Header错误,真实跳过了：" + skipTrunckCursor + "==应该跳过：" + trunckCursor);
            return null;
        }


        while (true) {
            //读取8个字节的数据（2个字节为/n/r)
            byte[] buffer = new byte[10];
            int bytes = HttpStreamUtil.readFully(inputStream, buffer);//inputStream.read(buffer);
            if (bytes < 3) {
                return null;
            }

            //找到数据长度所在的cursor位置
            int lineEnd = HttpStreamUtil.findLineEnd(buffer, 10);
            if (lineEnd <= 0) {
                Log.w(SocketMonitor.TAG, "寻找cursor失败，数据分片不完全");
                return null;
            }

            int chunckLength = Integer.parseInt(new String(buffer, 0, lineEnd - 2, StandardCharsets.UTF_8), 16);


            if (chunckLength == 0) {
                //output
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                for (ByteArrayOutputStream trunckItem : trunckItemList) {
                    byteArrayOutputStream.write(trunckItem.toInputStream());
                }
                chunkAggregateStream = byteArrayOutputStream.toInputStream();

                byte[] leftData = IOUtils.toByteArray(inputStream);
                data.reset();
                if (leftData.length > 0) {
                    data.write(leftData);
                }


                return chunkAggregateStream;
            }


            ByteArrayOutputStream trunckItem = new ByteArrayOutputStream();
            if (10 > lineEnd) {
                trunckItem.write(buffer, lineEnd, bytes - lineEnd);
            }

            //加2把/n/r给读进去
            byte[] tempBuffer = new byte[chunckLength - bytes + lineEnd];
            ///
            int readBytes = HttpStreamUtil.readFully(inputStream, tempBuffer);
            if (HttpStreamUtil.consume(inputStream, 2) != 2) {
                return null;
            }

            if (readBytes != tempBuffer.length) {
                return null;
            }

            trunckItem.write(tempBuffer);
            trunckItemList.add(trunckItem);
            trunckCursor += lineEnd + chunckLength + 2;
        }

    }


    private void decodeHeader() throws IOException {
        if (headerSplitByte > 0) {
            return;
        }
        int rlen = 0;
        InputStream inputStream = data.toInputStream();
        byte[] buf = new byte[HttpStreamUtil.BUFSIZE];
        int read = inputStream.read(buf, 0, HttpStreamUtil.BUFSIZE);

        while (read > 0) {
            rlen += read;
            headerSplitByte = HttpStreamUtil.findHeaderEnd(buf, rlen);
            if (headerSplitByte > 0) {
                break;
            }
            read = inputStream.read(buf, rlen, HttpStreamUtil.BUFSIZE - rlen);
        }
        if (headerSplitByte == 0) {
            //还没有读取到完整的header数据
            return;
        }
        // now parse http header
        if (null == this.headers) {
            this.headers = new HashMap<>();
        } else {
            this.headers.clear();
        }


        // Create a BufferedReader for parsing the header.
        BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, rlen)));
        decodeHeader(hin, headers);
        calcMeta();
    }

    private void calcMeta() throws IOException {
        String contentLengthStr = headers.get("Content-Length".toLowerCase(Locale.US));
        if (StringUtils.isNotBlank(contentLengthStr)) {
            contentLength = NumberUtils.toInt(StringUtils.trim(contentLengthStr));
        } else {
            //test if body is chuncked transfer
            if (StringUtils.equalsIgnoreCase(headers.get("Transfer-Encoding".toLowerCase(Locale.US)), "chunked")) {
                isTrunked = true;
            } else {
                //GET 方法会在这里，因为现在是请求响应一起解析
                contentLength = 0;
            }
        }

        contentType = headers.get("Content-Type".toLowerCase(Locale.US));

        //test for gzip
        //Content-Type: application/zip
        if (StringUtils.equalsIgnoreCase(headers.get("Content-Encoding".toLowerCase(Locale.US)), "gzip")) {
            isGzip = true;
        }
        if (StringUtils.containsIgnoreCase(contentType, "application/gzip")) {
            isGzip = true;
        }

        //如果是文本，解析数据的编码集，防止数据乱码
        if (contentType != null && contentType.contains(";")) {
            String[] arr2 = contentType.split(";");
            if (arr2[1].contains("=")) {
                arr2 = arr2[1].split("=");
                try {
                    charset = Charset.forName(StringUtils.trimToNull(arr2[1]));
                } catch (UnsupportedCharsetException e) {
                    //ignore
                }
            }
        }
        if (StringUtils.containsIgnoreCase(contentType, "text") || StringUtils.containsIgnoreCase(contentType, "application/json")) {
            //此时，必然是文本，如果charset解析失败，使用默认兜底
            if (charset == null) {
                charset = StandardCharsets.UTF_8;
            }
        }


        //解析头部数据
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.toByteArray());
        headData = IOUtils.toByteArray(byteArrayInputStream, headerSplitByte);

    }

    private void decodeHeader(BufferedReader in, Map<String, String> headers) throws IOException {
        // Read the request line
        String inLine = in.readLine();
        if (inLine == null) {
            // not happen
            return;
        }

        String line = in.readLine();

        while (line != null && !line.trim().isEmpty()) {
            int p = line.indexOf(':');
            if (p >= 0) {
                headers.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
            }
            line = in.readLine();

        }

    }
}
