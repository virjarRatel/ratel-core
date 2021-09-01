package com.virjar.ratel.manager.repo;

import android.util.Base64;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.virjar.ratel.allcommon.StandardEncryptor;
import com.virjar.ratel.manager.RatelManagerApp;
import com.virjar.ratel.manager.model.RatelCertificate;
import com.virjar.ratel.manager.model.RatelLicenceContainer;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.crypto.Cipher;

public class RatelCertificateRepo {
    private static Map<String, RatelCertificate> ratelCertificateMap = new HashMap<>();
    private static final List<RatelCertificateListener> sListeners = new CopyOnWriteArrayList<>();

    static {
        init();
    }

    private static void init() {
        List<RatelCertificate> ratelModules = SQLite.select().from(RatelCertificate.class).queryList();
        for (RatelCertificate ratelModule : ratelModules) {
            ratelCertificateMap.put(ratelModule.getLicenceId(), ratelModule);
        }
    }

    public static RatelCertificate queryById(String licenceId) {
        return ratelCertificateMap.get(licenceId);
    }

    public static List<RatelCertificate> storedCertificate() {
        return new ArrayList<>(ratelCertificateMap.values());
    }

    public interface RatelCertificateListener {
        void onRatelCertificateReload();
    }


    public static void addListener(RatelCertificateListener ratelCertificateListener) {
        if (!sListeners.contains(ratelCertificateListener)) {
            sListeners.add(ratelCertificateListener);
        }
    }

    public static void removeListener(RatelCertificateListener ratelCertificateListener) {
        sListeners.remove(ratelCertificateListener);
    }

    public static void fireRatelAppReload() {
        for (RatelCertificateListener ratelCertificateListener : sListeners) {
            ratelCertificateListener.onRatelCertificateReload();
        }
    }

    private static String join(String[] strings) {
        if (strings == null) {
            return null;
        }
        if (strings.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            sb.append(str).append(",");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }


    private static byte[] standardRSADecrypt(String data) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(StandardEncryptor.KEY_ALGORITHM);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(
                    Base64.decode(StandardEncryptor.containerRsaPublicKey, Base64.DEFAULT)
            );
            PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, pubKey);
            return StandardEncryptor.cipherDoFinal(cipher, Base64.decode(data, Base64.DEFAULT), StandardEncryptor.KEY_SIZE / 8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    public static String importCertificate(String certificateContent) {
        try {
            String containerJsonString = new String(standardRSADecrypt(certificateContent), StandardCharsets.UTF_8);
            Log.i(RatelManagerApp.TAG, "decrypt json string: " + containerJsonString);
            JSONObject containerJson = JSONObject.parseObject(containerJsonString);
            RatelLicenceContainer ratelLicenceContainer = containerJson.toJavaObject(RatelLicenceContainer.class);
            String licenceId = ratelLicenceContainer.getLicenceId();

            RatelCertificate ratelCertificate = queryById(licenceId);
            if (ratelCertificate != null && ratelCertificate.getLicenceVersion() == ratelLicenceContainer.getLicenceVersion()) {
                return "this certificate has imported already";
            }

            if (ratelCertificate == null) {
                ratelCertificate = new RatelCertificate();
            }
            ratelCertificate.setAccount(ratelLicenceContainer.getAccount());
            ratelCertificate.setDeviceList(join(ratelLicenceContainer.getDeviceList()));
            ratelCertificate.setExpire(ratelLicenceContainer.getExpire());
            ratelCertificate.setExtra(ratelLicenceContainer.getExtra());
            ratelCertificate.setLicenceId(ratelLicenceContainer.getLicenceId());
            ratelCertificate.setPackageList(join(ratelLicenceContainer.getPackageList()));
            ratelCertificate.setLicenceProtocolVersion(ratelLicenceContainer.getLicenceProtocolVersion());
            ratelCertificate.setLicenceVersion(ratelLicenceContainer.getLicenceVersion());
            ratelCertificate.setLicenceType(ratelLicenceContainer.getLicenceType());
            ratelCertificate.setPayload(ratelLicenceContainer.getPayload());
            addCertificate(ratelCertificate);

            return null;
        } catch (Exception e) {
            Log.e(RatelManagerApp.TAG, "failed to import certificate", e);
            return "invalid certificate";
        }
    }

    public static void removeCertificate(RatelCertificate ratelCertificate) {
        if (ratelCertificate == null) {
            return;
        }
        RatelCertificate existed = ratelCertificateMap.remove(ratelCertificate.getLicenceId());
        if (existed == null) {
            return;
        }
        existed.delete();
        fireRatelAppReload();
    }

    public static void addCertificate(RatelCertificate ratelCertificate) {
        if (ratelCertificate == null) {
            return;
        }
        RatelCertificate existed = ratelCertificateMap.get(ratelCertificate.getLicenceId());
        if (existed == null) {
            ratelCertificate.save();
        } else {
            ratelCertificate.update();
        }
        ratelCertificateMap.put(ratelCertificate.getLicenceId(), ratelCertificate);
        fireRatelAppReload();
    }
}
