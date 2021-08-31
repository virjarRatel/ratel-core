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

/*
    AXMLVisitor visiting sequence : visitBegin [visitNamespace]* visitFirstNode visitEnd
    NodeVisitor visiting sequence : visitBegin [visitContentAttr | visitContentText]* visitContentEnd visitChildNoe visitEnd
 */

/**
 * visitor to visit an axml
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class AxmlVisitor {

    public static final int TYPE_FIRST_INT = 0x10;
    public static final int TYPE_INT_BOOLEAN = 0x12;
    public static final int TYPE_INT_HEX = 0x11;
    public static final int TYPE_REFERENCE = 0x01;

    public static final int TYPE_STRING = 0x03;

    public static final String NS_ANDROID = "http://schemas.android.com/apk/res/android";

    /**
     * @see android.R
     */
    public static final int NAME_RESOURCE_ID = 16842755;
    public static final int VALUE_RESOURCE_ID = 16842788;
    public static final int DEBUG_RESOURCE_ID = 0x0101000f;
    public static final int requestLegacyExternalStorageID = 16844291;

    protected AxmlVisitor av;

    public AxmlVisitor() {
        super();

    }

    public AxmlVisitor(AxmlVisitor av) {
        super();
        this.av = av;
    }

    ;

    public void visitBegin() {
        if (av != null) {
            av.visitBegin();
        }
    }

    /**
     * end the visit
     */
    public void visitEnd() {
        if (av != null) {
            av.visitEnd();
        }
    }

    ;

    /**
     * create the first node
     *
     * @param namespace
     * @param name
     * @return
     */
    public NodeVisitor visitFirst(String namespace, String name) {
        if (av != null) {
            return av.visitFirst(namespace, name);
        }
        return null;
    }

    /**
     * create a namespace
     *
     * @param prefix
     * @param uri
     * @param ln
     */
    public void visitNamespace(String prefix, String uri, int ln) {
        if (av != null) {
            av.visitNamespace(prefix, uri, ln);
        }
    }

}