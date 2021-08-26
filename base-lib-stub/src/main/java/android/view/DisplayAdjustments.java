/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view;

import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;

import java.util.Objects;

public class DisplayAdjustments {
    public static final DisplayAdjustments DEFAULT_DISPLAY_ADJUSTMENTS = new DisplayAdjustments();

    public DisplayAdjustments() {
        throw new UnsupportedOperationException("STUB");
    }

    public DisplayAdjustments(Configuration configuration) {
        throw new UnsupportedOperationException("STUB");
    }

    public DisplayAdjustments(DisplayAdjustments daj) {
        throw new UnsupportedOperationException("STUB");
    }

    public void setCompatibilityInfo(CompatibilityInfo compatInfo) {
        throw new UnsupportedOperationException("STUB");
    }

    public CompatibilityInfo getCompatibilityInfo() {
        throw new UnsupportedOperationException("STUB");
    }

    public void setConfiguration(Configuration configuration) {
        throw new UnsupportedOperationException("STUB");
    }

    public Configuration getConfiguration() {
        throw new UnsupportedOperationException("STUB");
    }

}
