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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.virjar.ratel.api.SDK_VERSION_CODES;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.api.ui.interfaces.TrackViewStatus;
import com.virjar.ratel.api.ui.util.DisplayUtil;
import com.virjar.ratel.api.ui.util.LayoutUtil;
import com.virjar.ratel.api.ui.util.ViewUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sky on 2018/8/20.
 */
@TargetApi(SDK_VERSION_CODES.KITKAT)
public class SpinnerItemView extends FrameLayout implements View.OnClickListener, TrackViewStatus<String> {

    private TextView tvName;
    private TextView tvDesc;
    private TextView tvValue;
    private OnValueChangeListener mOnValueChangeListener;
    private List<String> mDisplayItems;

    public SpinnerItemView(Context context) {
        this(context, null);
    }

    public SpinnerItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpinnerItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public OnValueChangeListener getOnValueChangeListener() {
        return mOnValueChangeListener;
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        mOnValueChangeListener = onValueChangeListener;
    }

    private void initView() {

        int left = DisplayUtil.dip2px(getContext(), 15);

        setPadding(left, 0, left, 0);
        setBackground(ViewUtil.newBackgroundDrawable());
        setLayoutParams(LayoutUtil.newViewGroupParams(
                LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(getContext(), 40)));

        LinearLayout tvLayout = new LinearLayout(getContext());
        tvLayout.setOrientation(LinearLayout.VERTICAL);

        tvName = new TextView(getContext());
        tvName.setTextColor(Color.BLACK);
        tvName.setTextSize(15);

        tvDesc = new TextView(getContext());
        tvDesc.setTextColor(Color.GRAY);
        tvDesc.setTextSize(9);
        tvDesc.setPadding(DisplayUtil.dip2px(getContext(), 1), 0, 0, 0);

        tvLayout.addView(tvName);
        tvLayout.addView(tvDesc);

        FrameLayout.LayoutParams params = LayoutUtil.newWrapFrameLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL;

        addView(tvLayout, params);

        // 扩展的Layout
        LinearLayout extendLayout = new LinearLayout(getContext());
        extendLayout.setGravity(Gravity.CENTER_VERTICAL);
        extendLayout.setOrientation(LinearLayout.HORIZONTAL);

        tvValue = new TextView(getContext());
        tvValue.setTextColor(Color.BLACK);
        tvValue.setTextSize(14);

        TextView tvSymbol = new TextView(getContext());
        tvSymbol.setTextColor(Color.BLACK);
        tvSymbol.setPadding(DisplayUtil.dip2px(getContext(), 8), 0, DisplayUtil.dip2px(getContext(), 4), 0);
        tvSymbol.setTextSize(20);
        tvSymbol.setText("▾");

        extendLayout.addView(tvValue);
        extendLayout.addView(tvSymbol);

        params = LayoutUtil.newFrameLayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;

        addView(extendLayout, params);

        setOnClickListener(this);
    }

    public void setChooseItem(String... items) {

        if (items == null) return;

        setChooseItem(Arrays.asList(items));
    }

    public void setChooseItem(List<String> displayItems) {
        mDisplayItems = displayItems;
    }

    public void setName(String title) {
        tvName.setText(title);
    }

    public String getName() {
        return tvName.getText().toString();
    }

    public void setDesc(String desc) {
        tvDesc.setText(desc);
        ViewUtil.setVisibility(tvDesc,
                TextUtils.isEmpty(desc) ? View.GONE : View.VISIBLE);
    }

    public String getDesc() {
        return tvDesc.getText().toString();
    }

    public void setValue(String value) {
        tvValue.setText(value);
    }

    public String getValue() {
        return tvValue.getText().toString();
    }

    @Override
    public void onClick(View v) {

        if (mDisplayItems == null) return;

        final PopupMenu popupMenu = (PopupMenu) RposedHelpers.newInstance(PopupMenu.class, getContext().getApplicationContext(), v, Gravity.RIGHT);
        Menu menu = popupMenu.getMenu();

        for (int i = 0; i < mDisplayItems.size(); i++) {
            // 添加到菜单中
            menu.add(1, i + 1, 1, mDisplayItems.get(i));
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (mOnValueChangeListener != null) {
                    String item1 = mDisplayItems.get(item.getItemId() - 1);
                    mOnValueChangeListener.onValueChanged(SpinnerItemView.this, item1);
                }
                return false;
            }
        });

        popupMenu.show();
    }

    @Override
    public String bind(final SharedPreferences preferences,
                       final String key, String defValue, final TrackViewStatus.StatusChangeListener<String> listener) {

        // 获取信息
        String value = preferences.getString(key, defValue);
        setValue(value);

        setOnValueChangeListener(new OnValueChangeListener() {

            @Override
            public void onValueChanged(View view, String item) {

                if (listener.onStatusChange(view, key, item)) {
                    // 保存信息
                    setValue(item);
                    preferences.edit().putString(key, item).apply();
                }
            }
        });
        return value;
    }

    public interface OnValueChangeListener {

        void onValueChanged(View view, String value);
    }
}
