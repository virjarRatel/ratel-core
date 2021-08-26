package com.virjar.ratel.manager.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Table(database = DbConf.class)
@Data
@EqualsAndHashCode(of = "taskId", callSuper = false)
public class SchedulerTask extends BaseModel {

    /**
     * 任务id，其实就是targetAppPackage和taskImplementationClassName的联合组件
     */
    @PrimaryKey
    private String taskId;

    /**
     * 任务对应的宿主apk
     */
    @Column
    private String targetAppPackage;

    /**
     * 任务实现类，需要用户手动配置，必须是 com.virjar.ratel.api.scheduler.RatelTask的实现
     */
    @Column
    private String taskImplementationClassName;

    /**
     * 任务需要寄生在外部模块中，需要指定外部模块包名用于定位代码位置
     */
    @Column
    private String moduleApkPackage;

    /**
     * 基于cron表达式的调度任务，通过表达式来描述任务调度规则.<br>
     * 不过需要注意的是，ratel框架不支持基于秒级别的调度
     */
    @Column
    private String cronExpression;


    /**
     * 任务最长执行时间间隔，避免任务超时提供的一个控制机制。超时后app将会自杀。时间间隔单位：秒
     * <br> 默认值10分钟
     */
    @Column
    private Integer maxDuration = 10 * 60;

    /**
     * 任务上次执行时间
     */
    @Column
    private Long lastExecute = null;

    @Column
    private Long codeUpdate = null;

    @Column
    private int taskStatus = TASK_STATUS_STOP;

    @Column
    private boolean restartApp = true;

    public static final int TASK_STATUS_STOP = 0;
    public static final int TASK_STATUS_RUNNING = 1;


}
