package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;

import java.util.Iterator;
import java.util.List;

public class ConcatFunction implements FilterFunction {
    @Override
    public Object call(ViewImage element, List<SyntaxNode> params) {
        if (params.size() == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<SyntaxNode> iterator = params.iterator();
        stringBuilder.append(iterator.next().calc(element));
        while (iterator.hasNext()) {
            stringBuilder.append(" ").append(iterator.next().calc(element));
        }
        return stringBuilder.toString();
    }

    @Override
    public String getName() {
        return "concat";
    }
}

