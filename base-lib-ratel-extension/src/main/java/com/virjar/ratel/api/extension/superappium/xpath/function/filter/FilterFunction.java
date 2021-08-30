package com.virjar.ratel.api.extension.superappium.xpath.function.filter;

import com.virjar.ratel.api.extension.superappium.xpath.function.NameAware;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;

import java.util.List;

public interface FilterFunction extends NameAware {
    Object call(ViewImage viewImage, List<SyntaxNode> params);
}
