package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import android.util.Log;

import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;

import java.util.List;

import external.org.apache.commons.lang3.BooleanUtils;
import external.org.apache.commons.lang3.StringUtils;

public class PredicateFunction implements FilterFunction {
    @Override
    public Object call(ViewImage element, List<SyntaxNode> params) {
        if (element == null) {
            return false;
        }
        Object ret = params.get(0).calc(element);
        if (ret == null) {
            return false;
        }

        if (ret instanceof Number) {
            int i = ((Number) ret).intValue();
            return element.index() == i;
        }

        if (ret instanceof Boolean) {
            return ret;
        }

        if (ret instanceof CharSequence) {
            String s = ret.toString();
            Boolean booleanValue = BooleanUtils.toBooleanObject(s);
            if (booleanValue != null) {
                return booleanValue;
            }
            return StringUtils.isNotBlank(s);
        }

        //log.warn("can not recognize predicate expression calc result:" + ret);
        Log.w(SuperAppium.TAG, "can not recognize predicate expression calc result:" + ret);
        return false;
    }

    @Override
    public String getName() {
        return "inner_predicate";
    }
}
