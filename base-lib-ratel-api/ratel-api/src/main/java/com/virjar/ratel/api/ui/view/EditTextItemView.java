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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.virjar.ratel.api.ui.interfaces.TrackViewStatus;
import com.virjar.ratel.api.ui.util.Constant;
import com.virjar.ratel.api.ui.util.DisplayUtil;
import com.virjar.ratel.api.ui.util.LayoutUtil;
import com.virjar.ratel.api.ui.util.ViewUtil;


/**
 * Created by sky on 2018/8/8.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class EditTextItemView extends FrameLayout
        implements View.OnClickListener, TrackViewStatus<String> {

    private TextView tvName;
    private TextView tvExtend;
    private String mUnit;
    private int mMaxLength;
    private int mInputType = Constant.InputType.TEXT;
    private OnTextChangeListener mOnTextChangeListener;

    public EditTextItemView(Context context) {
        this(context, null);
    }

    public EditTextItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {

        int left = DisplayUtil.dip2px(getContext(), 15);

        setPadding(left, 0, left, 0);
        setBackground(ViewUtil.newBackgroundDrawable());
        setLayoutParams(LayoutUtil.newViewGroupParams(
                ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(getContext(), 40)));

        tvName = new TextView(getContext());
        tvName.setTextColor(Color.BLACK);
        tvName.setTextSize(15);

        tvExtend = new TextView(getContext());
        tvExtend.setTextColor(Color.GRAY);
        tvExtend.setGravity(Gravity.RIGHT);
        tvExtend.setTextSize(15);

        LayoutParams params = LayoutUtil.newWrapFrameLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL;

        addView(tvName, params);

        params = LayoutUtil.newWrapFrameLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;

        addView(tvExtend, params);

        setOnClickListener(this);
    }

    public OnTextChangeListener getOnTextChangeListener() {
        return mOnTextChangeListener;
    }

    public void setOnTextChangeListener(OnTextChangeListener onTextChangeListener) {
        mOnTextChangeListener = onTextChangeListener;
    }

    public void setName(String title) {
        tvName.setText(title);
    }

    public String getName() {
        return tvName.getText().toString();
    }

    public void setExtend(String value) {

        if ((Constant.InputType.NUMBER_PASSWORD == mInputType
                || Constant.InputType.TEXT_PASSWORD == mInputType)
                && !TextUtils.isEmpty(value)) {
            tvExtend.setText("******");
            return;
        }

        if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(mUnit)) {
            tvExtend.setText(value);
            tvExtend.append(mUnit);
            return;
        }

        if (mMaxLength != 0 && value.length() > mMaxLength) {
            tvExtend.setText(value.substring(0, mMaxLength));
            tvExtend.append("...");
            return;
        }

        tvExtend.setText(value);
    }

    public void setExtendHint(String value) {
        tvExtend.setHint(value);
    }

    public String getUnit() {
        return mUnit;
    }

    public void setUnit(String unit) {
        mUnit = unit;
    }

    public int getMaxLength() {
        return mMaxLength;
    }

    public void setMaxLength(int maxLength) {
        mMaxLength = maxLength;
    }

    public int getInputType() {
        return mInputType;
    }

    public void setInputType(int inputType) {
        mInputType = inputType;
    }

    public String getExtend() {
        return tvExtend.getText().toString();
    }

    @Override
    public void onClick(View v) {

        int top = DisplayUtil.dip2px(getContext(), 10);
        int left = DisplayUtil.dip2px(getContext(), 24);

        FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setLayoutParams(LayoutUtil.newFrameLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        frameLayout.setPadding(left, top, left, top);

        final EditText editText = new EditText(getContext());
        editText.setText(mOnTextChangeListener.getDefaultText());
        editText.setLayoutParams(LayoutUtil.newViewGroupParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ViewUtil.setInputType(editText, mInputType);
        frameLayout.addView(editText);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(getContext());
        builder.setTitle(getName());
        builder.setView(frameLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 返回文本的内容
                mOnTextChangeListener.onTextChanged(editText, editText.getText().toString());
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    public String bind(final SharedPreferences preferences,
                       final String key, final String defValue, final StatusChangeListener<String> listener) {

        // 获取状态信息
        String value = preferences.getString(key, defValue);

        // 设置显示信息
        setExtend(value);
        setOnTextChangeListener(new OnTextChangeListener() {
            @Override
            public String getDefaultText() {
                // 获取文本信息
                return preferences.getString(key, defValue);
            }

            @Override
            public void onTextChanged(View view, String text) {

                if (listener.onStatusChange(view, key, text)) {
                    // 保存信息
                    setExtend(text);
                    preferences.edit().putString(key, text).apply();
                }
            }
        });
        return value;
    }

    interface OnTextChangeListener {

        String getDefaultText();

        void onTextChanged(View view, String text);
    }
}
