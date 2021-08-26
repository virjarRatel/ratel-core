package com.virjar.ratel.api.extension.superappium.xpath.function.select;

import com.virjar.ratel.api.extension.superappium.ViewImages;
import com.virjar.ratel.api.extension.superappium.xpath.function.NameAware;
import com.virjar.ratel.api.extension.superappium.xpath.model.XNodes;
import com.virjar.ratel.api.extension.superappium.xpath.model.XpathNode;

import java.util.List;

public interface SelectFunction extends NameAware {
   XNodes call(XpathNode.ScopeEm scopeEm, ViewImages elements, List<String> args);
}
