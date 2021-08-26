package com.virjar.ratel.api.extension.superappium.xmodel.view;

import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xmodel.ValueGetter;

public class ContentDescriptionValueGetter implements ValueGetter<String> {

    @Override
    public String get(ViewImage viewImage) {
        CharSequence contentDescription = viewImage.getOriginView().getContentDescription();
        if (contentDescription == null) {
            return null;
        }
        return contentDescription.toString();
    }

    @Override
    public boolean support(Class type) {
        return true;
    }

    @Override
    public String attr() {
        return SuperAppium.contentDescription;
    }
}
