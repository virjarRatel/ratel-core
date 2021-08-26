package com.virjar.ratel.api.extension.superappium.xpath.function.axis;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.ViewImages;

import java.util.List;

public class SelfFunction implements AxisFunction {
    @Override
    public ViewImages call(ViewImage e, List<String> args) {
        return new ViewImages(e);
    }

    @Override
    public String getName() {
        return "self";
    }
}
