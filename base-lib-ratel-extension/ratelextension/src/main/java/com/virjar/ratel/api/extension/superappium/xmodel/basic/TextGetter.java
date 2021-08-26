package com.virjar.ratel.api.extension.superappium.xmodel.basic;

import android.widget.TextView;

import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xmodel.ValueGetter;


public class TextGetter implements ValueGetter<String> {
    @Override
    public String get(ViewImage viewImage) {
        TextView textView = (TextView) viewImage.getOriginView();
        CharSequence text = textView.getText();
        if (text == null) {
            return null;
        }
        return text.toString();
    }

    @Override
    public boolean support(Class type) {
        return TextView.class.isAssignableFrom(type);
    }

    @Override
    public String attr() {
        return SuperAppium.text;
    }
}
