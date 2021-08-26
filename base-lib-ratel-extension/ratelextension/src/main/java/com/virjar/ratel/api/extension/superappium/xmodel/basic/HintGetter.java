package com.virjar.ratel.api.extension.superappium.xmodel.basic;

import android.widget.TextView;

import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xmodel.ValueGetter;


public class HintGetter implements ValueGetter {
    @Override
    public Object get(ViewImage viewImage) {
        TextView textView = (TextView) viewImage.getOriginView();
        CharSequence hint = textView.getHint();
        if (hint == null) {
            return null;
        }
        return hint.toString();
    }

    @Override
    public String attr() {
        return SuperAppium.hint;
    }

    @Override
    public boolean support(Class type) {
        return TextView.class.isAssignableFrom(type);
    }
}
