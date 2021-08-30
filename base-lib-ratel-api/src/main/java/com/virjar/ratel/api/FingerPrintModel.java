package com.virjar.ratel.api;

import android.annotation.SuppressLint;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.api.hint.Beta;
import com.virjar.ratel.api.rposed.RposedHelpers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @since 1.2.1
 */
@Beta
public class FingerPrintModel {

    public FingerPrintModel() {
        genSystemDefaultKey(Settings.System.class, settingsSystemAndroidKey);
        genDefaultSystemPropertiesFakeMap();
    }

    /////// telephony 相关指纹隐藏
    /**
     * 序列号，Android9之后就拿不到了
     */
    public ValueHolder<String> serial = ValueHolder.defaultStringHolder();

    /**
     * imei 手机卡槽的唯一标记
     */
    public ValueHolder<String> imei = ValueHolder.defaultStringHolder();

    /**
     * imei 手机卡槽的唯一标记，可能有双卡
     */
    public ValueHolder<String> imei2 = ValueHolder.defaultStringHolder();

    /**
     * 手机卡槽的唯一标记 ，电信使用这个值
     */
    public ValueHolder<String> meid = ValueHolder.defaultStringHolder();

    /**
     * 手机卡槽的唯一标记 ，电信使用这个值，可能有双卡
     */
    public ValueHolder<String> meid2 = ValueHolder.defaultStringHolder();

    /**
     * 本机号码
     */
    public ValueHolder<String> line1Number = ValueHolder.defaultStringHolder();

    //  以下为基站数据相关（CellInfo）
    public ValueHolder<String> iccSerialNumber = ValueHolder.defaultStringHolder();

    public ValueHolder<String> subscriberId = ValueHolder.defaultStringHolder();

    @Deprecated
    public ValueHolder<Integer> lac = ValueHolder.integerValueHolder(4);

    //只有第一个cid可以直接修改
    public ValueHolder<Integer> cid = ValueHolder.integerValueHolder(4);

    @Deprecated
    public ValueHolder<Integer> tac = ValueHolder.integerValueHolder(4);

    @Deprecated
    public ValueHolder<Integer> lac2 = ValueHolder.integerValueHolder(4);

    @Deprecated
    public ValueHolder<Integer> cid2 = ValueHolder.integerValueHolder(4);

    @Deprecated
    public ValueHolder<Integer> tac2 = ValueHolder.integerValueHolder(4);

    /**
     * 经纬度差异，在自动飘逸模式下，下面两个参数生效。本参数描述的含义是：当前手机的地理位置会相对于他的真实坐标按照固定差值漂移。
     * 目前飘逸的范围是，当前手机定位为中心的30公里为半径的圆弧范围，且最终落点满足圆内高斯分布。
     * <p>
     * 如果你按照系统默认设定，那么框架将会使用自动飘逸模式。除非你设置了（latitude，longitude）元组参数。
     */
    public ValueHolder<Double> latitudeDiff = ValueHolder.defaultValueHolder();
    public ValueHolder<Double> longitudeDiff = ValueHolder.defaultValueHolder();

    /**
     * 经纬度模拟定位中心坐标，本参数和上面的参数互斥 （latitudeDiff，longitudeDiff）。如果设置了下面的两对参数，那么最终app可见定位为设定的经纬度坐标的微小抖动（可能一两公里的抖动）。
     * 这个参数是手动设定定位模式，适合模拟异地设备，但是异地模拟包括Wi-Fi，ip出口，基站信息，都需要同步模拟。否则容易被大数据分析发现。
     * <p>
     * 无论是否处于自动飘逸模式，你都可以通过这两个对象获取原始定位数据
     */
    public ValueHolder<Double> latitude = ValueHolder.defaultValueHolder();
    public ValueHolder<Double> longitude = ValueHolder.defaultValueHolder();

    /**
     * 当app通过VPN替换出口ip的时候，app可以监测到VPN的存在。默认隐藏VPN的flag
     */
    public boolean hiddenVPN = true;


    /**
     * 系统属性mock，默认mock序列号（serial）和基带版本（baseband）
     * <br>
     * 请注意，这个接口废弃了
     */
    @Deprecated
    public PropertiesReplacer<String> systemPropertiesFake = null;


    /**
     * 请注意，属性模拟需要在ratel框架启动完成之前设置生效，当app代码运行起来之后不能再设置替换项目了
     * <br>
     * 请注意，ro.serialno逻辑很复杂，Android8以下可以在这里赋值，Android8以上需要通过
     *
     * @see com.virjar.ratel.api.FingerPrintModel.serial
     */
    public Map<String, String> systemPropertiesFakeMap = new HashMap<>();

    public static final String serialKey = "ro.serialno";
    private static final String basebandKey = "gsm.version.baseband";
    private static final String persistSysUsbConfig = "persist.sys.usb.config";
    private static final String sysUsbConfig = "sys.usb.config";
    private static final String sysUsbStatus = "sys.usb.state";
    private static final String roDebuggable = "ro.debuggable";

    @SuppressLint("HardwareIds")
    private void genDefaultSystemPropertiesFakeMap() {

        systemPropertiesFakeMap.put(persistSysUsbConfig, "none");
        systemPropertiesFakeMap.put(sysUsbConfig, "mtp");
        systemPropertiesFakeMap.put(sysUsbStatus, "mtp");
        systemPropertiesFakeMap.put(roDebuggable, "0");
        systemPropertiesFakeMap.put("adb_enabled", "0");


        String baseBand = get(basebandKey);
        if (!TextUtils.isEmpty(baseBand)) {
            systemPropertiesFakeMap.put(basebandKey, SuffixTrimUtils.SuffixModifier4.modify(baseBand));
        }

        //序列号，请使用 @see com.virjar.ratel.api.FingerPrintModel.serial
    }

    /**
     * TODO 迁移这个函数
     */
    private static String get(String key) {
        try {
            return (String) systemPropertiesClass.getDeclaredMethod("get", String.class).invoke(key, new Object[]{key});
        } catch (Exception e) {
            Log.w(RatelToolKit.TAG, "call system properties get error");
            return null;
        }
    }

    private static Class<?> systemPropertiesClass = RposedHelpers.findClassIfExists("android.os.SystemProperties", ClassLoader.getSystemClassLoader());


    /**
     * 全局设置 see {@link android.provider.Settings.Global}
     */
    public Map<String, String> settingsGlobalFake = new HashMap<>();

    /**
     * 系统设置 see {@link android.provider.Settings.System}
     */
    public Map<String, String> settingsSystemFake = new HashMap<>();


    /**
     * 为避免app矩阵干扰全局设置，需要提供一个系统设置的白名单，其他app注入的key不与许注入
     */
    public static Set<String> settingsSystemAndroidKey = new HashSet<>();


    private static void genSystemDefaultKey(Class settingsClass, Set<String> container) {
        for (Field field : settingsClass.getDeclaredFields()) {
            if (field.isSynthetic()) {
                continue;
            }
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            if (!String.class.equals(field.getType())) {
                continue;
            }
            if (!Modifier.isPublic(field.getModifiers())) {
                //java.lang.Class<com.virjar.ratel.api.FingerPrintModel> cannot access private final static  field java.lang.String android.provider.Settings$System.className of class java.lang.Class<android.provider.Settings$System>
                continue;
            }
            try {
                String value = (String) field.get(null);
                if (value == null) {
                    continue;
                }
                container.add(value);
            } catch (Throwable e) {
                Log.e(RatelToolKit.TAG, "can not get default settings key ", e);
            }
        }
    }


    /**
     * 安全设置 see {@link android.provider.Settings.Secure}
     */
    public Map<String, String> settingsSecureFake = new HashMap<>();

    /**
     * @see android.media.MediaDrm#getPropertyByteArray(String)
     * @see android.media.MediaDrm#PROPERTY_DEVICE_UNIQUE_ID
     * <p>
     * deviceUniqueId是一个32位的byte数组，因此用base64 {@link android.util.Base64#NO_WRAP}保存
     */
    public ValueHolder<String> mediaDrmDeviceId = ValueHolder.defaultStringHolder();

    /**
     * Wi-Fi BSSID和SSID mock接口，Wi-Fi列表可以用来实现定位，你可以通过编程接口填入Wi-Fi列表。
     * 请注意，不能添加新的Wi-Fi信号。如果你拥有的Wi-Fi mock资源少于余实际扫描到的，那么框架将会按照框架的行为，替换BSSID的后两位。连接中的Wi-Fi资源除外
     */
    public WifiReplacer wifiReplacer = null;

    public static class WIFIReplaceItem {
        public String SSID;
        public String BSSID;
    }

    public interface WifiReplacer {
        void doReplace(WIFIReplaceItem wifiReplaceItem);
    }

    public interface PropertiesReplacer<T> {
        T replace(String key, T value);
    }

    public static class ValueHolder<T> {
        private T value;
        private boolean called = false;
        private T origin;
        private PropertiesReplacer<T> propertiesReplacer;

        public final T replace(String key, T originValue) {
            origin = originValue;
            if (called) {
                return value;
            }
            if (propertiesReplacer != null) {
                synchronized (this) {
                    if (!called) {
                        called = true;
                        value = propertiesReplacer.replace(key, originValue);
                    }
                }
            }

            return value;
        }

        public ValueHolder<T> set(T value) {
            this.value = value;
            called = true;
            return this;
        }

        public ValueHolder<T> set(PropertiesReplacer<T> propertiesReplacer) {
            this.propertiesReplacer = propertiesReplacer;
            return this;
        }

        public T get() {
            return value;
        }

        public T getOrigin() {
            return origin;
        }

        public ValueHolder<T> setOrigin(T t) {
            origin = t;
            return this;
        }

        public static ValueHolder<String> defaultStringHolder() {
            ValueHolder<String> valueHolder = new ValueHolder<>();
            valueHolder.set((key, value) -> SuffixTrimUtils.SuffixModifier5.modify(value));
            return valueHolder;
        }

        static ValueHolder<Integer> defaultIntegerHolder() {
            ValueHolder<Integer> valueHolder = new ValueHolder<>();
            valueHolder.set((key, value) -> value + SuffixTrimUtils.randomMockDiff(20));
            return valueHolder;
        }


        static ValueHolder<Integer> integerValueHolder(int space) {
            ValueHolder<Integer> valueHolder = new ValueHolder<>();
            valueHolder.set((key, value) -> value + SuffixTrimUtils.randomMockDiff(space));
            return valueHolder;
        }

        static <T> ValueHolder<T> defaultValueHolder() {
            return new ValueHolder<>();
        }
    }

}
