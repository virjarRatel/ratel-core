package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.operator;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.node.WrapperUnit;

/**
 * Created by virjar on 17/6/10.
 */
@OpKey(value = "or", priority = 0)
public class Or2Unit extends WrapperUnit {
    @Override
    protected String targetName() {
        return "||";
    }

    @Override
    public Object calc(ViewImage element) {
        return wrap().calc(element);
    }

}
