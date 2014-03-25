package com.seven.asimov.test.tool.utils;

import android.os.Environment;
import android.os.StatFs;
import com.seven.asimov.it.utils.IOUtil;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Utils.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public abstract class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class.getSimpleName());

    public static String getLogPrefix(long threadId) {
        return "[" + threadId + "] ";
    }

    public static String getLogPrefix(long threadId, int connection) {
        return "[" + threadId + "] Conn #" + connection + " ";
    }

    public static String getLogPrefix(long threadId, int connection, int requestCount) {
        return "[" + threadId + "] Conn #" + connection + ", req #" + requestCount + " ";
    }

    static final String HEXES = "0123456789ABCDEF";

    public static String getHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        // final StringBuilder hex = new StringBuilder( 2 * raw.length );
        final StringBuilder hex = new StringBuilder();
        for (final byte b : raw) {
            hex.append("0x").append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F))).append(" ");
        }
        return hex.toString();
    }

    /**
     * Whether the specified {@link String} is <code>null</code> or empty.
     */
    public static final boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static ArrayList<String> chunk(String str, int chunkSize) {
        ArrayList<String> temp = new ArrayList<String>();
        try {
            for (int i = 0; i < str.length(); i += chunkSize) {
                if (str.length() < (i + chunkSize)) {
                    temp.add(str.substring(i));
                } else {
                    temp.add(str.substring(i, i + chunkSize));
                }
            }
        } catch (Exception ex) {
        }
        return temp;
    }

    /*
     * This method saves (serializes) any java bean object into xml file
     */
    public static byte[] serializeObjectToXML(Object object) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Serializer serializer = new Persister();
        serializer.write(object, os);
        return os.toByteArray();
    }

    /*
     * Reads Java Bean Object From XML File
     */
    public static Object deserializeXMLToObject(Class<?> clazz, byte[] xmlArray) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(xmlArray);
        Serializer serializer = new Persister();
        Object deSerializedObject = serializer.read(clazz, is);
        return deSerializedObject;
    }

    public static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
                LOG.error("Failed to close " + is, e);
            }
        }
    }

    public static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (Exception e) {
                LOG.error("Failed to close " + os, e);
            }
        }
    }

    public static boolean isSDCardAvailable() {
        return Environment.getExternalStorageDirectory().canRead();
    }

    public static long getAvailableSpace(String path) {
        StatFs sf = new StatFs(path);
        return (long) sf.getAvailableBlocks() * (long) sf.getBlockSize();
    }

    public static long getAvailableSpaceSDCard() {
        return getAvailableSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public static void copyFile(File in, File out) throws IOException {
        if (!out.exists()) {
            out.getParentFile().mkdirs();
            out.createNewFile();
        }

        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtil.safeClose(inChannel);
            IOUtil.safeClose(outChannel);
        }
    }

    /*
     * shamelessly copied from OC client
     */
    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        return toHexString(bytes, bytes.length * 2);
    }

    /**
     * Length limited toHexString().
     *
     * @param len the maximum length of the resulting hex string. NOTE: to limit the portion of the input to be
     *            processed, pass (len * 2). For example, to toHexString() the first 4 bytes of a byte array, pass 8 to
     *            limit the output string to 8 characters.
     * @return hex string
     */
    public static String toHexString(byte[] bytes, int len) {
        if (bytes == null) {
            return "";
        }
        int max = bytes.length * 2;
        if (len > max) {
            len = max;
        }

        char[] buf = new char[len];
        for (int i = 0, j = 0; j < len; i++) {
            int x = (int) bytes[i] & 255;
            int n = x >> 4;
            buf[j++] = (n < 10) ? (char) (n + (int) '0') : (char) (n - 10 + (int) 'a');
            n = x & 15;
            buf[j++] = (n < 10) ? (char) (n + (int) '0') : (char) (n - 10 + (int) 'a');
        }
        return new String(buf);
    }

    public static void logHexDump(Logger log, byte[] bytes, int lineLength) {
        int chunkLength = 8; // bytes

        for (int i = 0; i <= bytes.length / lineLength; i++) {
            // 7 is the line prefix - 4-digit hex byte offset + 1 space
            // ascii representation is 1*lineLength, hex bytes are 2*lineLength, spaces
            // between hex bytes are another 1*lineLength; total = 4* lineLength
            StringBuilder hex = new StringBuilder(7 + lineLength * 4);
            StringBuilder str = new StringBuilder(lineLength);

            hex.append(String.format("%04x ", i * lineLength));
            byte b;
            for (int j = 0; j < lineLength && i * lineLength + j < bytes.length; j++) {
                if (j % chunkLength == 0) {
                    hex.append(' ');
                }

                b = bytes[i * lineLength + j];
                hex.append(String.format("%02x", b & 0xff));
                if (b >= ' ' && b < 127) { // looks like valid ASCII
                    str.append((char) b);
                } else {
                    str.append('.');
                }
            }

            // padding for last line
            int k = bytes.length - i * lineLength;
            if (k <= lineLength / 2) {
                hex.append(" ");
            }
            for (; k < lineLength; k++) {
                hex.append("  ");
            }

            hex.append("  ").append(str);

            log.debug(hex.toString());
        }
    }

    public static boolean isHeaderExtraLine(String headerLine) {
        return (headerLine != null && (headerLine.startsWith(" ") || headerLine.startsWith("\t")));
    }
}
