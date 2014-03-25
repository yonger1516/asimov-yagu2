package com.seven.asimov.it.utils.pms.z7;
//#ifndef J2ME

import com.seven.asimov.it.exception.z7.Z7Error;
import com.seven.asimov.it.exception.z7.Z7ErrorCode;
import com.seven.asimov.it.exception.z7.Z7Result;
import com.seven.asimov.it.utils.StringUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Marshal objects into a serialized form. This is a very simple serializer.
 * Builtin support is provided for the null value and the boolean, short,
 * int, long, float, double, String, Date, ArrayMap, and IntArrayMap List types.
 */

// TODO support double? float?
// TODO support BIGSTRING?
// TODO compound type?

public class Marshaller {

    private static final int NULL_TAG = 0;

    private static final int TRUE_TAG = 1;
    private static final int FALSE_TAG = 2;

    private static final int SHORT_TAG = 3;
    private static final int INT_TAG = 4;
    private static final int LONG_TAG = 5;
    private static final int BYTE_TAG = 15;

    private static final int STRING_TAG = 6;
    private static final int DATE_TAG = 7;
    private static final int BLOB_TAG = 8;

    private static final int LIST_TAG = 9;
    private static final int ARRAYMAP_TAG = 10;
    private static final int INTARRAYMAP_TAG = 11;
    private static final int ERROR_TAG = 12;

    private static final int TIME_ZONE_TAG = 13;

    private static final int TRANSPORT_ADDRESS_TAG = 14;

    //private static final int BIGSTRING_TAG      = 12;
    //private static final int FLOAT_TAG      = 0x08;
    //private static final int DOUBLE_TAG = 0x09;

    private DataOutputStream m_out;
    private DataInputStream m_in;

    public static final int STRING_ENCODING_UTF8 = 1;
    public static final int STRING_ENCODING_UTF16LE = 2;
    public static final int STRING_ENCODING_UTF16BE = 3;

    // generic max size limit in bytes
    public static final int MAX_SIZE = 16 * 1024 * 1024;

    static public String getEncodingName(int encoding) {
        switch (encoding) {
            case STRING_ENCODING_UTF8:
                return "UTF-8";
            case STRING_ENCODING_UTF16LE:
                return "UTF-16LE";
            case STRING_ENCODING_UTF16BE:
                return "UTF-16BE";
            default:
                // return UTF-8 as the default character set since that works in most cases and
                // returning null can cause unpredictable results
                return "UTF-8";
        }
    }

    /**
     * Encode the object, returning the encoded byte array.
     *
     * @param o the object to marshal
     * @return the encoded byte array
     */
    public static byte[] encode(Object o) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        encode(o, os);
        return os.toByteArray();
    }

    /**
     * Encode the object into a stream
     *
     * @param o  the object to marshal
     * @param os the stream to marshal to
     */
    public static void encode(Object o, OutputStream os) throws IOException {
        Marshaller m = new Marshaller(os);
        m.write(o);
        m.flush();
    }

    /**
     * Decodes an object from the given encoded data buffer. This version uses
     * direct byte array manipulation and is faster than the old decode
     * implementation, which needs to make calls to a lot of virtual methods
     *
     * @param data A byte array containing encoded object representation
     * @return The object created from the encoded data
     * @throws java.io.IOException
     */
    public static Object fastDecode(byte[] data) throws IOException {
        if (data.length == 0) {
            return null;
        }
        int[] dataPos = new int[1];
        try {
            return fastDecodeObject(data, dataPos);
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Unexpected end of data: " + e);
        }
    }

    private static Object fastDecodeObject(byte[] data, int[] dataPos) throws IOException, IndexOutOfBoundsException {
        int currentObjectDataPos = dataPos[0];
        int objectTag = data[currentObjectDataPos];
        ++currentObjectDataPos;
        dataPos[0] = currentObjectDataPos;

        Object responseObject = null;
        switch (objectTag) {
            case NULL_TAG:
                break;
            case STRING_TAG: {
                byte encoding = data[currentObjectDataPos];
                if (encoding != STRING_ENCODING_UTF8 && encoding != STRING_ENCODING_UTF16LE && encoding != STRING_ENCODING_UTF16BE)
                    throw new IOException("unsupported encoding " + encoding);

                ++currentObjectDataPos;
                int len = ((data[currentObjectDataPos] & 0xff) << 24) | ((data[currentObjectDataPos + 1] & 0xff) << 16) |
                        ((data[currentObjectDataPos + 2] & 0xff) << 8) | (data[currentObjectDataPos + 3] & 0xff);
                currentObjectDataPos += 4;

                if (len > MAX_SIZE) {
                    throw new IOException("Data structure exceeds maximum (" + len + " > " + MAX_SIZE);
                }

                if (encoding == STRING_ENCODING_UTF8)
                    responseObject = new String(data, currentObjectDataPos, len, "UTF-8");
                else if (encoding == STRING_ENCODING_UTF16LE)
                    responseObject = new String(data, currentObjectDataPos, len, "UTF-16LE");
                else
                    responseObject = new String(data, currentObjectDataPos, len, "UTF-16BE");

                currentObjectDataPos += len;
                dataPos[0] = currentObjectDataPos;
                break;
            }
            case FALSE_TAG:
                responseObject = new Boolean(false);
                break;
            case TRUE_TAG:
                responseObject = new Boolean(true);
                break;
            case BYTE_TAG:
                responseObject = new Byte(data[currentObjectDataPos]);
                dataPos[0] = currentObjectDataPos + 1;
                break;
            case SHORT_TAG:
                responseObject = new Short((short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff)));
                dataPos[0] = currentObjectDataPos + 2;
                break;
            case INT_TAG:
                responseObject = new Integer(((data[currentObjectDataPos] & 0xff) << 24) | ((data[currentObjectDataPos + 1] & 0xff) << 16) |
                        ((data[currentObjectDataPos + 2] & 0xff) << 8) | (data[currentObjectDataPos + 3] & 0xff));
                dataPos[0] = currentObjectDataPos + 4;
                break;
            case LONG_TAG:
            case DATE_TAG: {
                int i1 = ((data[currentObjectDataPos] & 0xff) << 24) | ((data[currentObjectDataPos + 1] & 0xff) << 16) |
                        ((data[currentObjectDataPos + 2] & 0xff) << 8) | (data[currentObjectDataPos + 3] & 0xff);
                int i2 = ((data[currentObjectDataPos + 4] & 0xff) << 24) | ((data[currentObjectDataPos + 5] & 0xff) << 16) |
                        ((data[currentObjectDataPos + 6] & 0xff) << 8) | (data[currentObjectDataPos + 7] & 0xff);
                long longValue = ((i1 & 0xffffffffL) << 32) | (i2 & 0xffffffffL);
                if (objectTag == LONG_TAG) {
                    responseObject = new Long(longValue);
                } else {
                    responseObject = new Date(longValue);
                }
                dataPos[0] = currentObjectDataPos + 8;
                break;
            }
            case LIST_TAG: {
                int len = ((data[currentObjectDataPos] & 0xff) << 24) | ((data[currentObjectDataPos + 1] & 0xff) << 16) |
                        ((data[currentObjectDataPos + 2] & 0xff) << 8) | (data[currentObjectDataPos + 3] & 0xff);
                currentObjectDataPos += 4;

                if (len > MAX_SIZE) {
                    throw new IOException("Data structure exceeds maximum (" + len + " > " + MAX_SIZE);
                }

                dataPos[0] = currentObjectDataPos;
                List list = new ArrayList(len);
                for (int i = len; --i >= 0; ) {
                    list.add(fastDecodeObject(data, dataPos));
                }
                responseObject = list;
                break;
            }
            case BLOB_TAG: {
                int len = ((data[currentObjectDataPos] & 0xff) << 24) | ((data[currentObjectDataPos + 1] & 0xff) << 16) |
                        ((data[currentObjectDataPos + 2] & 0xff) << 8) | (data[currentObjectDataPos + 3] & 0xff);
                currentObjectDataPos += 4;

                if (len > MAX_SIZE) {
                    throw new IOException("Data structure exceeds maximum (" + len + " > " + MAX_SIZE);
                }

                byte[] result = new byte[len];
                System.arraycopy(data, currentObjectDataPos, result, 0, len);
                dataPos[0] = currentObjectDataPos + len;
                responseObject = result;
                break;
            }
            case ARRAYMAP_TAG: {
                int len = ((data[currentObjectDataPos] & 0xff) << 24) | ((data[currentObjectDataPos + 1] & 0xff) << 16) |
                        ((data[currentObjectDataPos + 2] & 0xff) << 8) | (data[currentObjectDataPos + 3] & 0xff);
                currentObjectDataPos += 4;

                if (len > MAX_SIZE) {
                    throw new IOException("Data structure exceeds maximum (" + len + " > " + MAX_SIZE);
                }

                dataPos[0] = currentObjectDataPos;
                ArrayMap map = new ArrayMap(len);
                for (int i = len; --i >= 0; ) {
                    // Use special uncheckedAdd() method for speed.
                    // Uniqueness of keys does not need to be checked, as map starts out empty and
                    // the data came from an ArrayMap.
                    map.uncheckedAdd(fastDecodeObject(data, dataPos), fastDecodeObject(data, dataPos));
                }
                responseObject = map;
                break;
            }
            case INTARRAYMAP_TAG: {
                int len = ((data[currentObjectDataPos] & 0xff) << 24) | ((data[currentObjectDataPos + 1] & 0xff) << 16) |
                        ((data[currentObjectDataPos + 2] & 0xff) << 8) | (data[currentObjectDataPos + 3] & 0xff);
                currentObjectDataPos += 4;

                if (len > MAX_SIZE) {
                    throw new IOException("Data structure exceeds maximum (" + len + " > " + MAX_SIZE);
                }

                dataPos[0] = currentObjectDataPos;
                IntArrayMap map = new IntArrayMap(len);
                for (int i = len; --i >= 0; ) {
                    // Use special uncheckedAdd() method for speed.
                    // Uniqueness of keys does not need to be checked, as map starts out empty and
                    // the data came from an IntArrayMap.
                    currentObjectDataPos = dataPos[0];
                    int key = ((data[currentObjectDataPos] & 0xff) << 24) | ((data[currentObjectDataPos + 1] & 0xff) << 16) |
                            ((data[currentObjectDataPos + 2] & 0xff) << 8) | (data[currentObjectDataPos + 3] & 0xff);
                    dataPos[0] = currentObjectDataPos + 4;
                    map.uncheckedAdd(key, fastDecodeObject(data, dataPos));
                }

                responseObject = map;
                break;
            }
            case ERROR_TAG: {
                int errorCode = ((data[currentObjectDataPos] & 0xff) << 24) | ((data[currentObjectDataPos + 1] & 0xff) << 16) |
                        ((data[currentObjectDataPos + 2] & 0xff) << 8) | (data[currentObjectDataPos + 3] & 0xff);
                currentObjectDataPos += 4;
                int resultCode = ((data[currentObjectDataPos] & 0xff) << 24) | ((data[currentObjectDataPos + 1] & 0xff) << 16) |
                        ((data[currentObjectDataPos + 2] & 0xff) << 8) | (data[currentObjectDataPos + 3] & 0xff);
                currentObjectDataPos += 4;
                dataPos[0] = currentObjectDataPos;
                String descr = (String) fastDecodeObject(data, dataPos);
                Z7Error nested = (Z7Error) fastDecodeObject(data, dataPos);
                responseObject = new Z7Error(new Z7ErrorCode(errorCode), new Z7Result(resultCode), descr, nested);
                break;
            }
            case TIME_ZONE_TAG: {
                short standardBias = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                short standardStartMonth = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                short standardStartDay = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                short standardStartDayOfWeek = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                short standardStartHour = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                short standardStartMinute = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                short daylightBias = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                short daylightStartMonth = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                short daylightStartDay = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                short daylightStartDayOfWeek = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                short daylightStartHour = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                short daylightStartMinute = (short) (((data[currentObjectDataPos] & 0xff) << 8) | (data[currentObjectDataPos + 1] & 0xff));
                currentObjectDataPos += 2;
                dataPos[0] = currentObjectDataPos;
                if (standardStartMonth == 0 || daylightStartMonth == 0)
                    responseObject = new Z7TimeZone(standardBias);
                else
                    responseObject = new Z7TimeZone(standardBias, standardStartMonth, standardStartDay, standardStartDayOfWeek, standardStartHour, standardStartMinute,
                            daylightBias, daylightStartMonth, daylightStartDay, daylightStartDayOfWeek, daylightStartHour, daylightStartMinute);
                break;
            }
            case TRANSPORT_ADDRESS_TAG: {
                long l1 = (((long) data[currentObjectDataPos] & 0xff) << 56) |
                        ((long) (data[currentObjectDataPos + 1] & 0xff) << 48) |
                        ((long) (data[currentObjectDataPos + 2] & 0xff) << 40) |
                        ((long) (data[currentObjectDataPos + 3] & 0xff) << 32) |
                        ((long) (data[currentObjectDataPos + 4] & 0xff) << 24) |
                        ((long) (data[currentObjectDataPos + 5] & 0xff) << 16) |
                        ((long) (data[currentObjectDataPos + 6] & 0xff) << 8) |
                        (data[currentObjectDataPos + 7] & 0xff);
                responseObject = new Z7TransportAddress(l1, data[currentObjectDataPos + 8]);
                dataPos[0] = currentObjectDataPos + 9;
                break;
            }
            default:
                throw new IOException("Bad marshal tag = " + objectTag);
        }

        return responseObject;
    }

    /**
     * Encodes given object and returns a byte array containing the encoded
     * data. This version uses direct byte array manipulation and is faster
     * than the old encode implementation, which needs to make calls to a
     * lot of virtual methods
     *
     * @param o The object to encode
     * @return A byte array containing the encoded data
     * @throws java.io.IOException
     */
    public static byte[] fastEncode(Object o) throws IOException {
        byte[] buffer = new byte[500];
        int[] position = new int[1];
        buffer = fastEncode(o, buffer, position);
        if (position[0] != buffer.length) {
            byte[] newBuffer = new byte[position[0]];
            System.arraycopy(buffer, 0, newBuffer, 0, position[0]);
            buffer = newBuffer;
        }
        return buffer;
    }

    /**
     * Encode given object and writes the resulting data into the given
     * stream. This version uses direct byte array manipulation and is faster
     * than the old encode implementation, which needs to make calls to a
     * lot of virtual methods
     *
     * @param o  The object to encode
     * @param os the stream to marshal to
     */
    public static void fastEncode(Object o, OutputStream os) throws IOException {
        byte[] buffer = new byte[500];
        int[] position = new int[1];
        buffer = fastEncode(o, buffer, position);
        os.write(buffer, 0, position[0]);
    }

    private static byte[] enlargeBuffer(byte[] buffer, int minimumRequiredSize) {
        int newSize = buffer.length * 2;
        if (minimumRequiredSize > newSize) {
            newSize = minimumRequiredSize;
        }
        byte[] newBuffer = new byte[newSize];
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        return newBuffer;
    }

    private static byte[] fastEncode(Object o, byte[] buffer, int[] position) throws IOException {
        int currentPosition = position[0];
        if (o == null) {
            if (currentPosition + 1 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 1);
            }
            buffer[currentPosition] = (byte) NULL_TAG;
            position[0] = currentPosition + 1;
        } else if (o instanceof ArrayMap) {
            ArrayMap map = (ArrayMap) o;
            if (currentPosition + 5 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 5);
            }
            buffer[currentPosition] = (byte) ARRAYMAP_TAG;
            int size = map.size();
            buffer[currentPosition + 1] = (byte) (size >> 24);
            buffer[currentPosition + 2] = (byte) (size >> 16);
            buffer[currentPosition + 3] = (byte) (size >> 8);
            buffer[currentPosition + 4] = (byte) size;
            position[0] = currentPosition + 5;

            for (int i = 0; i < size; ++i) {
                buffer = fastEncode(map.getKeyAt(i), buffer, position);
                buffer = fastEncode(map.getAt(i), buffer, position);
            }
        } else if (o instanceof IntArrayMap) {
            IntArrayMap map = (IntArrayMap) o;
            if (currentPosition + 5 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 5);
            }
            buffer[currentPosition] = (byte) INTARRAYMAP_TAG;
            int size = map.size();
            buffer[currentPosition + 1] = (byte) (size >> 24);
            buffer[currentPosition + 2] = (byte) (size >> 16);
            buffer[currentPosition + 3] = (byte) (size >> 8);
            buffer[currentPosition + 4] = (byte) size;
            position[0] = currentPosition + 5;

            for (int i = 0; i < size; ++i) {
                currentPosition = position[0];
                if (currentPosition + 4 > buffer.length) {
                    buffer = enlargeBuffer(buffer, currentPosition + 4);
                }
                int key = map.getKeyAt(i);
                buffer[currentPosition] = (byte) (key >> 24);
                buffer[currentPosition + 1] = (byte) (key >> 16);
                buffer[currentPosition + 2] = (byte) (key >> 8);
                buffer[currentPosition + 3] = (byte) key;
                position[0] = currentPosition + 4;

                buffer = fastEncode(map.getAt(i), buffer, position);
            }

        } else if (o instanceof String) {
            String s = (String) o;
            final int stringLen = s.length();
            if (currentPosition + 6 + stringLen > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 6 + stringLen);
            }
            buffer[currentPosition] = (byte) STRING_TAG;
            buffer[currentPosition + 1] = (byte) STRING_ENCODING_UTF8;
            byte[] utf8Data = StringUtil.getUTF8BytesFast(s);
            final int len = utf8Data.length;
            buffer[currentPosition + 2] = (byte) (len >> 24);
            buffer[currentPosition + 3] = (byte) (len >> 16);
            buffer[currentPosition + 4] = (byte) (len >> 8);
            buffer[currentPosition + 5] = (byte) len;
            System.arraycopy(utf8Data, 0, buffer, currentPosition + 6, len);
            position[0] = currentPosition + 6 + len;
        } else if (o instanceof Boolean) {
            if (currentPosition + 1 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 1);
            }
            buffer[currentPosition] = (byte) (((Boolean) o).booleanValue() ? TRUE_TAG : FALSE_TAG);
            ;
            position[0] = currentPosition + 1;
        } else if (o instanceof Byte) {
            if (currentPosition + 2 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 1);
            }
            buffer[currentPosition] = (byte) BYTE_TAG;
            byte val = ((Byte) o).byteValue();
            buffer[currentPosition + 1] = val;
            position[0] = currentPosition + 2;
        } else if (o instanceof Short) {
            if (currentPosition + 3 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 3);
            }
            buffer[currentPosition] = (byte) SHORT_TAG;
            int val = ((Short) o).shortValue();
            buffer[currentPosition + 1] = (byte) (val >> 8);
            buffer[currentPosition + 2] = (byte) val;
            position[0] = currentPosition + 3;
        } else if (o instanceof Integer) {
            if (currentPosition + 5 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 5);
            }
            buffer[currentPosition] = (byte) INT_TAG;
            int val = ((Integer) o).intValue();
            buffer[currentPosition + 1] = (byte) (val >> 24);
            buffer[currentPosition + 2] = (byte) (val >> 16);
            buffer[currentPosition + 3] = (byte) (val >> 8);
            buffer[currentPosition + 4] = (byte) val;
            position[0] = currentPosition + 5;
        } else if (o instanceof Long || o instanceof Date) {
            if (currentPosition + 9 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 9);
            }
            long val = 0;
            if (o instanceof Long) {
                buffer[currentPosition] = (byte) LONG_TAG;
                val = ((Long) o).longValue();
            } else {
                buffer[currentPosition] = (byte) DATE_TAG;
                val = ((Date) o).getTime();
            }

            buffer[currentPosition + 1] = (byte) (val >> 56);
            buffer[currentPosition + 2] = (byte) (val >> 48);
            buffer[currentPosition + 3] = (byte) (val >> 40);
            buffer[currentPosition + 4] = (byte) (val >> 32);
            buffer[currentPosition + 5] = (byte) (val >> 24);
            buffer[currentPosition + 6] = (byte) (val >> 16);
            buffer[currentPosition + 7] = (byte) (val >> 8);
            buffer[currentPosition + 8] = (byte) val;

            position[0] = currentPosition + 9;
        } else if (o instanceof List || o instanceof Z7ContactParameter) {
            List list = (o instanceof List) ? (List) o : ((Z7ContactParameter) o).toList();
            if (currentPosition + 5 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 5);
            }

            buffer[currentPosition] = (byte) LIST_TAG;
            final int len = list.size();
            buffer[currentPosition + 1] = (byte) (len >> 24);
            buffer[currentPosition + 2] = (byte) (len >> 16);
            buffer[currentPosition + 3] = (byte) (len >> 8);
            buffer[currentPosition + 4] = (byte) len;

            position[0] = currentPosition + 5;

            for (int i = 0; i < len; ++i) {
                buffer = fastEncode(list.get(i), buffer, position);
            }

        } else if (o instanceof byte[]) {
            byte[] dataToWrite = (byte[]) o;
            if (currentPosition + 5 + dataToWrite.length > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 5 + dataToWrite.length);
            }
            buffer[currentPosition] = (byte) BLOB_TAG;
            final int len = dataToWrite.length;
            buffer[currentPosition + 1] = (byte) (len >> 24);
            buffer[currentPosition + 2] = (byte) (len >> 16);
            buffer[currentPosition + 3] = (byte) (len >> 8);
            buffer[currentPosition + 4] = (byte) len;
            System.arraycopy(dataToWrite, 0, buffer, currentPosition + 5, len);
            position[0] = currentPosition + 5 + len;
        } else if (o instanceof Z7Error) {
            Z7Error error = (Z7Error) o;
            if (currentPosition + 9 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 9);
            }

            buffer[currentPosition] = (byte) ERROR_TAG;
            int errorCode = error.getErrorCode().m_value;
            buffer[currentPosition + 1] = (byte) (errorCode >> 24);
            buffer[currentPosition + 2] = (byte) (errorCode >> 16);
            buffer[currentPosition + 3] = (byte) (errorCode >> 8);
            buffer[currentPosition + 4] = (byte) errorCode;

            int resultCode = error.getResultCode().m_value;
            buffer[currentPosition + 5] = (byte) (resultCode >> 24);
            buffer[currentPosition + 6] = (byte) (resultCode >> 16);
            buffer[currentPosition + 7] = (byte) (resultCode >> 8);
            buffer[currentPosition + 8] = (byte) resultCode;

            position[0] = currentPosition + 9;

            buffer = fastEncode(error.getDescriptionOrParameters(), buffer, position);
            buffer = fastEncode(error.getNestedError(), buffer, position);

        } else if (o instanceof Z7TimeZone) {
            Z7TimeZone tz = (Z7TimeZone) o;
            if (currentPosition + 25 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 25);
            }

            buffer[currentPosition] = (byte) TIME_ZONE_TAG;
            short value = tz.getStandardBias();
            buffer[currentPosition + 1] = (byte) (value >> 8);
            buffer[currentPosition + 2] = (byte) value;
            value = tz.getStandardStartMonth();
            buffer[currentPosition + 3] = (byte) (value >> 8);
            buffer[currentPosition + 4] = (byte) value;
            value = tz.getStandardStartDay();
            buffer[currentPosition + 5] = (byte) (value >> 8);
            buffer[currentPosition + 6] = (byte) value;
            value = tz.getStandardStartDayOfWeek();
            buffer[currentPosition + 7] = (byte) (value >> 8);
            buffer[currentPosition + 8] = (byte) value;
            value = tz.getStandardStartHour();
            buffer[currentPosition + 9] = (byte) (value >> 8);
            buffer[currentPosition + 10] = (byte) value;
            value = tz.getStandardStartMinute();
            buffer[currentPosition + 11] = (byte) (value >> 8);
            buffer[currentPosition + 12] = (byte) value;
            value = tz.getDaylightBias();
            buffer[currentPosition + 13] = (byte) (value >> 8);
            buffer[currentPosition + 14] = (byte) value;
            value = tz.getDaylightStartMonth();
            buffer[currentPosition + 15] = (byte) (value >> 8);
            buffer[currentPosition + 16] = (byte) value;
            value = tz.getDaylightStartDay();
            buffer[currentPosition + 17] = (byte) (value >> 8);
            buffer[currentPosition + 18] = (byte) value;
            value = tz.getDaylightStartDayOfWeek();
            buffer[currentPosition + 19] = (byte) (value >> 8);
            buffer[currentPosition + 20] = (byte) value;
            value = tz.getDaylightStartHour();
            buffer[currentPosition + 21] = (byte) (value >> 8);
            buffer[currentPosition + 22] = (byte) value;
            value = tz.getDaylightStartMinute();
            buffer[currentPosition + 23] = (byte) (value >> 8);
            buffer[currentPosition + 24] = (byte) value;

            position[0] = currentPosition + 25;

        } else if (o instanceof Z7TransportAddress) {
            Z7TransportAddress address = (Z7TransportAddress) o;
            if (currentPosition + 10 > buffer.length) {
                buffer = enlargeBuffer(buffer, currentPosition + 10);
            }

            buffer[currentPosition] = (byte) TRANSPORT_ADDRESS_TAG;
            long value = address.getHostId();
            buffer[currentPosition + 1] = (byte) (value >> 56);
            buffer[currentPosition + 2] = (byte) (value >> 48);
            buffer[currentPosition + 3] = (byte) (value >> 40);
            buffer[currentPosition + 4] = (byte) (value >> 32);
            buffer[currentPosition + 5] = (byte) (value >> 24);
            buffer[currentPosition + 6] = (byte) (value >> 16);
            buffer[currentPosition + 7] = (byte) (value >> 8);
            buffer[currentPosition + 8] = (byte) value;
            buffer[currentPosition + 9] = address.getInstanceId();
            position[0] = currentPosition + 10;
        } else {
            throw new IOException("Cannot marshal object: " + o + " of type:" + o.getClass().getName());
        }

        return buffer;
    }

    /**
     * Decodes and returns the object represented in the specified byte array.
     *
     * @param b the byte array containing marshalled data
     * @return the reconstituted object
     */
    public static Object decode(byte[] b) throws IOException {
        return decode(new ByteArrayInputStream(b));
    }

    /**
     * Decodes and returns the object represented in the specified stream.
     *
     * @param is the stream containing marshalled data
     * @return the reconstituted object
     */
    public static Object decode(InputStream is) throws IOException {
        Marshaller m = new Marshaller(is);
        return m.read();
    }

    /**
     * Create a new Marshaller on the specified output stream.
     *
     * @param strm - the output stream to marshal to
     */
    public Marshaller(OutputStream strm) {
        m_out = (strm instanceof DataOutputStream) ? (DataOutputStream) strm : new DataOutputStream(strm);
    }

    public static int encodedSize(Object o) throws IOException {
        CountOutputStream cos = new CountOutputStream();
        Marshaller m = new Marshaller(cos);
        m.write(o);
        return cos.count;
    }

    public void flush() throws IOException {
        m_out.flush();
    }

    private static class CountOutputStream extends OutputStream {
        int count = 0;

        public void write(int i) {
            count++;
        }

        public void write(byte[] buf, int offset, int len) {
            count += len;
        }
    }

    /* --------------- Output ----------------- */

    /**
     * Marshal an object.
     *
     * @param o the object to marshal.
     */
    public void write(Object o) throws IOException {
        if (o == null)
            writeNull();
        else if (o instanceof ArrayMap)
            writeArrayMap((ArrayMap) o);
        else if (o instanceof IntArrayMap)
            writeIntArrayMap((IntArrayMap) o);
        else if (o instanceof String)
            writeString((String) o);
        else if (o instanceof Boolean)
            writeBoolean((Boolean) o);
        else if (o instanceof Byte)
            writeByte((Byte) o);
        else if (o instanceof Short)
            writeShort((Short) o);
        else if (o instanceof Integer)
            writeInt((Integer) o);
        else if (o instanceof Long)
            writeLong((Long) o);
/*
        else if (o instanceof Float)
            writeFloat((Float)o);
        else if (o instanceof Double)
            writeDouble((Double)o);
*/
        else if (o instanceof Date)
            writeDate((Date) o);
        else if (o instanceof List)
            writeList((List) o);
        else if (o instanceof byte[])
            writeBlobData((byte[]) o);
        else if (o instanceof Z7Error)
            writeError((Z7Error) o);

        else if (o instanceof Z7TimeZone)
            writeTimeZone((Z7TimeZone) o);
        else if (o instanceof Z7TransportAddress)
            writeZ7TransportAddress((Z7TransportAddress) o);
        else if (o instanceof Z7ContactParameter) {
            writeList(((Z7ContactParameter) o).toList());
        } else
            throw new IOException("Cannot marshal object: " + o + " of type:" + o.getClass().getName());
    }

    public void writeNull() throws IOException {
        m_out.writeByte(NULL_TAG);
    }

    public void writeArrayMap(ArrayMap o) throws IOException {
        final int size = o.size();
        m_out.writeByte(ARRAYMAP_TAG);
        m_out.writeInt(size);
        for (int i = 0; i < size; i++) {
            write(o.getKeyAt(i));
            write(o.getAt(i));
        }
    }

    public void writeIntArrayMap(IntArrayMap o) throws IOException {
        final int size = o.size();
        m_out.writeByte(INTARRAYMAP_TAG);
        m_out.writeInt(size);
        for (int i = 0; i < size; i++) {
            m_out.writeInt(o.getKeyAt(i));
            write(o.getAt(i));
        }
    }

    public void writeList(List l) throws IOException {
        m_out.writeByte(LIST_TAG);
        final int len = l.size();
        m_out.writeInt(len);
        for (int i = 0; i < len; i++) {
            write(l.get(i));
        }
    }

    public void writeBlobData(byte[] b) throws IOException {
        m_out.writeByte(BLOB_TAG);
        m_out.writeInt(b.length);
        m_out.write(b);
    }

    public void writeString(String s) throws IOException {
        /*
        int len = s.length();
        int utf8len = len*3; //worst case utf8 length must fit in a short
        if (utf8len > 32767) {
            //write a sequence of utf8 chunks
            m_out.writeByte(BIGSTRING_TAG);
            m_out.writeInt(len);
            int offset = 0;
            int chunksize = 16384;
            while (len > 0) {
                if (len > chunksize) {
                    m_out.writeUTF(s.substring(offset, offset+chunksize));
                    len -= chunksize;
                    offset += chunksize;
                } else {
                    m_out.writeUTF(s.substring(offset));
                    len -= len;
                }
            }
        } else {*/
        m_out.writeByte(STRING_TAG);
        // TODO m_out.writeUTF(s);
        m_out.writeByte(STRING_ENCODING_UTF8);
        byte[] buf = StringUtil.getUTF8Bytes(s);
        m_out.writeInt(buf.length);
        m_out.write(buf);
        //}
    }

    public void writeError(Z7Error e) throws IOException {
        m_out.writeByte(ERROR_TAG);
        m_out.writeInt(e.getErrorCode().m_value);
        m_out.writeInt(e.getResultCode().m_value);
        //Update for https://jira.seven.com/requests/browse/ZSEVEN-14077
        //If there are parameters, then use send back the parameters instead of
        //original description
        write(e.getDescriptionOrParameters());
        write(e.getNestedError());
    }

    public void writeTimeZone(Z7TimeZone tz) throws IOException {
        m_out.writeByte(TIME_ZONE_TAG);
        m_out.writeShort(tz.getStandardBias());
        m_out.writeShort(tz.getStandardStartMonth());
        m_out.writeShort(tz.getStandardStartDay());
        m_out.writeShort(tz.getStandardStartDayOfWeek());
        m_out.writeShort(tz.getStandardStartHour());
        m_out.writeShort(tz.getStandardStartMinute());
        m_out.writeShort(tz.getDaylightBias());
        m_out.writeShort(tz.getDaylightStartMonth());
        m_out.writeShort(tz.getDaylightStartDay());
        m_out.writeShort(tz.getDaylightStartDayOfWeek());
        m_out.writeShort(tz.getDaylightStartHour());
        m_out.writeShort(tz.getDaylightStartMinute());
    }

    public void writeZ7TransportAddress(Z7TransportAddress ta) throws IOException {
        m_out.writeByte(TRANSPORT_ADDRESS_TAG);
        m_out.writeLong(ta.getHostId());
        m_out.writeByte(ta.getInstanceId());
    }

    public void writeDate(Date d) throws IOException {
        m_out.writeByte(DATE_TAG);
        m_out.writeLong(d.getTime());
    }

    public void writeBoolean(Boolean b) throws IOException {
        m_out.writeByte(b.booleanValue() ? TRUE_TAG : FALSE_TAG);
    }

    public void writeBoolean(boolean b) throws IOException {
        m_out.writeByte(b ? TRUE_TAG : FALSE_TAG);
    }

    public void writeShort(Short i) throws IOException {
        m_out.writeByte(SHORT_TAG);
        m_out.writeShort(i.shortValue());
    }

    public void writeShort(short i) throws IOException {
        m_out.writeByte(SHORT_TAG);
        m_out.writeShort(i);
    }

    public void writeByte(Byte i) throws IOException {
        m_out.writeByte(BYTE_TAG);
        m_out.writeByte(i.byteValue());
    }

    public void writeByte(byte i) throws IOException {
        m_out.writeByte(BYTE_TAG);
        m_out.writeByte(i);
    }

    public void writeInt(Integer i) throws IOException {
        m_out.writeByte(INT_TAG);
        m_out.writeInt(i.intValue());
    }

    public void writeInt(int i) throws IOException {
        m_out.writeByte(INT_TAG);
        m_out.writeInt(i);
    }

    public void writeLong(Long i) throws IOException {
        m_out.writeByte(LONG_TAG);
        m_out.writeLong(i.longValue());
    }

    public void writeLong(long i) throws IOException {
        m_out.writeByte(LONG_TAG);
        m_out.writeLong(i);
    }
    /*
    public void writeFloat(Float f) throws IOException {
        m_out.writeByte(FLOAT_TAG);
        m_out.writeFloat(f.floatValue());
    }
    public void writeDouble(Double f) throws IOException {
        m_out.writeByte(DOUBLE_TAG);
        m_out.writeDouble(f.doubleValue());
    }
    */

    /* --------------- Input ----------------- */

    /**
     * Create  Marshaller object to read a marshalled stream.
     *
     * @param strm the input stream to decode
     */
    public Marshaller(InputStream strm) {
        m_in = (strm instanceof DataInputStream) ? (DataInputStream) strm : new DataInputStream(strm);
    }

    public Object read() throws IOException {
        return decodeObject(m_in.readByte());
    }

    private Object decodeObject(int tag) throws IOException {
        switch (tag) {
            case NULL_TAG:
                return null;
            case STRING_TAG:
                return readStringData(); // TODO! m_in.readUTF();
            case FALSE_TAG:
                return new Boolean(false);
            case TRUE_TAG:
                return new Boolean(true);
            case BYTE_TAG:
                return new Byte(m_in.readByte());
            case SHORT_TAG:
                return new Short(m_in.readShort());
            case INT_TAG:
                return new Integer(m_in.readInt());
            case LONG_TAG:
                return new Long(m_in.readLong());
            /*
            case FLOAT_TAG: return new Float(m_in.readFloat());
            case DOUBLE_TAG: return new Double(m_in.readDouble());
            */
            case DATE_TAG:
                return new Date(m_in.readLong());
            case LIST_TAG:
                return readListData();
            case BLOB_TAG:
                return readBlobData();
            case ARRAYMAP_TAG:
                return readArrayMapData();
            case INTARRAYMAP_TAG:
                return readIntArrayMapData();
            /*
            case BIGSTRING_TAG: return readBigStringData();
            */
            case ERROR_TAG:
                return readErrorData();

            case TIME_ZONE_TAG:
                return readTimeZoneData();
            case TRANSPORT_ADDRESS_TAG:
                return readTransportAddressData();

            default:
                throw new IOException("Bad marshal tag = " + tag);
        }
    }

    public String readString() throws IOException {
        int tag = m_in.readByte();
        if (tag == NULL_TAG) return null;
        if (tag == STRING_TAG) return readStringData(); // TODO m_in.readUTF();
        throw new IOException("not a string: " + decodeObject(tag));
    }

    public int readInt() throws IOException {
        int tag = m_in.readByte();
        if (tag == INT_TAG) return m_in.readInt();
        throw new IOException("not an int: " + decodeObject(tag));
    }

    public short readByte() throws IOException {
        int tag = m_in.readByte();
        if (tag == BYTE_TAG) return m_in.readByte();
        throw new IOException("not a byte: " + decodeObject(tag));
    }

    public short readShort() throws IOException {
        int tag = m_in.readByte();
        if (tag == SHORT_TAG) return m_in.readShort();
        throw new IOException("not a short: " + decodeObject(tag));
    }

    public long readLong() throws IOException {
        int tag = m_in.readByte();
        if (tag == LONG_TAG) return m_in.readLong();
        throw new IOException("not a long: " + decodeObject(tag));
    }

    public Date readDate() throws IOException {
        int tag = m_in.readByte();
        if (tag == NULL_TAG) return null;
        if (tag == DATE_TAG) return new Date(m_in.readLong());
        throw new IOException("not a date: " + decodeObject(tag));
    }

    public boolean readBoolean() throws IOException {
        int tag = m_in.readByte();
        if (tag == TRUE_TAG) return true;
        if (tag == FALSE_TAG) return false;
        throw new IOException("not a boolean: " + decodeObject(tag));
    }

    public byte[] readBlob() throws IOException {
        int tag = m_in.readByte();
        if (tag == NULL_TAG) return null;
        if (tag == BLOB_TAG) return readBlobData();
        throw new IOException("not a blob: " + decodeObject(tag));
    }

    public Z7Error readError() throws IOException {
        int tag = m_in.readByte();
        if (tag == NULL_TAG) return null;
        if (tag == ERROR_TAG) return readErrorData();
        throw new IOException("not an error: " + decodeObject(tag));
    }

    public Z7TimeZone readTimeZone() throws IOException {
        int tag = m_in.readByte();
        if (tag == NULL_TAG) return null;
        if (tag == TIME_ZONE_TAG) return readTimeZoneData();
        throw new IOException("not a time zone: " + decodeObject(tag));
    }

    public Z7TransportAddress readTransportAddress() throws IOException {
        int tag = m_in.readByte();
        if (tag == NULL_TAG) return null;
        if (tag == TRANSPORT_ADDRESS_TAG) return readTransportAddressData();
        throw new IOException("not a transport address: " + decodeObject(tag));
    }

    public List readList() throws IOException {
        int tag = m_in.readByte();
        if (tag == NULL_TAG) return null;
        if (tag == LIST_TAG) return readListData();
        throw new IOException("not a string: " + decodeObject(tag));
    }

    public ArrayMap readArrayMap() throws IOException {
        int tag = m_in.readByte();
        if (tag == NULL_TAG) return null;
        if (tag == ARRAYMAP_TAG) return readArrayMapData();
        throw new IOException("not an array map: " + decodeObject(tag));
    }

    public IntArrayMap readIntArrayMap() throws IOException {
        int tag = m_in.readByte();
        if (tag == NULL_TAG) return null;
        if (tag == INTARRAYMAP_TAG) return readIntArrayMapData();
        throw new IOException("not an int array map: " + decodeObject(tag));
    }

    // TODO this should go away
    public String readStringData() throws IOException {
        byte encoding = m_in.readByte();
        if (encoding != STRING_ENCODING_UTF8 && encoding != STRING_ENCODING_UTF16LE && encoding != STRING_ENCODING_UTF16BE)
            throw new IOException("unsupported encoding " + encoding);
        int len = m_in.readInt();

        if (len > MAX_SIZE) {
            throw new IOException("Data structure exceeds maximum (" + len + " > " + MAX_SIZE);
        }

        byte[] buf = new byte[len];
        m_in.readFully(buf);
        if (encoding == STRING_ENCODING_UTF8)
            return new String(buf, "UTF-8");
        else if (encoding == STRING_ENCODING_UTF16LE)
            return new String(buf, "UTF-16LE");
        else
            return new String(buf, "UTF-16BE");
    }

    public List readListData() throws IOException {
        int len = m_in.readInt();

        if (len > MAX_SIZE) {
            throw new IOException("Data structure exceeds maximum (" + len + " > " + MAX_SIZE);
        }

        List l = new ArrayList(len);
        for (int i = len; --i >= 0; ) {
            l.add(read());
        }
        return l;
    }

    public byte[] readBlobData() throws IOException {
        int len = m_in.readInt();

        if (len > MAX_SIZE) {
            throw new IOException("Data structure exceeds maximum (" + len + " > " + MAX_SIZE);
        }

        byte[] result = new byte[len];
        m_in.readFully(result);
        return result;
    }

    public ArrayMap readArrayMapData() throws IOException {
        int len = m_in.readInt();

        if (len > MAX_SIZE) {
            throw new IOException("Data structure exceeds maximum (" + len + " > " + MAX_SIZE);
        }

        ArrayMap map = new ArrayMap(len);
        for (int i = len; --i >= 0; ) {
            // Use special uncheckedAdd() method for speed.
            // Uniqueness of keys does not need to be checked, as map starts out empty and
            // the data came from an ArrayMap.
            map.uncheckedAdd(read(), read());
        }
        return map;
    }

    public IntArrayMap readIntArrayMapData() throws IOException {
        int len = m_in.readInt();

        if (len > MAX_SIZE) {
            throw new IOException("Data structure exceeds maximum (" + len + " > " + MAX_SIZE);
        }

        IntArrayMap map = new IntArrayMap(len);
        for (int i = len; --i >= 0; ) {
            // Use special uncheckedAdd() method for speed.
            // Uniqueness of keys does not need to be checked, as map starts out empty and
            // the data came from an IntArrayMap.
            map.uncheckedAdd(m_in.readInt(), read());
        }
        return map;
    }

/*
    public String readBigStringData() throws IOException {
        int remaining = m_in.readInt();
        StringBuffer buf = new StringBuffer(remaining);
        while (remaining > 0) {
            String s = m_in.readUTF();
            remaining -= s.length();
            buf.append(s);
        }
        return buf.toString();
    }
    */

    public Z7Error readErrorData() throws IOException {
        int errorCode = m_in.readInt();
        int resultCode = m_in.readInt();
        String descr = readString();
        Z7Error nested = readError();
        return new Z7Error(new Z7ErrorCode(errorCode), new Z7Result(resultCode), descr, nested);
    }


    public Z7TimeZone readTimeZoneData() throws IOException {
        short standardBias = m_in.readShort();
        short standardStartMonth = m_in.readShort();
        short standardStartDay = m_in.readShort();
        short standardStartDayOfWeek = m_in.readShort();
        short standardStartHour = m_in.readShort();
        short standardStartMinute = m_in.readShort();
        short daylightBias = m_in.readShort();
        short daylightStartMonth = m_in.readShort();
        short daylightStartDay = m_in.readShort();
        short daylightStartDayOfWeek = m_in.readShort();
        short daylightStartHour = m_in.readShort();
        short daylightStartMinute = m_in.readShort();
        if (standardStartMonth == 0 || daylightStartMonth == 0)
            return new Z7TimeZone(standardBias);
        else
            return new Z7TimeZone(standardBias, standardStartMonth, standardStartDay, standardStartDayOfWeek, standardStartHour, standardStartMinute,
                    daylightBias, daylightStartMonth, daylightStartDay, daylightStartDayOfWeek, daylightStartHour, daylightStartMinute);
    }

    public Z7TransportAddress readTransportAddressData() throws IOException {
        return new Z7TransportAddress(m_in.readLong(), m_in.readByte());
    }

}
