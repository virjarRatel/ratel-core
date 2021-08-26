package com.virjar.ratel.authorize;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RatelLicence {
    private String licenceId;
    private int licenceVersion;
    private int licenceProtocolVersion = 1;
    private long expire = System.currentTimeMillis() + 365L * 24L * 60L * 60L * 1000L;
    private int licenceType;
    private String account;
    private String[] packageList = new String[0];
    private String[] deviceList = new String[0];
    private String extra;

    private RatelLicence(String licenceId, int licenceVersion, int licenceProtocolVersion, long expire, int licenceType, String account, String[] packageList, String[] deviceList, String extra) {
        this.licenceId = licenceId;
        this.licenceVersion = licenceVersion;
        this.licenceProtocolVersion = licenceProtocolVersion;
        this.expire = expire;
        this.licenceType = licenceType;
        this.account = account;
        this.packageList = packageList;
        this.deviceList = deviceList;
        this.extra = extra;
    }

    private RatelLicence() {

    }

    public String getLicenceId() {
        return licenceId;
    }

    public int getLicenceVersion() {
        return licenceVersion;
    }

    public int getLicenceProtocolVersion() {
        return licenceProtocolVersion;
    }

    public long getExpire() {
        return expire;
    }

    public int getLicenceType() {
        return licenceType;
    }

    public String getAccount() {
        return account;
    }

    public String[] getPackageList() {
        return packageList;
    }

    public String[] getDeviceList() {
        return deviceList;
    }

    public String getExtra() {
        return extra;
    }

    public byte[] encode() {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        packageList = trim(packageList);
        deviceList = trim(deviceList);

        putString(licenceId, buffer);
        buffer.putInt(licenceVersion);
        buffer.putInt(licenceProtocolVersion);
        buffer.putLong(expire);
        buffer.putInt(licenceType);
        putString(account, buffer);


        buffer.putInt(packageList.length);
        for (String str : packageList) {
            putString(str, buffer);
        }
        buffer.putInt(deviceList.length);
        for (String str : deviceList) {
            putString(str, buffer);
        }
        putString(extra, buffer);

        int position = buffer.position();
        byte[] out = new byte[position];
        buffer.position(0);
        buffer.get(out);
        return out;
    }

    private static String[] trim(String[] input) {
        if (input == null) {
            return new String[0];
        }
        Set<String> duplicateRemove = new HashSet<>(Arrays.asList(input));
        Iterator<String> iterator = duplicateRemove.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (next.isEmpty()) {
                iterator.remove();
            }
        }
        return new ArrayList<>(new HashSet<>(duplicateRemove)).toArray(new String[0]);
    }

    public static RatelLicence decode(byte[] encodeData) {
        ByteBuffer buffer = ByteBuffer.wrap(encodeData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        RatelLicence ratelLicence = new RatelLicence();
        ratelLicence.licenceId = readString(buffer);
        ratelLicence.licenceVersion = buffer.getInt();
        ratelLicence.licenceProtocolVersion = buffer.getInt();
        ratelLicence.expire = buffer.getLong();
        ratelLicence.licenceType = buffer.getInt();
        ratelLicence.account = readString(buffer);
        int packageListLength = buffer.getInt();
        ratelLicence.packageList = new String[packageListLength];
        for (int i = 0; i < ratelLicence.packageList.length; i++) {
            ratelLicence.packageList[i] = readString(buffer);
        }

        int deviceListLength = buffer.getInt();
        ratelLicence.deviceList = new String[deviceListLength];
        for (int i = 0; i < ratelLicence.deviceList.length; i++) {
            ratelLicence.deviceList[i] = readString(buffer);
        }
        ratelLicence.extra = readString(buffer);
        return ratelLicence;
    }

    private static String readString(ByteBuffer buffer) {
        int length = buffer.getInt();
        if (length < 0) {
            return null;
        }
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        buffer.get();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void putString(String str, ByteBuffer buffer) {
        if (str == null) {
            buffer.putInt(-1);
            return;
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.put((byte) 0);
    }

    public enum AuthorizeType {
        AuthorizeTypeDebug(0x0001),
        AuthorizeTypePerson(0x0002),
        AuthorizeTypeQunar(0x0004),
        AuthorizeTypeTest(0x0008),
        AuthorizeTypeMiniGroup(0x0010),
        AuthorizeTypeMediumGroup(0x0020),
        AuthorizeTypeBigGroup(0x0040),
        AuthorizeTypeSlave(0x0080),
        AuthorizeTypeSelfExplosion(0x0100),
        AuthorizeTypeB(0x0200),
        AuthorizeTypeC(0x0400),
        AuthorizeTypeD(0x0800),
        AuthorizeTypeE(0x1000),
        AuthorizeTypeF(0x2000),
        AuthorizeTypeG(0x4000),
        AuthorizeTypeH(0x8000),
        ;
        public int typeMask;

        AuthorizeType(int typeMask) {
            this.typeMask = typeMask;
        }
    }

    /**
     * #define AuthorizeTypeDebug 0x0001
     * #define AuthorizeTypePerson 0x0002
     * #define AuthorizeTypeQunar 0x0004
     * #define AuthorizeTypeTest 0x0008
     * #define AuthorizeTypeMiniGroup 0x0010
     * #define AuthorizeTypeMediumGroup 0x0020
     * #define AuthorizeTypeBigGroup 0x0040
     * #define AuthorizeTypeSlave 0x0080
     * #define AuthorizeTypeA 0x0100
     * #define AuthorizeTypeB 0x0200
     * #define AuthorizeTypeC 0x0400
     * #define AuthorizeTypeD 0x0800
     * #define AuthorizeTypeE 0x1000
     * #define AuthorizeTypeF 0x2000
     * #define AuthorizeTypeG 0x4000
     * #define AuthorizeTypeH 0x8000
     */
    public static class RatelLicenceBuilder {
        private String licenceId;
        private int licenceVersion = 0;
        private int licenceProtocolVersion = 1;
        //默认，只给两天使用权限
        private long expire = System.currentTimeMillis() + 2L * 24L * 60L * 60L * 1000L;
        private int licenceType = 0;
        private String account;
        private List<String> packageList = new ArrayList<>();
        private List<String> deviceList = new ArrayList<>();
        private String extra;

        public static RatelLicenceBuilder create() {
            return new RatelLicenceBuilder();
        }

        public RatelLicenceBuilder mirror(RatelLicence ratelLicence) {
            if (ratelLicence == null) {
                return this;
            }
            setAccount(ratelLicence.account)
                    .setExpire(ratelLicence.expire)
                    .setExtra(ratelLicence.extra)
                    .setLicenceId(ratelLicence.licenceId)
                    .setLicenceProtocolVersion(ratelLicence.licenceProtocolVersion)
                    .setLicenceVersion(ratelLicence.licenceVersion);
            licenceType = ratelLicence.licenceType;
            if (ratelLicence.packageList != null) {
                Collections.addAll(packageList, ratelLicence.packageList);
            }
            if (ratelLicence.deviceList != null) {
                Collections.addAll(deviceList, ratelLicence.deviceList);
            }
            return this;
        }

        public RatelLicenceBuilder setLicenceId(String licenceId) {
            this.licenceId = licenceId;
            return this;
        }

        public RatelLicenceBuilder setLicenceVersion(int licenceVersion) {
            this.licenceVersion = licenceVersion;

            return this;
        }

        public RatelLicenceBuilder setLicenceProtocolVersion(int licenceProtocolVersion) {
            this.licenceProtocolVersion = licenceProtocolVersion;
            return this;
        }

        public RatelLicenceBuilder setExpire(long expire) {
            this.expire = expire;
            return this;
        }

        public RatelLicenceBuilder addLicenceTypeMask(AuthorizeType authorizeType) {
            this.licenceType |= authorizeType.typeMask;
            return this;
        }

        public RatelLicenceBuilder setAccount(String account) {
            this.account = account;
            return this;
        }

        public RatelLicenceBuilder addPackage(String packageName) {
            this.packageList.add(packageName);
            return this;
        }

        public RatelLicenceBuilder addDevice(String devices) {
            this.deviceList.add(devices);
            return this;
        }

        public RatelLicenceBuilder setExtra(String extra) {
            this.extra = extra;
            return this;
        }

        public RatelLicence build() {
            if (licenceId == null) {
                licenceId = UUID.randomUUID().toString();
            }
            if (account == null) {
                throw new IllegalStateException("need account");
            }
            if (licenceType == 0) {
                addLicenceTypeMask(AuthorizeType.AuthorizeTypePerson);
            }

            return new RatelLicence(licenceId,
                    licenceVersion,
                    licenceProtocolVersion,
                    expire,
                    licenceType,
                    account,
                    new ArrayList<>(new HashSet<>(packageList)).toArray(new String[0]),
                    new ArrayList<>(new HashSet<>(deviceList)).toArray(new String[0]),
                    extra);
        }
    }

}
