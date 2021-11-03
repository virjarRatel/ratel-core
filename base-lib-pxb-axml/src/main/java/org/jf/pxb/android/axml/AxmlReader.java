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

import org.jf.pxb.googlecode.dex2jar.reader.io.ArrayDataIn;
import org.jf.pxb.googlecode.dex2jar.reader.io.DataIn;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.jf.pxb.android.axml.AxmlVisitor.TYPE_INT_BOOLEAN;
import static org.jf.pxb.android.axml.AxmlVisitor.TYPE_STRING;


/**
 * a class to read android axml
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class AxmlReader {
    static final int CHUNK_AXML_FILE = 0x00080003;
    static final int CHUNK_RESOURCEIDS = 0x00080180;
    static final int CHUNK_STRINGS = 0x001C0001;
    static final int CHUNK_XML_END_NAMESPACE = 0x00100101;
    static final int CHUNK_XML_END_TAG = 0x00100103;
    static final int CHUNK_XML_START_NAMESPACE = 0x00100100;
    static final int CHUNK_XML_START_TAG = 0x00100102;
    static final int CHUNK_XML_TEXT = 0x00100104;
    private static final NodeVisitor EMPTY_VISITOR = new NodeVisitor() {

        @Override
        public NodeVisitor visitChild(String namespace, String name) {
            return EMPTY_VISITOR;
        }
    };
    //static final int UTF8_FLAG = 0x00000100;
    // If set, the string index is sorted by the string values (based on strcmp16()).
    public static final int SORTED_FLAG = 1;
    // String pool is encoded in UTF-8
    public static final int UTF8_FLAG = 1 << 8;

    private DataIn input;
    private List<Integer> resourceIds = new ArrayList<Integer>();
    private StringItems stringItems = new StringItems();

    public AxmlReader(byte[] data) {
        this(ArrayDataIn.le(data), null);
    }

    public AxmlReader(DataIn input, DataIn input1) {
        this.input = input;
    }

    public static AxmlReader create(InputStream is) {
        try {
            int available = is.available();
            byte[] buffer = new byte[available];

            int len = is.read(buffer);
            if (len == available) {
                return new AxmlReader(buffer);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public void accept(final AxmlVisitor documentVisitor) throws IOException {
        DataIn input = this.input;
        int fileSize;
        {
            int type = input.readIntx();
            if (type != CHUNK_AXML_FILE) {
                throw new RuntimeException("not AXML file");
            }
            fileSize = input.readIntx();
        }
//        NodeVisitor rootVisitor = documentVisitor == null ? EMPTY_VISITOR : new NodeVisitor() {
//            @Override
//            public NodeVisitor visitChild(String namespace, String name) {
//                return documentVisitor.visitFirst(namespace, name);
//            }
//        };

        NodeVisitor rootVisitor = new NodeVisitor() {
            @Override
            public NodeVisitor visitChild(String namespace, String name) {
                return documentVisitor.visitFirst(namespace, name);
            }
        };

        NodeVisitor stackTop;
        Stack<NodeVisitor> nodeVisitorStack = new Stack<NodeVisitor>();
        Stack<Boolean> nodeVisitorContentsHandled = new Stack<Boolean>();

        String name, namespace;
        int nameIdx, namespaceIdx;
        int lineNumber;

        stackTop = rootVisitor;
        nodeVisitorStack.push(rootVisitor);
        nodeVisitorContentsHandled.push(false);

        documentVisitor.visitBegin();

        for (int position = input.getCurrentPosition(); position < fileSize; position = input.getCurrentPosition()) {
            int type = input.readIntx();
            int size = input.readIntx();
            switch (type) {
                case CHUNK_XML_START_TAG: {
                    {
                        lineNumber = input.readIntx();
                        input.skip(4);/* 0xFFFFFFFF */
                        namespaceIdx = input.readIntx();
                        nameIdx = input.readIntx();
                        int flag = input.readIntx();// 0x00140014 ?
                        if (flag != 0x00140014) {
                            throw new RuntimeException();
                        }
                        name = stringItems.get(nameIdx).data;
                        namespace = namespaceIdx >= 0 ? stringItems.get(namespaceIdx).data : null;

                        if (!nodeVisitorContentsHandled.peek()) {
                            nodeVisitorContentsHandled.pop();
                            nodeVisitorContentsHandled.push(true);
                            stackTop.visitContentEnd();
                        }

                        stackTop = stackTop.visitChild(namespace, name);
                        if (stackTop == null) {
                            stackTop = EMPTY_VISITOR;
                        }
                        nodeVisitorStack.push(stackTop);
                        nodeVisitorContentsHandled.push(false);
                        stackTop.visitBegin();
                        stackTop.visitLineNumber(lineNumber);
                    }

                    int attributeCount = input.readUShortx();
                    // int idAttribute = input.readUShortx();
                    // int classAttribute = input.readUShortx();
                    // int styleAttribute = input.readUShortx();
                    input.skip(6);
                    if (stackTop != EMPTY_VISITOR) {
                        for (int i = 0; i < attributeCount; i++) {
                            namespaceIdx = input.readIntx();
                            nameIdx = input.readIntx();
                            input.skip(4);// skip valueString
                            int aValueType = input.readIntx() >>> 24;
                            int aValue = input.readIntx();
                            name = stringItems.get(nameIdx).data;
                            namespace = namespaceIdx >= 0 ? stringItems.get(namespaceIdx).data : null;
                            Object value = null;
                            switch (aValueType) {
                                case TYPE_STRING:
                                    value = stringItems.get(aValue).data;
                                    break;
                                case TYPE_INT_BOOLEAN:
                                    value = aValue != 0;
                                    break;
                                default:
                                    value = aValue;
                            }
                            int resourceId = nameIdx < resourceIds.size() ? resourceIds.get(nameIdx) : -1;
                            stackTop.visitContentAttr(namespace, name, resourceId, aValueType, value);
                        }
                    } else {
                        input.skip(5 * 4);
                    }
                }
                break;
                case CHUNK_XML_END_TAG: {
                    input.skip(size - 8);

                    if (!nodeVisitorContentsHandled.peek()) {
                        stackTop.visitContentEnd();
                        nodeVisitorContentsHandled.pop();
                        nodeVisitorContentsHandled.push(true);
                    }
                    stackTop.visitEnd();

                    nodeVisitorStack.pop();
                    stackTop = nodeVisitorStack.peek();
                }
                break;
                case CHUNK_XML_START_NAMESPACE:
                    lineNumber = input.readIntx();
                    input.skip(4);/* 0xFFFFFFFF */
                    int prefixIdx = input.readIntx();
                    namespaceIdx = input.readIntx();
                    documentVisitor.visitNamespace(stringItems.get(prefixIdx).data, stringItems.get(namespaceIdx).data,
                            lineNumber);
                    break;
                case CHUNK_XML_END_NAMESPACE:
                    input.skip(size - 8);
                    break;
                case CHUNK_STRINGS:
                    stringItems.read(input, size);
                    break;
                case CHUNK_RESOURCEIDS:
                    int count = size / 4 - 2;
                    for (int i = 0; i < count; i++) {
                        resourceIds.add(input.readIntx());
                    }
                    break;
                case CHUNK_XML_TEXT:
                    if (stackTop == EMPTY_VISITOR) {
                        input.skip(20);
                    } else {
                        lineNumber = input.readIntx();
                        input.skip(4);/* 0xFFFFFFFF */
                        nameIdx = input.readIntx();
                        input.skip(8); /* 00000008 00000000 */
                        name = stringItems.get(nameIdx).data;
                        stackTop.visitContentText(lineNumber, name);
                    }
                    break;
                default:
                    System.out.println("waning: unknown chunk type: " + type);
                    //throw new RuntimeException("unknown chunk type");
            }
            input.move(position + size);
        }

        documentVisitor.visitEnd();
    }
}