package com.virjar.ratel.api.extension.superappium.sekiro;

import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroRequestHandler;
import com.virjar.sekiro.api.SekiroResponse;
import com.virjar.sekiro.api.databind.AutoBind;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import external.org.apache.commons.lang3.StringUtils;

@Deprecated
public class FileExploreHandler implements SekiroRequestHandler {

    @AutoBind(defaultStringValue = "/")
    private String path;

    @AutoBind(defaultStringValue = "get")
    private String op;

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
        if (StringUtils.isBlank(path)) {
            path = "/";
        }
        String base = RatelToolKit.sContext.getFilesDir().getParentFile().getAbsolutePath();
        if (path.startsWith(base)) {
            path = path.substring(base.length());
        }
        File targetFile = new File(RatelToolKit.sContext.getFilesDir().getParent());
        if (!"/".equals(path)) {
            targetFile = new File(RatelToolKit.sContext.getFilesDir().getParentFile(), path);
        }


        if ("get".equalsIgnoreCase(op)) {
            if (!targetFile.exists()) {
                sekiroResponse.failed(404, "the file :" + targetFile.getAbsolutePath() + " not exist");
                return;
            }
            handleGet(sekiroRequest, sekiroResponse, targetFile);
        } else if ("post".equalsIgnoreCase(op)) {
            if (!targetFile.exists()) {
                sekiroResponse.failed(404, "the file :" + targetFile.getAbsolutePath() + " not exist");
                return;
            }
            handlePost(sekiroRequest, sekiroResponse, targetFile);
        } else if ("put".equalsIgnoreCase(op)) {
            handlePut(sekiroRequest, sekiroResponse, targetFile);
        } else if ("delete".equalsIgnoreCase(op)) {
            handleDelete(sekiroResponse, targetFile);
        }
    }

    private void handleDelete(SekiroResponse sekiroResponse, File targetFile) {
        if (!targetFile.isFile() || !targetFile.exists()) {
            sekiroResponse.failed("filed not exist");
        }
        if (targetFile.delete()) {
            sekiroResponse.success("success");
        } else {
            sekiroResponse.failed("remove failed");
        }

    }

    private void handleGet(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse, File targetFile) {
        if (targetFile.isDirectory()) {
            List<String> ret = new ArrayList<>();
            String[] list = targetFile.list();
            if (list == null) {
                sekiroResponse.failed("can not read dir file: " + targetFile.getAbsolutePath());
                return;
            }
            String base = RatelToolKit.sContext.getFilesDir().getParentFile().getAbsolutePath();
            for (String str : list) {
                if (str.startsWith(base)) {
                    ret.add(str.substring(base.length()));
                } else {
                    ret.add(str);
                }
            }
            sekiroResponse.success(ret);
            return;
        }
        if (!targetFile.canRead()) {
            sekiroResponse.failed("can not read file: " + targetFile.getAbsolutePath());
            return;
        }
        try {
            sekiroResponse.sendFile(targetFile);
        } catch (IOException e) {
            Log.e(SuperAppium.TAG, "read filed failed", e);
            sekiroResponse.failed("can not read file: " + targetFile.getAbsolutePath());
        }
    }

    private void handlePost(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse, File targetFile) {
        sekiroResponse.failed("not implement now");
    }

    private void handlePut(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse, File targetFile) {
        sekiroResponse.failed("not implement now");
    }
}
