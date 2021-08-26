package android.view;
import android.annotation.NonNull;
import android.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
/**
 * {@link ViewHierarchyEncoder} is a serializer that is tailored towards writing out
 * view hierarchies (the view tree, along with the properties for each view) to a stream.
 *
 * It is typically used as follows:
 * <pre>
 *   ViewHierarchyEncoder e = new ViewHierarchyEncoder();
 *
 *   for (View view : views) {
 *      e.beginObject(view);
 *      e.addProperty("prop1", value);
 *      ...
 *      e.endObject();
 *   }
 *
 *   // repeat above snippet for each view, finally end with:
 *   e.endStream();
 * </pre>
 *
 * <p>On the stream, a snippet such as the above gets encoded as a series of Map's (one
 * corresponding to each view) with the property name as the key and the property value
 * as the value.
 *
 * <p>Since the property names are practically the same across all views, rather than using
 * the property name directly as the key, we use a short integer id corresponding to each
 * property name as the key. A final map is added at the end which contains the mapping
 * from the integer to its property name.
 *
 * <p>A value is encoded as a single byte type identifier followed by the encoding of the
 * value. Only primitive types are supported as values, in addition to the Map type.
 *
 * @hide
 */
public class ViewHierarchyEncoder {
    public ViewHierarchyEncoder(@NonNull ByteArrayOutputStream stream) {
        throw new UnsupportedOperationException("STUB");
    }
    public void beginObject(@NonNull Object o) {
        throw new UnsupportedOperationException("STUB");
    }
    public void endObject() {
        throw new UnsupportedOperationException("STUB");
    }
    public void endStream() {
        throw new UnsupportedOperationException("STUB");
    }
    public void addProperty(@NonNull String name, boolean v) {
        throw new UnsupportedOperationException("STUB");
    }
    public void addProperty(@NonNull String name, short s) {
        throw new UnsupportedOperationException("STUB");
    }
    public void addProperty(@NonNull String name, int v) {
        throw new UnsupportedOperationException("STUB");
    }
    public void addProperty(@NonNull String name, float v) {
        throw new UnsupportedOperationException("STUB");
    }
    public void addProperty(@NonNull String name, @Nullable String s) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Writes the given name as the property name, and leaves it to the callee
     * to fill in value for this property.
     */
    public void addPropertyKey(@NonNull String name) {
        throw new UnsupportedOperationException("STUB");
    }
}