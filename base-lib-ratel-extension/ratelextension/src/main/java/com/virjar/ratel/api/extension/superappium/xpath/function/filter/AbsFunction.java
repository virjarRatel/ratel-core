package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.exception.EvaluateException;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;

import java.math.BigDecimal;
import java.util.List;

public class AbsFunction implements FilterFunction {
    @Override
    public Object call(ViewImage element, List<SyntaxNode> params) {
        //Preconditions.checkArgument(params.size() > 0, getName() + " must have one parameter");
        Object calc = params.get(0).calc(element);
        if (!(calc instanceof Number)) {
            throw new EvaluateException(getName() + " must have one number parameter, now it is: " + calc);
        }
        Number number = (Number) calc;
        if (calc instanceof Integer) {
            return Math.abs(number.intValue());
        }
        if (calc instanceof Double) {
            return Math.abs(number.doubleValue());
        }
        if (calc instanceof Long) {
            return Math.abs(number.longValue());
        }
        if (calc instanceof Float) {
            return Math.abs(number.floatValue());
        }
        if (calc instanceof BigDecimal) {
            return ((BigDecimal) calc).abs();
        }

        // default transform to a double
        return Math.abs(number.doubleValue());
    }

    @Override
    public String getName() {
        return "abs";
    }
}
