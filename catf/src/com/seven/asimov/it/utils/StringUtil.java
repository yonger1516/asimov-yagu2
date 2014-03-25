package com.seven.asimov.it.utils;

import com.seven.asimov.it.exception.z7.Z7Error;
import com.seven.asimov.it.utils.pms.z7.ArrayMap;
import com.seven.asimov.it.utils.pms.z7.IntArrayMap;
import com.seven.asimov.it.utils.pms.z7.Z7TimeZone;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO testcases
public class StringUtil {

    public static char[] HTTP_SPECIAL_CHARS = {';', '/', '?', ':', '@', '&', '=', '+', '$', ',', ' ', '<', '>', '#', '%', '"', '{', '}', '|', '\\', '^', '[', ']', '\''};

    public static String toHexString(byte[] hash) {
        if (hash == null) return "";
        return toHexString(hash, hash.length * 2);
    }

    /**
     * Length limited toHexString().
     *
     * @param len the maximum length of the resulting hex string. NOTE: to limit the portion of
     *            the input to be processed, pass (len * 2). For example, to toHexString() the first 4 bytes of
     *            a byte array, pass 8 to limit the output string to 8 characters.
     * @return hex string
     */
    public static String toHexString(byte[] hash, int len) {
        if (hash == null)
            return "";
        int max = hash.length * 2;
        if (len > max) len = max;

        char buf[] = new char[len];
        for (int i = 0, j = 0; j < len; i++) {
            int x = (int) hash[i] & 255;
            int n = x >> 4;
            buf[j++] = (n < 10) ? (char) (n + (int) '0') : (char) (n - 10 + (int) 'a');
            n = x & 15;
            buf[j++] = (n < 10) ? (char) (n + (int) '0') : (char) (n - 10 + (int) 'a');
        }
        return new String(buf);
    }

    public static StringBuffer hexDump(byte[] buf) {
        StringBuffer buffer = new StringBuffer();
        StringBuffer s1 = new StringBuffer();
        int i = 0;

        while (true) {
            if (i % 16 == 0) {
                int pos = buffer.length();
                buffer.append(Integer.toHexString(i));
                while (buffer.length() - pos < 12)
                    buffer.append(' ');
            }
            if (buf[i] >= 0 && buf[i] < 16)
                buffer.append('0');
            buffer.append(Integer.toHexString(buf[i] & 0xff)).append(' ');

            s1.append(
                    (buf[i] <= 31) ? ' ' : (char) buf[i]);
            i++;
            if (i % 16 == 0 || i == buf.length) {
                for (int j = i % 16; j != 0; j = (j + 1) % 16)
                    buffer.append("   ");
                buffer.append("  ").append(s1.toString()).append('\n');
                s1.setLength(0);
            }

            if (i == buf.length)
                break;
        }
        return buffer;
    }

    public static byte[] fromHexString(String s) {
        if (s == null)
            return null;
        s = s.toLowerCase();
        int i, j = 0, len = s.length();
        byte[] result = new byte[len / 2];
        for (i = 0; i < len; i += 2) {
            char chi = s.charAt(i), clo = s.charAt(i + 1);
            int nibhi = (chi <= '9') ? (int) (chi - '0') : (int) (chi - 'a') + 10;
            int niblo = (clo <= '9') ? (int) (clo - '0') : (int) (clo - 'a') + 10;
            result[j++] = (byte) ((nibhi << 4) + niblo);
        }
        return result;
    }

    public static boolean compareByteArrays(byte[] b1, byte[] b2) {
        if (b1 == null && b2 == null) return true;
        if (b1 == null || b2 == null) return false;
        if (b1.length != b2.length) return false;
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) return false;
        }
        return true;
    }

    public static boolean compareByteArrays(byte[] b1, byte[] b2, int len) {
        if (b1 == null && b2 == null) return true;
        if (b1 == null || b2 == null) return false;
        if (b1.length < len || b2.length < len) return false;
        for (int i = 0; i < len; i++) {
            if (b1[i] != b2[i]) return false;
        }
        return true;
    }

    public static int toInt(byte[] src, int offset) {
        return (((src[offset++] & 0xff) << 24) | ((src[offset++] & 0xff) << 16)
                | ((src[offset++] & 0xff) << 8) | ((src[offset++] & 0xff)));
    }

    public static void writeInt(int v, byte[] dest, int index) {
        dest[index++] = (byte) ((v >>> 24) & 0xFF);
        dest[index++] = (byte) ((v >>> 16) & 0xFF);
        dest[index++] = (byte) ((v >>> 8) & 0xFF);
        dest[index++] = (byte) ((v >>> 0) & 0xFF);
    }

    public static void writeShort(int v, byte[] dest, int index) {
        dest[index++] = (byte) ((v >>> 8) & 0xFF);
        dest[index++] = (byte) ((v >>> 0) & 0xFF);


    }

    /**
     * Returns the number of bytes required to hold UTF-8 encoded version of
     * the given UTF-16 character array
     *
     * @param charArray The character array whose length to determine
     * @return Number of bytes required for output buffer
     */
    public static int getUTF8Length(char[] charArray) {
        int count = 0;
        for (int i = 0; i < charArray.length; ++i) {
            char c = charArray[i];

            if (c < 0x80) {
                count++;
            } else if (c < 0x800) {
                count += 2;
            } else {
                short surrogate = (short) (((short) c) & 0xfc00);
                if ((surrogate ^ 0xdc00) == 0)
                    count += 4;  // surrogate in range 0xdc00..0xdfff
                else if ((surrogate ^ 0xd800) != 0)
                    count += 3;  // not a surrogate in range 0xd800..0xdbff
            }
        }

        return count;
    }

    /**
     * Converts a UTF-16 encoded character array into UTF-8 encoded array
     *
     * @param charArray The UTF-16 character array to convert
     * @param data      Output buffer to receive the UTF-8 encoded data. This must
     *                  be at least as many bytes as what getUTF8Length returns for the
     *                  charArray
     */
    public static void convertUTF16ToUTF8(char[] charArray, byte[] data) {
        int index = 0;
        for (int i = 0; i < charArray.length; ++i) {
            char c = charArray[i];

            if (c < 0x80) {
                data[index++] = (byte) c;
            } else if (c < 0x800) {
                data[index++] = (byte) ((byte) 0xc0 | (byte) (((short) c) >> 6));
                data[index++] = (byte) ((byte) 0x80 | (byte) (((short) c) & 0x3f));
            } else {
                short surrogate = (short) (((short) c) & 0xfc00);
                if ((surrogate ^ 0xdc00) == 0) {
                    // invalid occurrence of a surrogate in range 0xdc00..0xdfff
                    data[index++] = '?';
                } else if ((surrogate ^ 0xd800) == 0) {
                    // surrogate in range 0xd800..0xdbff
                    // next element must be a surrogate in range 0xdc00..0xdfff
                    if (i + 1 >= charArray.length)
                        return;

                    ++i;
                    char c2 = charArray[i];
                    surrogate = (short) (((short) c2) & 0xfc00);
                    if ((surrogate ^ 0xdc00) != 0) {
                        // no surrogate in range 0xdc00..0xdfff found
                        data[index++] = '?';
                    } else {
                        int bigC = (((int) (c - 0xd800)) << 10) + ((int) (c2 - 0xdc00)) + 0x10000;

                        data[index++] = (byte) (0xf0 | (bigC >> 18));
                        data[index++] = (byte) (0x80 | ((bigC >> 12) & 0x3f));
                        data[index++] = (byte) (0x80 | ((bigC >> 6) & 0x3f));
                        data[index++] = (byte) (0x80 | (bigC & 0x3f));
                    }
                } else {
                    data[index++] = (byte) (0xe0 | (c >> 12));
                    data[index++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                    data[index++] = (byte) (0x80 | (c & 0x3f));
                }
            }
        }
    }

    /**
     * Java's implementation of getBytes("UTF-8") is slow and apparently
     * buggy on some J2ME devices (Samsung Loches/Tocco) so do the encoding
     * ourselves. On Android's the Java implementation is 20 timer slower
     * than this code
     */
    public static byte[] getUTF8BytesFast(String s) {
        char[] charArray = s.toCharArray();
        byte[] data = new byte[getUTF8Length(charArray)];
        try {
            convertUTF16ToUTF8(charArray, data);
        } catch (IndexOutOfBoundsException e) {
            // Invalid UTF-16 data could cause IndexOutOfBoundsException, just ignore it for now
        }
        return data;
    }

    /**
     * This method is a workaround for a bug in Samsung Loches/Tocco devices,
     * sometimes String.getBytes("UTF-8") returns an empty byte array instead of
     * correct bytes.
     */
    public static byte[] getUTF8Bytes(String s) throws IOException {
        byte[] data = s.getBytes("UTF-8");
        //#ifdef J2ME
        //# if (data == null || data.length < s.length()) {
        //#   // in UTF-8, all characters take at least one byte - so if we receive
        //#   // less bytes than there are characters, we have corrupted byte array.
        //#   ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //#   OutputStreamWriter osw = new OutputStreamWriter(baos, "UTF-8");
        //#   for (int i=0 ; i < s.length() ; i++) {
        //#       osw.write(s.charAt(i));
        //#   }
        //#   data = baos.toByteArray();
        //# }
        //#endif
        return data;
    }

    public static void appendObjectToStringBuffer(Object o, StringBuffer buf) {
        if (o == null) {
            buf.append("null");
        } else if (o instanceof Boolean) {
            buf.append(((Boolean) o).booleanValue() ? "true" : "false");
        } else if (o instanceof String) {
            String string = (String) o;
            if (string.length() == 0) {
                buf.append('"').append('"');
            } else {
                buf.append('"').append(string).append('"');
            }
        } else if (o instanceof Integer) {
            buf.append(((Integer) o).intValue());
        } else if (o instanceof List) {
            List list = (List) o;
            buf.append('[');
            final int listSize = list.size();
            for (int j = 0; j < listSize; ++j) {
                if (j != 0) {
                    buf.append(',').append(' ');
                }
                appendObjectToStringBuffer(list.get(j), buf);
            }
            buf.append(']');
        } else if (o instanceof IntArrayMap) {
            ((IntArrayMap) o).toString(buf);
        } else if (o instanceof ArrayMap) {
            ((ArrayMap) o).toString(buf);
        } else if (o instanceof Short) {
            buf.append((int) ((Short) o).shortValue());
        } else if (o instanceof Z7TimeZone) {
            ((Z7TimeZone) o).toString(buf);
        } else if (o instanceof Z7Error) {
            ((Z7Error) o).toString(buf);
        } else {
            buf.append(o);
        }
    }


    //Escaping/unescaping for JID disallowed characters according to XEP-0106 JID ESCAPING
    public static char[] PROHIBIT = new char[]{' ', '\"', '&', '\'', '/', ':', '<', '>', '@', '\\'};

    public static boolean needEscape(char c) {
        boolean isFind = false;
        for (int i = 0; i < PROHIBIT.length; i++) {
            if (c == PROHIBIT[i]) {
                isFind = true;
                break;
            }
        }
        return isFind;
    }

    public static char unEscape(String s) {
        for (int i = 0; i < PROHIBIT.length; i++) {
            if (s.equals(Integer.toHexString(PROHIBIT[i]))) {
                return PROHIBIT[i];
            }
        }
        //This should not happen.
        return 0;
    }

    public static String escapedJIDString(String userId) {
        if (userId == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < userId.length(); i++) {
            char c = userId.charAt(i);
            if (needEscape(c)) {
                sb.append('\\');
                sb.append(Integer.toHexString(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String unescapedJIDString(String escapedJid) {
        if (escapedJid == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < escapedJid.length(); ) {
            char c = escapedJid.charAt(i);
            if (c == '\\') {
                String escapedchar = escapedJid.substring(i + 1, i + 3);
                if (unEscape(escapedchar) != 0) {
                    sb.append(unEscape(escapedchar));
                    i += 3;
                } else {
                    sb.append(c);
                    i++;
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    public static String escapeAmpersandFromUrl(String str) {
        if (!str.contains("&")) {
            return str;
        }
        boolean first = false;
        int indexOffirst = -1;
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            temp.append(str.charAt(i));
            if (str.charAt(i) == '&') {
                if (!first) {
                    first = true;
                    indexOffirst = temp.length() - 1;
                } else {
                    temp.replace(indexOffirst, indexOffirst + 1, "%26");
                    indexOffirst = temp.length() - 1;
                }
            } else if (str.charAt(i) == '=') {
                first = false;
            }
        }
        if (first) {
            temp.replace(indexOffirst, indexOffirst + 1, "%26");
        }
        return temp.toString();
    }

    /**
     * A utility method to create a regexp pattern from a given string by escaping all regexp characters and optionally
     * handling wildcard characters.
     *
     * @param original
     * @param wildcard set to true if you have wildcard characters ('*', '?') in the original string and want them to be replaced with the corresponding regexp
     * @return
     */
    public static String stringToRegex(String original, boolean wildcard) {
        StringBuffer s = new StringBuffer(original.length());
        for (int i = 0, is = original.length(); i < is; i++) {
            char c = original.charAt(i);
            switch (c) {
                case '*':
                    if (wildcard) {
                        s.append(".*");
                    } else {
                        s.append(c);
                    }
                    break;
                case '?':
                    if (wildcard) {
                        s.append(".");
                    } else {
                        s.append(c);
                    }
                    break;
                // escape special regexp-characters
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        return (s.toString());
    }

    /**
     * @param s            input string
     * @param defaultValue Long to be returned if given string cannot be parsed
     * @return the <code>long</code> represented by the argument in decimal
     */
    public static Long parseLong(String s, Long defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        try {
            return new Long(s);
        } catch (NumberFormatException nfe) {
        }
        return defaultValue;
    }

    public static String findGroup(String pattern, CharSequence content) {
        return findGroup(pattern, content, false);
    }

    public static String findGroup(String pattern, CharSequence content, boolean caseInsensitive) {
        Pattern p;
        if (caseInsensitive) {
            p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } else {
            p = Pattern.compile(pattern);
        }
        Matcher m = p.matcher(content);
        if (m.find()) {
            if (m.groupCount() != 1) {
                throw new IllegalArgumentException("Pattern must contain one and only one group");
            }
            return m.group(1);
        }
        return null;
    }

    public static String replace(String pattern, CharSequence content, CharSequence search, CharSequence replacement) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);
        if (m.find()) {
            return m.replaceFirst(m.group().replace(search, replacement));
        }
        return content.toString();
    }

    public static String replaceGroup(String pattern, CharSequence content, CharSequence replacement) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);
        if (m.find()) {
            if (m.groupCount() != 1) {
                throw new IllegalArgumentException("Pattern must contain one and only one group");
            }
            return m.replaceFirst(m.group().replace(m.group(1), replacement));
        }
        return content.toString();
    }

    public static boolean startsWithCaseInsensitive(String content, String startWith) {
        if (content == null) {
            return false;
        }
        if (startWith == null) {
            return false;
        }
        return startWith.compareToIgnoreCase(content.substring(0, startWith.length())) == 0;
    }
}

