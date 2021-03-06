package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;

import java.util.List;

public class PositionFunction implements FilterFunction {
    @Override
    public Object call(ViewImage element, List<SyntaxNode> params) {
        if (params.size() > 0) {
            Object calc = params.get(0).calc(element);
            if (calc instanceof ViewImage) {
                return ((ViewImage) calc).index();
            } else {
                throw new IllegalStateException("result of function :<position> not a view element");
            }
        }
        return element.index();
    }

    @Override
    public String getName() {
        return "position";
    }
}
