package com.virjar.ratel.runtime.fixer.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.os.Build;
import android.util.DisplayMetrics;

import com.virjar.ratel.utils.BuildCompat;

import java.io.File;

import mirror.android.content.pm.PackageParserJellyBean;
import mirror.android.content.pm.PackageParserJellyBean17;
import mirror.android.content.pm.PackageParserLollipop;
import mirror.android.content.pm.PackageParserLollipop22;
import mirror.android.content.pm.PackageParserMarshmallow;
import mirror.android.content.pm.PackageParserNougat;
import mirror.android.content.pm.PackageParserPie;
import mirror.android.content.pm.PackageUserState;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;

/**
 * @author Lody
 */

public class PackageParserCompat {

    private static final int API_LEVEL = Build.VERSION.SDK_INT;
    private static final Object sUserState = API_LEVEL >= JELLY_BEAN_MR1 ? PackageUserState.ctor.newInstance() : null;


    public static PackageParser createParser(File packageFile) {
        if (API_LEVEL >= M) {
            return PackageParserMarshmallow.ctor.newInstance();
        } else if (API_LEVEL >= LOLLIPOP_MR1) {
            return PackageParserLollipop22.ctor.newInstance();
        } else if (API_LEVEL >= LOLLIPOP) {
            return PackageParserLollipop.ctor.newInstance();
        } else if (API_LEVEL >= JELLY_BEAN_MR1) {
            return PackageParserJellyBean17.ctor.newInstance(packageFile.getAbsolutePath());
        } else if (API_LEVEL >= JELLY_BEAN) {
            return PackageParserJellyBean.ctor.newInstance(packageFile.getAbsolutePath());
        } else {
            return mirror.android.content.pm.PackageParser.ctor.newInstance(packageFile.getAbsolutePath());
        }
    }

    public static Package parsePackage(PackageParser parser, File packageFile, int flags) throws Throwable {
        if (API_LEVEL >= M) {
            return PackageParserMarshmallow.parsePackage.callWithException(parser, packageFile, flags);
        } else if (API_LEVEL >= LOLLIPOP_MR1) {
            return PackageParserLollipop22.parsePackage.callWithException(parser, packageFile, flags);
        } else if (API_LEVEL >= LOLLIPOP) {
            return PackageParserLollipop.parsePackage.callWithException(parser, packageFile, flags);
        } else if (API_LEVEL >= JELLY_BEAN_MR1) {
            return PackageParserJellyBean17.parsePackage.callWithException(parser, packageFile, null,
                    new DisplayMetrics(), flags);
        } else if (API_LEVEL >= JELLY_BEAN) {
            return PackageParserJellyBean.parsePackage.callWithException(parser, packageFile, null,
                    new DisplayMetrics(), flags);
        } else {
            return mirror.android.content.pm.PackageParser.parsePackage.callWithException(parser, packageFile, null,
                    new DisplayMetrics(), flags);
        }
    }


    public static void collectCertificates(PackageParser parser, Package p, int flags) throws Throwable {
        if (BuildCompat.isPie()) {
            PackageParserPie.collectCertificates.callWithException(p, true/*skipVerify*/);
        } else if (API_LEVEL >= N) {
            PackageParserNougat.collectCertificates.callWithException(p, flags);
        } else if (API_LEVEL >= M) {
            PackageParserMarshmallow.collectCertificates.callWithException(parser, p, flags);
        } else if (API_LEVEL >= LOLLIPOP_MR1) {
            PackageParserLollipop22.collectCertificates.callWithException(parser, p, flags);
        } else if (API_LEVEL >= LOLLIPOP) {
            PackageParserLollipop.collectCertificates.callWithException(parser, p, flags);
        } else if (API_LEVEL >= JELLY_BEAN_MR1) {
            PackageParserJellyBean17.collectCertificates.callWithException(parser, p, flags);
        } else if (API_LEVEL >= JELLY_BEAN) {
            PackageParserJellyBean.collectCertificates.callWithException(parser, p, flags);
        } else {
            mirror.android.content.pm.PackageParser.collectCertificates.call(parser, p, flags);
        }
    }
}
