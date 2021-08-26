package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;

import java.util.List;

public class FirstFunction implements FilterFunction {
    @Override
    public Object call(ViewImage element, List<SyntaxNode> params) {
        return element.index() == 0;
    }

    @Override
    public String getName() {
        return "first";
    }
}
