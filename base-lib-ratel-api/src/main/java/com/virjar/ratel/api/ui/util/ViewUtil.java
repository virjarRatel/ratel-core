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


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.virjar.ratel.api.SDK_VERSION_CODES;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.api.ui.view.SimpleItemView;
import com.virjar.ratel.api.ui.view.SpinnerItemView;
import com.virjar.ratel.api.ui.view.SwitchItemView;


/**
 * Created by sky on 2018/8/8.
 */
public class ViewUtil {

    private ViewUtil() {

    }

    public static void setVisibility(View view, int visibility) {

        if (view == null || view.getVisibility() == visibility) return;

        view.setVisibility(visibility);
    }

    public static void setVisibility(int visibility, View... views) {

        if (views == null) return;

        for (View view : views) {
            setVisibility(view, visibility);
        }
    }

    public static void setTypeface(Typeface typeface, TextView... textViews) {

        if (typeface == null || textViews == null) return;

        for (TextView textView : textViews) {
            textView.setTypeface(typeface);
        }
    }

    public static String getText(TextView textView) {
        return textView != null ? charSequenceToString(textView.getText()) : null;
    }

    public static String charSequenceToString(CharSequence charSequence) {
        return charSequence != null ? charSequence.toString() : null;
    }

    /**
     * EditText竖直方向是否可以滚动
     *
     * @param editText 需要判断的EditText
     * @return true：可以滚动   false：不可以滚动
     */
    public static boolean canVerticalScroll(EditText editText) {

        if (editText == null) return false;

        //滚动的距离
        int scrollY = editText.getScrollY();
        //控件内容的总高度
        int scrollRange = editText.getLayout().getHeight();
        //控件实际显示的高度
        int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() - editText.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        int scrollDifference = scrollRange - scrollExtent;

        if (scrollDifference == 0) {
            return false;
        }

        return (scrollY > 0) || (scrollY < scrollDifference - 1);
    }

    /**
     * 默认点击背景色
     *
     * @return
     */
    public static Drawable newBackgroundDrawable() {

        if (Build.VERSION.SDK_INT >= SDK_VERSION_CODES.LOLLIPOP) {

            int[][] states = new int[2][];
            states[0] = new int[]{android.R.attr.state_pressed};
            states[1] = new int[]{};

            int[] colors = new int[]{Constant.Color.DEFAULT_PRESSED, Constant.Color.DEFAULT_PRESSED};

//            return new RippleDrawable(new ColorStateList(states, colors),
//                    new ColorDrawable(Color.WHITE), new ColorDrawable(Color.WHITE));
            return (Drawable) RposedHelpers.newInstance(
                    RposedHelpers.findClass("android.graphics.drawable.RippleDrawable.RippleDrawable", ViewUtil.class.getClassLoader()),
                    new ColorStateList(states, colors),
                    new ColorDrawable(Color.WHITE), new ColorDrawable(Color.WHITE)
            );
        }

        StateListDrawable drawable = new StateListDrawable();

        drawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Constant.Color.DEFAULT_PRESSED));
        drawable.addState(new int[]{}, new ColorDrawable(Color.WHITE));

        return drawable;
    }

    /**
     * 默认的标题背景色
     *
     * @return
     */
    public static StateListDrawable newTitleBackgroundDrawable() {

        StateListDrawable drawable = new StateListDrawable();

        drawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(0x66666666));
        drawable.addState(new int[]{}, new ColorDrawable(0x00000000));

        return drawable;
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public static void setInputType(EditText editText, int inputType) {

        switch (inputType) {
            case Constant.InputType.NUMBER:
                editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                break;
            case Constant.InputType.NUMBER_DECIMAL:
                editText.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL | EditorInfo.TYPE_CLASS_NUMBER);
                break;
            case Constant.InputType.NUMBER_SIGNED:
                editText.setInputType(EditorInfo.TYPE_NUMBER_FLAG_SIGNED | EditorInfo.TYPE_CLASS_NUMBER);
                break;
            case Constant.InputType.PHONE:
                editText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
                break;
            case Constant.InputType.TEXT:
                editText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
                break;
            case Constant.InputType.TEXT_PASSWORD:
                editText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                break;
            case Constant.InputType.NUMBER_PASSWORD:
                editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD);
                break;
        }
    }

    public static View newLineView(Context context) {

        View lineView = new View(context);
        lineView.setBackgroundColor(0xFFDFDFDF);
        lineView.setLayoutParams(LayoutUtil.newViewGroupParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 2));
        return lineView;
    }

    public static SimpleItemView newSimpleItemView(Context context, String name) {

        SimpleItemView itemView = new SimpleItemView(context);
        itemView.setName(name);

        return itemView;
    }

    public static SwitchItemView newSwitchItemView(Context context, String name) {
        return newSwitchItemView(context, name, "");
    }

    public static SwitchItemView newSwitchItemView(Context context, String name, String desc) {

        SwitchItemView itemView = new SwitchItemView(context);
        itemView.setName(name);
        itemView.setDesc(desc);

        return itemView;
    }

    public static SpinnerItemView newSpinnerItemView(Context context, String name, String desc, String... items) {

        SpinnerItemView itemView = new SpinnerItemView(context);
        itemView.setName(name);
        itemView.setDesc(desc);
        itemView.setChooseItem(items);

        return itemView;
    }
}
