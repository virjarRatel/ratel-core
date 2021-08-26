package com.virjar.ratel.api.extension.superappium.xpath.function.axis;

import com.virjar.ratel.api.extension.superappium.ViewImage;

import java.util.List;

public class AncestorOrSelfFunction implements AxisFunction {
    @Override
    public List<ViewImage> call(ViewImage e, List<String> args) {
        List<ViewImage> rs = e.parents();
        rs.add(e);
        return rs;
    }

    @Override
    public String getName() {
        return "ancestorOrSelf";
    }
}

