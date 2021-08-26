package org.jf.pxb.android.axml;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/20/12
 * Time: 1:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class NodeVisitor {
    protected NodeVisitor nv;

    public NodeVisitor() {
        super();
    }

    public NodeVisitor(NodeVisitor nv) {
        super();
        this.nv = nv;
    }

    public void visitBegin(){
        if( nv != null ){
            nv.visitBegin();
        }
    }

    /**
     * add attribute to the node
     *
     * @param ns
     * @param name
     * @param resourceId
     * @param type
     *            {@link AxmlVisitor#TYPE_STRING} or others
     * @param obj
     *            a string for {@link AxmlVisitor#TYPE_STRING} ,and Integer for others
     */
    public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
        if (nv != null) {
            nv.visitContentAttr(ns, name, resourceId, type, obj);
        }
    }

    public void visitContentEnd(){
        if(nv != null){
            nv.visitContentEnd();
        }
    }

    /**
     * create a child node
     *
     * @param ns
     * @param name
     * @return
     */
    public NodeVisitor visitChild(String ns, String name) {
        if (nv != null) {
            return nv.visitChild(ns, name);
        }
        return null;
    }

    /**
     * end the visit
     */
    public void visitEnd() {
        if (nv != null) {
            nv.visitEnd();
        }
    }

    /**
     * line number in the .xml
     *
     * @param ln
     */
    public void visitLineNumber(int ln) {
        if (nv != null) {
            nv.visitLineNumber(ln);
        }
    }

    /**
     * the node text
     *
     * @param value
     */
    public void visitContentText(int lineNumber, String value) {
        if (nv != null) {
            nv.visitContentText(lineNumber, value);
        }
    }
}