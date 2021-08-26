package com.virjar.ratel.api.extension.superappium.xmodel.view;

import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xmodel.ValueGetter;

public class ClassNameGetter implements ValueGetter<String> {

    @Override
    public String get(ViewImage viewImage) {
        return viewImage.getOriginView().getClass().getName();
    }

    @Override
    public boolean support(Class type) {
        return true;
    }

    @Override
    public String attr() {
        return SuperAppium.className;
    }
}
