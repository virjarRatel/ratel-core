package com.virjar.ratel.api.extension.superappium.sekiro;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.virjar.ratel.api.extension.superappium.PageTriggerManager;
import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.ViewImages;
import com.virjar.ratel.api.extension.superappium.traversor.Collector;
import com.virjar.ratel.api.extension.superappium.traversor.Evaluator;
import com.virjar.ratel.api.extension.superappium.traversor.SuperAppiumDumper;
import com.virjar.ratel.api.extension.superappium.xpath.XpathParser;
import com.virjar.ratel.api.extension.superappium.xpath.model.XNode;
import com.virjar.ratel.api.extension.superappium.xpath.model.XNodes;
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
public class DumpTopActivityHandler implements SekiroRequestHandler {

    @AutoBind
    private String viewHash;

    @AutoBind
    private String xpath;

    @AutoBind
    private String type = "activity";

    @AutoBind
    private boolean webView = false;

    @Override
    public void handleRequest(final SekiroRequest sekiroRequest, final SekiroResponse sekiroResponse) {

        PageTriggerManager.getMainLooperHandler().post(new Runnable() {

            private void dumpInternal() {
                View topRootView = null;

                if ("dialog".equalsIgnoreCase(type)) {
                    Window topDialogWindow = PageTriggerManager.getTopDialogWindow();
                    if (topDialogWindow != null) {
                        topRootView = topDialogWindow.getDecorView();
                    }
                } else if ("popupWindow".equalsIgnoreCase(type)) {
                    topRootView = PageTriggerManager.getTopPupWindowView();
                }

                if (topRootView == null) {
                    topRootView = PageTriggerManager.getTopRootView();
                }


                if (topRootView == null) {
                    sekiroResponse.failed("no topRootView found");
                    return;
                }

                ViewImage viewImage = new ViewImage(topRootView);

                if (webView) {
                    handleWebViewDump(viewImage);
                } else {
                    handleNativeDump(viewImage);
                }


            }

            private void handleWebViewDump(ViewImage viewImage) {
                final WebView webView = viewImage.findWebViewIfExist();
                if (webView == null) {
                    sekiroResponse.failed("no WebView found");
                    return;
                }
                new Handler((Looper) RposedHelpers.getObjectField(webView, "mWebViewThread")).post(new Runnable() {
                    @Override
                    public void run() {
                        //android.webkit.WebView#evaluateJavascript(String script, @RecentlyNullable ValueCallback<String> resultCallback) {
                        RposedHelpers.callMethod(webView, "evaluateJavascript", "document.getElementsByTagName('html')[0].innerHTML", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                JSONObject jsonObject = JSON.parseObject("{\"data\":" + s + "}");
                                sekiroResponse.send("text/html", jsonObject.getString("data"));
                            }
                        });
                    }
                });

            }

            private void handleNativeDump(ViewImage viewImage) {
                if (!TextUtils.isEmpty(viewHash)) {
                    ViewImages collect = Collector.collect(new Evaluator.ByHash(viewHash), viewImage);
                    if (collect.isEmpty()) {
                        sekiroResponse.failed("no data");
                        return;
                    }
                    sekiroResponse.success(JSON.parseObject(SuperAppiumDumper.dumpToJson(collect.get(0))));
                    return;
                } else if (!TextUtils.isEmpty(xpath)) {
                    XNodes xNodes = XpathParser.compileNoError(xpath).evaluate(new XNodes(XNode.e(viewImage)));
                    List<Object> dumpedData = new ArrayList<>();
                    for (XNode xNode : xNodes) {
                        if (xNode.isText()) {
                            dumpedData.add(xNode.getTextVal());
                        } else {
                            dumpedData.add(JSON.parseObject(SuperAppiumDumper.dumpToJson(xNode.getElement())));
                        }
                    }
                    if (dumpedData.size() == 1) {
                        sekiroResponse.success(dumpedData.get(0));
                        return;
                    }
                    sekiroResponse.success(dumpedData);
                    return;
                }
                sekiroResponse.success(JSON.parse(SuperAppiumDumper.dumpToJson(viewImage)));
            }

            @Override
            public void run() {
                try {
                    dumpInternal();
                } catch (Exception e) {
                    sekiroResponse.failed(-1, e);
                    Log.w(SuperAppium.TAG, "dump activity error", e);
                }
            }
        });
    }
}
