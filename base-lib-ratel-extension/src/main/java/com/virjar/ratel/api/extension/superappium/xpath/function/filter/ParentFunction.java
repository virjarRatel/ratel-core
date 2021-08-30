package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;
import com.virjar.ratel.api.extension.superappium.xpath.XpathUtil;

import java.util.List;

public class ParentFunction implements FilterFunction {
    @Override
    public Object call(ViewImage element, List<SyntaxNode> params) {
        int index = 1;
        Integer integer = XpathUtil.firstParamToInt(params, element, getName());
        if (integer != null) {
            index = integer;
        }
        for (int i = 0; i < index; i++) {
            element = element.parentNode();
        }
        return element;
    }

    @Override
    public String getName() {
        return "parent";
    }
}
