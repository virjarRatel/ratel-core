package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;

import java.util.List;

public class StringFunction implements FilterFunction {
    @Override
    public Object call(ViewImage element, List<SyntaxNode> params) {
        //Preconditions.checkArgument(params.size() > 0, getName() + " must has one parameter");
        Object calc = params.get(0).calc(element);
        if (calc == null) {
            return "null";
        }
        return calc.toString();
    }

    @Override
    public String getName() {
        return "string";
    }
}
