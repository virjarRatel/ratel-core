package com.virjar.ratel.builder;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import net.dongliu.apk.parser.utils.Pair;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ShellDetector {

    private static Map<String, String> markNameMap = new HashMap<>();

    static {
        initShellFilesConfig();
    }

    private static void initShellFilesConfig() {
        markNameMap.put("libchaosvmp.so", "娜迦");
        markNameMap.put("libddog.so", "娜迦");
        markNameMap.put("libfdog.so", "娜迦");
        markNameMap.put("libedog.so", "娜迦企业版");
        markNameMap.put("libexecmain.so", "爱加密");
        markNameMap.put("ijiami.dat", "爱加密");
        markNameMap.put("ijiami.ajm", "爱加密企业版");
        markNameMap.put("libsecexe.so", "梆梆免费版");
        markNameMap.put("libsecmain.so", "梆梆免费版");
        markNameMap.put("libSecShell.so", "梆梆免费版");
        markNameMap.put("libDexHelper.so", "梆梆企业版");
        markNameMap.put("libDexHelper-x86.so", "梆梆企业版");
        markNameMap.put("libprotectClass.so", "360");
        markNameMap.put("libjiagu.so", "360");
        markNameMap.put("libjiagu_art.so", "360");
        markNameMap.put("libjiagu_x86.so", "360");
        markNameMap.put("libegis.so", "通付盾");
        markNameMap.put("libNSaferOnly.so", "通付盾");
        markNameMap.put("libnqshield.so", "网秦");
        markNameMap.put("libbaiduprotect.so", "百度");
        markNameMap.put("aliprotect.dat", "阿里聚安全");
        markNameMap.put("libsgmain.so", "阿里聚安全");
        markNameMap.put("libsgsecuritybody.so", "阿里聚安全");
        markNameMap.put("libmobisec.so", "阿里聚安全");
        markNameMap.put("libtup.so", "腾讯");
        markNameMap.put("libexec.so", "腾讯或爱加密");
        markNameMap.put("libshell.so", "腾讯");
        markNameMap.put("mix.dex", "腾讯");
        markNameMap.put("lib/armeabi/mix.dex", "腾讯");
        markNameMap.put("lib/armeabi/mixz.dex", "腾讯");
        markNameMap.put("libtosprotection.armeabi.so", "腾讯御安全");
        markNameMap.put("libtosprotection.armeabi-v7a.so", "腾讯御安全");
        markNameMap.put("libtosprotection.x86.so", "腾讯御安全");
        markNameMap.put("libnesec.so", "网易易盾");
        markNameMap.put("libAPKProtect.so", "APKProtect");
        markNameMap.put("libkwscmm.so", "几维安全");
        markNameMap.put("libkwscr.so", "几维安全");
        markNameMap.put("libkwslinker.so", "几维安全");
        markNameMap.put("libx3g.so", "顶像科技");
        markNameMap.put("libapssec.so", "盛大");
        markNameMap.put("librsprotect.so", "瑞星");
    }

    public static Pair<String, String> findShellEntry(File apk) throws IOException {
        ZipFile zipFile = new ZipFile(apk);
        Pair<String, String> shellEntry = findShellEntry(zipFile);
        zipFile.close();
        return shellEntry;
    }

    private static Pair<String, String> findShellEntry(ZipFile zipFile) throws IOException {
        Enumeration<ZipEntry> entries = zipFile.getEntries();
        Map<String, String> shellFutures = Maps.newHashMap();

        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            String entryName = zipEntry.getName();
            if (entryName.startsWith("/")) {
                entryName = entryName.substring(1);
            }
            if (entryName.startsWith("assets/")) {
                int searchStart = entryName.indexOf('/') + 1;
                int searchEnd = entryName.indexOf(entryName, searchStart);
                if (searchEnd >= 0) {
                    continue;
                }

                String fileName = entryName.substring(searchStart);
                if (markNameMap.containsKey(fileName)) {
                    shellFutures.put(fileName, markNameMap.get(fileName));
                }
            } else if (entryName.startsWith("lib/")) {
                int searchStart = entryName.lastIndexOf("/") + 1;
                String fileName = entryName.substring(searchStart);
                if (markNameMap.containsKey(fileName)) {
                    shellFutures.put(fileName, markNameMap.get(fileName));
                }
            } else if (markNameMap.containsKey(entryName)) {
                shellFutures.put(entryName, markNameMap.get(entryName));
            }
        }
        if (shellFutures.isEmpty()) {
            return null;
        }

        String featureKey = Joiner.on("|").join(shellFutures.keySet());
        String featureValue = Joiner.on("|").join(shellFutures.values());
        return new Pair<>(featureKey, featureValue);
    }

    public static boolean hasShell(File apk) throws IOException {
        ZipFile zipFile = new ZipFile(apk);
        Pair<String, String> shellEntry = findShellEntry(zipFile);
        zipFile.close();

        if (shellEntry == null) {
            System.out.println("none shell apk...");
            return false;
        }
        System.out.println("find shell for: " + shellEntry.getRight() + "  with file: " + shellEntry.getLeft());
        return true;
    }
}
