package com.virjar.ratel.api.extension.superappium.xpath.function.axis;

import com.virjar.ratel.api.inspect.Lists;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.ViewImages;

import java.util.LinkedList;
import java.util.List;

public class PrecedingSiblingFunction implements AxisFunction {
    @Override
    public ViewImages call(ViewImage e, List<String> args) {
        ViewImage tmp = e.previousSibling();
        LinkedList<ViewImage> tempList = Lists.newLinkedList();
        while (tmp != null) {
            tempList.addFirst(tmp);
            tmp = tmp.previousSibling();
        }
        return new ViewImages(tempList);
    }

    @Override
    public String getName() {
        return "preceding-sibling";
    }
}
