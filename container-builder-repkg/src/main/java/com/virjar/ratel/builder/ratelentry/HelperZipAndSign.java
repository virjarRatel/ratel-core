package com.virjar.ratel.builder.ratelentry;

import com.android.apksig.ApkSigner;
import com.android.apksig.apk.MinSdkVersionException;
import com.android.apksigner.PasswordRetriever;
import com.android.apksigner.SignerParams;
import com.google.common.collect.Lists;
import com.virjar.ratel.allcommon.BuildEnv;
import com.virjar.ratel.allcommon.NewConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class HelperZipAndSign {
    static void zipAndSign(BuilderContext context) throws Exception {
        File signatureKeyFile = BindingResourceManager.get(BuildEnv.ANDROID_ENV ?
                NewConstants.BUILDER_RESOURCE_LAYOUT.DEFAULT_SIGN_KEY_ANDROID :
                NewConstants.BUILDER_RESOURCE_LAYOUT.DEFAULT_SIGN_KEY);
        try {
            if (useV1Sign(context)) {
                //7.0之前，走V1签名，所以要先签名再对齐
                signatureApk(context.outFile, signatureKeyFile, context);
                zipalign(context.outFile, context.theWorkDir);
            } else {
                zipalign(context.outFile, context.theWorkDir);
                signatureApk(context.outFile, signatureKeyFile, context);
            }
        } catch (Exception e) {
            // we need remove final output apk if sign failed,because of out shell think a illegal apk format file as task success flag,
            // android system think signed apk as really right apk,we can not install this apk on the android cell phone if rebuild success but sign failed
            FileUtils.forceDelete(context.outFile);
            throw e;
        }
    }

    public static void zipalign(File outApk, File theWorkDir) throws IOException, InterruptedException {
        System.out.println("zip align output apk: " + outApk);
        //use different executed binary file with certain OS platforms
        File zipalignBinPath;
        if (BuildEnv.ANDROID_ENV) {
            zipalignBinPath = BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.ZIP_ALIGN_ANDROID);
        } else {
            // 为什么这么写？ 因为在Android环境和非Android环境下，优化器只会保留一个分支的代码，
            // 方便代码瘦身
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.startsWith("Mac OS".toLowerCase())) {
                zipalignBinPath = BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.ZIP_ALIGN_MAC);//"zipalign/mac/zipalign";
            } else if (osName.startsWith("Windows".toLowerCase())) {
                zipalignBinPath = BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.ZIP_ALIGN_WINDOWS);
            } else {
                zipalignBinPath = BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.ZIP_ALIGN_LINUX);
            }
        }


        zipalignBinPath.setExecutable(true);

        File tmpOutputApk = File.createTempFile("zipalign", ".apk", theWorkDir);
        tmpOutputApk.deleteOnExit();

        String command = zipalignBinPath.getAbsolutePath() + " -f -p  4 " + outApk.getAbsolutePath() + " " + tmpOutputApk.getAbsolutePath();
        System.out.println("zip align apk with command: " + command);

        String[] envp = new String[]{"LANG=zh_CN.UTF-8", "LANGUAGE=zh_CN.UTF-8"};
        Process process = Runtime.getRuntime().exec(command, envp, null);
        autoFillBuildLog(process.getInputStream(), "zipalign-stand");
        autoFillBuildLog(process.getErrorStream(), "zipalign-error");

        process.waitFor();

        Files.move(
                tmpOutputApk.toPath(), outApk.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println(outApk.getAbsolutePath() + " has been zipalign ");
    }

    private static void autoFillBuildLog(InputStream inputStream, String type) {
        new Thread("read-" + type) {
            @Override
            public void run() {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(type + " : " + line);
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public static void signatureApk(File outApk, File keyStoreFile, BuilderContext buildParamMeta) throws Exception {
        System.out.println("auto sign apk with ratel KeyStore");
        File tmpOutputApk = File.createTempFile("apksigner", ".apk", buildParamMeta.theWorkDir);
        tmpOutputApk.deleteOnExit();

        SignerParams signerParams = new SignerParams();
        signerParams.setKeystoreFile(keyStoreFile.getAbsolutePath());
        signerParams.setKeystoreKeyAlias("hermes");
        signerParams.setKeystorePasswordSpec("pass:hermes");
//        signerParams.setKeyPasswordSpec("hermes");


        signerParams.setName("ratelSign");
        PasswordRetriever passwordRetriever = new PasswordRetriever();
        signerParams.loadPrivateKeyAndCerts(passwordRetriever);

        String v1SigBasename;
        if (signerParams.getV1SigFileBasename() != null) {
            v1SigBasename = signerParams.getV1SigFileBasename();
        } else if (signerParams.getKeystoreKeyAlias() != null) {
            v1SigBasename = signerParams.getKeystoreKeyAlias();
        } else if (signerParams.getKeyFile() != null) {
            String keyFileName = new File(signerParams.getKeyFile()).getName();
            int delimiterIndex = keyFileName.indexOf('.');
            if (delimiterIndex == -1) {
                v1SigBasename = keyFileName;
            } else {
                v1SigBasename = keyFileName.substring(0, delimiterIndex);
            }
        } else {
            throw new RuntimeException(
                    "Neither KeyStore key alias nor private key file available");
        }

        ApkSigner.SignerConfig signerConfig =
                new ApkSigner.SignerConfig.Builder(
                        v1SigBasename, signerParams.getPrivateKey(), signerParams.getCerts())
                        .build();

        ApkSigner.Builder apkSignerBuilder =
                new ApkSigner.Builder(Lists.newArrayList(signerConfig))
                        .setInputApk(outApk)
                        .setOutputApk(tmpOutputApk)
                        .setOtherSignersSignaturesPreserved(false)
                        //这个需要设置为true，否则debug版本的apk无法签名
                        .setDebuggableApkPermitted(true);
        if (useV1Sign(buildParamMeta)) {
            //低版本需要支持V1签名，Android7.0之后，Android系统才切换为V2签名
            apkSignerBuilder.setV1SigningEnabled(true);
            apkSignerBuilder.setV2SigningEnabled(false);
            apkSignerBuilder.setV3SigningEnabled(false);
            System.out.println("[signApk] use v1 sign");
        } else {
            apkSignerBuilder.setV1SigningEnabled(false)
                    .setV2SigningEnabled(true)
                    .setV3SigningEnabled(true);
            System.out.println("[signApk] use v2&v3 sign");
        }
        // .setSigningCertificateLineage(lineage);
        ApkSigner apkSigner = apkSignerBuilder.build();
        try {
            apkSigner.sign();
        } catch (MinSdkVersionException e) {
            System.out.println("Failed to determine APK's minimum supported platform version,use default skd api level 21");
            apkSigner = apkSignerBuilder.setMinSdkVersion(21).build();
            apkSigner.sign();
        }

        Files.move(
                tmpOutputApk.toPath(), outApk.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println(outApk.getAbsolutePath() + " has been Signed");
    }

    private static boolean useV1Sign(BuilderContext buildParamMeta) {
        // targetSdkVersion为安卓11的必须使用v2签名
        // 安卓7之前使用v1签名
        return buildParamMeta != null && NumberUtils.toInt(buildParamMeta.infectApk.apkMeta.getMinSdkVersion(), 24) <= 23 && NumberUtils.toInt(buildParamMeta.infectApk.apkMeta.getTargetSdkVersion()) < 30;
    }

}
