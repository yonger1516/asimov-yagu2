package com.seven.asimov.test.tool.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public abstract class MD5Util {
    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32
            // chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // private static String calculateKey(String uri, String headers, byte[]
    // body) {
    // String requestHash = Md5Calculator.md5(uri) + Md5Calculator.md5(headers);
    // if (body != null) {
    // requestHash += Md5Calculator.md5(body);
    // }
    // return requestHash;
    // }
    //

    // public static void main(String[] args) throws NoSuchAlgorithmException {
    // System.out.println(getMD5("Javarmi.com"));
    // }
}
