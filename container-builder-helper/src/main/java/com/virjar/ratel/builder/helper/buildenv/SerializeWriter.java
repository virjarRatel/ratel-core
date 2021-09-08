package com.virjar.ratel.builder.helper.buildenv;


/**
 * @author wenshao[szujobs@hotmail.com]
 */
public final class SerializeWriter {

    /**
     * The buffer where data is stored.
     */
    protected char[] buf;

    protected int count;

    private final static ThreadLocal<char[]> bufLocal = new ThreadLocal<>();


    public SerializeWriter() {


        buf = bufLocal.get();

        if (bufLocal != null) {
            bufLocal.set(null);
        }

        if (buf == null) {
            buf = new char[1024];
        }
    }


    /**
     * Writes a character to the buffer.
     */
    public void write(int c) {
        int newcount = count + 1;
        if (newcount > buf.length) {

            expandCapacity(newcount);

        }
        buf[count] = (char) c;
        count = newcount;
    }


    protected void expandCapacity(int minimumCapacity) {
        int newCapacity = (buf.length * 3) / 2 + 1;

        if (newCapacity < minimumCapacity) {
            newCapacity = minimumCapacity;
        }
        char newValue[] = new char[newCapacity];
        System.arraycopy(buf, 0, newValue, 0, count);
        buf = newValue;
    }

    /**
     * Write a portion of a string to the buffer.
     *
     * @param str String to be written from
     * @param off Offset from which to start reading characters
     * @param len Number of characters to be written
     */
    public void write(String str, int off, int len) {
        int newcount = count + len;
        if (newcount > buf.length) {
            expandCapacity(newcount);
        }
        str.getChars(off, off + len, buf, count);
        count = newcount;
    }


    public String toString() {
        return new String(buf, 0, count);
    }


    public void write(String text) {
        if (text == null) {
            writeNull();
            return;
        }

        write(text, 0, text.length());
    }


    public void writeNull() {
        write("null");
    }

    protected void writeStringWithDoubleQuote(String text, final char seperator, boolean checkSpecial) {
        if (text == null) {
            writeNull();
            if (seperator != 0) {
                write(seperator);
            }
            return;
        }

        int len = text.length();
        int newcount = count + len + 2;
        if (seperator != 0) {
            newcount++;
        }

        if (newcount > buf.length) {
            expandCapacity(newcount);
        }

        int start = count + 1;
        int end = start + len;

        buf[count] = '\"';
        text.getChars(0, len, buf, start);

        count = newcount;

        int specialCount = 0;
        int lastSpecialIndex = -1;
        int firstSpecialIndex = -1;
        char lastSpecial = '\0';
        if (checkSpecial) {
            for (int i = start; i < end; ++i) {
                char ch = buf[i];

                if (ch == '\u2028') {
                    specialCount++;
                    lastSpecialIndex = i;
                    lastSpecial = ch;
                    newcount += 4;

                    if (firstSpecialIndex == -1) {
                        firstSpecialIndex = i;
                    }
                    continue;
                }

                if (ch >= ']') {
                    if (ch >= 0x7F && ch < 0xA0) {
                        if (firstSpecialIndex == -1) {
                            firstSpecialIndex = i;
                        }

                        specialCount++;
                        lastSpecialIndex = i;
                        lastSpecial = ch;
                        newcount += 4;
                    }
                    continue;
                }

                final boolean isSpecial;
                {
                    if (ch == ' ') {
                        isSpecial = false;
                    } else if (ch == '/') {
                        isSpecial = true;
                    } else if (ch > '#' && ch != '\\') {
                        isSpecial = false;
                    } else if (ch <= 0x1F || ch == '\\' || ch == '"') {
                        isSpecial = true;
                    } else {
                        isSpecial = false;
                    }
                }

                if (isSpecial) {
                    specialCount++;
                    lastSpecialIndex = i;
                    lastSpecial = ch;

                    if (ch < specicalFlags_doubleQuotes.length //
                            && specicalFlags_doubleQuotes[ch] == 4 //
                    ) {
                        newcount += 4;
                    }

                    if (firstSpecialIndex == -1) {
                        firstSpecialIndex = i;
                    }
                }
            }

            if (specialCount > 0) {
                newcount += specialCount;
                if (newcount > buf.length) {
                    expandCapacity(newcount);
                }
                count = newcount;

                if (specialCount == 1) {
                    if (lastSpecial == '\u2028') {
                        int srcPos = lastSpecialIndex + 1;
                        int destPos = lastSpecialIndex + 6;
                        int LengthOfCopy = end - lastSpecialIndex - 1;
                        System.arraycopy(buf, srcPos, buf, destPos, LengthOfCopy);
                        buf[lastSpecialIndex] = '\\';
                        buf[++lastSpecialIndex] = 'u';
                        buf[++lastSpecialIndex] = '2';
                        buf[++lastSpecialIndex] = '0';
                        buf[++lastSpecialIndex] = '2';
                        buf[++lastSpecialIndex] = '8';
                    } else {
                        final char ch = lastSpecial;
                        if (ch < specicalFlags_doubleQuotes.length //
                                && specicalFlags_doubleQuotes[ch] == 4) {
                            int srcPos = lastSpecialIndex + 1;
                            int destPos = lastSpecialIndex + 6;
                            int LengthOfCopy = end - lastSpecialIndex - 1;
                            System.arraycopy(buf, srcPos, buf, destPos, LengthOfCopy);

                            int bufIndex = lastSpecialIndex;
                            buf[bufIndex++] = '\\';
                            buf[bufIndex++] = 'u';
                            buf[bufIndex++] = DIGITS[(ch >>> 12) & 15];
                            buf[bufIndex++] = DIGITS[(ch >>> 8) & 15];
                            buf[bufIndex++] = DIGITS[(ch >>> 4) & 15];
                            buf[bufIndex++] = DIGITS[ch & 15];
                        } else {
                            int srcPos = lastSpecialIndex + 1;
                            int destPos = lastSpecialIndex + 2;
                            int LengthOfCopy = end - lastSpecialIndex - 1;
                            System.arraycopy(buf, srcPos, buf, destPos, LengthOfCopy);
                            buf[lastSpecialIndex] = '\\';
                            buf[++lastSpecialIndex] = replaceChars[(int) ch];
                        }
                    }
                } else if (specialCount > 1) {
                    int textIndex = firstSpecialIndex - start;
                    int bufIndex = firstSpecialIndex;
                    for (int i = textIndex; i < text.length(); ++i) {
                        char ch = text.charAt(i);

                        if (ch < specicalFlags_doubleQuotes.length //
                                && specicalFlags_doubleQuotes[ch] != 0 //
                                || (ch == '/')) {
                            buf[bufIndex++] = '\\';
                            if (specicalFlags_doubleQuotes[ch] == 4) {
                                buf[bufIndex++] = 'u';
                                buf[bufIndex++] = DIGITS[(ch >>> 12) & 15];
                                buf[bufIndex++] = DIGITS[(ch >>> 8) & 15];
                                buf[bufIndex++] = DIGITS[(ch >>> 4) & 15];
                                buf[bufIndex++] = DIGITS[ch & 15];
                                end += 5;
                            } else {
                                buf[bufIndex++] = replaceChars[(int) ch];
                                end++;
                            }
                        } else {
                            if (ch == '\u2028') {
                                buf[bufIndex++] = '\\';
                                buf[bufIndex++] = 'u';
                                buf[bufIndex++] = DIGITS[(ch >>> 12) & 15];
                                buf[bufIndex++] = DIGITS[(ch >>> 8) & 15];
                                buf[bufIndex++] = DIGITS[(ch >>> 4) & 15];
                                buf[bufIndex++] = DIGITS[ch & 15];
                                end += 5;
                            } else {
                                buf[bufIndex++] = ch;
                            }
                        }
                    }
                }
            }
        }

        if (seperator != 0) {
            buf[count - 2] = '\"';
            buf[count - 1] = seperator;
        } else {
            buf[count - 1] = '\"';
        }
    }


    public void writeString(String text) {
        writeStringWithDoubleQuote(text, (char) 0, true);
    }


    final static byte[] specicalFlags_doubleQuotes = new byte[161];
    final static byte[] specicalFlags_singleQuotes = new byte[161];

    final static char[] replaceChars = new char[93];

    static {
        specicalFlags_doubleQuotes['\0'] = 4;
        specicalFlags_doubleQuotes['\1'] = 4;
        specicalFlags_doubleQuotes['\2'] = 4;
        specicalFlags_doubleQuotes['\3'] = 4;
        specicalFlags_doubleQuotes['\4'] = 4;
        specicalFlags_doubleQuotes['\5'] = 4;
        specicalFlags_doubleQuotes['\6'] = 4;
        specicalFlags_doubleQuotes['\7'] = 4;
        specicalFlags_doubleQuotes['\b'] = 1; // 8
        specicalFlags_doubleQuotes['\t'] = 1; // 9
        specicalFlags_doubleQuotes['\n'] = 1; // 10
        specicalFlags_doubleQuotes['\u000B'] = 4; // 11
        specicalFlags_doubleQuotes['\f'] = 1;
        specicalFlags_doubleQuotes['\r'] = 1;
        specicalFlags_doubleQuotes['\"'] = 1;
        specicalFlags_doubleQuotes['\\'] = 1;

        specicalFlags_singleQuotes['\0'] = 4;
        specicalFlags_singleQuotes['\1'] = 4;
        specicalFlags_singleQuotes['\2'] = 4;
        specicalFlags_singleQuotes['\3'] = 4;
        specicalFlags_singleQuotes['\4'] = 4;
        specicalFlags_singleQuotes['\5'] = 4;
        specicalFlags_singleQuotes['\6'] = 4;
        specicalFlags_singleQuotes['\7'] = 4;
        specicalFlags_singleQuotes['\b'] = 1; // 8
        specicalFlags_singleQuotes['\t'] = 1; // 9
        specicalFlags_singleQuotes['\n'] = 1; // 10
        specicalFlags_singleQuotes['\u000B'] = 4; // 11
        specicalFlags_singleQuotes['\f'] = 1; // 12
        specicalFlags_singleQuotes['\r'] = 1; // 13
        specicalFlags_singleQuotes['\\'] = 1;
        specicalFlags_singleQuotes['\''] = 1;

        for (int i = 14; i <= 31; ++i) {
            specicalFlags_doubleQuotes[i] = 4;
            specicalFlags_singleQuotes[i] = 4;
        }

        for (int i = 127; i < 160; ++i) {
            specicalFlags_doubleQuotes[i] = 4;
            specicalFlags_singleQuotes[i] = 4;
        }

        replaceChars['\0'] = '0';
        replaceChars['\1'] = '1';
        replaceChars['\2'] = '2';
        replaceChars['\3'] = '3';
        replaceChars['\4'] = '4';
        replaceChars['\5'] = '5';
        replaceChars['\6'] = '6';
        replaceChars['\7'] = '7';
        replaceChars['\b'] = 'b'; // 8
        replaceChars['\t'] = 't'; // 9
        replaceChars['\n'] = 'n'; // 10
        replaceChars['\u000B'] = 'v'; // 11
        replaceChars['\f'] = 'f'; // 12
        replaceChars['\r'] = 'r'; // 13
        replaceChars['\"'] = '"'; // 34
        replaceChars['\''] = '\''; // 39
        replaceChars['/'] = '/'; // 47
        replaceChars['\\'] = '\\'; // 92
    }

    public final static char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F'};
}
