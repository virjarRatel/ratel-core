package com.virjar.ratel.api;

import java.util.Set;

public interface VirtualEnv {
    enum VirtualEnvModel {
        /**
         * 未启用虚拟环境
         */
        DISABLE,
        /**
         * 每次启动app切换设备数据，适合未登录数据抓取使用
         */
        START_UP,
        /**
         * app重新安装的时候切换设备数据，适合登录态+不会编程控制
         */
        INSTALL,
        /**
         * 多用户模式，这个模式比较特殊，将会放大用户。通过时间分割的方式实现多用户
         */
        MULTI,
        /**
         * 通过ratel api来控制设备信息切换，灵活的根据业务逻辑控制。比如发现设备被拉黑，主动控制设备切换<br>
         * 目前MULTI支持通过apk手动控制，所以不再需要支持MANUAL模式了
         */
        @Deprecated
        MANUAL
    }


    class RawFingerData {
        public RawFingerData(String imei, String serial, double latitude, double longitude) {
            this.imei = imei;
            this.serial = serial;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public RawFingerData() {
        }

        public String imei = "";
        public String serial = "";
        public Double latitude;
        public Double longitude;
    }

    /**
     * 查询当前设备虚拟环境，结果为枚举。包含系统ratel定义的几种模式
     *
     * @return 虚拟化环境模式
     * @see VirtualEnvModel
     */
    VirtualEnvModel getVirtualEnvModel();


    /**
     * 切换设备，本API在不同的 {@link com.virjar.ratel.api.VirtualEnv.VirtualEnvModel}。如下：<br>
     * <ul>
     * <li>{@link com.virjar.ratel.api.VirtualEnv.VirtualEnvModel#DISABLE}: 不会有任何作用，调用直接被忽略</li>
     * <li>{@link com.virjar.ratel.api.VirtualEnv.VirtualEnvModel#START_UP} | {@link com.virjar.ratel.api.VirtualEnv.VirtualEnvModel#INSTALL} </li>
     * <li>{@link com.virjar.ratel.api.VirtualEnv.VirtualEnvModel#MULTI}: 切换到userId对应的账户下，如果userId不存在，那么重新创建。如果userId为控，那么ratel自动创建一个</li>
     * </ul>
     * 本API只有在main进程中执行有效，所有环境操作只能由主进程执行<br>
     * 如果设备数据切换成功，那么将会导致app主动停止。需要被其他框架重新守护
     *
     * @param userId 目标用户id，仅在 {@link com.virjar.ratel.api.VirtualEnv.VirtualEnvModel#MULTI}下有效 ,请注意，userId需要满足JavaIdentify规则（数字、字母、下划线），不满足规则的字符将会被抹除
     */
    void switchEnv(String userId);

    /**
     * 系统可用的user集合，仅在multi模式下有效
     *
     * @return userList集合
     */
    Set<String> availableUserSet();

    /**
     * 删除某个用户。用户需要存在，并且不能删除nowUser
     *
     * @param userId 用户id
     * @return 是否删除成功。一般来说，除非用户不存在，否则不会删除失败
     */
    boolean removeUser(String userId);

    /**
     * manual 模式下，需要手动控制是否切换设备，如果设置设备切换状态为true。那么设备信息永远不会切换。除非将标记从新设置为false。 keep标记默认为false
     * multi模式支持API控制，不需要存在manual了
     *
     * @param keep 是否需要保持设备不被切换
     */
    @Deprecated
    void keepEnvForManualModel(boolean keep);

    /**
     * 当前用户生效的用户，仅在MULTI模式下有效在
     *
     * @return userId, 如果userId在输入的时候存在特殊字符。那么特殊字符将会被清楚
     */
    String nowUser();

    /**
     * 查询原生的imei数据，如果没有权限，或者没有触发IMEI调用。那么查询结果为空
     *
     * @return 被替换前的IMEI
     * @deprecated see {@link com.virjar.ratel.api.FingerPrintModel#imei}
     */
    @Deprecated
    String originIMEI();

    /**
     * 查询原生的AndroidId，如果没有触发AndroidId调用，那么查询结果为空
     * <br>
     * 当前已经没有意义，由于android本身是随机值，获取原始的随机只并没有意义
     *
     * @return 被替换前的AndroidId，这个数值意义不大。本身AndroidId就是在Settings中记录的一个随机数
     */
    @Deprecated
    String originAndroidId();

    /**
     * 查询原生的手机号，如果没有触发手机号获取调用，或者本app权限为空，那么查询结果为空
     *
     * @return 被替换前的手机号
     * @deprecated see {@link com.virjar.ratel.api.FingerPrintModel#line1Number}
     */
    @Deprecated
    String originLine1Number();

    /**
     * MEID 另一个模式下的id，类似IMEI，平常使用较少
     *
     * @return 被替换前的Meid
     * @deprecated see {@link com.virjar.ratel.api.FingerPrintModel#meid}
     */
    @Deprecated
    String originMeid();


    /**
     * @return IccSerialNumber
     * @deprecated see {@link com.virjar.ratel.api.FingerPrintModel#iccSerialNumber}
     */
    @Deprecated
    String originIccSerialNumber();

    /**
     * 清理用户数据,主要用在zelda模式和startUp模式下。请注意，如果app的sdcard没有隔离，那么这个调用可能无法真正完全清空
     */
    void cleanPkgData();

    /**
     * 序列号为各厂商自定义，正常情况下，在同一个厂商的手机里面是唯一的。所以可以作为唯一的设备信息标记
     *
     * @return 被替换前的序列号
     * @deprecated see {@link com.virjar.ratel.api.FingerPrintModel#serial}
     */
    @Deprecated
    String originSerialNumber();

    RawFingerData rawFingerData();

    /**
     * 在自己实现了定位模拟的场景下，需要关闭平头哥内部的定位模拟模块
     */
    void disableLocalMock();

    String defaultMultiUserId = "default_0";
}
