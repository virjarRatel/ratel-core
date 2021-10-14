package com.virjar.ratel.api.extension.superappium.xpath.function.select;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.model.XNode;

import java.util.List;

import external.org.apache.commons.lang3.StringUtils;

public class AttrFunction extends AttrBaseFunction {

    @Override
    public void handle(boolean allAttr, String attrKey, ViewImage element, List<XNode> ret) {
        if (allAttr) {
            ret.add(XNode.t(element.attributes()));
        } else {
            Object value = element.attribute(attrKey);
            if (value == null) {
                return;
            }
            String str = value.toString();

            if (StringUtils.isNotBlank(str)) {
                ret.add(XNode.t(str));
            }
        }
    }

    @Override
    public String getName() {
        return "@";
    }
}
