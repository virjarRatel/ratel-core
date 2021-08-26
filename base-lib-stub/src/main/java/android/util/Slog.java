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

package android.util;

import android.annotation.UnsupportedAppUsage;

/**
 * @hide
 */
public final class Slog {

    private Slog() {
    }

    @UnsupportedAppUsage
    public static int v(String tag, String msg) {
        throw new UnsupportedOperationException("STUB");
    }

    public static int v(String tag, String msg, Throwable tr) {
        throw new UnsupportedOperationException("STUB");
    }

    @UnsupportedAppUsage
    public static int d(String tag, String msg) {
        throw new UnsupportedOperationException("STUB");
    }

    @UnsupportedAppUsage
    public static int d(String tag, String msg, Throwable tr) {
        throw new UnsupportedOperationException("STUB");
    }

    @UnsupportedAppUsage
    public static int i(String tag, String msg) {
        throw new UnsupportedOperationException("STUB");
    }

    public static int i(String tag, String msg, Throwable tr) {
        throw new UnsupportedOperationException("STUB");
    }

    @UnsupportedAppUsage
    public static int w(String tag, String msg) {
        throw new UnsupportedOperationException("STUB");
    }

    @UnsupportedAppUsage
    public static int w(String tag, String msg, Throwable tr) {
        throw new UnsupportedOperationException("STUB");
    }

    public static int w(String tag, Throwable tr) {
        throw new UnsupportedOperationException("STUB");
    }

    @UnsupportedAppUsage
    public static int e(String tag, String msg) {
        throw new UnsupportedOperationException("STUB");
    }

    @UnsupportedAppUsage
    public static int e(String tag, String msg, Throwable tr) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Like {@link Log#wtf(String, String)}, but will never cause the caller to crash, and
     * will always be handled asynchronously.  Primarily for use by coding running within
     * the system process.
     */
    @UnsupportedAppUsage
    public static int wtf(String tag, String msg) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Like {@link #wtf(String, String)}, but does not output anything to the log.
     */
    public static void wtfQuiet(String tag, String msg) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Like {@link Log#wtfStack(String, String)}, but will never cause the caller to crash, and
     * will always be handled asynchronously.  Primarily for use by coding running within
     * the system process.
     */

    public static int wtfStack(String tag, String msg) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Like {@link Log#wtf(String, Throwable)}, but will never cause the caller to crash,
     * and will always be handled asynchronously.  Primarily for use by coding running within
     * the system process.
     */
    public static int wtf(String tag, Throwable tr) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Like {@link Log#wtf(String, String, Throwable)}, but will never cause the caller to crash,
     * and will always be handled asynchronously.  Primarily for use by coding running within
     * the system process.
     */
    @UnsupportedAppUsage
    public static int wtf(String tag, String msg, Throwable tr) {
        throw new UnsupportedOperationException("STUB");
    }

    @UnsupportedAppUsage
    public static int println(int priority, String tag, String msg) {
        throw new UnsupportedOperationException("STUB");
    }
}

