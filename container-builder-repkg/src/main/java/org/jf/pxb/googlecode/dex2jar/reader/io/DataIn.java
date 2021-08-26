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
package org.jf.pxb.googlecode.dex2jar.reader.io;

/**
 * 输入流
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 3b20152caede $
 */
public interface DataIn {

    /**
     * 获取当前位置
     *
     * @return
     */
    int getCurrentPosition();

    void move(int absOffset);

    void pop();

    void push();

    /**
     * equals to
     *
     * <pre>
     * push();
     * move(absOffset);
     * </pre>
     *
     * @param absOffset
     * @see #push()
     * @see #move(int)
     */
    void pushMove(int absOffset);

    /**
     *
     */
    int readByte();

    byte[] readBytes(int size);

    int readIntx();

    int readUIntx();

    int readShortx();

    char readChar();

    int readUShortx();

    long readLeb128();

    //之前的代码这里有问题，解析AndroidManifest的时候出错了，所以同步APkParser代码
    // https://github.com/hsiafan/apk-parser/blob/bdab2f0a7670f4ac4ea98278b6657671fa03c0b1/src/main/java/net/dongliu/apk/parser/utils/ParseUtils.java#L63
    int readLen();

    int readLen16();

    /**
     * @return
     */
    int readUByte();

    long readULeb128();

    /**
     * @param i
     */
    void skip(int bytes);

}
