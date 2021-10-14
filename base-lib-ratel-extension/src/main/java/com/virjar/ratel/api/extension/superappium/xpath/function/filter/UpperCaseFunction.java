package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;

import java.util.List;

public class UpperCaseFunction extends AbstractStringFunction {
    @Override
    public Object call(ViewImage element, List<SyntaxNode> params) {
        return firstParamToString(element, params).toUpperCase();
    }

    @Override
    public String getName() {
        return "upper-case";
    }
}
