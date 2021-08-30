package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.node;

import com.virjar.ratel.api.extension.superappium.ViewImage;

public abstract class BooleanRevertUnit extends WrapperUnit {
    @Override
    public Object calc(ViewImage element) {
        return !((Boolean) wrap().calc(element));
    }

}
