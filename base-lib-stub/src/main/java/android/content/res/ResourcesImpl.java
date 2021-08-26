package android.content.res;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.graphics.Typeface;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.DisplayAdjustments;
/**
 * The implementation of Resource access. This class contains the AssetManager and all caches
 * associated with it.
 *
 * {@link Resources} is just a thing wrapper around this class. When a configuration change
 * occurs, clients can retain the same {@link Resources} reference because the underlying
 * {@link ResourcesImpl} object will be updated or re-created.
 *
 * @hide
 */
public class ResourcesImpl {
    public static final boolean TRACE_FOR_DETAILED_PRELOAD =
            SystemProperties.getBoolean("debug.trace_resource_preload", false);
    /**
     * Creates a new ResourcesImpl object with CompatibilityInfo.
     *
     * @param assets Previously created AssetManager.
     * @param metrics Current display metrics to consider when
     *                selecting/computing resource values.
     * @param config Desired device configuration to consider when
     *               selecting/computing resource values (optional).
     * @param displayAdjustments this resource's Display override and compatibility info.
     *                           Must not be null.
     */
    public ResourcesImpl(@NonNull AssetManager assets, @Nullable DisplayMetrics metrics,
                         @Nullable Configuration config, @NonNull DisplayAdjustments displayAdjustments) {
        throw new UnsupportedOperationException("STUB");
    }
    public DisplayAdjustments getDisplayAdjustments() {
        throw new UnsupportedOperationException("STUB");
    }
    public AssetManager getAssets() {
        throw new UnsupportedOperationException("STUB");
    }
    @NonNull
    public void updateConfiguration(Configuration config, DisplayMetrics metrics,
                                    CompatibilityInfo compat) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Applies the new configuration, returning a bitmask of the changes
     * between the old and new configurations.
     *
     * @param config the new configuration
     * @return bitmask of config changes
     */
    public int calcConfigChanges(@Nullable Configuration config) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Call this to remove all cached loaded layout resources from the
     * Resources object.  Only intended for use with performance testing
     * tools.
     */
    public void flushLayoutCache() {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Loads a font from XML or resources stream.
     */
    @Nullable
    public Typeface loadFont(Resources wrapper, TypedValue value, int id) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Start preloading of resource data using this Resources object.  Only
     * for use by the zygote process for loading common system resources.
     * {@hide}
     */
    public final void startPreloading() {
        throw new UnsupportedOperationException("STUB");
    }
    public class ThemeImpl {
        /**
         * Unique key for the series of styles applied to this theme.
         */
        private final Resources.ThemeKey mKey = new Resources.ThemeKey();
        @SuppressWarnings("hiding")
        private final AssetManager mAssets;
        private final long mTheme;
        /**
         * Resource identifier for the theme.
         */
        private int mThemeResId = 0;
        /*package*/ ThemeImpl() {
            throw new UnsupportedOperationException("STUB");
        }
        @Override
        protected void finalize() throws Throwable {
            throw new UnsupportedOperationException("STUB");
        }
        /*package*/ Resources.ThemeKey getKey() {
            return mKey;
        }
        /*package*/ long getNativeTheme() {
            return mTheme;
        }
        /*package*/ int getAppliedStyleResId() {
            return mThemeResId;
        }
        void applyStyle(int resId, boolean force) {
            throw new UnsupportedOperationException("STUB");
        }
        @NonNull
        TypedArray obtainStyledAttributes(@NonNull Resources.Theme wrapper,
                                          AttributeSet set,
                                          int[] attrs,
                                          int defStyleAttr,
                                          int defStyleRes) {
            throw new UnsupportedOperationException("STUB");
        }
        public void dump(int priority, String tag, String prefix) {
            throw new UnsupportedOperationException("STUB");
        }
        String[] getTheme() {
            throw new UnsupportedOperationException("STUB");
        }
        /**
         * Rebases the theme against the parent Resource object's current
         * configuration by re-applying the styles passed to
         * {@link #applyStyle(int, boolean)}.
         */
        void rebase() {
            throw new UnsupportedOperationException("STUB");
        }
    }
}