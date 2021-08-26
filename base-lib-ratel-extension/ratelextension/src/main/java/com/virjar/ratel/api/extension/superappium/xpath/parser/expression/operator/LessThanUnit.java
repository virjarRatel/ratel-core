package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.operator;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.node.AlgorithmUnit;
import com.virjar.ratel.api.extension.superappium.xpath.XpathUtil;

/**
 * Created by virjar on 17/6/10.
 */
@OpKey(value = "<", priority = 10)
public class LessThanUnit extends AlgorithmUnit {
    @Override
    public Object calc(ViewImage element) {
        Object leftValue = left.calc(element);
        Object rightValue = right.calc(element);
        if (leftValue == null || rightValue == null) {
            return XpathUtil.toPlainString(rightValue).compareTo(XpathUtil.toPlainString(rightValue)) < 0;
        }
        // 左右都不为空,开始计算
        // step one think as number
        if (leftValue instanceof Number && rightValue instanceof Number) {
            return ((Number) leftValue).doubleValue() < ((Number) rightValue).doubleValue();
        }

        return XpathUtil.toPlainString(leftValue).compareTo(XpathUtil.toPlainString(rightValue)) < 0;
    }
}
