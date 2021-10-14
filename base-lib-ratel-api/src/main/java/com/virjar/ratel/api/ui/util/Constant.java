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


/**
 * Created by sky on 2018/8/8.
 */
public interface Constant {

    interface Action {

        String REFRESH_PREFERENCE = "com.virjar.ratel.api.ui.ACTION_REFRESH_PREFERENCE";
    }

    interface Key {

        String DATA = "data";
    }

    interface InputType {

        int NUMBER = 0;

        int NUMBER_SIGNED = 1;

        int NUMBER_DECIMAL = 2;

        int TEXT = 3;

        int PHONE = 4;

        int TEXT_PASSWORD = 5;

        int NUMBER_PASSWORD = 6;
    }

    interface Color {

        int DEFAULT_PRESSED = 0XFFE5E5E5;

        int DEFAULT_BACKGROUND = 0xFFFFFFFF;
    }
}
