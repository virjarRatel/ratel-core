package com.virjar.ratel.api.extension.superappium.xpath.function.axis;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.ViewImages;

import java.util.List;

public class PrecedingSiblingOneFunction implements AxisFunction {
    @Override
    public ViewImages call(ViewImage e, List<String> args) {
        ViewImages rs = new ViewImages();
        if (e.previousSibling() != null) {
            rs.add(e.previousSibling());
        }
        return rs;
    }

    @Override
    public String getName() {
        return "preceding-sibling-one";
    }
}
