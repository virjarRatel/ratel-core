package com.virjar.ratel.api.extension.superappium.xmodel.basic;

import android.widget.ImageView;

import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xmodel.ValueGetter;
import com.virjar.ratel.api.rposed.RposedHelpers;


public class ImageUriGetter implements ValueGetter {
    @Override
    public Object get(ViewImage viewImage) {
        return RposedHelpers.getObjectField(viewImage.getOriginView(), "mUri");
    }

    @Override
    public String attr() {
        return SuperAppium.mUri;
    }

    @Override
    public boolean support(Class type) {
        return ImageView.class.isAssignableFrom(type);
    }
}
