package com.virjar.ratel.api.extension.superappium.xmodel.view;

import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xmodel.ValueGetter;

public class ClickableValueGetter implements ValueGetter<Boolean> {

    @Override
    public Boolean get(ViewImage viewImage) {
        return viewImage.getOriginView().isClickable();
    }

    @Override
    public boolean support(Class type) {
        return true;
    }

    @Override
    public String attr() {
        return SuperAppium.clickable;
    }
}
