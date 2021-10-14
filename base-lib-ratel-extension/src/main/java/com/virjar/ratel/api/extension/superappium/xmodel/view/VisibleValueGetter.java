package com.virjar.ratel.api.extension.superappium.xmodel.view;

import android.graphics.Rect;

import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xmodel.ValueGetter;

public class VisibleValueGetter implements ValueGetter<Boolean> {


    @Override
    public Boolean get(ViewImage viewImage) {
        return viewImage.getOriginView().getLocalVisibleRect(new Rect());
    }

    @Override
    public boolean support(Class type) {
        return true;
    }

    @Override
    public String attr() {
        return SuperAppium.visible;
    }
}
