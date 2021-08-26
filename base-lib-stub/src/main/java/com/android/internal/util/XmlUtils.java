package com.android.internal.util;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.ArrayMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
/** {@hide} */
public class XmlUtils {
    private static final String STRING_ARRAY_SEPARATOR = ":";
    public static void skipCurrentTag(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public static final int
    convertValueToList(CharSequence value, String[] options, int defaultValue)
    {
        throw new UnsupportedOperationException("STUB");
    }
    public static final boolean
    convertValueToBoolean(CharSequence value, boolean defaultValue)
    {
        throw new UnsupportedOperationException("STUB");
    }
    public static final int
    convertValueToInt(CharSequence charSeq, int defaultValue)
    {
        throw new UnsupportedOperationException("STUB");
    }
    public static int convertValueToUnsignedInt(String value, int defaultValue) {
        throw new UnsupportedOperationException("STUB");
    }
    public static int parseUnsignedIntAttribute(CharSequence charSeq) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten a Map into an output stream as XML.  The map can later be
     * read back with readMapXml().
     *
     * @param val The map to be flattened.
     * @param out Where to write the XML data.
     *
     * @see #writeMapXml(Map, String, XmlSerializer)
     * @see #writeListXml
     * @see #writeValueXml
     * @see #readMapXml
     */
    public static final void writeMapXml(Map val, OutputStream out)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten a List into an output stream as XML.  The list can later be
     * read back with readListXml().
     *
     * @param val The list to be flattened.
     * @param out Where to write the XML data.
     *
     * @see #writeListXml(List, String, XmlSerializer)
     * @see #writeMapXml
     * @see #writeValueXml
     * @see #readListXml
     */
    public static final void writeListXml(List val, OutputStream out)
            throws XmlPullParserException, java.io.IOException
    {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten a Map into an XmlSerializer.  The map can later be read back
     * with readThisMapXml().
     *
     * @param val The map to be flattened.
     * @param name Name attribute to include with this list's tag, or null for
     *             none.
     * @param out XmlSerializer to write the map into.
     *
     * @see #writeMapXml(Map, OutputStream)
     * @see #writeListXml
     * @see #writeValueXml
     * @see #readMapXml
     */
    public static final void writeMapXml(Map val, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten a Map into an XmlSerializer.  The map can later be read back
     * with readThisMapXml().
     *
     * @param val The map to be flattened.
     * @param name Name attribute to include with this list's tag, or null for
     *             none.
     * @param out XmlSerializer to write the map into.
     * @param callback Method to call when an Object type is not recognized.
     *
     * @see #writeMapXml(Map, OutputStream)
     * @see #writeListXml
     * @see #writeValueXml
     * @see #readMapXml
     *
     * @hide
     */
    public static final void writeMapXml(Map val, String name, XmlSerializer out,
                                         WriteMapCallback callback) throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten a Map into an XmlSerializer.  The map can later be read back
     * with readThisMapXml(). This method presumes that the start tag and
     * name attribute have already been written and does not write an end tag.
     *
     * @param val The map to be flattened.
     * @param out XmlSerializer to write the map into.
     *
     * @see #writeMapXml(Map, OutputStream)
     * @see #writeListXml
     * @see #writeValueXml
     * @see #readMapXml
     *
     * @hide
     */
    public static final void writeMapXml(Map val, XmlSerializer out,
                                         WriteMapCallback callback) throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten a List into an XmlSerializer.  The list can later be read back
     * with readThisListXml().
     *
     * @param val The list to be flattened.
     * @param name Name attribute to include with this list's tag, or null for
     *             none.
     * @param out XmlSerializer to write the list into.
     *
     * @see #writeListXml(List, OutputStream)
     * @see #writeMapXml
     * @see #writeValueXml
     * @see #readListXml
     */
    public static final void writeListXml(List val, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException
    {
        throw new UnsupportedOperationException("STUB");
    }

    public static final void writeSetXml(Set val, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten a byte[] into an XmlSerializer.  The list can later be read back
     * with readThisByteArrayXml().
     *
     * @param val The byte array to be flattened.
     * @param name Name attribute to include with this array's tag, or null for
     *             none.
     * @param out XmlSerializer to write the array into.
     *
     * @see #writeMapXml
     * @see #writeValueXml
     */
    public static final void writeByteArrayXml(byte[] val, String name,
                                               XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten an int[] into an XmlSerializer.  The list can later be read back
     * with readThisIntArrayXml().
     *
     * @param val The int array to be flattened.
     * @param name Name attribute to include with this array's tag, or null for
     *             none.
     * @param out XmlSerializer to write the array into.
     *
     * @see #writeMapXml
     * @see #writeValueXml
     * @see #readThisIntArrayXml
     */
    public static final void writeIntArrayXml(int[] val, String name,
                                              XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten a long[] into an XmlSerializer.  The list can later be read back
     * with readThisLongArrayXml().
     *
     * @param val The long array to be flattened.
     * @param name Name attribute to include with this array's tag, or null for
     *             none.
     * @param out XmlSerializer to write the array into.
     *
     * @see #writeMapXml
     * @see #writeValueXml
     * @see #readThisIntArrayXml
     */
    public static final void writeLongArrayXml(long[] val, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten a double[] into an XmlSerializer.  The list can later be read back
     * with readThisDoubleArrayXml().
     *
     * @param val The double array to be flattened.
     * @param name Name attribute to include with this array's tag, or null for
     *             none.
     * @param out XmlSerializer to write the array into.
     *
     * @see #writeMapXml
     * @see #writeValueXml
     * @see #readThisIntArrayXml
     */
    public static final void writeDoubleArrayXml(double[] val, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten a String[] into an XmlSerializer.  The list can later be read back
     * with readThisStringArrayXml().
     *
     * @param val The String array to be flattened.
     * @param name Name attribute to include with this array's tag, or null for
     *             none.
     * @param out XmlSerializer to write the array into.
     *
     * @see #writeMapXml
     * @see #writeValueXml
     * @see #readThisIntArrayXml
     */
    public static final void writeStringArrayXml(String[] val, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten a boolean[] into an XmlSerializer.  The list can later be read back
     * with readThisBooleanArrayXml().
     *
     * @param val The boolean array to be flattened.
     * @param name Name attribute to include with this array's tag, or null for
     *             none.
     * @param out XmlSerializer to write the array into.
     *
     * @see #writeMapXml
     * @see #writeValueXml
     * @see #readThisIntArrayXml
     */
    public static final void writeBooleanArrayXml(boolean[] val, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten an object's value into an XmlSerializer.  The value can later
     * be read back with readThisValueXml().
     *
     * Currently supported value types are: null, String, Integer, Long,
     * Float, Double Boolean, Map, List.
     *
     * @param v The object to be flattened.
     * @param name Name attribute to include with this value's tag, or null
     *             for none.
     * @param out XmlSerializer to write the object into.
     *
     * @see #writeMapXml
     * @see #writeListXml
     * @see #readValueXml
     */
    public static final void writeValueXml(Object v, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Flatten an object's value into an XmlSerializer.  The value can later
     * be read back with readThisValueXml().
     *
     * Currently supported value types are: null, String, Integer, Long,
     * Float, Double Boolean, Map, List.
     *
     * @param v The object to be flattened.
     * @param name Name attribute to include with this value's tag, or null
     *             for none.
     * @param out XmlSerializer to write the object into.
     * @param callback Handler for Object types not recognized.
     *
     * @see #writeMapXml
     * @see #writeListXml
     * @see #readValueXml
     */
    private static final void writeValueXml(Object v, String name, XmlSerializer out,
                                            WriteMapCallback callback)  throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read a HashMap from an InputStream containing XML.  The stream can
     * previously have been written by writeMapXml().
     *
     * @param in The InputStream from which to read.
     *
     * @return HashMap The resulting map.
     *
     * @see #readListXml
     * @see #readValueXml
     * @see #readThisMapXml
     * #see #writeMapXml
     */
    @SuppressWarnings("unchecked")
    public static final HashMap<String, ?> readMapXml(InputStream in)
            throws XmlPullParserException, java.io.IOException
    {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read an ArrayList from an InputStream containing XML.  The stream can
     * previously have been written by writeListXml().
     *
     * @param in The InputStream from which to read.
     *
     * @return ArrayList The resulting list.
     *
     * @see #readMapXml
     * @see #readValueXml
     * @see #readThisListXml
     * @see #writeListXml
     */
    public static final ArrayList readListXml(InputStream in)
            throws XmlPullParserException, java.io.IOException
    {
        throw new UnsupportedOperationException("STUB");
    }


    /**
     * Read a HashSet from an InputStream containing XML. The stream can
     * previously have been written by writeSetXml().
     *
     * @param in The InputStream from which to read.
     *
     * @return HashSet The resulting set.
     *
     * @throws XmlPullParserException
     * @throws java.io.IOException
     *
     * @see #readValueXml
     * @see #readThisSetXml
     * @see #writeSetXml
     */
    public static final HashSet readSetXml(InputStream in)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read a HashMap object from an XmlPullParser.  The XML data could
     * previously have been generated by writeMapXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the map.
     *
     * @param parser The XmlPullParser from which to read the map data.
     * @param endTag Name of the tag that will end the map, usually "map".
     * @param name An array of one string, used to return the name attribute
     *             of the map's tag.
     *
     * @return HashMap The newly generated map.
     *
     * @see #readMapXml
     */
    public static final HashMap<String, ?> readThisMapXml(XmlPullParser parser, String endTag,
                                                          String[] name) throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read a HashMap object from an XmlPullParser.  The XML data could
     * previously have been generated by writeMapXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the map.
     *
     * @param parser The XmlPullParser from which to read the map data.
     * @param endTag Name of the tag that will end the map, usually "map".
     * @param name An array of one string, used to return the name attribute
     *             of the map's tag.
     *
     * @return HashMap The newly generated map.
     *
     * @see #readMapXml
     * @hide
     */
    public static final HashMap<String, ?> readThisMapXml(XmlPullParser parser, String endTag,
                                                          String[] name, ReadMapCallback callback)
            throws XmlPullParserException, java.io.IOException
    {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Like {@link #readThisMapXml}, but returns an ArrayMap instead of HashMap.
     * @hide
     */
    public static final ArrayMap<String, ?> readThisArrayMapXml(XmlPullParser parser, String endTag,
                                                                String[] name, ReadMapCallback callback)
            throws XmlPullParserException, java.io.IOException
    {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read an ArrayList object from an XmlPullParser.  The XML data could
     * previously have been generated by writeListXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the list.
     *
     * @param parser The XmlPullParser from which to read the list data.
     * @param endTag Name of the tag that will end the list, usually "list".
     * @param name An array of one string, used to return the name attribute
     *             of the list's tag.
     *
     * @return HashMap The newly generated list.
     *
     * @see #readListXml
     */
    public static final ArrayList readThisListXml(XmlPullParser parser, String endTag,
                                                  String[] name) throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read an ArrayList object from an XmlPullParser.  The XML data could
     * previously have been generated by writeListXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the list.
     *
     * @param parser The XmlPullParser from which to read the list data.
     * @param endTag Name of the tag that will end the list, usually "list".
     * @param name An array of one string, used to return the name attribute
     *             of the list's tag.
     *
     * @return HashMap The newly generated list.
     *
     * @see #readListXml
     */
    private static final ArrayList readThisListXml(XmlPullParser parser, String endTag,
                                                   String[] name, ReadMapCallback callback, boolean arrayMap)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read a HashSet object from an XmlPullParser. The XML data could previously
     * have been generated by writeSetXml(). The XmlPullParser must be positioned
     * <em>after</em> the tag that begins the set.
     *
     * @param parser The XmlPullParser from which to read the set data.
     * @param endTag Name of the tag that will end the set, usually "set".
     * @param name An array of one string, used to return the name attribute
     *             of the set's tag.
     *
     * @return HashSet The newly generated set.
     *
     * @throws XmlPullParserException
     * @throws java.io.IOException
     *
     * @see #readSetXml
     */
    public static final HashSet readThisSetXml(XmlPullParser parser, String endTag, String[] name)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read a HashSet object from an XmlPullParser. The XML data could previously
     * have been generated by writeSetXml(). The XmlPullParser must be positioned
     * <em>after</em> the tag that begins the set.
     *
     * @param parser The XmlPullParser from which to read the set data.
     * @param endTag Name of the tag that will end the set, usually "set".
     * @param name An array of one string, used to return the name attribute
     *             of the set's tag.
     *
     * @return HashSet The newly generated set.
     *
     * @throws XmlPullParserException
     * @throws java.io.IOException
     *
     * @see #readSetXml
     * @hide
     */
    private static final HashSet readThisSetXml(XmlPullParser parser, String endTag, String[] name,
                                                ReadMapCallback callback, boolean arrayMap)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read a byte[] object from an XmlPullParser.  The XML data could
     * previously have been generated by writeByteArrayXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the list.
     *
     * @param parser The XmlPullParser from which to read the list data.
     * @param endTag Name of the tag that will end the list, usually "list".
     * @param name An array of one string, used to return the name attribute
     *             of the list's tag.
     *
     * @return Returns a newly generated byte[].
     *
     * @see #writeByteArrayXml
     */
    public static final byte[] readThisByteArrayXml(XmlPullParser parser,
                                                    String endTag, String[] name)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read an int[] object from an XmlPullParser.  The XML data could
     * previously have been generated by writeIntArrayXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the list.
     *
     * @param parser The XmlPullParser from which to read the list data.
     * @param endTag Name of the tag that will end the list, usually "list".
     * @param name An array of one string, used to return the name attribute
     *             of the list's tag.
     *
     * @return Returns a newly generated int[].
     *
     * @see #readListXml
     */
    public static final int[] readThisIntArrayXml(XmlPullParser parser,
                                                  String endTag, String[] name)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read a long[] object from an XmlPullParser.  The XML data could
     * previously have been generated by writeLongArrayXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the list.
     *
     * @param parser The XmlPullParser from which to read the list data.
     * @param endTag Name of the tag that will end the list, usually "list".
     * @param name An array of one string, used to return the name attribute
     *             of the list's tag.
     *
     * @return Returns a newly generated long[].
     *
     * @see #readListXml
     */
    public static final long[] readThisLongArrayXml(XmlPullParser parser,
                                                    String endTag, String[] name)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read a double[] object from an XmlPullParser.  The XML data could
     * previously have been generated by writeDoubleArrayXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the list.
     *
     * @param parser The XmlPullParser from which to read the list data.
     * @param endTag Name of the tag that will end the list, usually "double-array".
     * @param name An array of one string, used to return the name attribute
     *             of the list's tag.
     *
     * @return Returns a newly generated double[].
     *
     * @see #readListXml
     */
    public static final double[] readThisDoubleArrayXml(XmlPullParser parser, String endTag,
                                                        String[] name) throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read a String[] object from an XmlPullParser.  The XML data could
     * previously have been generated by writeStringArrayXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the list.
     *
     * @param parser The XmlPullParser from which to read the list data.
     * @param endTag Name of the tag that will end the list, usually "string-array".
     * @param name An array of one string, used to return the name attribute
     *             of the list's tag.
     *
     * @return Returns a newly generated String[].
     *
     * @see #readListXml
     */
    public static final String[] readThisStringArrayXml(XmlPullParser parser, String endTag,
                                                        String[] name) throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read a boolean[] object from an XmlPullParser.  The XML data could
     * previously have been generated by writeBooleanArrayXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the list.
     *
     * @param parser The XmlPullParser from which to read the list data.
     * @param endTag Name of the tag that will end the list, usually "string-array".
     * @param name An array of one string, used to return the name attribute
     *             of the list's tag.
     *
     * @return Returns a newly generated boolean[].
     *
     * @see #readListXml
     */
    public static final boolean[] readThisBooleanArrayXml(XmlPullParser parser, String endTag,
                                                          String[] name) throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Read a flattened object from an XmlPullParser.  The XML data could
     * previously have been written with writeMapXml(), writeListXml(), or
     * writeValueXml().  The XmlPullParser must be positioned <em>at</em> the
     * tag that defines the value.
     *
     * @param parser The XmlPullParser from which to read the object.
     * @param name An array of one string, used to return the name attribute
     *             of the value's tag.
     *
     * @return Object The newly generated value object.
     *
     * @see #readMapXml
     * @see #readListXml
     * @see #writeValueXml
     */
    public static final Object readValueXml(XmlPullParser parser, String[] name)
            throws XmlPullParserException, java.io.IOException
    {
        throw new UnsupportedOperationException("STUB");
    }
    private static final Object readThisValueXml(XmlPullParser parser, String[] name,
                                                 ReadMapCallback callback, boolean arrayMap)
            throws XmlPullParserException, java.io.IOException {
        throw new UnsupportedOperationException("STUB");
    }
    private static final Object readThisPrimitiveValueXml(XmlPullParser parser, String tagName)
            throws XmlPullParserException, java.io.IOException
    {
        throw new UnsupportedOperationException("STUB");
    }
    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException
    {
        throw new UnsupportedOperationException("STUB");
    }
    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        throw new UnsupportedOperationException("STUB");
    }
    public static boolean nextElementWithin(XmlPullParser parser, int outerDepth)
            throws IOException, XmlPullParserException {
        throw new UnsupportedOperationException("STUB");
    }
    public static int readIntAttribute(XmlPullParser in, String name, int defaultValue) {
        throw new UnsupportedOperationException("STUB");
    }
    public static int readIntAttribute(XmlPullParser in, String name) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public static void writeIntAttribute(XmlSerializer out, String name, int value)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public static long readLongAttribute(XmlPullParser in, String name, long defaultValue) {
        throw new UnsupportedOperationException("STUB");
    }
    public static long readLongAttribute(XmlPullParser in, String name) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public static void writeLongAttribute(XmlSerializer out, String name, long value)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public static float readFloatAttribute(XmlPullParser in, String name) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public static void writeFloatAttribute(XmlSerializer out, String name, float value)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public static boolean readBooleanAttribute(XmlPullParser in, String name) {
        throw new UnsupportedOperationException("STUB");
    }
    public static boolean readBooleanAttribute(XmlPullParser in, String name,
                                               boolean defaultValue) {
        throw new UnsupportedOperationException("STUB");
    }
    public static void writeBooleanAttribute(XmlSerializer out, String name, boolean value)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public static Uri readUriAttribute(XmlPullParser in, String name) {
        throw new UnsupportedOperationException("STUB");
    }
    public static void writeUriAttribute(XmlSerializer out, String name, Uri value)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public static String readStringAttribute(XmlPullParser in, String name) {
        throw new UnsupportedOperationException("STUB");
    }
    public static void writeStringAttribute(XmlSerializer out, String name, CharSequence value)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public static byte[] readByteArrayAttribute(XmlPullParser in, String name) {
        throw new UnsupportedOperationException("STUB");
    }
    public static void writeByteArrayAttribute(XmlSerializer out, String name, byte[] value)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public static Bitmap readBitmapAttribute(XmlPullParser in, String name) {
        throw new UnsupportedOperationException("STUB");
    }
    @Deprecated
    public static void writeBitmapAttribute(XmlSerializer out, String name, Bitmap value)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /** @hide */
    public interface WriteMapCallback {
        /**
         * Called from writeMapXml when an Object type is not recognized. The implementer
         * must write out the entire element including start and end tags.
         *
         * @param v The object to be written out
         * @param name The mapping key for v. Must be written into the "name" attribute of the
         *             start tag.
         * @param out The XML output stream.
         * @throws XmlPullParserException on unrecognized Object type.
         * @throws IOException on XmlSerializer serialization errors.
         * @hide
         */
        public void writeUnknownObject(Object v, String name, XmlSerializer out)
                throws XmlPullParserException, IOException;
    }
    /** @hide */
    public interface ReadMapCallback {
        /**
         * Called from readThisMapXml when a START_TAG is not recognized. The input stream
         * is positioned within the start tag so that attributes can be read using in.getAttribute.
         *
         * @param in the XML input stream
         * @param tag the START_TAG that was not recognized.
         * @return the Object parsed from the stream which will be put into the map.
         * @throws XmlPullParserException if the START_TAG is not recognized.
         * @throws IOException on XmlPullParser serialization errors.
         * @hide
         */
        public Object readThisUnknownObjectXml(XmlPullParser in, String tag)
                throws XmlPullParserException, IOException;
    }
}
