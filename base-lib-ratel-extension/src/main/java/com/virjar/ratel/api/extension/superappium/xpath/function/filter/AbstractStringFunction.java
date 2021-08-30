package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.exception.EvaluateException;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;

import java.util.List;

public abstract class AbstractStringFunction implements FilterFunction {
    protected String firstParamToString(ViewImage element, List<SyntaxNode> params) {
        //Preconditions.checkArgument(params.size() > 0, getName() + " must have parameter at lest 1");
        Object string = params.get(0).calc(element);
        if (!(string instanceof String)) {
            throw new EvaluateException(getName() + " first parameter is not a string :" + string);
        }
        return string.toString();
    }
}
