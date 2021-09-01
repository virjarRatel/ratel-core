package com.virjar.ratel.envmock;

import android.annotation.SuppressLint;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.SuffixTrimUtils;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.utils.BuildCompat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

import external.org.apache.commons.io.FileUtils;
import external.org.apache.commons.lang3.StringUtils;

class LinuxFileFake {
    private static final String bootIdFilePath = "/proc/sys/kernel/random/boot_id";
    private static final String osReleaseFilepath = "/proc/sys/kernel/osrelease";
    private static final String netFilePrefix = "/sys/class/net/";
    private static final String soc0Serial_numberFilePath = "/sys/devices/soc0/serial_number";

    static void fakeLinuxFeatureFile() {
        fakeBootId();
        fakeNetAddress("/sys/class/net/eth0/address");
        fakeNetAddress("/sys/class/net/eth1/address");
        fakeNetAddress("/sys/class/net/eth2/address");
        fakeNetAddress("/sys/class/net/wlan0/address");
        fakeNetAddress("/sys/class/net/wifi/address");

        //TODO 再考虑一下要不要修改osrelease
        fakeOsReleaseFile();

        //TODO 外置存储卡 /sys/block/mmcblk0/device/cid

        fakeSoc0SerialNumber();
    }

    private static void fakeOsReleaseFile() {
        handleIfFileAccessible(osReleaseFilepath, new FileAccessEvent() {
            @Override
            public void onFileAccess(File file) {

            }
        });
    }


    private static void fakeNetAddress(String addressFile) {

        if (!addressFile.startsWith(netFilePrefix)) {
            Log.w(Constants.TAG, "illegal network driver file:" + addressFile);
            return;
        }

        final String networkFileId = addressFile.substring(netFilePrefix.length()).replaceAll("/", "_");

        handleIfFileAccessible(addressFile, new FileAccessEvent() {

            private void generateFakeNetworkFile(File fakeNetworkFile, File originNetworkFile) throws IOException {
                String networkAddressFileContent = FileUtils.readFileToString(originNetworkFile, StandardCharsets.UTF_8);
                //ac:37:43:a1:0b:88
                String fakeNetworkAddressId = SuffixTrimUtils.mockSuffix(networkAddressFileContent, 7);
                FileUtils.writeStringToFile(fakeNetworkFile, fakeNetworkAddressId, StandardCharsets.UTF_8);
            }

            @Override
            public void onFileAccess(File file) {
                File fakeNetworkFile = new File(RatelEnvironment.envMockRoot(), networkFileId);
                if (!fakeNetworkFile.exists()) {
                    try {
                        generateFakeNetworkFile(fakeNetworkFile, file);
                    } catch (IOException e) {
                        Log.w(Constants.TAG, "read net address file failed", e);
                        return;
                    }
                }

                RatelNative.redirectFile(addressFile, fakeNetworkFile.getAbsolutePath());
                RatelNative.readOnlyFile(fakeNetworkFile.getAbsolutePath());
            }
        });
    }


    private static void fakeBootId() {
        handleIfFileAccessible(bootIdFilePath, file -> {
            File kernelBootIdFile = RatelEnvironment.kernelBootIdFile();
            try {
                String bootIdContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                String userIdentifier = RatelEnvironment.userIdentifier();
                String newBootId = UUID.nameUUIDFromBytes((bootIdContent + userIdentifier).getBytes(StandardCharsets.UTF_8)).toString();
                FileUtils.writeStringToFile(kernelBootIdFile, newBootId, StandardCharsets.UTF_8);

                if (BuildCompat.isR() || BuildCompat.isQ()) {
                    RatelNative.redirectFile("/dev/ashmem" + newBootId, "/dev/ashmem" + StringUtils.chomp(bootIdContent));
                    Log.i(Constants.TAG, "redirect anoymonus shared memory file:" + "/dev/ashmem" + newBootId + " to " + "/dev/ashmem" + bootIdContent);
                }
            } catch (IOException e) {
                Log.w(Constants.TAG, "create fake bootId failed", e);
                return;
            }
            Log.i(Constants.TAG, String.format("redirect bootId %s to %s", bootIdFilePath, kernelBootIdFile.getAbsolutePath()));
            RatelNative.redirectFile(bootIdFilePath, kernelBootIdFile.getAbsolutePath());
            RatelNative.readOnlyFile(kernelBootIdFile.getAbsolutePath());
        });
    }

    private static void fakeSoc0SerialNumber() {
        handleIfFileAccessible(soc0Serial_numberFilePath, file -> {
            File fakeSoc0SerialNumberFile = RatelEnvironment.soc0SerialNumberFile();
            try {
                // String soc0SerialNumberContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                long origin = Long.parseLong(StringUtils.chomp(StringUtils.trimToEmpty(FileUtils.readFileToString(file, StandardCharsets.UTF_8))));
                int suffer = new Random(RatelEnvironment.userIdentifierSeed()).nextInt(100000) - 5000;

                String newSoc0SerialNumber = String.valueOf(origin + suffer);
                FileUtils.writeStringToFile(fakeSoc0SerialNumberFile, newSoc0SerialNumber, StandardCharsets.UTF_8);

            } catch (IOException e) {
                Log.w(Constants.TAG, "create fake Soc0SerialNumber failed", e);
                return;
            }
            RatelNative.redirectFile(soc0Serial_numberFilePath, fakeSoc0SerialNumberFile.getAbsolutePath());
            RatelNative.readOnlyFile(fakeSoc0SerialNumberFile.getAbsolutePath());
        });
    }

    interface FileAccessEvent {
        void onFileAccess(File file);
    }

    private static void handleIfFileAccessible(String path, FileAccessEvent fileAccessEvent) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.canRead()) {
            return;
        }
        fileAccessEvent.onFileAccess(file);
    }
}
