package android.os;

/**
 * Created by rover12421 on 3/16/17.
 */
public class SystemProperties {
    public static final int PROP_NAME_MAX = 31;
    public static final int PROP_VALUE_MAX = 91;

    /**
     * Get the value for the given key.
     * @return an empty string if the key isn't found
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    public static String get(String key) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Get the value for the given key.
     * @return if the key isn't found, return def if it isn't null, or an empty string otherwise
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    public static String get(String key, String def) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Get the value for the given key, and return as an integer.
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as an integer, or def if the key isn't found or
     *         cannot be parsed
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    public static int getInt(String key, int def) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Get the value for the given key, and return as a long.
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as a long, or def if the key isn't found or
     *         cannot be parsed
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    public static long getLong(String key, long def) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Get the value for the given key, returned as a boolean.
     * Values 'n', 'no', '0', 'false' or 'off' are considered false.
     * Values 'y', 'yes', '1', 'true' or 'on' are considered true.
     * (case sensitive).
     * If the key does not exist, or has any other value, then the default
     * result is returned.
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as a boolean, or def if the key isn't found or is
     *         not able to be parsed as a boolean.
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    public static boolean getBoolean(String key, boolean def) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Set the value for the given key.
     * @throws IllegalArgumentException if the key exceeds 32 characters
     * @throws IllegalArgumentException if the value exceeds 92 characters
     */
    public static void set(String key, String val) {
        throw new UnsupportedOperationException("STUB");
    }
    public static void addChangeCallback(Runnable callback) {
        throw new UnsupportedOperationException("STUB");
    }
    static void callChangeCallbacks() {
        throw new UnsupportedOperationException("STUB");
    }
}
