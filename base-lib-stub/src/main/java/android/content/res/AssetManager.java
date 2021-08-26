package android.content.res;

import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
/**
 * Provides access to an application's raw asset files; see {@link Resources}
 * for the way most applications will want to retrieve their resource data.
 * This class presents a lower-level API that allows you to open and read raw
 * files that have been bundled with the application as a simple stream of
 * bytes.
 */
public final class AssetManager implements AutoCloseable {
    /* modes used when opening an asset */
    /**
     * Mode for {@link #open(String, int)}: no specific information about how
     * data will be accessed.
     */
    public static final int ACCESS_UNKNOWN = 0;
    /**
     * Mode for {@link #open(String, int)}: Read chunks, and seek forward and
     * backward.
     */
    public static final int ACCESS_RANDOM = 1;
    /**
     * Mode for {@link #open(String, int)}: Read sequentially, with an
     * occasional forward seek.
     */
    public static final int ACCESS_STREAMING = 2;
    /**
     * Mode for {@link #open(String, int)}: Attempt to load contents into
     * memory, for fast small reads.
     */
    public static final int ACCESS_BUFFER = 3;

    /**
     * Create a new AssetManager containing only the basic system assets.
     * Applications will not generally use this method, instead retrieving the
     * appropriate asset manager with {@link Resources#getAssets}.    Not for
     * use by applications.
     * {@hide}
     */
    public AssetManager() {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Return a global shared asset manager that provides access to only
     * system assets (no application assets).
     * {@hide}
     */
    public static AssetManager getSystem() {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Close this asset manager.
     */
    public void close() {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Open an asset using ACCESS_STREAMING mode.  This provides access to
     * files that have been bundled with an application as assets -- that is,
     * files placed in to the "assets" directory.
     *
     * @param fileName The name of the asset to open.  This name can be
     *                 hierarchical.
     *
     * @see #open(String, int)
     * @see #list
     */
    public final InputStream open(String fileName) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Open an asset using an explicit access mode, returning an InputStream to
     * read its contents.  This provides access to files that have been bundled
     * with an application as assets -- that is, files placed in to the
     * "assets" directory.
     *
     * @param fileName The name of the asset to open.  This name can be
     *                 hierarchical.
     * @param accessMode Desired access mode for retrieving the data.
     *
     * @see #ACCESS_UNKNOWN
     * @see #ACCESS_STREAMING
     * @see #ACCESS_RANDOM
     * @see #ACCESS_BUFFER
     * @see #open(String)
     * @see #list
     */
    public final InputStream open(String fileName, int accessMode)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public final AssetFileDescriptor openFd(String fileName)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Return a String array of all the assets at the given path.
     *
     * @param path A relative path within the assets, i.e., "docs/home.html".
     *
     * @return String[] Array of strings, one for each asset.  These file
     *         names are relative to 'path'.  You can open the file by
     *         concatenating 'path' and a name in the returned string (via
     *         File) and passing that to open().
     *
     * @see #open
     */
    public native final String[] list(String path)
            throws IOException;
    /**
     * {@hide}
     * Open a non-asset file as an asset using ACCESS_STREAMING mode.  This
     * provides direct access to all of the files included in an application
     * package (not only its assets).  Applications should not normally use
     * this.
     *
     * @see #open(String)
     */
    public final InputStream openNonAsset(String fileName) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * {@hide}
     * Open a non-asset file as an asset using a specific access mode.  This
     * provides direct access to all of the files included in an application
     * package (not only its assets).  Applications should not normally use
     * this.
     *
     * @see #open(String, int)
     */
    public final InputStream openNonAsset(String fileName, int accessMode)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * {@hide}
     * Open a non-asset in a specified package.  Not for use by applications.
     *
     * @param cookie Identifier of the package to be opened.
     * @param fileName Name of the asset to retrieve.
     */
    public final InputStream openNonAsset(int cookie, String fileName)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * {@hide}
     * Open a non-asset in a specified package.  Not for use by applications.
     *
     * @param cookie Identifier of the package to be opened.
     * @param fileName Name of the asset to retrieve.
     * @param accessMode Desired access mode for retrieving the data.
     */
    public final InputStream openNonAsset(int cookie, String fileName, int accessMode)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    public final AssetFileDescriptor openNonAssetFd(String fileName)
            throws IOException {
        return openNonAssetFd(0, fileName);
    }

    public final AssetFileDescriptor openNonAssetFd(int cookie,
                                                    String fileName) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Retrieve a parser for a compiled XML file.
     *
     * @param fileName The name of the file to retrieve.
     */
    public final XmlResourceParser openXmlResourceParser(String fileName)
            throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Retrieve a parser for a compiled XML file.
     *
     * @param cookie Identifier of the package to be opened.
     * @param fileName The name of the file to retrieve.
     */
    public final XmlResourceParser openXmlResourceParser(int cookie,
                                                         String fileName) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }
    protected void finalize() throws Throwable {
        throw new UnsupportedOperationException("STUB");
    }

    public final class AssetInputStream extends InputStream {
        /**
         * @hide
         */
        public final int getAssetInt() {
            throw new UnsupportedOperationException();
        }
        /**
         * @hide
         */
        public final long getNativeAsset() {
            throw new UnsupportedOperationException("STUB");
        }
        public final int read() throws IOException {
            throw new UnsupportedOperationException("STUB");
        }
        public final boolean markSupported() {
            throw new UnsupportedOperationException("STUB");
        }
        public final int available() throws IOException {
            throw new UnsupportedOperationException("STUB");
        }
        public final void close() throws IOException {
            throw new UnsupportedOperationException("STUB");
        }
        public final void mark(int readlimit) {
            throw new UnsupportedOperationException("STUB");
        }
        public final void reset() throws IOException {
            throw new UnsupportedOperationException("STUB");
        }
        public final int read(byte[] b) throws IOException {
            throw new UnsupportedOperationException("STUB");
        }
        public final int read(byte[] b, int off, int len) throws IOException {
            throw new UnsupportedOperationException("STUB");
        }
        public final long skip(long n) throws IOException {
            throw new UnsupportedOperationException("STUB");
        }
        protected void finalize() throws Throwable
        {
            throw new UnsupportedOperationException("STUB");
        }
    }
    /**
     * Add an additional set of assets to the asset manager.  This can be
     * either a directory or ZIP file.  Not for use by applications.  Returns
     * the cookie of the added asset, or 0 on failure.
     * {@hide}
     */
    public final int addAssetPath(String path) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Add an application assets to the asset manager and loading it as shared library.
     * This can be either a directory or ZIP file.  Not for use by applications.  Returns
     * the cookie of the added asset, or 0 on failure.
     * {@hide}
     */
    public final int addAssetPathAsSharedLibrary(String path) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Add a set of assets to overlay an already added set of assets.
     *
     * This is only intended for application resources. System wide resources
     * are handled before any Java code is executed.
     *
     * {@hide}
     */
    public final int addOverlayPath(String idmapPath) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * See addOverlayPath.
     *
     * {@hide}
     */
    public native final int addOverlayPathNative(String idmapPath);
    /**
     * Add multiple sets of assets to the asset manager at once.  See
     * {@link #addAssetPath(String)} for more information.  Returns array of
     * cookies for each added asset with 0 indicating failure, or null if
     * the input array of paths is null.
     * {@hide}
     */
    public final int[] addAssetPaths(String[] paths) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Determine whether the state in this asset manager is up-to-date with
     * the files on the filesystem.  If false is returned, you need to
     * instantiate a new AssetManager class to see the new data.
     * {@hide}
     */
    public native final boolean isUpToDate();
    /**
     * Get the locales that this asset manager contains data for.
     *
     * <p>On SDK 21 (Android 5.0: Lollipop) and above, Locale strings are valid
     * <a href="https://tools.ietf.org/html/bcp47">BCP-47</a> language tags and can be
     * parsed using {@link java.util.Locale#forLanguageTag(String)}.
     *
     * <p>On SDK 20 (Android 4.4W: Kitkat for watches) and below, locale strings
     * are of the form {@code ll_CC} where {@code ll} is a two letter language code,
     * and {@code CC} is a two letter country code.
     */
    public native final String[] getLocales();
    /**
     * Same as getLocales(), except that locales that are only provided by the system (i.e. those
     * present in framework-res.apk or its overlays) will not be listed.
     *
     * For example, if the "system" assets support English, French, and German, and the additional
     * assets support Cherokee and French, getLocales() would return
     * [Cherokee, English, French, German], while getNonSystemLocales() would return
     * [Cherokee, French].
     * {@hide}
     */
    public native final String[] getNonSystemLocales();
    /** {@hide} */
    public native final Configuration[] getSizeConfigurations();
    /**
     * Change the configuation used when retrieving resources.  Not for use by
     * applications.
     * {@hide}
     */
    public native final void setConfiguration(int mcc, int mnc, String locale,
                                              int orientation, int touchscreen, int density, int keyboard,
                                              int keyboardHidden, int navigation, int screenWidth, int screenHeight,
                                              int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
                                              int screenLayout, int uiMode, int colorMode, int majorVersion);
    /**
     * {@hide}
     */
    public native final String getCookieName(int cookie);
    /**
     * {@hide}
     */
    public native final SparseArray<String> getAssignedPackageIdentifiers();
    /**
     * {@hide}
     */
    public native static final int getGlobalAssetCount();

    /**
     * {@hide}
     */
    public native static final String getAssetAllocations();

    /**
     * {@hide}
     */
    public native static final int getGlobalAssetManagerCount();

}