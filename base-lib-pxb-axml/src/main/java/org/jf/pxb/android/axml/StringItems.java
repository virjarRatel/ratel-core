/*
 * Copyright (c) 2009-2012 Panxiaobo
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
package org.jf.pxb.android.axml;

import org.jf.pxb.googlecode.dex2jar.reader.io.DataIn;
import org.jf.pxb.googlecode.dex2jar.reader.io.DataOut;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
class StringItems extends ArrayList<StringItem> {

    byte[] stringData;

    public int getSize() {
        return 5 * 4 + this.size() * 4 + stringData.length + 0;// TODO
    }

    public void prepare() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = 0;
        int offset = 0;
        baos.reset();
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (StringItem item : this) {
            item.index = i++;
            String stringData = item.data;
            Integer of = map.get(stringData);
            if (of != null) {
                item.dataOffset = of;
            } else {
                item.dataOffset = offset;
                map.put(stringData, offset);
                int length = stringData.length();
                byte[] data = stringData.getBytes("UTF-16LE");
                baos.write(length);
                baos.write(length >> 8);
                baos.write(data);
                baos.write(0);
                baos.write(0);
                offset += 4 + data.length;
            }
        }
        // TODO
        stringData = baos.toByteArray();
    }

    public void read(DataIn in, int stringChunkHeaderSize) {
        int trunkOffset = in.getCurrentPosition() - 8;
        int stringCount = in.readIntx();
        int styleOffsetCount = in.readIntx();
        int flags = in.readIntx();
        boolean sorted = (flags & AxmlReader.SORTED_FLAG) != 0;
        boolean utf8 = (flags & AxmlReader.UTF8_FLAG) != 0;
        int stringDataOffset = in.readIntx();
        int stylesOffset = in.readIntx();


        for (int i = 0; i < stringCount; i++) {
            StringItem stringItem = new StringItem();
            stringItem.index = i;
            stringItem.dataOffset = in.readIntx();
            this.add(stringItem);
        }

        if (styleOffsetCount != 0) {
            throw new RuntimeException("style offset must be zero");
        }
        //Map<Integer, String> stringMap = new TreeMap<>();
        int stringPos = trunkOffset + stringDataOffset;

        for (int i = 0; i < stringCount; i++) {
            StringItem stringItem = get(i);
            in.move(stringPos + stringItem.dataOffset);
            stringItem.data = readString(in, utf8);
        }

//        int base = in.getCurrentPosition();
//        if (utf8) {
//            int index = 0;
//            for (int p = base; p < endOfStringData; p = in.getCurrentPosition()) {
//                //需要多读一位
//                int u16Length = in.readLen();
//                int length = in.readLen();
//                ByteArrayOutputStream bos = new ByteArrayOutputStream(length + 10);
//                for (int r = in.readByte(); r != 0; r = in.readByte()) {
//                    bos.write(r);
//                }
//                String value = new String(bos.toByteArray(), StandardCharsets.UTF_8);
//                stringMap.put(p - base, value);
//            }
//        } else {
//            for (int p = base; p < endOfStringData; p = in.getCurrentPosition()) {
//                int length = in.readLen16();
//                if (length == 0) {
//                    // 如果有空行，跳过解析，参见下厨房的：
//                    // http://ratel-bin.oss-cn-beijing.aliyuncs.com/com.xiachufang_7.2.8_524_c764eb76333b223da94b72e8f2a8f7b3.apk?Expires=1585166155&OSSAccessKeyId=LTAI4FmJVVq4QEHDRbE7vMwA&Signature=xf5CE57Bdx9R7xhmVPSQ1Iricqk%3D
//                    continue;
//                }
//                StringBuilder sb = new StringBuilder(length);
//                for (int i = 0; i < length; i++) {
//                    sb.append(in.readChar());
//                }
////                //如果未来这里出现问题，参考UTF8的做法
////                int length = in.readShortx();
////                byte[] data = in.readBytes(length * 2);
////                in.skip(2);
////                String value = new String(data, StandardCharsets.UTF_16LE);
//
//                String value = sb.toString(); //new String(bos.toByteArray(), StandardCharsets.UTF_16LE);
//                stringMap.put(p - base, value);
//                in.readUShortx();
//
//                // System.out.println(String.format("%08x %s", p - base, value));
//            }
//        }
        if (stylesOffset != 0) {
            throw new RuntimeException("style offset must be zero");
        }
//        for (StringItem item : this) {
//            item.data = stringMap.get(item.dataOffset);
//            // System.out.println(item);
//            if (item.data == null) {
//                // new Throwable("null").printStackTrace();
//                item.data = "";
//            }
//        }
    }

    private static String readString(DataIn in, boolean utf8) {
        if (utf8) {
            //  The lengths are encoded in the same way as for the 16-bit format
            // but using 8-bit rather than 16-bit integers.
            int strLen = in.readLen();
            int bytesLen = in.readLen();
            byte[] bytes = in.readBytes(bytesLen);// Buffers.readBytes(buffer, bytesLen);
            String str = new String(bytes, StandardCharsets.UTF_8);
            // zero
            int trailling = in.readUByte();//Buffers.readUByte(buffer);
            return str;
        } else {
            // The length is encoded as either one or two 16-bit integers as per the commentRef...
            int strLen = in.readLen16();
            // String str = Buffers.readString(buffer, strLen);
            StringBuilder sb = new StringBuilder(strLen);
            for (int i = 0; i < strLen; i++) {
                sb.append(in.readChar());
            }
            // zero
            int trailling = in.readUShortx(); //Buffers.readUShort(buffer);
            return sb.toString();
        }
    }

    public void write(DataOut out) throws IOException {
        out.writeInt(this.size());
        out.writeInt(0);// TODO
        out.writeInt(0);
        out.writeInt(7 * 4 + this.size() * 4);
        out.writeInt(0);
        for (StringItem item : this) {
            out.writeInt(item.dataOffset);
        }
        out.writeBytes(stringData);
        // TODO
    }
}