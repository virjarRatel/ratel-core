package android.content.res;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.res.Resources.Theme;
/**
 * Data structure used for caching data against themes.
 *
 * @param <T> type of data to cache
 */
abstract class ThemedResourceCache<T> {
    /**
     * Adds a new theme-dependent entry to the cache.
     *
     * @param key a key that uniquely identifies the entry
     * @param theme the theme against which this entry was inflated, or
     *              {@code null} if the entry has no theme applied
     * @param entry the entry to cache
     */
    public void put(long key, @Nullable Theme theme, @NonNull T entry) {
        put(key, theme, entry, true);
    }
    /**
     * Adds a new entry to the cache.
     *
     * @param key a key that uniquely identifies the entry
     * @param theme the theme against which this entry was inflated, or
     *              {@code null} if the entry has no theme applied
     * @param entry the entry to cache
     * @param usesTheme {@code true} if the entry is affected theme changes,
     *                  {@code false} otherwise
     */
    public void put(long key, @Nullable Theme theme, @NonNull T entry, boolean usesTheme) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Returns an entry from the cache.
     *
     * @param key a key that uniquely identifies the entry
     * @param theme the theme where the entry will be used
     * @return a cached entry, or {@code null} if not in the cache
     */
    @Nullable
    public T get(long key, @Nullable Theme theme) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Prunes cache entries that have been invalidated by a configuration
     * change.
     *
     * @param configChanges a bitmask of configuration changes
     */
    public void onConfigurationChange(int configChanges) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Returns whether a cached entry has been invalidated by a configuration
     * change.
     *
     * @param entry a cached entry
     * @param configChanges a non-zero bitmask of configuration changes
     * @return {@code true} if the entry is invalid, {@code false} otherwise
     */
    protected abstract boolean shouldInvalidateEntry(@NonNull T entry, int configChanges);
}