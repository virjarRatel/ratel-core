package com.virjar.ratel.api.extension.superappium.sekiro;

import android.text.TextUtils;
import android.view.View;

import com.virjar.ratel.api.extension.superappium.PageTriggerManager;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.traversor.SuperAppiumDumper;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroRequestHandler;
import com.virjar.sekiro.api.SekiroResponse;
import com.virjar.sekiro.api.databind.AutoBind;

import java.util.ArrayList;
import java.util.List;

import external.com.alibaba.fastjson.JSON;
import external.com.alibaba.fastjson.JSONObject;

@Deprecated
public class DumpTopFragmentHandler implements SekiroRequestHandler {

    @AutoBind
    private String fragmentClass;

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, final SekiroResponse sekiroResponse) {
        PageTriggerManager.getMainLooperHandler().post(new Runnable() {
            @Override
            public void run() {
                List<Object> topFragment;
                if (TextUtils.isEmpty(fragmentClass)) {
                    topFragment = PageTriggerManager.getTopFragment();
                } else {
                    topFragment = new ArrayList<>();
                    Object topFragment1 = PageTriggerManager.getTopFragment(fragmentClass);
                    if (topFragment1 != null) {
                        topFragment.add(topFragment1);
                    }
                }
                List<ViewImage> viewImages = new ArrayList<>();
                for (Object fragment : topFragment) {
                    viewImages.add(new ViewImage((View) RposedHelpers.callMethod(fragment, "getView")));
                }

                if (viewImages.size() == 0) {
                    sekiroResponse.failed("no data");
                    return;
                }
                if (viewImages.size() == 1) {
                    sekiroResponse.success(JSON.parse(SuperAppiumDumper.dumpToJson(viewImages.get(0))));
                    return;
                }

                List<JSONObject> jsonObjects = new ArrayList<>();
                for (ViewImage viewImage : viewImages) {
                    jsonObjects.add(JSON.parseObject(SuperAppiumDumper.dumpToJson(viewImage)));
                }
                sekiroResponse.success(jsonObjects);
            }
        });

    }
}
