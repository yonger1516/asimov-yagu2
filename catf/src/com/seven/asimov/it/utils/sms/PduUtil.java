package com.seven.asimov.it.utils.sms;


import java.io.ByteArrayOutputStream;
import java.util.BitSet;

public class PduUtil {
    // ==================================================
    // GSM ALPHABET
    // ==================================================
    private static final char[][] grcAlphabetRemapping = {{'\u0386', '\u0041'}, // GREEK CAPITAL LETTER ALPHA WITH TONOS
            {'\u0388', '\u0045'}, // GREEK CAPITAL LETTER EPSILON WITH TONOS
            {'\u0389', '\u0048'}, // GREEK CAPITAL LETTER ETA WITH TONOS
            {'\u038A', '\u0049'}, // GREEK CAPITAL LETTER IOTA WITH TONOS
            {'\u038C', '\u004F'}, // GREEK CAPITAL LETTER OMICRON WITH TONOS
            {'\u038E', '\u0059'}, // GREEK CAPITAL LETTER UPSILON WITH TONOS
            {'\u038F', '\u03A9'}, // GREEK CAPITAL LETTER OMEGA WITH TONOS
            {'\u0390', '\u0049'}, // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
            {'\u0391', '\u0041'}, // GREEK CAPITAL LETTER ALPHA
            {'\u0392', '\u0042'}, // GREEK CAPITAL LETTER BETA
            {'\u0393', '\u0393'}, // GREEK CAPITAL LETTER GAMMA
            {'\u0394', '\u0394'}, // GREEK CAPITAL LETTER DELTA
            {'\u0395', '\u0045'}, // GREEK CAPITAL LETTER EPSILON
            {'\u0396', '\u005A'}, // GREEK CAPITAL LETTER ZETA
            {'\u0397', '\u0048'}, // GREEK CAPITAL LETTER ETA
            {'\u0398', '\u0398'}, // GREEK CAPITAL LETTER THETA
            {'\u0399', '\u0049'}, // GREEK CAPITAL LETTER IOTA
            {'\u039A', '\u004B'}, // GREEK CAPITAL LETTER KAPPA
            {'\u039B', '\u039B'}, // GREEK CAPITAL LETTER LAMDA
            {'\u039C', '\u004D'}, // GREEK CAPITAL LETTER MU
            {'\u039D', '\u004E'}, // GREEK CAPITAL LETTER NU
            {'\u039E', '\u039E'}, // GREEK CAPITAL LETTER XI
            {'\u039F', '\u004F'}, // GREEK CAPITAL LETTER OMICRON
            {'\u03A0', '\u03A0'}, // GREEK CAPITAL LETTER PI
            {'\u03A1', '\u0050'}, // GREEK CAPITAL LETTER RHO
            {'\u03A3', '\u03A3'}, // GREEK CAPITAL LETTER SIGMA
            {'\u03A4', '\u0054'}, // GREEK CAPITAL LETTER TAU
            {'\u03A5', '\u0059'}, // GREEK CAPITAL LETTER UPSILON
            {'\u03A6', '\u03A6'}, // GREEK CAPITAL LETTER PHI
            {'\u03A7', '\u0058'}, // GREEK CAPITAL LETTER CHI
            {'\u03A8', '\u03A8'}, // GREEK CAPITAL LETTER PSI
            {'\u03A9', '\u03A9'}, // GREEK CAPITAL LETTER OMEGA
            {'\u03AA', '\u0049'}, // GREEK CAPITAL LETTER IOTA WITH DIALYTIKA
            {'\u03AB', '\u0059'}, // GREEK CAPITAL LETTER UPSILON WITH DIALYTIKA
            {'\u03AC', '\u0041'}, // GREEK SMALL LETTER ALPHA WITH TONOS
            {'\u03AD', '\u0045'}, // GREEK SMALL LETTER EPSILON WITH TONOS
            {'\u03AE', '\u0048'}, // GREEK SMALL LETTER ETA WITH TONOS
            {'\u03AF', '\u0049'}, // GREEK SMALL LETTER IOTA WITH TONOS
            {'\u03B0', '\u0059'}, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND TONOS
            {'\u03B1', '\u0041'}, // GREEK SMALL LETTER ALPHA
            {'\u03B2', '\u0042'}, // GREEK SMALL LETTER BETA
            {'\u03B3', '\u0393'}, // GREEK SMALL LETTER GAMMA
            {'\u03B4', '\u0394'}, // GREEK SMALL LETTER DELTA
            {'\u03B5', '\u0045'}, // GREEK SMALL LETTER EPSILON
            {'\u03B6', '\u005A'}, // GREEK SMALL LETTER ZETA
            {'\u03B7', '\u0048'}, // GREEK SMALL LETTER ETA
            {'\u03B8', '\u0398'}, // GREEK SMALL LETTER THETA
            {'\u03B9', '\u0049'}, // GREEK SMALL LETTER IOTA
            {'\u03BA', '\u004B'}, // GREEK SMALL LETTER KAPPA
            {'\u03BB', '\u039B'}, // GREEK SMALL LETTER LAMDA
            {'\u03BC', '\u004D'}, // GREEK SMALL LETTER MU
            {'\u03BD', '\u004E'}, // GREEK SMALL LETTER NU
            {'\u03BE', '\u039E'}, // GREEK SMALL LETTER XI
            {'\u03BF', '\u004F'}, // GREEK SMALL LETTER OMICRON
            {'\u03C0', '\u03A0'}, // GREEK SMALL LETTER PI
            {'\u03C1', '\u0050'}, // GREEK SMALL LETTER RHO
            {'\u03C2', '\u03A3'}, // GREEK SMALL LETTER FINAL SIGMA
            {'\u03C3', '\u03A3'}, // GREEK SMALL LETTER SIGMA
            {'\u03C4', '\u0054'}, // GREEK SMALL LETTER TAU
            {'\u03C5', '\u0059'}, // GREEK SMALL LETTER UPSILON
            {'\u03C6', '\u03A6'}, // GREEK SMALL LETTER PHI
            {'\u03C7', '\u0058'}, // GREEK SMALL LETTER CHI
            {'\u03C8', '\u03A8'}, // GREEK SMALL LETTER PSI
            {'\u03C9', '\u03A9'}, // GREEK SMALL LETTER OMEGA
            {'\u03CA', '\u0049'}, // GREEK SMALL LETTER IOTA WITH DIALYTIKA
            {'\u03CB', '\u0059'}, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA
            {'\u03CC', '\u004F'}, // GREEK SMALL LETTER OMICRON WITH TONOS
            {'\u03CD', '\u0059'}, // GREEK SMALL LETTER UPSILON WITH TONOS
            {'\u03CE', '\u03A9'} // GREEK SMALL LETTER OMEGA WITH TONOS
    };

    private static final char[] extAlphabet = {'\u000c', // FORM FEED
            '\u005e', // CIRCUMFLEX ACCENT
            '\u007b', // LEFT CURLY BRACKET
            '\u007d', // RIGHT CURLY BRACKET
            '\\',     // REVERSE SOLIDUS
            '\u005b', // LEFT SQUARE BRACKET
            '\u007e', // TILDE
            '\u005d', // RIGHT SQUARE BRACKET
            '\u007c', // VERTICAL LINES
            '\u20ac', // EURO SIGN
    };

    private static final String[] extBytes = {
            "1b0a", // FORM FEED
            "1b14", // CIRCUMFLEX ACCENT
            "1b28", // LEFT CURLY BRACKET
            "1b29", // RIGHT CURLY BRACKET
            "1b2f", // REVERSE SOLIDUS
            "1b3c", // LEFT SQUARE BRACKET
            "1b3d", // TILDE
            "1b3e", // RIGHT SQUARE BRACKET
            "1b40", // VERTICAL LINES
            "1b65", // EURO SIGN
    };

    // NOTE: this is an adjustment required to compensate for
    //       multi-byte characters split across the end of a pdu part
    //       if the previous part is noted to be ending in a '1b'
    //       call this method on the first char of the next part
    //       to adjust it for the missing '1b'
    public static String getMultiCharFor(char c) {
        switch (c) {
            // GSM 0x0A (line feed) ==> form feed
            case '\n':
                return "'\u000c'";
            // GSM 0x14 (greek capital lamda) ==> circumflex
            case '\u039B':
                return "^";
            // GSM 0x28 (left parenthesis) ==> left curly brace
            case '(':
                return "{";
            // GSM 0x29 (right parenthesis) ==> right curly brace
            case ')':
                return "}";
            // GSM 0x2f (solidus or slash) ==> reverse solidus or backslash
            case '/':
                return "\\";
            // GSM 0x3c (less than sign) ==> left square bracket
            case '<':
                return "[";
            // GSM 0x3d (equals sign) ==> tilde
            case '=':
                return "~";
            // GSM 0x3e (greater than sign) ==> right square bracket
            case '>':
                return "]";
            // GSM 0x40 (inverted exclamation point) ==> pipe
            case '\u00A1':
                return "|";
            // GSM 0x65 (latin small e) ==> euro
            case 'e':
                return "\u20ac";
        }

        return "";
    }

    private static final char[] stdAlphabet = {'\u0040', // COMMERCIAL AT
            '\u00A3', // POUND SIGN
            '\u0024', // DOLLAR SIGN
            '\u00A5', // YEN SIGN
            '\u00E8', // LATIN SMALL LETTER E WITH GRAVE
            '\u00E9', // LATIN SMALL LETTER E WITH ACUTE
            '\u00F9', // LATIN SMALL LETTER U WITH GRAVE
            '\u00EC', // LATIN SMALL LETTER I WITH GRAVE
            '\u00F2', // LATIN SMALL LETTER O WITH GRAVE
            '\u00E7', // LATIN SMALL LETTER C WITH CEDILLA
            '\n', // LINE FEED
            '\u00D8', // LATIN CAPITAL LETTER O WITH STROKE
            '\u00F8', // LATIN SMALL LETTER O WITH STROKE
            '\r', // CARRIAGE RETURN
            '\u00C5', // LATIN CAPITAL LETTER A WITH RING ABOVE
            '\u00E5', // LATIN SMALL LETTER A WITH RING ABOVE
            '\u0394', // GREEK CAPITAL LETTER DELTA
            '\u005F', // LOW LINE
            '\u03A6', // GREEK CAPITAL LETTER PHI
            '\u0393', // GREEK CAPITAL LETTER GAMMA
            '\u039B', // GREEK CAPITAL LETTER LAMDA
            '\u03A9', // GREEK CAPITAL LETTER OMEGA
            '\u03A0', // GREEK CAPITAL LETTER PI
            '\u03A8', // GREEK CAPITAL LETTER PSI
            '\u03A3', // GREEK CAPITAL LETTER SIGMA
            '\u0398', // GREEK CAPITAL LETTER THETA
            '\u039E', // GREEK CAPITAL LETTER XI
            '\u00A0', // ESCAPE TO EXTENSION TABLE (or displayed as NBSP, see
            // note
            // above)
            '\u00C6', // LATIN CAPITAL LETTER AE
            '\u00E6', // LATIN SMALL LETTER AE
            '\u00DF', // LATIN SMALL LETTER SHARP S (German)
            '\u00C9', // LATIN CAPITAL LETTER E WITH ACUTE
            '\u0020', // SPACE
            '\u0021', // EXCLAMATION MARK
            '\u0022', // QUOTATION MARK
            '\u0023', // NUMBER SIGN
            '\u00A4', // CURRENCY SIGN
            '\u0025', // PERCENT SIGN
            '\u0026', // AMPERSAND
            '\'', // APOSTROPHE
            '\u0028', // LEFT PARENTHESIS
            '\u0029', // RIGHT PARENTHESIS
            '\u002A', // ASTERISK
            '\u002B', // PLUS SIGN
            '\u002C', // COMMA
            '\u002D', // HYPHEN-MINUS
            '\u002E', // FULL STOP
            '\u002F', // SOLIDUS
            '\u0030', // DIGIT ZERO
            '\u0031', // DIGIT ONE
            '\u0032', // DIGIT TWO
            '\u0033', // DIGIT THREE
            '\u0034', // DIGIT FOUR
            '\u0035', // DIGIT FIVE
            '\u0036', // DIGIT SIX
            '\u0037', // DIGIT SEVEN
            '\u0038', // DIGIT EIGHT
            '\u0039', // DIGIT NINE
            '\u003A', // COLON
            '\u003B', // SEMICOLON
            '\u003C', // LESS-THAN SIGN
            '\u003D', // EQUALS SIGN
            '\u003E', // GREATER-THAN SIGN
            '\u003F', // QUESTION MARK
            '\u00A1', // INVERTED EXCLAMATION MARK
            '\u0041', // LATIN CAPITAL LETTER A
            '\u0042', // LATIN CAPITAL LETTER B
            '\u0043', // LATIN CAPITAL LETTER C
            '\u0044', // LATIN CAPITAL LETTER D
            '\u0045', // LATIN CAPITAL LETTER E
            '\u0046', // LATIN CAPITAL LETTER F
            '\u0047', // LATIN CAPITAL LETTER G
            '\u0048', // LATIN CAPITAL LETTER H
            '\u0049', // LATIN CAPITAL LETTER I
            '\u004A', // LATIN CAPITAL LETTER J
            '\u004B', // LATIN CAPITAL LETTER K
            '\u004C', // LATIN CAPITAL LETTER L
            '\u004D', // LATIN CAPITAL LETTER M
            '\u004E', // LATIN CAPITAL LETTER N
            '\u004F', // LATIN CAPITAL LETTER O
            '\u0050', // LATIN CAPITAL LETTER P
            '\u0051', // LATIN CAPITAL LETTER Q
            '\u0052', // LATIN CAPITAL LETTER R
            '\u0053', // LATIN CAPITAL LETTER S
            '\u0054', // LATIN CAPITAL LETTER T
            '\u0055', // LATIN CAPITAL LETTER U
            '\u0056', // LATIN CAPITAL LETTER V
            '\u0057', // LATIN CAPITAL LETTER W
            '\u0058', // LATIN CAPITAL LETTER X
            '\u0059', // LATIN CAPITAL LETTER Y
            '\u005A', // LATIN CAPITAL LETTER Z
            '\u00C4', // LATIN CAPITAL LETTER A WITH DIAERESIS
            '\u00D6', // LATIN CAPITAL LETTER O WITH DIAERESIS
            '\u00D1', // LATIN CAPITAL LETTER N WITH TILDE
            '\u00DC', // LATIN CAPITAL LETTER U WITH DIAERESIS
            '\u00A7', // SECTION SIGN
            '\u00BF', // INVERTED QUESTION MARK
            '\u0061', // LATIN SMALL LETTER A
            '\u0062', // LATIN SMALL LETTER B
            '\u0063', // LATIN SMALL LETTER C
            '\u0064', // LATIN SMALL LETTER D
            '\u0065', // LATIN SMALL LETTER E
            '\u0066', // LATIN SMALL LETTER F
            '\u0067', // LATIN SMALL LETTER G
            '\u0068', // LATIN SMALL LETTER H
            '\u0069', // LATIN SMALL LETTER I
            '\u006A', // LATIN SMALL LETTER J
            '\u006B', // LATIN SMALL LETTER K
            '\u006C', // LATIN SMALL LETTER L
            '\u006D', // LATIN SMALL LETTER M
            '\u006E', // LATIN SMALL LETTER N
            '\u006F', // LATIN SMALL LETTER O
            '\u0070', // LATIN SMALL LETTER P
            '\u0071', // LATIN SMALL LETTER Q
            '\u0072', // LATIN SMALL LETTER R
            '\u0073', // LATIN SMALL LETTER S
            '\u0074', // LATIN SMALL LETTER T
            '\u0075', // LATIN SMALL LETTER U
            '\u0076', // LATIN SMALL LETTER V
            '\u0077', // LATIN SMALL LETTER W
            '\u0078', // LATIN SMALL LETTER X
            '\u0079', // LATIN SMALL LETTER Y
            '\u007A', // LATIN SMALL LETTER Z
            '\u00E4', // LATIN SMALL LETTER A WITH DIAERESIS
            '\u00F6', // LATIN SMALL LETTER O WITH DIAERESIS
            '\u00F1', // LATIN SMALL LETTER N WITH TILDE
            '\u00FC', // LATIN SMALL LETTER U WITH DIAERESIS
            '\u00E0', // LATIN SMALL LETTER A WITH GRAVE
    };

    public static final int ADDRESS_NUMBER_PLAN_ID_MASK = 0x0F;

    public static int extractNumberPlan(int addressType) {
        return addressType & ADDRESS_NUMBER_PLAN_ID_MASK;
    }

    public static String stringToPdu(String txt) {
        byte[] txtBytes = stringToUnencodedSeptets(txt);
        return bytesToPdu(unencodedSeptetsToEncodedSeptets(txtBytes));
    }

    // from Java String to uncompressed septets (GSM characters)
    public static byte[] stringToUnencodedSeptets(String s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i, j, k, index;
        char ch;
        k = 0;
        for (i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            index = -1;
            for (j = 0; j < extAlphabet.length; j++)
                if (extAlphabet[j] == ch) {
                    index = j;
                    break;
                }
            if (index != -1) // An extended char...
            {
                baos.write((byte) Integer.parseInt(extBytes[index].substring(0, 2), 16));
                k++;
                baos.write((byte) Integer.parseInt(extBytes[index].substring(2, 4), 16));
                k++;
            } else
            // Maybe a standard char...
            {
                index = -1;
                for (j = 0; j < stdAlphabet.length; j++)
                    if (stdAlphabet[j] == ch) {
                        index = j;
                        baos.write((byte) j);
                        k++;
                        break;
                    }
                if (index == -1) // Maybe a Greek Char...
                {
                    for (j = 0; j < grcAlphabetRemapping.length; j++)
                        if (grcAlphabetRemapping[j][0] == ch) {
                            index = j;
                            ch = grcAlphabetRemapping[j][1];
                            break;
                        }
                    if (index != -1) {
                        for (j = 0; j < stdAlphabet.length; j++)
                            if (stdAlphabet[j] == ch) {
                                index = j;
                                baos.write((byte) j);
                                k++;
                                break;
                            }
                    } else
                    // Unknown char replacement...
                    {
                        baos.write((byte) ' ');
                        k++;
                    }
                }
            }
        }
        return baos.toByteArray();
    }

    // from compress unencoded septets
    public static byte[] unencodedSeptetsToEncodedSeptets(byte[] septetBytes) {
        byte[] txtBytes;
        byte[] txtSeptets;
        int txtBytesLen;
        BitSet bits;
        int i, j;
        txtBytes = septetBytes;
        txtBytesLen = txtBytes.length;
        bits = new BitSet();
        for (i = 0; i < txtBytesLen; i++)
            for (j = 0; j < 7; j++)
                if ((txtBytes[i] & (1 << j)) != 0) bits.set((i * 7) + j);
        // big diff here
        int encodedSeptetByteArrayLength = txtBytesLen * 7 / 8 + ((txtBytesLen * 7 % 8 != 0) ? 1 : 0);
        txtSeptets = new byte[encodedSeptetByteArrayLength];
        for (i = 0; i < encodedSeptetByteArrayLength; i++) {
            for (j = 0; j < 8; j++) {
                txtSeptets[i] |= (byte) ((bits.get((i * 8) + j) ? 1 : 0) << j);
            }
        }
        return txtSeptets;
    }

    // converts a byte array to PDU style string
    public static String bytesToPdu(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(byteToPdu(bytes[i] & 0xFF));
        }
        return sb.toString();
    }

    public static String byteToPdu(int b) {
        StringBuffer sb = new StringBuffer();
        String s = Integer.toHexString(b & 0xFF);
        if (s.length() == 1) {
            sb.append("0");
        }
        sb.append(s);
        return sb.toString().toUpperCase();
    }
}

