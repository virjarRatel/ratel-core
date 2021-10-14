package com.virjar.ratel.runtime.fixer.pm;


import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.runtime.XposedModuleLoader;
import com.virjar.ratel.runtime.ipc.ClientHandlerServiceConnection;
import com.virjar.ratel.utils.BuildCompat;
import com.virjar.ratel.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lody
 */

public class PackageParserEx {

    private static final String TAG = Constants.TAG;
    private static Signature[] mSignature = null;
    private static Map<String, SignatureOptional> fakeSignatureCache = new ConcurrentHashMap<>();
    private static final java.lang.String WX_APP_SIGNATURE = "308202eb30820254a00302010202044d36f7a4300d06092a864886f70d01010505003081b9310b300906035504061302383631123010060355040813094775616e67646f6e673111300f060355040713085368656e7a68656e31353033060355040a132c54656e63656e7420546563686e6f6c6f6779285368656e7a68656e2920436f6d70616e79204c696d69746564313a3038060355040b133154656e63656e74204775616e677a686f7520526573656172636820616e6420446576656c6f706d656e742043656e7465723110300e0603550403130754656e63656e74301e170d3131303131393134333933325a170d3431303131313134333933325a3081b9310b300906035504061302383631123010060355040813094775616e67646f6e673111300f060355040713085368656e7a68656e31353033060355040a132c54656e63656e7420546563686e6f6c6f6779285368656e7a68656e2920436f6d70616e79204c696d69746564313a3038060355040b133154656e63656e74204775616e677a686f7520526573656172636820616e6420446576656c6f706d656e742043656e7465723110300e0603550403130754656e63656e7430819f300d06092a864886f70d010101050003818d0030818902818100c05f34b231b083fb1323670bfbe7bdab40c0c0a6efc87ef2072a1ff0d60cc67c8edb0d0847f210bea6cbfaa241be70c86daf56be08b723c859e52428a064555d80db448cdcacc1aea2501eba06f8bad12a4fa49d85cacd7abeb68945a5cb5e061629b52e3254c373550ee4e40cb7c8ae6f7a8151ccd8df582d446f39ae0c5e930203010001300d06092a864886f70d0101050500038181009c8d9d7f2f908c42081b4c764c377109a8b2c70582422125ce545842d5f520aea69550b6bd8bfd94e987b75a3077eb04ad341f481aac266e89d3864456e69fba13df018acdc168b9a19dfd7ad9d9cc6f6ace57c746515f71234df3a053e33ba93ece5cd0fc15f3e389a3f365588a9fcb439e069d3629cd7732a13fff7b891499";

    static {
        // 已存的系统签名，这样没有容器感染微信也可以调用微信api
        createWellKnownSignature("com.tencent.mm", WX_APP_SIGNATURE);
    }

    private static void createWellKnownSignature(String pkg, String signatureData) {
        SignatureOptional signatureOptional = new SignatureOptional();
        signatureOptional.packageName = pkg;
        signatureOptional.signatures = new Signature[]{new Signature(signatureData)};
        fakeSignatureCache.put(signatureOptional.packageName, signatureOptional);
    }


    private static class SignatureOptional {
        Signature[] signatures = null;
        String packageName;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SignatureOptional that = (SignatureOptional) o;
            return Objects.equals(packageName, that.packageName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(packageName);
        }
    }


    public static Signature[] getFakeSignature(String packageName) {
        if (RatelRuntime.nowPackageName.equals(packageName)) {
            Signature[] ret = getFakeSignatureForOwner();
            try {
                saveSignatureToManager(ret);
            } catch (IOException e) {
                Log.e(Constants.TAG, "save signature data failed", e);
            }
            return ret;
        }

        SignatureOptional signatures = fakeSignatureCache.get(packageName);
        if (signatures != null) {
            return signatures.signatures;
        }

        SignatureOptional signatureOptional = queryFakeSignature(packageName);

        fakeSignatureCache.put(packageName, signatureOptional);
        return signatureOptional.signatures;

    }


    private static SignatureOptional queryFakeSignature(String packageName) {
        //ipc call content provider first
        SignatureOptional ret = new SignatureOptional();
        ret.packageName = packageName;
        try {
            Cursor cursor = RatelRuntime.getOriginContext().getContentResolver().query(
                    Uri.parse("content://com.virjar.ratel.manager/fake_signature"),
                    null,
                    "mPackage=?",
                    new String[]{packageName},
                    null
            );
            if (cursor != null) {
                ret = new SignatureOptional();

                if (cursor.moveToNext()) {
                    String signatureBase64 = cursor.getString(cursor.getColumnIndex("signature"));
                    ret.signatures = decode(signatureBase64);
                }
                cursor.close();
                return ret;
            } else {
                Log.w(Constants.TAG, "get fake signature data cursor return null");
            }
        } catch (Exception e) {
            Log.w(Constants.TAG, "failed to get fake signature data cursor!!", e);
        }

        //then try with sdcard
        boolean canSdcardRead = XposedModuleLoader.testHasSDCardReadPermission();
        if (canSdcardRead) {
            File ratelConfigRoot = new File(Environment.getExternalStorageDirectory(), Constants.ratelSDCardRoot);
            File fakeSignatureDir = new File(ratelConfigRoot, Constants.fakeSignatureDir);
            File fakeSignatureFile = new File(fakeSignatureDir, packageName + ".txt");

            if (fakeSignatureFile.exists() && fakeSignatureFile.canRead()) {
                try {
                    ret.signatures = decode(external.org.apache.commons.io.FileUtils.readFileToString(fakeSignatureFile, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    Log.w(Constants.TAG, "failed to read fakeSignatureFile: " + fakeSignatureFile.getAbsolutePath());
                }
            }
        }
        return ret;
    }

    private static Signature[] decode(String base64String) {
        byte[] bytes = Base64.decode(base64String, 0);
        Parcel p = Parcel.obtain();
        try {
            p.unmarshall(bytes, 0, bytes.length);
            p.setDataPosition(0);
            return p.createTypedArray(Signature.CREATOR);
        } finally {
            p.recycle();
        }
    }


    public static Signature[] getFakeSignatureForOwner() {
        Signature[] signatures = mSignature;
        if (signatures != null) {
            return signatures;
        }
        signatures = readSignature();
        if (signatures != null) {
            return signatures;
        }

        try {
            Signature[] fakeSignatureInternal = getFakeSignatureInternal(RatelEnvironment.originApkDir());
            mSignature = fakeSignatureInternal;
            savePackageCache(fakeSignatureInternal);
            return fakeSignatureInternal;
        } catch (Throwable throwable) {
            Log.e(TAG, "签名读取失败", throwable);
            throw new RuntimeException(throwable);
        }
    }

    private static Signature[] getFakeSignatureInternal(File packageFile) throws Throwable {
        PackageParser parser = PackageParserCompat.createParser(packageFile);
        if (BuildCompat.isQ()) {
            //请注意，不能直接这么访问，原因是 hidden API，需要通过双段反射来操作
            PackageParser.CallbackImpl callbackImpl = (PackageParser.CallbackImpl) RposedHelpers.newInstance(PackageParser.CallbackImpl.class, RatelRuntime.getOriginContext().getPackageManager());
            RposedHelpers.callMethod(parser, "setCallback", callbackImpl);
            // parser.setCallback(new PackageParser.CallbackImpl(RatelRuntime.getOriginContext().getPackageManager()));
        }

        PackageParser.Package p = PackageParserCompat.parsePackage(parser, packageFile, 0);
        if (p.requestedPermissions.contains("android.permission.FAKE_PACKAGE_SIGNATURE")
                && p.mAppMetaData != null
                && p.mAppMetaData.containsKey("fake-signature")) {
            String sig = p.mAppMetaData.getString("fake-signature");
            //p.mSignatures = new Signature[]{new Signature(sig)};
            //android 10更新
            buildSignature(p, new Signature[]{new Signature(sig)});
            Log.i(TAG, "Using fake-signature feature on : " + p.packageName);
        } else {
            PackageParserCompat.collectCertificates(parser, p, PackageParser.PARSE_IS_SYSTEM);
        }
        if (BuildCompat.isPie()) {
            return p.mSigningDetails.signatures;
        } else {
            return p.mSignatures;
        }
    }

    private static void buildSignature(PackageParser.Package packageR, Signature[] signatureArr) {
        if (BuildCompat.isQ()) {
            Object obj = mirror.android.content.pm.PackageParser.Package.mSigningDetails.get(packageR);
            mirror.android.content.pm.PackageParser.SigningDetails.pastSigningCertificates.set(obj, signatureArr);
            mirror.android.content.pm.PackageParser.SigningDetails.signatures.set(obj, signatureArr);
            return;
        }
        packageR.mSignatures = signatureArr;
    }

    private static Signature[] readSignature() {
        File signatureFile = RatelEnvironment.originAPKSignatureFile();
        if (!signatureFile.exists()) {
            return null;
        }
        Parcel p = Parcel.obtain();
        try {
            FileInputStream fis = new FileInputStream(signatureFile);
            byte[] bytes = FileUtils.toByteArray(fis);
            fis.close();
            p.unmarshall(bytes, 0, bytes.length);
            p.setDataPosition(0);
            return p.createTypedArray(Signature.CREATOR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            p.recycle();
        }
        return null;
    }


    private static void savePackageCache(Signature[] signatures) {
        if (signatures == null) {
            return;
        }
        File signatureFile = RatelEnvironment.originAPKSignatureFile();
        if (signatureFile.exists() && !signatureFile.delete()) {
            Log.w(TAG, "Unable to delete the signatures  file:  " + signatureFile);
        }
        Parcel p = Parcel.obtain();
        try {
            p.writeTypedArray(signatures, 0);
            FileUtils.writeParcelToFile(p, signatureFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            p.recycle();
        }

    }

    private static boolean hasSave = false;

    private static void saveSignatureToManager(Signature[] signatures) throws IOException {
        if (signatures == null) {
            return;
        }

        if (hasSave) {
            return;
        }

        String encodeSignatureData;
        Parcel p = Parcel.obtain();
        try {
            p.writeTypedArray(signatures, 0);
            byte[] bytes = p.marshall();
            encodeSignatureData = Base64.encodeToString(bytes, 0);
        } finally {
            p.recycle();
        }


        //ipc save
        String finalEncodeSignatureData = encodeSignatureData;
        ClientHandlerServiceConnection.addOnManagerIPCListener(iRatelManagerClientRegister -> {
            if (XposedModuleLoader.getRatelManagerVersionCode() < 4) {
                //1.0.4之前不支持通过manager存储
                return;
            }

            try {
                iRatelManagerClientRegister.saveMSignature(RatelRuntime.nowPackageName, finalEncodeSignatureData);
            } catch (RemoteException e) {
                Log.e(Constants.TAG, "save mSignature data failed", e);
            }
        });

        //sdcard save
        if (!XposedModuleLoader.testHasSDCardWritePermission()) {
            return;
        }

        File ratelConfigRoot = new File(Environment.getExternalStorageDirectory(), Constants.ratelSDCardRoot);
        File fakeSignatureDir = new File(ratelConfigRoot, Constants.fakeSignatureDir);
        File fakeSignatureFile = new File(fakeSignatureDir, RatelRuntime.nowPackageName + ".txt");

        external.org.apache.commons.io.FileUtils.forceMkdirParent(fakeSignatureFile);
        external.org.apache.commons.io.FileUtils.writeStringToFile(fakeSignatureFile, encodeSignatureData, StandardCharsets.UTF_8);

        hasSave = true;
    }

}
