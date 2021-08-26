package android.content.res;

/**
 * A Cache class which can be used to cache resource objects that are easy to clone but more
 * expensive to inflate.
 *
 * @hide For internal use only.
 */
public class ConfigurationBoundResourceCache<T> extends ThemedResourceCache<ConstantState<T>> {
    /**
     * If the resource is cached, creates and returns a new instance of it.
     *
     * @param key a key that uniquely identifies the drawable resource
     * @param resources a Resources object from which to create new instances.
     * @param theme the theme where the resource will be used
     * @return a new instance of the resource, or {@code null} if not in
     *         the cache
     */
    public T getInstance(long key, Resources resources, Resources.Theme theme) {
        throw new UnsupportedOperationException("STUB");
    }
    @Override
    public boolean shouldInvalidateEntry(ConstantState<T> entry, int configChanges) {
        throw new UnsupportedOperationException("STUB");
    }
}