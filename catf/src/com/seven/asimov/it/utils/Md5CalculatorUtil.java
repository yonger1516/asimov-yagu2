package com.seven.asimov.it.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5CalculatorUtil {
    private static final Logger logger = LoggerFactory.getLogger(Md5CalculatorUtil.class.getSimpleName());
    public static final String CHARSET = "UTF-8";

    private MessageDigest md5er;
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public Md5CalculatorUtil() {
        try {
            md5er = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.debug(e.getMessage());
            md5er = null;
        }
    }

    protected String getCharset() {
        return CHARSET;
    }

    public void update(ByteBuffer input) {
        md5er.update(input);
    }

    public void update(byte[] data, int offset, int len) {
        md5er.update(data, offset, len);
    }

    public void update(byte[] input) {
        md5er.update(input);
    }

    public void update(String input) {
        update(input, getCharset());
    }

    public void update(String input, String charset) {
        try {
            md5er.update(input.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            logger.debug("Cannot decode input", e);
        }
    }

    public void reset() {
        md5er.reset();
    }

    public byte[] digest() {
        return md5er.digest();
    }

    public char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    public String md5() {
        return new String(encodeHex(digest(), DIGITS_UPPER));
    }

    public static String md5(byte[] input) {
        Md5CalculatorUtil cal = new Md5CalculatorUtil();
        cal.update(input);
        return cal.md5();
    }

    public static String md5(String input) {
        Md5CalculatorUtil cal = new Md5CalculatorUtil();
        cal.update(input);
        return cal.md5();
    }
}
