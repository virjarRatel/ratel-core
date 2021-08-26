/*
 * Copyright (C) 2005-2015 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.schlichtherle.util;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * A utility class used to replace string literals in Java source code with an
 * obfuscated representation of the string.
 * Client applications should use this class to implement the
 * {@link de.schlichtherle.license.LicenseParam},
 * {@link de.schlichtherle.license.KeyStoreParam}
 * and {@link de.schlichtherle.license.CipherParam} interfaces in order to
 * make it considerably hard (although still not impossible) for a reverse
 * engineer to find these string literals while providing comparably fast
 * operation and minimum memory footprint.
 * <p>
 * To use this class you need to provide the string literal to obfuscate as a
 * parameter to the static {@link #obfuscate} method.
 * Its return value is a string which contains the Java code which you should
 * substitute for the string literal in the client application's source code.
 * <p>
 * Please note that obfuscation is <em>not</em> equal to encryption:
 * In contrast to the obfuscation provided by this class, encryption is
 * comparably slow and expensive in terms of resources - no matter what
 * algorithm is actually used.
 * More importantly, encrypting string literals in Java code does not really
 * increase the privacy of these strings compared to obfuscation as long as
 * the encryption key is still placed in the Java code itself and tracing the
 * calls to the JVM is possible.
 * Hence, obfuscation is selected in favour of encryption. 
 * <p>
 * In order to provide a reasonable level of security for your application,
 * you should <em>always</em> obfuscate the <em>application code</em> too,
 * including this class.
 * Otherwise, a reverse engineer could simply use the UNIX "strings" utility
 * to search for all usages of this class, which would render its use
 * completely pointless!
 * In case you're looking for a Java code obfuscation tool for this task,
 * please consider ProGuard, available and usable for free at
 * <a href="http://proguard.sourceforge.net">http://proguard.sourceforge.net</a>.
 * <p>
 * This class is designed to be thread safe.
 *
 * @author  Christian Schlichtherle
 * @version $Id$
 */
public final class ObfuscatedString {

    //
    // Regular string constants:
    // These can't be obfuscated using this class because it would cause an
    // illegal recursion in the class initializer.
    // Still we want these strings to be obfuscated in a minimal way in order
    // to prevent a reverse engineer from locating the obfuscated version
    // of this class (created with e.g. ProGuard) by searching for these
    // strings.
    // This is not a 100% secure protection, but it's supposed to make
    // the life of a reverse engineer a little bit harder.
    //

    private static final String UTF8
            = new String(new char[] { '\u0055', '\u0054', '\u0046', '\u0038' }); // => "UTF8"

    /**
     * Obfuscates each given argument.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++)
            System.out.println(obfuscate(args[i]));
    }

    /**
     * Returns a string containing obfuscated string generating Java code which
     * you can copy-paste into your source code in order to represent the given
     * string.
     * Obfuscation is performed by encoding the given string into UTF8 and then
     * XOR-ing a sequence of pseudo random numbers to it in order to prevent
     * attacks based on character probability.
     * The result is encoded into an array of longs which is embedded in some
     * Java code which would produce the original string again.
     * The sequence of pseudo random numbers is seeded with a 48 bit random
     * number in order to provide a non-deterministic result for the generated
     * code.
     * Hence, two subsequent calls with the same string will produce equal
     * results by a chance of 1/(2<sup>48</sup>-1) (0 isn't used as a seed) only!
     * <p>
     * As an example, calling this method with {@code "Hello world!"} as
     * its parameter may produce the result
     * {@code "new ObfuscatedString(new long[] {
     *     0x3676CB307FBD35FEL, 0xECFB991E2033C169L, 0xD8C3D3E365645589L
     * }).toString()"}.
     * If this code is compiled and executed later, it will produce the string
     * {@code "Hello world!"} again.
     *
     * @param  s The string to obfuscate. This may not contain null characters.
     * @throws IllegalArgumentException If {@code s} contains a null
     *         character.
     * @return Some obfuscated Java code to produce the given string again.
     */
    public static String obfuscate(final String s) {
        // Any string literal used in this method is represented as an
        // ObfuscatedString unless it's no longer than two characters.
        // This should help to prevent location of this class in the obfuscated
        // code generated by ProGuard.

        if (-1 != s.indexOf(0)) {
            throw new IllegalArgumentException(new ObfuscatedString(new long[] {
                0x241005931110FC70L, 0xDCD925A88EAD9F37L, 0x19ADA1C861E2A85DL,
                0x9A5948E700FCAD8AL, 0x2E11C83A72441DE2L
            }).toString()); // => "Null characters are not allowed!";
        }

        // Obtain the string as a sequence of UTF-8 encoded bytes.
        final byte[] encoded;
        try {
            encoded = s.getBytes(UTF8);
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex); // UTF8 is always supported
        }

        // Create and seed a Pseudo Random Number Generator (PRNG) with a
        // random long number.
        final Random prng = new Random(); // randomly seeded
        final long seed = prng.nextLong(); // seed strength is effectively 48 bits
        prng.setSeed(seed);

        // Construct a StringBuffer to hold the generated code and append the
        // seed as the first element of the encoded array of longs.
        // The value is represented in hexadecimal in order to keep the string
        // representation as short as possible.
        final StringBuffer code = new StringBuffer(new ObfuscatedString(new long[] {
            0xA28E32BB0D3E394EL, 0xF842D1C94E549EECL, 0x7D07DFF01F907E4L,
            0x4E0BDE791ECD467CL, 0xDFF389B58DA3E44FL, 0x2477FAED0CE62C79L
        }).toString()); // => "new ObfuscatedString(new long[] {";
        appendHexLiteral(code, seed);

        final int length = encoded.length;
        for (int i = 0; i < length; i += 8) {
            final long key = prng.nextLong();
            // Compute the value of the next array element as an obfuscated
            // version of the next eight bytes of the UTF8 encoded string.
            final long obfuscated = toLong(encoded, i) ^ key;

            code.append(", ");
            appendHexLiteral(code, obfuscated);
        }

        code.append(new ObfuscatedString(new long[] {
            0x4200B7AD6FFFF546L, 0x9B822E95FE73769DL, 0x23C2800C6CACFCE3L,
            0x21C30B492D9AEF99L
        }).toString()); // => "}).toString() /* => \"";

        // Append the original string to the generated code comment,
        // properly escaping quotation marks and backslashes.
        code.append(
                s.replaceAll("\\\\", new ObfuscatedString(new long[] {
                    0x6D2C680D49523A01L, 0xB932F1DBD19E82CEL}).toString() /* => "\\\\\\\\" */)
                .replaceAll("\"", new ObfuscatedString(new long[] {
                    0x85E9D53EF7A9324BL, 0xB05BD65C9F19DE07L}).toString() /* => "\\\\\"" */));

        code.append(new ObfuscatedString(new long[] {
            0xC54FFF0621E7D107L, 0x194EAD468C6FCF93L
        }).toString()); // => "\" */"

        return code.toString();
    }

    private static void appendHexLiteral(final StringBuffer sb, final long l) {
        sb.append('0'); // obfuscation futile - too short
        sb.append('x'); // dito
        sb.append(Long.toHexString(l).toUpperCase());
        sb.append('L'); // dito
    }

    /**
     * Decodes a long value from eight bytes in little endian order,
     * beginning at index {@code off}.
     * This is the inverse of {@link #toBytes(long, byte[], int)}.
     * If less than eight bytes are remaining in the array,
     * only these low order bytes are processed and the complementary high
     * order bytes of the returned value are set to zero.
     *
     * @param bytes The array containing the bytes to decode in little endian
     *        order.
     * @param off The offset of the bytes in the array.
     *
     * @return The decoded long value.
     */
    private static long toLong(final byte[] bytes, int off) {
        final int end = Math.min(bytes.length, off + 8);
        long l = 0;
        for (int i = end; --i >= off; ) {
            l <<= 8;
            l |= bytes[i] & 0xFF;
        }
        return l;
    }

    /**
     * Encodes a long value to eight bytes in little endian order,
     * beginning at index {@code off}.
     * This is the inverse of {@link #toLong(byte[], int)}.
     * If less than eight bytes are remaining in the array,
     * only these low order bytes of the long value are processed and the
     * complementary high order bytes are ignored.
     *
     * @param l The long value to encode.
     * @param bytes The array which holds the encoded bytes upon return.
     * @param off The offset of the bytes in the array.
     */
    private static void toBytes(long l, byte[] bytes, int off) {
        final int end = Math.min(bytes.length, off + 8);
        for (int i = off; i < end; i++) {
            bytes[i] = (byte) l;
            l >>= 8;
        }
    }

    /** The obfuscated string. */
    private final long[] obfuscated;

    /**
     * Constructs an obfuscated string.
     *
     * @param obfuscated The obfuscated string.
     * @throws NullPointerException If {@code obfuscated} is
     *         {@code null}.
     * @throws ArrayIndexOutOfBoundsException If the provided array does not
     *         contain at least one element.
     * @see    #obfuscate(String)
     */
    public ObfuscatedString(final long[] obfuscated) {
        this.obfuscated = (long[]) obfuscated.clone();
        this.obfuscated[0] = obfuscated[0];
    }

    /** Returns the original string. */
    public String toString() {
        final int length = obfuscated.length;

        // The original UTF8 encoded string was probably not a multiple
        // of eight bytes long and is thus actually shorter than this array.
        final byte[] encoded = new byte[8 * (length - 1)];

        // Obtain the seed and initialize a new PRNG with it.
        final long seed = obfuscated[0];
        final Random prng = new Random(seed);

        // De-obfuscate.
        for (int i = 1; i < length; i++) {
            final long key = prng.nextLong();
            toBytes(obfuscated[i] ^ key, encoded, 8 * (i - 1));
        }

        // Decode the UTF-8 encoded byte array into a string.
        // This will create null characters at the end of the decoded string
        // in case the original UTF8 encoded string was not a multiple of
        // eight bytes long.
        final String decoded;
        try {
            decoded = new String(encoded, UTF8);
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex); // UTF-8 is always supported
        }

        // Cut off trailing null characters in case the original UTF8 encoded
        // string was not a multiple of eight bytes long.
        final int i = decoded.indexOf(0);
        return -1 == i ? decoded : decoded.substring(0, i);
    }
}
