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

package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * An identity that uniquely identifies a particular device. In this
 * implementation, the identity is represented as a 64-bit integer encoded to a
 * 13-character string using RFC 4648's Base32 encoding without the trailing
 * padding. This makes it easy for users to read and write the code without
 * confusing 'I' (letter) with '1' (one) or 'O' (letter) with '0' (zero).
 *
 * @hide
 */
public class VerifierDeviceIdentity implements Parcelable {
    /**
     * Encoded size of a long (64-bit) into Base32. This format will end up
     * looking like XXXX-XXXX-XXXX-X (length ignores hyphens) when applied with
     * the GROUP_SIZE below.
     */
    private static final int LONG_SIZE = 13;

    /**
     * Size of groupings when outputting as strings. This helps people read it
     * out and keep track of where they are.
     */
    private static final int GROUP_SIZE = 4;

    private final long mIdentity;

    private final String mIdentityString;

    /**
     * Create a verifier device identity from a long.
     *
     * @param identity device identity in a 64-bit integer.
     * @throws
     */
    public VerifierDeviceIdentity(long identity) {
        mIdentity = identity;
        mIdentityString = encodeBase32(identity);
    }

    private VerifierDeviceIdentity(Parcel source) {
        final long identity = source.readLong();

        mIdentity = identity;
        mIdentityString = encodeBase32(identity);
    }

    /**
     * Generate a new device identity.
     *
     * @return random uniformly-distributed device identity
     */
    public static VerifierDeviceIdentity generate() {
        final SecureRandom sr = new SecureRandom();
        return generate(sr);
    }

    /**
     * Generate a new device identity using a provided random number generator
     * class. This is used for testing.
     *
     * @param rng random number generator to retrieve the next long from
     * @return verifier device identity based on the input from the provided
     *         random number generator
     */
    static VerifierDeviceIdentity generate(Random rng) {
        long identity = rng.nextLong();
        return new VerifierDeviceIdentity(identity);
    }

    private static final char ENCODE[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', '2', '3', '4', '5', '6', '7',
    };

    private static final char SEPARATOR = '-';

    private static final String encodeBase32(long input) {
        final char[] alphabet = ENCODE;

        throw new UnsupportedOperationException("STUB");
    }

    // TODO move this out to its own class (android.util.Base32)
    private static final long decodeBase32(byte[] input) throws IllegalArgumentException {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int hashCode() {
        return (int) mIdentity;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof VerifierDeviceIdentity)) {
            return false;
        }

        final VerifierDeviceIdentity o = (VerifierDeviceIdentity) other;
        return mIdentity == o.mIdentity;
    }

    @Override
    public String toString() {
        return mIdentityString;
    }

    public static VerifierDeviceIdentity parse(String deviceIdentity)
            throws IllegalArgumentException {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mIdentity);
    }

    public static final Creator<VerifierDeviceIdentity> CREATOR
            = new Creator<VerifierDeviceIdentity>() {
        public VerifierDeviceIdentity createFromParcel(Parcel source) {
            return new VerifierDeviceIdentity(source);
        }

        public VerifierDeviceIdentity[] newArray(int size) {
            return new VerifierDeviceIdentity[size];
        }
    };
}
