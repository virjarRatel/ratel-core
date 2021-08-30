package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;

import java.util.List;

public class ContainsFunction implements FilterFunction {
    @Override
    public Object call(ViewImage element, List<SyntaxNode> params) {
        //  Preconditions.checkArgument(params.size() >= 2, "contains need 2 params");
        Object containerObjc = params.get(0).calc(element);
        if (containerObjc == null) {
            return false;
        }
        return containerObjc.toString().contains(params.get(1).calc(element).toString());
    }

    @Override
    public String getName() {
        return "contains";
    }
}
