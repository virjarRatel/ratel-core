/*
 * Copyright (C) 2007 The Android Open Source Project
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

package android.content.pm;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.jar.StrictJarFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Parser for package files (APKs) on disk. This supports apps packaged either
 * as a single "monolithic" APK, or apps packaged as a "cluster" of multiple
 * APKs in a single directory.
 * <p>
 * Apps packaged as multiple APKs always consist of a single "base" APK (with a
 * {@code null} split name) and zero or more "split" APKs (with unique split
 * names). Any subset of those split APKs are a valid install, as long as the
 * following constraints are met:
 * <ul>
 * <li>All APKs must have the exact same package name, version code, and signing
 * certificates.
 * <li>All APKs must have unique split names.
 * <li>All installations must contain a single base APK.
 * </ul>
 *
 * @hide
 */
public class PackageParser {
    public static final int APK_SIGNING_UNKNOWN = 0;
    public static final int APK_SIGNING_V1 = 1;
    public static final int APK_SIGNING_V2 = 2;


    /** @hide */
    public static class NewPermissionInfo {
        public final String name;
        public final int sdkVersion;
        public final int fileVersion;

        public NewPermissionInfo(String name, int sdkVersion, int fileVersion) {
            this.name = name;
            this.sdkVersion = sdkVersion;
            this.fileVersion = fileVersion;
        }
    }

    /** @hide */
    public static class SplitPermissionInfo {
        public final String rootPerm;
        public final String[] newPerms;
        public final int targetSdk;

        public SplitPermissionInfo(String rootPerm, String[] newPerms, int targetSdk) {
            this.rootPerm = rootPerm;
            this.newPerms = newPerms;
            this.targetSdk = targetSdk;
        }
    }

    /**
     * List of new permissions that have been added since 1.0.
     * NOTE: These must be declared in SDK version order, with permissions
     * added to older SDKs appearing before those added to newer SDKs.
     * If sdkVersion is 0, then this is not a permission that we want to
     * automatically add to older apps, but we do want to allow it to be
     * granted during a platform update.
     * @hide
     */
    public static final PackageParser.NewPermissionInfo NEW_PERMISSIONS[] =
            new PackageParser.NewPermissionInfo[] {
                    new PackageParser.NewPermissionInfo(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.os.Build.VERSION_CODES.DONUT, 0),
                    new PackageParser.NewPermissionInfo(android.Manifest.permission.READ_PHONE_STATE,
                            android.os.Build.VERSION_CODES.DONUT, 0)
            };

    /**
     * List of permissions that have been split into more granular or dependent
     * permissions.
     * @hide
     */
    public static final PackageParser.SplitPermissionInfo SPLIT_PERMISSIONS[] =
            new PackageParser.SplitPermissionInfo[] {
                    // READ_EXTERNAL_STORAGE is always required when an app requests
                    // WRITE_EXTERNAL_STORAGE, because we can't have an app that has
                    // write access without read access.  The hack here with the target
                    // target SDK version ensures that this grant is always done.
                    new PackageParser.SplitPermissionInfo(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE },
                            android.os.Build.VERSION_CODES.CUR_DEVELOPMENT+1),
                    new PackageParser.SplitPermissionInfo(android.Manifest.permission.READ_CONTACTS,
                            new String[] { android.Manifest.permission.READ_CALL_LOG },
                            android.os.Build.VERSION_CODES.JELLY_BEAN),
                    new PackageParser.SplitPermissionInfo(android.Manifest.permission.WRITE_CONTACTS,
                            new String[] { android.Manifest.permission.WRITE_CALL_LOG },
                            android.os.Build.VERSION_CODES.JELLY_BEAN)
            };


    static class ParsePackageItemArgs {
        final Package owner;
        final String[] outError;
        final int nameRes;
        final int labelRes;
        final int iconRes;
        final int roundIconRes;
        final int logoRes;
        final int bannerRes;

        String tag;
        TypedArray sa;

        ParsePackageItemArgs(Package _owner, String[] _outError,
                             int _nameRes, int _labelRes, int _iconRes, int _roundIconRes, int _logoRes,
                             int _bannerRes) {
            owner = _owner;
            outError = _outError;
            nameRes = _nameRes;
            labelRes = _labelRes;
            iconRes = _iconRes;
            logoRes = _logoRes;
            bannerRes = _bannerRes;
            roundIconRes = _roundIconRes;
        }
    }

    /** @hide */
    public static class ParseComponentArgs extends ParsePackageItemArgs {
        final String[] sepProcesses;
        final int processRes;
        final int descriptionRes;
        final int enabledRes;
        int flags;

        public ParseComponentArgs(Package _owner, String[] _outError,
                                  int _nameRes, int _labelRes, int _iconRes, int _roundIconRes, int _logoRes,
                                  int _bannerRes,
                                  String[] _sepProcesses, int _processRes,
                                  int _descriptionRes, int _enabledRes) {
            super(_owner, _outError, _nameRes, _labelRes, _iconRes, _roundIconRes, _logoRes,
                    _bannerRes);
            sepProcesses = _sepProcesses;
            processRes = _processRes;
            descriptionRes = _descriptionRes;
            enabledRes = _enabledRes;
        }
    }

    /**
     * Lightweight parsed details about a single package.
     */
    public static class PackageLite {
        public final String packageName;
        public final int versionCode;
        public final int installLocation;
        public final VerifierInfo[] verifiers;

        /** Names of any split APKs, ordered by parsed splitName */
        public final String[] splitNames;

        /** Names of any split APKs that are features. Ordered by splitName */
        public final boolean[] isFeatureSplits;

        /** Dependencies of any split APKs, ordered by parsed splitName */
        public final String[] usesSplitNames;
        public final String[] configForSplit;

        /**
         * Path where this package was found on disk. For monolithic packages
         * this is path to single base APK file; for cluster packages this is
         * path to the cluster directory.
         */
        public final String codePath;

        /** Path of base APK */
        public final String baseCodePath;
        /** Paths of any split APKs, ordered by parsed splitName */
        public final String[] splitCodePaths;

        /** Revision code of base APK */
        public final int baseRevisionCode;
        /** Revision codes of any split APKs, ordered by parsed splitName */
        public final int[] splitRevisionCodes;

        public final boolean coreApp;
        public final boolean debuggable;
        public final boolean multiArch;
        public final boolean use32bitAbi;
        public final boolean extractNativeLibs;
        public final boolean isolatedSplits;

        public PackageLite(String codePath, ApkLite baseApk, String[] splitNames,
                           boolean[] isFeatureSplits, String[] usesSplitNames, String[] configForSplit,
                           String[] splitCodePaths, int[] splitRevisionCodes) {
            this.packageName = baseApk.packageName;
            this.versionCode = baseApk.versionCode;
            this.installLocation = baseApk.installLocation;
            this.verifiers = baseApk.verifiers;
            this.splitNames = splitNames;
            this.isFeatureSplits = isFeatureSplits;
            this.usesSplitNames = usesSplitNames;
            this.configForSplit = configForSplit;
            this.codePath = codePath;
            this.baseCodePath = baseApk.codePath;
            this.splitCodePaths = splitCodePaths;
            this.baseRevisionCode = baseApk.revisionCode;
            this.splitRevisionCodes = splitRevisionCodes;
            this.coreApp = baseApk.coreApp;
            this.debuggable = baseApk.debuggable;
            this.multiArch = baseApk.multiArch;
            this.use32bitAbi = baseApk.use32bitAbi;
            this.extractNativeLibs = baseApk.extractNativeLibs;
            this.isolatedSplits = baseApk.isolatedSplits;
        }

        public List<String> getAllCodePaths() {
            throw new UnsupportedOperationException("STUB");
        }
    }

    /**
     * Lightweight parsed details about a single APK file.
     */
    public static class ApkLite {
        public final String codePath;
        public final String packageName;
        public final String splitName;
        public boolean isFeatureSplit;
        public final String configForSplit;
        public final String usesSplitName;
        public final int versionCode;
        public final int revisionCode;
        public final int installLocation;
        public final VerifierInfo[] verifiers;
        public final Signature[] signatures;
        public final Certificate[][] certificates;
        public final boolean coreApp;
        public final boolean debuggable;
        public final boolean multiArch;
        public final boolean use32bitAbi;
        public final boolean extractNativeLibs;
        public final boolean isolatedSplits;

        public ApkLite(String codePath, String packageName, String splitName, boolean isFeatureSplit,
                       String configForSplit, String usesSplitName, int versionCode, int revisionCode,
                       int installLocation, List<VerifierInfo> verifiers, Signature[] signatures,
                       Certificate[][] certificates, boolean coreApp, boolean debuggable,
                       boolean multiArch, boolean use32bitAbi, boolean extractNativeLibs,
                       boolean isolatedSplits) {
            this.codePath = codePath;
            this.packageName = packageName;
            this.splitName = splitName;
            this.isFeatureSplit = isFeatureSplit;
            this.configForSplit = configForSplit;
            this.usesSplitName = usesSplitName;
            this.versionCode = versionCode;
            this.revisionCode = revisionCode;
            this.installLocation = installLocation;
            this.verifiers = verifiers.toArray(new VerifierInfo[verifiers.size()]);
            this.signatures = signatures;
            this.certificates = certificates;
            this.coreApp = coreApp;
            this.debuggable = debuggable;
            this.multiArch = multiArch;
            this.use32bitAbi = use32bitAbi;
            this.extractNativeLibs = extractNativeLibs;
            this.isolatedSplits = isolatedSplits;
        }
    }

    public PackageParser() {
        throw new UnsupportedOperationException("STUB");
    }

    public void setSeparateProcesses(String[] procs) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Flag indicating this parser should only consider apps with
     * {@code coreApp} manifest attribute to be valid apps. This is useful when
     * creating a minimalist boot environment.
     */
    public void setOnlyCoreApps(boolean onlyCoreApps) {
        throw new UnsupportedOperationException("STUB");
    }

    public void setDisplayMetrics(DisplayMetrics metrics) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Sets the cache directory for this package parser.
     */
    public void setCacheDir(File cacheDir) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Callback interface for retrieving information that may be needed while parsing
     * a package.
     */
    public interface Callback {
        boolean hasFeature(String feature);
        String[] getOverlayPaths(String targetPackageName, String targetPath);
        String[] getOverlayApks(String targetPackageName);
    }

    /**
     * Standard implementation of {@link Callback} on top of the public {@link PackageManager}
     * class.
     */
    public static final class CallbackImpl implements Callback {
        private final PackageManager mPm;

        public CallbackImpl(PackageManager pm) {
            mPm = pm;
        }

        @Override public boolean hasFeature(String feature) {
            return mPm.hasSystemFeature(feature);
        }

        @Override public String[] getOverlayPaths(String targetPackageName, String targetPath) {
            return null;
        }

        @Override public String[] getOverlayApks(String targetPackageName) {
            return null;
        }
    }

    /**
     * Set the {@link Callback} that can be used while parsing.
     */
    public void setCallback(Callback cb) {
        throw new UnsupportedOperationException("STUB");
    }

    public static final boolean isApkFile(File file) {
        throw new UnsupportedOperationException("STUB");
    }

    public static boolean isApkPath(String path) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Generate and return the {@link PackageInfo} for a parsed package.
     *
     * @param p the parsed package.
     * @param flags indicating which optional information is included.
     */
    public static PackageInfo generatePackageInfo(PackageParser.Package p,
                                                  int gids[], int flags, long firstInstallTime, long lastUpdateTime,
                                                  Set<String> grantedPermissions, PackageUserState state) {

        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Returns true if the package is installed and not hidden, or if the caller
     * explicitly wanted all uninstalled and hidden packages as well.
     * @param appInfo The applicationInfo of the app being checked.
     */
    private static boolean checkUseInstalledOrHidden(int flags, PackageUserState state,
                                                     ApplicationInfo appInfo) {
        throw new UnsupportedOperationException("STUB");
    }

    public static boolean isAvailable(PackageUserState state) {
        return checkUseInstalledOrHidden(0, state, null);
    }

    public static PackageInfo generatePackageInfo(PackageParser.Package p,
                                                  int gids[], int flags, long firstInstallTime, long lastUpdateTime,
                                                  Set<String> grantedPermissions, PackageUserState state, int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    public final static int PARSE_IS_SYSTEM = 1<<0;
    public final static int PARSE_CHATTY = 1<<1;
    public final static int PARSE_MUST_BE_APK = 1<<2;
    public final static int PARSE_IGNORE_PROCESSES = 1<<3;
    public final static int PARSE_FORWARD_LOCK = 1<<4;
    public final static int PARSE_EXTERNAL_STORAGE = 1<<5;
    public final static int PARSE_IS_SYSTEM_DIR = 1<<6;
    public final static int PARSE_IS_PRIVILEGED = 1<<7;
    public final static int PARSE_COLLECT_CERTIFICATES = 1<<8;
    public final static int PARSE_TRUSTED_OVERLAY = 1<<9;
    public final static int PARSE_ENFORCE_CODE = 1<<10;
    /** @deprecated remove when fixing b/34761192 */
    @Deprecated
    public final static int PARSE_IS_EPHEMERAL = 1<<11;
    public final static int PARSE_FORCE_SDK = 1<<12;

    /**
     * Parse only lightweight details about the package at the given location.
     * Automatically detects if the package is a monolithic style (single APK
     * file) or cluster style (directory of APKs).
     * <p>
     * This performs sanity checking on cluster style packages, such as
     * requiring identical package name and version codes, a single base APK,
     * and unique split names.
     *
     * @see PackageParser#parsePackage(File, int)
     *
     * Since SDK21
     *
     * API >= 21 才有的方法
     */
    public static PackageLite parsePackageLite(File packageFile, int flags)
            throws PackageParserException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Before SDK21
     *
     * API >= 21 不要使用，已经没有这个方法了
     **/
    @Deprecated()
    public static PackageLite parsePackageLite(String packageFile, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Parse the package at the given location. Automatically detects if the
     * package is a monolithic style (single APK file) or cluster style
     * (directory of APKs).
     * <p>
     * This performs sanity checking on cluster style packages, such as
     * requiring identical package name and version codes, a single base APK,
     * and unique split names.
     * <p>
     * Note that this <em>does not</em> perform signature verification; that
     * must be done separately in {@link #collectCertificates(Package, int)}.
     *
     * If {@code useCaches} is true, the package parser might return a cached
     * result from a previous parse of the same {@code packageFile} with the same
     * {@code flags}. Note that this method does not check whether {@code packageFile}
     * has changed since the last parse, it's up to callers to do so.
     *
     * @see #parsePackageLite(File, int)
     */
    public Package parsePackage(File packageFile, int flags, boolean useCaches)
            throws PackageParserException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Equivalent to {@link #parsePackage(File, int, boolean)} with {@code useCaches == false}.
     */
    public Package parsePackage(File packageFile, int flags) throws PackageParserException {
        throw new UnsupportedOperationException("STUB");
    }

    protected Package fromCacheEntry(byte[] bytes) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    protected byte[] toCacheEntry(Package pkg) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Parse the given APK file, treating it as as a single monolithic package.
     * <p>
     * Note that this <em>does not</em> perform signature verification; that
     * must be done separately in {@link #collectCertificates(Package, int)}.
     *
     * @deprecated external callers should move to
     *             {@link #parsePackage(File, int)}. Eventually this method will
     *             be marked private.
     */
    @Deprecated
    public Package parseMonolithicPackage(File apkFile, int flags) throws PackageParserException {
        throw new UnsupportedOperationException("STUB");
    }

    public static int getApkSigningVersion(Package pkg) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Populates the correct packages fields with the given certificates.
     * <p>
     * This is useful when we've already processed the certificates [such as during package
     * installation through an installer session]. We don't re-process the archive and
     * simply populate the correct fields.
     */
    public static void populateCertificates(Package pkg, Certificate[][] certificates)
            throws PackageParserException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Collect certificates from all the APKs described in the given package,
     * populating {@link Package#mSignatures}. Also asserts that all APK
     * contents are signed correctly and consistently.
     */
    public static void collectCertificates(Package pkg, int parseFlags)
            throws PackageParserException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Utility method that retrieves lightweight details about a single APK
     * file, including package name, split name, and install location.
     *
     * @param apkFile path to a single APK
     * @param flags optional parse flags, such as
     *            {@link #PARSE_COLLECT_CERTIFICATES}
     */
    public static ApkLite parseApkLite(File apkFile, int flags)
            throws PackageParserException {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Temporary.
     */
    static public Signature stringToSignature(String str) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Computes the targetSdkVersion to use at runtime. If the package is not
     * compatible with this platform, populates {@code outError[0]} with an
     * error message.
     * <p>
     * If {@code targetCode} is not specified, e.g. the value is {@code null},
     * then the {@code targetVers} will be returned unmodified.
     * <p>
     * Otherwise, the behavior varies based on whether the current platform
     * is a pre-release version, e.g. the {@code platformSdkCodenames} array
     * has length > 0:
     * <ul>
     * <li>If this is a pre-release platform and the value specified by
     * {@code targetCode} is contained within the array of allowed pre-release
     * codenames, this method will return {@link Build.VERSION_CODES#CUR_DEVELOPMENT}.
     * <li>If this is a released platform, this method will return -1 to
     * indicate that the package is not compatible with this platform.
     * </ul>
     *
     * @param targetVers targetSdkVersion number, if specified in the
     *                   application manifest, or 0 otherwise
     * @param targetCode targetSdkVersion code, if specified in the application
     *                   manifest, or {@code null} otherwise
     * @param platformSdkVersion platform SDK version number, typically
     *                           Build.VERSION.SDK_INT
     * @param platformSdkCodenames array of allowed pre-release SDK codenames
     *                             for this platform
     * @param outError output array to populate with error, if applicable
     * @return the targetSdkVersion to use at runtime, or -1 if the package is
     *         not compatible with this platform
     * @hide Exposed for unit testing only.
     */
    public static int computeTargetSdkVersion(int targetVers,
                                              @Nullable String targetCode, int platformSdkVersion,
                                              @NonNull String[] platformSdkCodenames, @NonNull String[] outError) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Computes the minSdkVersion to use at runtime. If the package is not
     * compatible with this platform, populates {@code outError[0]} with an
     * error message.
     * <p>
     * If {@code minCode} is not specified, e.g. the value is {@code null},
     * then behavior varies based on the {@code platformSdkVersion}:
     * <ul>
     * <li>If the platform SDK version is greater than or equal to the
     * {@code minVers}, returns the {@code mniVers} unmodified.
     * <li>Otherwise, returns -1 to indicate that the package is not
     * compatible with this platform.
     * </ul>
     * <p>
     * Otherwise, the behavior varies based on whether the current platform
     * is a pre-release version, e.g. the {@code platformSdkCodenames} array
     * has length > 0:
     * <ul>
     * <li>If this is a pre-release platform and the value specified by
     * {@code targetCode} is contained within the array of allowed pre-release
     * codenames, this method will return {@link Build.VERSION_CODES#CUR_DEVELOPMENT}.
     * <li>If this is a released platform, this method will return -1 to
     * indicate that the package is not compatible with this platform.
     * </ul>
     *
     * @param minVers minSdkVersion number, if specified in the application
     *                manifest, or 1 otherwise
     * @param minCode minSdkVersion code, if specified in the application
     *                manifest, or {@code null} otherwise
     * @param platformSdkVersion platform SDK version number, typically
     *                           Build.VERSION.SDK_INT
     * @param platformSdkCodenames array of allowed prerelease SDK codenames
     *                             for this platform
     * @param outError output array to populate with error, if applicable
     * @return the minSdkVersion to use at runtime, or -1 if the package is not
     *         compatible with this platform
     * @hide Exposed for unit testing only.
     */
    public static int computeMinSdkVersion(int minVers,
                                           @Nullable String minCode, int platformSdkVersion,
                                           @NonNull String[] platformSdkCodenames, @NonNull String[] outError) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @param configChanges The bit mask of configChanges fetched from AndroidManifest.xml.
     * @param recreateOnConfigChanges The bit mask recreateOnConfigChanges fetched from
     *                                AndroidManifest.xml.
     * @hide Exposed for unit testing only.
     */
    public static int getActivityConfigChanges(int configChanges, int recreateOnConfigChanges) {
        throw new UnsupportedOperationException("STUB");
    }


    public static final PublicKey parsePublicKey(final String encodedPublicKey) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Representation of a full package parsed from APK files on disk. A package
     * consists of a single base APK, and zero or more split APKs.
     */
    public final static class Package implements Parcelable {

        public String packageName;

        // The package name declared in the manifest as the package can be
        // renamed, for example static shared libs use synthetic package names.
        public String manifestPackageName;

        /** Names of any split APKs, ordered by parsed splitName */
        public String[] splitNames;

        // TODO: work towards making these paths invariant

        public String volumeUuid;

        /**
         * Path where this package was found on disk. For monolithic packages
         * this is path to single base APK file; for cluster packages this is
         * path to the cluster directory.
         */
        public String codePath;

        /** Path of base APK */
        public String baseCodePath;
        /** Paths of any split APKs, ordered by parsed splitName */
        public String[] splitCodePaths;

        /** Revision code of base APK */
        public int baseRevisionCode;
        /** Revision codes of any split APKs, ordered by parsed splitName */
        public int[] splitRevisionCodes;

        /** Flags of any split APKs; ordered by parsed splitName */
        public int[] splitFlags;

        /**
         * Private flags of any split APKs; ordered by parsed splitName.
         *
         * {@hide}
         */
        public int[] splitPrivateFlags;

        public boolean baseHardwareAccelerated;

        // For now we only support one application per package.
        public ApplicationInfo applicationInfo = new ApplicationInfo();

        public final ArrayList<Permission> permissions = new ArrayList<Permission>(0);
        public final ArrayList<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>(0);
        public final ArrayList<Activity> activities = new ArrayList<Activity>(0);
        public final ArrayList<Activity> receivers = new ArrayList<Activity>(0);
        public final ArrayList<Provider> providers = new ArrayList<Provider>(0);
        public final ArrayList<Service> services = new ArrayList<Service>(0);
        public final ArrayList<Instrumentation> instrumentation = new ArrayList<Instrumentation>(0);

        public final ArrayList<String> requestedPermissions = new ArrayList<String>();

        public ArrayList<String> protectedBroadcasts;

        public Package parentPackage;
        public ArrayList<Package> childPackages;

        public String staticSharedLibName = null;
        public int staticSharedLibVersion = 0;
        public ArrayList<String> libraryNames = null;
        public ArrayList<String> usesLibraries = null;
        public ArrayList<String> usesStaticLibraries = null;
        public int[] usesStaticLibrariesVersions = null;
        public String[] usesStaticLibrariesCertDigests = null;
        public ArrayList<String> usesOptionalLibraries = null;
        public String[] usesLibraryFiles = null;

        public ArrayList<ActivityIntentInfo> preferredActivityFilters = null;

        public ArrayList<String> mOriginalPackages = null;
        public String mRealPackage = null;
        public ArrayList<String> mAdoptPermissions = null;

        // We store the application meta-data independently to avoid multiple unwanted references
        public Bundle mAppMetaData = null;

        // The version code declared for this package.
        public int mVersionCode;

        // The version name declared for this package.
        public String mVersionName;

        // The shared user id that this package wants to use.
        public String mSharedUserId;

        // The shared user label that this package wants to use.
        public int mSharedUserLabel;

        // Signatures that were read from the package.
        public Signature[] mSignatures;
        public SigningDetails mSigningDetails;
        public Certificate[][] mCertificates;

        // For use by package manager service for quick lookup of
        // preferred up order.
        public int mPreferredOrder = 0;

        // For use by package manager to keep track of when a package was last used.
        public long[] mLastPackageUsageTimeInMills =
                new long[PackageManager.NOTIFY_PACKAGE_USE_REASONS_COUNT];

        // // User set enabled state.
        // public int mSetEnabled = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        //
        // // Whether the package has been stopped.
        // public boolean mSetStopped = false;

        // Additional data supplied by callers.
        public Object mExtras;

        // Applications hardware preferences
        public ArrayList<ConfigurationInfo> configPreferences = null;

        // Applications requested features
        public ArrayList<FeatureInfo> reqFeatures = null;

        // Applications requested feature groups
        public ArrayList<Object> featureGroups = null;

        public int installLocation;

        public boolean coreApp;

        /* An app that's required for all users and cannot be uninstalled for a user */
        public boolean mRequiredForAllUsers;

        /* The restricted account authenticator type that is used by this application */
        public String mRestrictedAccountType;

        /* The required account type without which this application will not function */
        public String mRequiredAccountType;

        public String mOverlayTarget;
        public int mOverlayPriority;
        public boolean mIsStaticOverlay;
        public boolean mTrustedOverlay;

        /**
         * Data used to feed the KeySetManagerService
         */
        public ArraySet<PublicKey> mSigningKeys;
        public ArraySet<String> mUpgradeKeySets;
        public ArrayMap<String, ArraySet<PublicKey>> mKeySetMapping;

        /**
         * The install time abi override for this package, if any.
         *
         * TODO: This seems like a horrible place to put the abiOverride because
         * this isn't something the packageParser parsers. However, this fits in with
         * the rest of the PackageManager where package scanning randomly pushes
         * and prods fields out of {@code this.applicationInfo}.
         */
        public String cpuAbiOverride;
        /**
         * The install time abi override to choose 32bit abi's when multiple abi's
         * are present. This is only meaningfull for multiarch applications.
         * The use32bitAbi attribute is ignored if cpuAbiOverride is also set.
         */
        public boolean use32bitAbi;

        public byte[] restrictUpdateHash;

        /**
         * Set if the app or any of its components are visible to Instant Apps.
         */
        public boolean visibleToInstantApps;

        public Package(String packageName) {
            this.packageName = packageName;
            this.manifestPackageName = packageName;
            applicationInfo.packageName = packageName;
            applicationInfo.uid = -1;
        }

        public void setApplicationVolumeUuid(String volumeUuid) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setApplicationInfoCodePath(String codePath) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setApplicationInfoResourcePath(String resourcePath) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setApplicationInfoBaseResourcePath(String resourcePath) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setApplicationInfoBaseCodePath(String baseCodePath) {
            throw new UnsupportedOperationException("STUB");
        }

        public List<String> getChildPackageNames() {
            throw new UnsupportedOperationException("STUB");
        }

        public boolean hasChildPackage(String packageName) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setApplicationInfoSplitCodePaths(String[] splitCodePaths) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setApplicationInfoSplitResourcePaths(String[] resroucePaths) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setSplitCodePaths(String[] codePaths) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setCodePath(String codePath) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setBaseCodePath(String baseCodePath) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setSignatures(Signature[] signatures) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setVolumeUuid(String volumeUuid) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setApplicationInfoFlags(int mask, int flags) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setUse32bitAbi(boolean use32bitAbi) {
            throw new UnsupportedOperationException("STUB");
        }

        public boolean isLibrary() {
            throw new UnsupportedOperationException("STUB");
        }

        public List<String> getAllCodePaths() {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Filtered set of {@link #getAllCodePaths()} that excludes
         * resource-only APKs.
         */
        public List<String> getAllCodePathsExcludingResourceOnly() {
            throw new UnsupportedOperationException("STUB");
        }

        public void setPackageName(String newName) {
            throw new UnsupportedOperationException("STUB");
        }

        public boolean hasComponentClassName(String name) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * @hide
         */
        public boolean isForwardLocked() {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * @hide
         */
        public boolean isSystemApp() {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * @hide
         */
        public boolean isPrivilegedApp() {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * @hide
         */
        public boolean isUpdatedSystemApp() {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * @hide
         */
        public boolean canHaveOatDir() {
            throw new UnsupportedOperationException("STUB");
        }

        public boolean isMatch(int flags) {
            throw new UnsupportedOperationException("STUB");
        }

        public long getLatestPackageUseTimeInMills() {
            throw new UnsupportedOperationException("STUB");
        }

        public long getLatestForegroundPackageUseTimeInMills() {
            throw new UnsupportedOperationException("STUB");
        }

        public String toString() {
            throw new UnsupportedOperationException("STUB");
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public Package(Parcel dest) {
            throw new UnsupportedOperationException("STUB");
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException("STUB");
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Package>() {
            public Package createFromParcel(Parcel in) {
                return new Package(in);
            }

            public Package[] newArray(int size) {
                return new Package[size];
            }
        };
    }

    public static abstract class Component<II extends IntentInfo> {
        public final ArrayList<II> intents;
        public final String className;

        public Bundle metaData;
        public Package owner;

        ComponentName componentName;
        String componentShortName;

        public Component(Package _owner) {
            owner = _owner;
            intents = null;
            className = null;
        }

        public Component(final ParsePackageItemArgs args, final PackageItemInfo outInfo) {
            throw new UnsupportedOperationException("STUB");
        }

        public Component(final ParseComponentArgs args, final ComponentInfo outInfo) {
            throw new UnsupportedOperationException("STUB");
        }

        public Component(Component<II> clone) {
            throw new UnsupportedOperationException("STUB");
        }

        public ComponentName getComponentName() {
            throw new UnsupportedOperationException("STUB");
        }

        protected Component(Parcel in) {
            throw new UnsupportedOperationException("STUB");
        }

        protected void writeToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException("STUB");
        }

        public void appendComponentShortName(StringBuilder sb) {
            throw new UnsupportedOperationException("STUB");
        }

        public void printComponentShortName(PrintWriter pw) {
            throw new UnsupportedOperationException("STUB");
        }

        public void setPackageName(String packageName) {
            throw new UnsupportedOperationException("STUB");
        }
    }

    public final static class Permission extends Component<IntentInfo> implements Parcelable {
        public final PermissionInfo info;
        public boolean tree;
        public PermissionGroup group;

        public Permission(Package _owner) {
            super(_owner);
            info = new PermissionInfo();
        }

        public Permission(Package _owner, PermissionInfo _info) {
            super(_owner);
            info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            return "Permission{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " " + info.name + "}";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException("STUB");
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Permission>() {
            public Permission createFromParcel(Parcel in) {
                throw new UnsupportedOperationException("STUB");
            }

            public Permission[] newArray(int size) {
                return new Permission[size];
            }
        };
    }

    public final static class PermissionGroup extends Component<IntentInfo> implements Parcelable {
        public final PermissionGroupInfo info;

        public PermissionGroup(Package _owner) {
            super(_owner);
            info = new PermissionGroupInfo();
        }

        public PermissionGroup(Package _owner, PermissionGroupInfo _info) {
            super(_owner);
            info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            return "PermissionGroup{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " " + info.name + "}";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException("STUB");
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<PermissionGroup>() {
            public PermissionGroup createFromParcel(Parcel in) {
                throw new UnsupportedOperationException("STUB");
            }

            public PermissionGroup[] newArray(int size) {
                return new PermissionGroup[size];
            }
        };
    }

    public static ApplicationInfo generateApplicationInfo(Package p, int flags,
                                                          PackageUserState state) {
        throw new UnsupportedOperationException("STUB");
    }

    public static ApplicationInfo generateApplicationInfo(Package p, int flags,
                                                          PackageUserState state, int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    public static ApplicationInfo generateApplicationInfo(ApplicationInfo ai, int flags,
                                                          PackageUserState state, int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    public static final PermissionInfo generatePermissionInfo(
            Permission p, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public static final PermissionGroupInfo generatePermissionGroupInfo(
            PermissionGroup pg, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public final static class Activity extends Component<ActivityIntentInfo> implements Parcelable {
        public final ActivityInfo info;

        public Activity(final ParseComponentArgs args, final ActivityInfo _info) {
            super(args, _info);
            info = _info;
            info.applicationInfo = args.owner.applicationInfo;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException("STUB");
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Activity>() {
            public Activity createFromParcel(Parcel in) {
                throw new UnsupportedOperationException("STUB");
            }

            public Activity[] newArray(int size) {
                return new Activity[size];
            }
        };
    }

    public static final ActivityInfo generateActivityInfo(Activity a, int flags,
                                                          PackageUserState state, int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    public static final ActivityInfo generateActivityInfo(ActivityInfo ai, int flags,
                                                          PackageUserState state, int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    public final static class Service extends Component<ServiceIntentInfo> implements Parcelable {
        public final ServiceInfo info;

        public Service(final ParseComponentArgs args, final ServiceInfo _info) {
            super(args, _info);
            info = _info;
            info.applicationInfo = args.owner.applicationInfo;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException("STUB");
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Service>() {
            public Service createFromParcel(Parcel in) {
                throw new UnsupportedOperationException("STUB");
            }

            public Service[] newArray(int size) {
                return new Service[size];
            }
        };
    }

    public static final ServiceInfo generateServiceInfo(Service s, int flags,
                                                        PackageUserState state, int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    public final static class Provider extends Component<ProviderIntentInfo> implements Parcelable {
        public final ProviderInfo info;
        public boolean syncable;

        public Provider(final ParseComponentArgs args, final ProviderInfo _info) {
            super(args, _info);
            info = _info;
            info.applicationInfo = args.owner.applicationInfo;
            syncable = false;
        }

        public Provider(Provider existingProvider) {
            super(existingProvider);
            this.info = existingProvider.info;
            this.syncable = existingProvider.syncable;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException("STUB");
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Provider>() {
            public Provider createFromParcel(Parcel in) {
                throw new UnsupportedOperationException("STUB");
            }

            public Provider[] newArray(int size) {
                return new Provider[size];
            }
        };
    }

    public static final ProviderInfo generateProviderInfo(Provider p, int flags,
                                                          PackageUserState state, int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    public final static class Instrumentation extends Component<IntentInfo> implements
            Parcelable {
        public final InstrumentationInfo info;

        public Instrumentation(final ParsePackageItemArgs args, final InstrumentationInfo _info) {
            super(args, _info);
            info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(info, flags);
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Instrumentation>() {
            public Instrumentation createFromParcel(Parcel in) {
                throw new UnsupportedOperationException("STUB");
            }

            public Instrumentation[] newArray(int size) {
                return new Instrumentation[size];
            }
        };
    }

    public static final InstrumentationInfo generateInstrumentationInfo(
            Instrumentation i, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    @TargetApi(28)
    public static final class SigningDetails {
        public Signature[] signatures;

        public static final SigningDetails UNKNOWN = null;
    }


    public static abstract class IntentInfo extends IntentFilter {
        public boolean hasDefault;
        public int labelRes;
        public CharSequence nonLocalizedLabel;
        public int icon;
        public int logo;
        public int banner;
        public int preferred;

        protected IntentInfo() {
        }

        protected IntentInfo(Parcel dest) {
            throw new UnsupportedOperationException("STUB");
        }


        public void writeIntentInfoToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException("STUB");
        }
    }

    public final static class ActivityIntentInfo extends IntentInfo {
        public Activity activity;

        public ActivityIntentInfo(Activity _activity) {
            activity = _activity;
        }

        public ActivityIntentInfo(Parcel in) {
            super(in);
        }
    }

    public final static class ServiceIntentInfo extends IntentInfo {
        public Service service;

        public ServiceIntentInfo(Service _service) {
            service = _service;
        }

        public ServiceIntentInfo(Parcel in) {
            super(in);
        }
    }

    public static final class ProviderIntentInfo extends IntentInfo {
        public Provider provider;

        public ProviderIntentInfo(Provider provider) {
            this.provider = provider;
        }

        public ProviderIntentInfo(Parcel in) {
            super(in);
        }
    }

    /**
     * @hide
     */
    public static void setCompatibilityModeEnabled(boolean compatibilityModeEnabled) {
        throw new UnsupportedOperationException("STUB");
    }

    public static long readFullyIgnoringContents(InputStream in) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    public static void closeQuietly(StrictJarFile jarFile) {
        throw new UnsupportedOperationException("STUB");
    }

    public static class PackageParserException extends Exception {
        public final int error;

        public PackageParserException(int error, String detailMessage) {
            super(detailMessage);
            this.error = error;
        }

        public PackageParserException(int error, String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
            this.error = error;
        }
    }
}
