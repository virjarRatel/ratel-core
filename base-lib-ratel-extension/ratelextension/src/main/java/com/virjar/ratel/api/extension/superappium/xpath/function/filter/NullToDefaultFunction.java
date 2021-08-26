package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;

import java.util.List;

public class NullToDefaultFunction implements FilterFunction {
    @Override
    public Object call(ViewImage element, List<SyntaxNode> params) {
        // Preconditions.checkArgument(params.size() >= 2, getName() + " must have 2 parameter");
        Object calc = params.get(0).calc(element);
        if (calc != null) {
            return calc;
        }
        return params.get(1).calc(element);
    }

    @Override
    public String getName() {
        return "nullToDefault";
    }
}

