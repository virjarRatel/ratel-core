/*
 * Copyright (C) 2006 The Android Open Source Project
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

package android.content.res;

import android.content.pm.ApplicationInfo;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;

/**
 * CompatibilityInfo class keeps the information about compatibility mode that the application is
 * running under.
 * 
 *  {@hide} 
 */
public class CompatibilityInfo implements Parcelable {
    /** default compatibility info object for compatible applications */
    public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = new CompatibilityInfo() {
    };

    /**
     * This is the number of pixels we would like to have along the
     * short axis of an app that needs to run on a normal size screen.
     */
    public static final int DEFAULT_NORMAL_SHORT_DIMENSION = 320;

    /**
     * This is the maximum aspect ratio we will allow while keeping
     * applications in a compatible screen size.
     */
    public static final float MAXIMUM_ASPECT_RATIO = (854f/480f);

//    /**
//     *  A compatibility flags
//     */
//    private final int mCompatibilityFlags;

    /**
     * A flag mask to tell if the application needs scaling (when mApplicationScale != 1.0f)
     * {@see compatibilityFlag}
     */
    private static final int SCALING_REQUIRED = 1; 

    /**
     * Application must always run in compatibility mode?
     */
    private static final int ALWAYS_NEEDS_COMPAT = 2;

    /**
     * Application never should run in compatibility mode?
     */
    private static final int NEVER_NEEDS_COMPAT = 4;

    /**
     * Set if the application needs to run in screen size compatibility mode.
     */
    private static final int NEEDS_SCREEN_COMPAT = 8;

    /**
     * The effective screen density we have selected for this application.
     */
    public final int applicationDensity = 0;//Modify
    
    /**
     * Application's scale.
     */
    public final float applicationScale = 0;//Modify

    /**
     * Application's inverted scale.
     */
    public final float applicationInvertedScale = 0;//Modify

    public CompatibilityInfo(ApplicationInfo appInfo, int screenLayout, int sw,
            boolean forceCompat) {
        throw new UnsupportedOperationException("STUB");
    }

    private CompatibilityInfo(int compFlags,
            int dens, float scale, float invertedScale) {
        throw new UnsupportedOperationException("STUB");
    }

    private CompatibilityInfo() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @return true if the scaling is required
     */
    public boolean isScalingRequired() {
        throw new UnsupportedOperationException("STUB");
    }
    
    public boolean supportsScreen() {
        throw new UnsupportedOperationException("STUB");
    }
    
    public boolean neverSupportsScreen() {
        throw new UnsupportedOperationException("STUB");
    }

    public boolean alwaysSupportsScreen() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Returns the translator which translates the coordinates in compatibility mode.
     * @param params the window's parameter
     */
    public Translator getTranslator() {
        return isScalingRequired() ? new Translator() : null;
    }

    /**
     * A helper object to translate the screen and window coordinates back and forth.
     * @hide
     */
    public class Translator {
        final public float applicationScale;
        final public float applicationInvertedScale;
        
        private Rect mContentInsetsBuffer = null;
        private Rect mVisibleInsetsBuffer = null;
        private Region mTouchableAreaBuffer = null;
        
        Translator(float applicationScale, float applicationInvertedScale) {
            this.applicationScale = applicationScale;
            this.applicationInvertedScale = applicationInvertedScale;
        }

        Translator() {
            this(CompatibilityInfo.this.applicationScale,
                    CompatibilityInfo.this.applicationInvertedScale);
        }

        /**
         * Translate the screen rect to the application frame.
         */
        public void translateRectInScreenToAppWinFrame(Rect rect) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Translate the region in window to screen. 
         */
        public void translateRegionInWindowToScreen(Region transparentRegion) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Apply translation to the canvas that is necessary to draw the content.
         */
        public void translateCanvas(Canvas canvas) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Translate the motion event captured on screen to the application's window.
         */
        public void translateEventInScreenToAppWindow(MotionEvent event) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Translate the window's layout parameter, from application's view to
         * Screen's view.
         */
        public void translateWindowLayout(LayoutParams params) {
            throw new UnsupportedOperationException("STUB");
        }
        
        /**
         * Translate a Rect in application's window to screen.
         */
        public void translateRectInAppWindowToScreen(Rect rect) {
            throw new UnsupportedOperationException("STUB");
        }
 
        /**
         * Translate a Rect in screen coordinates into the app window's coordinates.
         */
        public void translateRectInScreenToAppWindow(Rect rect) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Translate a Point in screen coordinates into the app window's coordinates.
         */
        public void translatePointInScreenToAppWindow(PointF point) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Translate the location of the sub window.
         * @param params
         */
        public void translateLayoutParamsInAppWindowToScreen(LayoutParams params) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Translate the content insets in application window to Screen. This uses
         * the internal buffer for content insets to avoid extra object allocation.
         */
        public Rect getTranslatedContentInsets(Rect contentInsets) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Translate the visible insets in application window to Screen. This uses
         * the internal buffer for visible insets to avoid extra object allocation.
         */
        public Rect getTranslatedVisibleInsets(Rect visibleInsets) {
            throw new UnsupportedOperationException("STUB");
        }

        /**
         * Translate the touchable area in application window to Screen. This uses
         * the internal buffer for touchable area to avoid extra object allocation.
         */
        public Region getTranslatedTouchableArea(Region touchableArea) {
            throw new UnsupportedOperationException("STUB");
        }
    }

    public void applyToDisplayMetrics(DisplayMetrics inoutDm) {
        throw new UnsupportedOperationException("STUB");
    }

    public void applyToConfiguration(int displayDensity, Configuration inoutConfig) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Compute the frame Rect for applications runs under compatibility mode.
     *
     * @param dm the display metrics used to compute the frame size.
     * @param outDm If non-null the width and height will be set to their scaled values.
     * @return Returns the scaling factor for the window.
     */
    public static float computeCompatibleScaling(DisplayMetrics dm, DisplayMetrics outDm) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public static final Creator<CompatibilityInfo> CREATOR
            = new Creator<CompatibilityInfo>() {
        @Override
        public CompatibilityInfo createFromParcel(Parcel source) {
            throw new UnsupportedOperationException("STUB");
        }

        @Override
        public CompatibilityInfo[] newArray(int size) {
            throw new UnsupportedOperationException("STUB");
        }
    };

}
