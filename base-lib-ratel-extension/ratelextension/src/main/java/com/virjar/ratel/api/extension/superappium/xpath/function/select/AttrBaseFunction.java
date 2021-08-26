package com.virjar.ratel.api.extension.superappium.xpath.function.select;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.ViewImages;
import com.virjar.ratel.api.extension.superappium.xpath.model.XNode;
import com.virjar.ratel.api.extension.superappium.xpath.model.XNodes;
import com.virjar.ratel.api.extension.superappium.xpath.model.XpathNode;

import java.util.List;

public abstract class AttrBaseFunction implements SelectFunction {
    @Override
    public XNodes call(XpathNode.ScopeEm scopeEm, ViewImages elements, List<String> args) {
        XNodes ret = new XNodes();
        boolean allAttr = "*".equals(args.get(0));
        String attrName = args.get(0);
        for (ViewImage element : elements) {
            handle(allAttr, attrName, element, ret);
            if (scopeEm == XpathNode.ScopeEm.RECURSIVE || scopeEm == XpathNode.ScopeEm.CURREC) {
                ViewImages allElements = element.getAllElements();
                for (ViewImage subElement : allElements) {
                    handle(allAttr, attrName, subElement, ret);
                }
            }
        }
        return ret;
    }

    abstract void handle(boolean allAttr, String attrKey, ViewImage element, List<XNode> ret);
}
