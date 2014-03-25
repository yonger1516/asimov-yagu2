package com.seven.asimov.it.base;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Calculates hash of object, even when the object is available as a stream (broken into portions of bytes).
 * @author msvintsov
 *
 */
public class StreamedHash {
    
    private MessageDigest md;
    
    public StreamedHash() {
        md = getMessageDigest();
    }
    
    public void append(byte[] input) {
        md.update(input);
    }
    
    public void append(byte[] input, int offest, int len) {
        md.update(input, offest, len);
    }
    
    public byte[] getHash() {
        return md.digest();
    }

    public static byte[] getHash(byte[] input) {
        return getMessageDigest().digest(input);
    }
    
    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
}
