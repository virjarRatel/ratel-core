package com.virjar.ratel.api.extension.superappium.xmodel.view;

import android.content.Context;
import android.view.View;

import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xmodel.ValueGetter;
import com.virjar.ratel.api.rposed.RposedHelpers;

public class PackageNameValueGetter implements ValueGetter<String> {

    @Override
    public String get(ViewImage viewImage) {
        View originView = viewImage.getOriginView();
        Context context = (Context) RposedHelpers.getObjectField(originView, "mContext");
        return context.getPackageName();
    }

    @Override
    public boolean support(Class type) {
        return true;
    }

    @Override
    public String attr() {
        return SuperAppium.packageName;
    }
}
