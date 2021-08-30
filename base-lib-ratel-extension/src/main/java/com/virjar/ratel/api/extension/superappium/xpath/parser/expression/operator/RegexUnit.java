package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.operator;


import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.node.AlgorithmUnit;
import com.virjar.ratel.api.extension.superappium.xpath.XpathUtil;

/**
 * Created by virjar on 17/6/10.
 */
@OpKey(value = "~=", priority = 10)
public class RegexUnit extends AlgorithmUnit {
    @Override
    public Object calc(ViewImage element) {
        Object leftValue = left.calc(element);
        Object rightValue = right.calc(element);
        if (leftValue == null || rightValue == null) {
            return Boolean.FALSE;
        }
        return XpathUtil.toPlainString(leftValue).matches(XpathUtil.toPlainString(rightValue));

    }
}
