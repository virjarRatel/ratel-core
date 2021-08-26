package com.virjar.ratel.api.ui.interfaces;

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


import android.content.SharedPreferences;
import android.view.View;

/**
 * Created by sky on 2018/8/8.
 */
public interface TrackViewStatus<T> {

    T bind(SharedPreferences preferences, String key, T defValue, StatusChangeListener<T> listener);

    interface StatusChangeListener<T> {

        boolean onStatusChange(View view, String key, T value);
    }
}
