package com.virjar.ratel.api.ui.view;
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


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.virjar.ratel.api.ui.util.DisplayUtil;
import com.virjar.ratel.api.ui.util.LayoutUtil;
import com.virjar.ratel.api.ui.util.ViewUtil;


/**
 * Created by sky on 2018/8/8.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class CommentItemView extends FrameLayout {

    private TextView tvContent;

    public CommentItemView(Context context) {
        this(context, null);
    }

    public CommentItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommentItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackground(ViewUtil.newBackgroundDrawable());
        setLayoutParams(LayoutUtil.newViewGroupParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tvContent = new TextView(context);
        tvContent.setTextColor(Color.BLACK);
        tvContent.setMinLines(2);
        tvContent.setMaxLines(2);
        tvContent.setTextSize(14f);
        tvContent.setGravity(Gravity.CENTER_VERTICAL);
        tvContent.setEllipsize(TextUtils.TruncateAt.END);

        int topMargin = DisplayUtil.dip2px(context, 6f);
        int leftMargin = DisplayUtil.dip2px(context, 15f);

        FrameLayout.LayoutParams params = LayoutUtil.newWrapFrameLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL;
        params.leftMargin = leftMargin;
        params.rightMargin = leftMargin;
        params.topMargin = topMargin;
        params.bottomMargin = topMargin;

        addView(ViewUtil.newLineView(context));
        addView(tvContent, params);
    }

    public TextView getContentView() {
        return tvContent;
    }

    public void setContent(String content) {
        tvContent.setText(content);
    }

    public String getContent() {
        return tvContent.getText().toString();
    }
}
