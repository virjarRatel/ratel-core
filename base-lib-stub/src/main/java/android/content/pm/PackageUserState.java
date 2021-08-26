/*
 * Copyright (C) 2012 The Android Open Source Project
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

package android.content.pm;

import android.util.ArraySet;

/**
 * Per-user state information about a package.
 * @hide
 */
public class PackageUserState {
    public long ceDataInode;
    public boolean installed;
    public boolean stopped;
    public boolean notLaunched;
    public boolean hidden; // Is the app restricted by owner / admin
    public boolean suspended;
    public boolean instantApp;
    public int enabled;
    public String lastDisableAppCaller;
    public int domainVerificationStatus;
    public int appLinkGeneration;
    public int categoryHint = -1;
    public int installReason;

    public ArraySet<String> disabledComponents;
    public ArraySet<String> enabledComponents;

    public String[] overlayPaths;

    public PackageUserState() {
        throw new UnsupportedOperationException("STUB");
    }

    public PackageUserState(PackageUserState o) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Test if this package is installed.
     */
    public boolean isAvailable(int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Test if the given component is considered installed, enabled and a match
     * for the given flags.
     *
     * <p>
     * Expects at least one of {@link PackageManager#MATCH_DIRECT_BOOT_AWARE} and
     * {@link PackageManager#MATCH_DIRECT_BOOT_UNAWARE} are specified in {@code flags}.
     * </p>
     */
    public boolean isMatch(ComponentInfo componentInfo, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Test if the given component is considered enabled.
     */
    public boolean isEnabled(ComponentInfo componentInfo, int flags) {
        throw new UnsupportedOperationException("STUB");
    }
}
