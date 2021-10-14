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

import org.jf.pxb.googlecode.dex2jar.reader.io.DataOut;
import org.jf.pxb.googlecode.dex2jar.reader.io.LeDataOut;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * a class to write android axml
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class AxmlWriter extends AxmlVisitor {
    static class Attr {
        public StringItem name;
        public StringItem ns;
        public int resourceId;
        public int type;
        public Object value;

        public Attr(StringItem ns, StringItem name, int resourceId, int type, Object value) {
            super();
            this.ns = ns;
            this.name = name;
            this.resourceId = resourceId;
            this.type = type;
            this.value = value;
        }

        public void prepare(AxmlWriter axmlWriter) {
            ns = axmlWriter.updateNs(ns);
            if (this.name != null) {
                if (resourceId != -1) {
                    this.name = axmlWriter.updateWithResourceId(this.name, this.resourceId);
                } else {
                    this.name = axmlWriter.update(this.name);
                }
            }
            if (value instanceof StringItem) {
                value = axmlWriter.update((StringItem) value);
            }
        }
    }

    static class NodeImpl extends NodeVisitor {
        private Map<String, Attr> attrs = new HashMap<String, Attr>();
        private List<NodeImpl> children = new ArrayList<NodeImpl>();
        private int line;
        private StringItem name;
        private StringItem ns;
        private StringItem text;
        private int textLineNumber;

        public NodeImpl(String ns, String name) {
            super(null);
            this.ns = ns == null ? null : new StringItem(ns);
            this.name = name == null ? null : new StringItem(name);
        }

        @Override
        public void visitContentAttr(String ns, String name, int resourceId, int type, Object value) {
            if (name == null) {
                throw new RuntimeException("name can't be null");
            }
            attrs.put((ns == null ? "zzz" : ns) + "." + name, new Attr(ns == null ? null : new StringItem(ns),
                    new StringItem(name), resourceId, type, type == TYPE_STRING ? new StringItem((String) value)
                    : value));
        }

        @Override
        public NodeVisitor visitChild(String ns, String name) {
            NodeImpl child = new NodeImpl(ns, name);
            this.children.add(child);
            return child;
        }

        @Override
        public void visitEnd() {
        }

        @Override
        public void visitLineNumber(int ln) {
            this.line = ln;
        }

        public int prepare(AxmlWriter axmlWriter) {
            ns = axmlWriter.updateNs(ns);
            name = axmlWriter.update(name);

            for (Attr attr : this.sortedAttrs()) {
                attr.prepare(axmlWriter);
            }
            text = axmlWriter.update(text);
            int size = 24 + 36 + attrs.size() * 20;// 24 for end tag,36+x*20 for
            // start tag
            for (NodeImpl child : children) {
                size += child.prepare(axmlWriter);
            }
            if (text != null) {
                size += 28;
            }
            return size;
        }

        List<Attr> sortedAttrs() {
            List<Attr> lAttrs = new ArrayList<Attr>(attrs.values());
            Collections.sort(lAttrs, new Comparator<Attr>() {

                @Override
                public int compare(Attr a, Attr b) {
                    if (a.ns == null) {
                        if (b.ns == null) {
                            return b.name.data.compareTo(a.name.data);
                        } else {
                            return 1;
                        }
                    } else if (b.ns == null) {
                        return -1;
                    } else {
                        int x = a.ns.data.compareTo(b.ns.data);
                        if (x == 0) {
                            x = a.resourceId - b.resourceId;
                            if (x == 0) {
                                return a.name.data.compareTo(b.name.data);
                            }
                        }
                        return x;
                    }
                }
            });
            return lAttrs;
        }

        @Override
        public void visitContentText(int ln, String value) {
            this.text = new StringItem(value);
            this.textLineNumber = ln;
        }

        void write(DataOut out) throws IOException {
            // start tag
            out.writeInt(AxmlReader.CHUNK_XML_START_TAG);
            out.writeInt(36 + attrs.size() * 20);
            out.writeInt(line);
            out.writeInt(0xFFFFFFFF);
            out.writeInt(ns != null ? this.ns.index : -1);
            out.writeInt(name.index);
            out.writeInt(0x00140014);// TODO
            out.writeShort(this.attrs.size());
            out.writeShort(0);
            out.writeShort(0);
            out.writeShort(0);
            for (Attr attr : this.sortedAttrs()) {
                out.writeInt(attr.ns == null ? -1 : attr.ns.index);
                out.writeInt(attr.name.index);
                out.writeInt(attr.value instanceof StringItem ? ((StringItem) attr.value).index : -1);
                out.writeInt((attr.type << 24) | 0x000008);
                Object v = attr.value;
                if (v instanceof StringItem) {
                    out.writeInt(((StringItem) attr.value).index);
                } else if (v instanceof Boolean) {
                    out.writeInt(Boolean.TRUE.equals(v) ? -1 : 0);
                } else {
                    out.writeInt((Integer) attr.value);
                }
            }

            if (this.text != null) {
                out.writeInt(AxmlReader.CHUNK_XML_TEXT);
                out.writeInt(28);
                out.writeInt(textLineNumber);
                out.writeInt(0xFFFFFFFF);
                out.writeInt(text.index);
                out.writeInt(0x00000008);
                out.writeInt(0x00000000);
            }

            // children
            for (NodeImpl child : children) {
                child.write(out);
            }

            // end tag
            out.writeInt(AxmlReader.CHUNK_XML_END_TAG);
            out.writeInt(24);
            out.writeInt(-1);
            out.writeInt(0xFFFFFFFF);
            out.writeInt(ns != null ? this.ns.index : -1);
            out.writeInt(name.index);
        }
    }

    static class Ns {
        int ln;
        StringItem prefix;
        StringItem uri;

        public Ns(StringItem prefix, StringItem uri, int ln) {
            super();
            this.prefix = prefix;
            this.uri = uri;
            this.ln = ln;
        }
    }

    private List<NodeImpl> firsts = new ArrayList<NodeImpl>(3);

    private Map<String, Ns> nses = new HashMap<String, Ns>();

    private List<StringItem> otherString = new ArrayList<StringItem>();

    private Map<Integer, StringItem> resourceId2Str = new HashMap<Integer, StringItem>();

    private List<Integer> resourceIds = new ArrayList<Integer>();

    private List<StringItem> resourceString = new ArrayList<StringItem>();

    private StringItems stringItems = new StringItems();

    // TODO add style support
    // private List<StringItem> styleItems = new ArrayList();

    @Override
    public void visitEnd() {
    }

    @Override
    public NodeVisitor visitFirst(String ns, String name) {
        NodeImpl first = new NodeImpl(ns, name);
        this.firsts.add(first);
        return first;
    }

    @Override
    public void visitNamespace(String prefix, String uri, int ln) {
        nses.put(uri, new Ns(new StringItem(prefix), new StringItem(uri), ln));
    }

    private int prepare() throws IOException {

        int size = nses.size() * 24 * 2;
        for (NodeImpl first : firsts) {
            size += first.prepare(this);
        }
        {
            int a = 0;
            for (Map.Entry<String, Ns> e : nses.entrySet()) {
                Ns ns = e.getValue();
                if (ns == null) {
                    ns = new Ns(new StringItem(String.format("axml_auto_%02d", a++)), new StringItem(e.getKey()), 0);
                    e.setValue(ns);
                }
                ns.prefix = update(ns.prefix);
                ns.uri = update(ns.uri);
            }
        }
        this.stringItems.addAll(resourceString);
        resourceString = null;
        this.stringItems.addAll(otherString);
        otherString = null;
        this.stringItems.prepare();
        int stringSize = this.stringItems.getSize();
        if (stringSize % 4 != 0) {
            stringSize += 4 - stringSize % 4;
        }
        size += 8 + stringSize;
        size += 8 + resourceIds.size() * 4;
        return size;
    }

    public void writeTo(OutputStream os) throws IOException {
        DataOut out = new LeDataOut(os);
        int size = prepare();
        out.writeInt(AxmlReader.CHUNK_AXML_FILE);
        out.writeInt(size + 8);

        int stringSize = this.stringItems.getSize();
        int padding = 0;
        if (stringSize % 4 != 0) {
            padding = 4 - stringSize % 4;
        }
        out.writeInt(AxmlReader.CHUNK_STRINGS);
        out.writeInt(stringSize + padding + 8);
        this.stringItems.write(out);
        out.writeBytes(new byte[padding]);

        out.writeInt(AxmlReader.CHUNK_RESOURCEIDS);
        out.writeInt(8 + this.resourceIds.size() * 4);
        for (Integer i : resourceIds) {
            out.writeInt(i);
        }

        Stack<Ns> stack = new Stack<Ns>();
        for (Map.Entry<String, Ns> e : this.nses.entrySet()) {
            Ns ns = e.getValue();
            stack.push(ns);
            out.writeInt(AxmlReader.CHUNK_XML_START_NAMESPACE);
            out.writeInt(24);
            out.writeInt(-1);
            out.writeInt(0xFFFFFFFF);
            out.writeInt(ns.prefix.index);
            out.writeInt(ns.uri.index);
        }

        for (NodeImpl first : firsts) {
            first.write(out);
        }

        while (stack.size() > 0) {
            Ns ns = stack.pop();
            out.writeInt(AxmlReader.CHUNK_XML_END_NAMESPACE);
            out.writeInt(24);
            out.writeInt(ns.ln);
            out.writeInt(0xFFFFFFFF);
            out.writeInt(ns.prefix.index);
            out.writeInt(ns.uri.index);
        }
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        this.writeTo(os);
        return os.toByteArray();
    }

    StringItem update(StringItem item) {
        if (item == null)
            return null;
        int i = this.otherString.indexOf(item);
        if (i < 0) {
            StringItem copy = new StringItem(item.data);
            this.otherString.add(copy);
            return copy;
        } else {
            return this.otherString.get(i);
        }
    }

    StringItem updateNs(StringItem item) {
        if (item == null) {
            return null;
        }
        String ns = item.data;
        if (!this.nses.containsKey(ns)) {
            this.nses.put(ns, null);
        }
        return update(item);
    }

    StringItem updateWithResourceId(StringItem name, int resourceId) {
        StringItem item = this.resourceId2Str.get(resourceId);
        if (item != null) {
            return item;
        } else {
            StringItem copy = new StringItem(name.data);
            resourceIds.add(resourceId);
            resourceString.add(copy);
            resourceId2Str.put(resourceId, copy);
            return copy;
        }
    }
}