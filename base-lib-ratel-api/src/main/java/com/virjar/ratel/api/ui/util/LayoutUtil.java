package com.virjar.ratel.api.ui.util;

/*
 * Copyright (c) 2018 The sky Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


/**
 * Created by sky on 2018/8/8.
 */
public class LayoutUtil {

    private LayoutUtil() {

    }

    public static LinearLayout.LayoutParams newLinearLayoutParams(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    public static LinearLayout.LayoutParams newMatchLinearLayoutParams() {
        return newLinearLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    public static LinearLayout.LayoutParams newWrapLinearLayoutParams() {
        return newLinearLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public static ViewGroup.LayoutParams newViewGroupParams(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    public static ViewGroup.LayoutParams newMatchViewGroupParams() {
        return newViewGroupParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public static ViewGroup.LayoutParams newWrapViewGroupParams() {
        return newViewGroupParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static FrameLayout.LayoutParams newFrameLayoutParams(int width, int height) {
        return new FrameLayout.LayoutParams(width, height);
    }

    public static FrameLayout.LayoutParams newMatchFrameLayoutParams() {
        return newFrameLayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    }

    public static FrameLayout.LayoutParams newWrapFrameLayoutParams() {
        return newFrameLayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    public static LinearLayout newCommonLayout(Context context) {

        int left = DisplayUtil.sp2px(context, 25);
        int width = (int) DisplayUtil.getWidthPixels(context) - (left << 1);
        LinearLayout.LayoutParams params = LayoutUtil
                .newLinearLayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout content = new LinearLayout(context);
        content.setLayoutParams(params);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(Color.WHITE);

        return content;
    }
}
