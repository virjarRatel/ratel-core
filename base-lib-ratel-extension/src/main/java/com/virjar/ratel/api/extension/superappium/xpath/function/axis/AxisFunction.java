package com.virjar.ratel.api.extension.superappium.xpath.function.axis;

import com.virjar.ratel.api.extension.superappium.xpath.function.NameAware;
import com.virjar.ratel.api.extension.superappium.ViewImage;

import java.util.List;

public interface AxisFunction extends NameAware {
    //TODO 是否允许轴函数存在参数
    List<ViewImage> call(ViewImage viewImage, List<String> args);
}
