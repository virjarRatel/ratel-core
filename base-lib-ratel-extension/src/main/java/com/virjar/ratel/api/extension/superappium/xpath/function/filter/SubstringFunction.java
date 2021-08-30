package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;
import com.virjar.ratel.api.extension.superappium.xpath.exception.EvaluateException;

import java.util.List;

public class SubstringFunction extends AbstractStringFunction {
    @Override
    public Object call(ViewImage element, List<SyntaxNode> params) {
       // Preconditions.checkArgument(params.size() >= 2, getName() + " must have parameter at lest 2");
        String string = firstParamToString(element, params);
        Object index = params.get(1).calc(element);
        if (!(index instanceof Integer)) {
            throw new EvaluateException(getName() + " index must be a integer :" + index);
        }
        int end = -1;
        if (params.size() > 2) {
            Object endObj = params.get(2).calc(element);
            if (!(endObj instanceof Integer)) {
                throw new EvaluateException(getName() + "end index must be a integer :" + index);
            }
            end = (int) endObj;
        }

        return end < 0 ? string.substring((int) index) : string.substring((int) index, end);
    }

    @Override
    public String getName() {
        return "substring";
    }
}
