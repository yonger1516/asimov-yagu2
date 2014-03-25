package com.seven.asimov.it.utils.tcpdump;

import com.seven.asimov.it.base.constants.TFConstantsIF;

import java.util.BitSet;

import static com.seven.asimov.it.base.constants.TFConstantsIF.BITS_IN_BYTE;

public final class TcpDumpHelper {

    public static int convertByteArrayToIntLittleEndian(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value += (bytes[i] & 0xffL) << (8 * i);
        }
        return value;
    }

    public static int convertByteArrayToIntBigEndian(byte[] bytes) {
        int value = 0;
        for (byte aByte : bytes) {
            value = (value << 8) + (aByte & 0xff);
        }
        return value;
    }

    public static String convertBytesToIp6Address(byte[] bytes) {
        int addr[]=new int[8];
        for(int i=0;i<bytes.length/2;i++){
            int left=((((int)bytes[i*2])<<8)&0x0000FF00);
            int right=(((int)bytes[i*2+1])&0x000000FF);
                addr[i]=(left|right);
            }

        return String.format("%04x:%04x:%04x:%04x:%04x:%04x:%04x:%04x",
               addr[0],
               addr[1],
               addr[2],
               addr[3],
               addr[4],
               addr[5],
               addr[6],
               addr[7]);
        }

    public static String convertBytesToIpAddress(byte[] bytes) {
        return (bytes[0] & 0xff) + "." + (bytes[1] & 0xff) + "." + (bytes[2] & 0xff) + "." + (bytes[3] & 0xff);
    }

    public static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet(bytes.length * BITS_IN_BYTE);
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            for (int j = BITS_IN_BYTE - 1; j >= 0; j--) {
                bits.set(j + i * BITS_IN_BYTE, (b & 1) == 1);
                b >>= 1;
            }
        }
        return bits;
    }

    public static BitSet getReversedBitSet(BitSet bitSet, int startIndex, int endIndex) {
        BitSet result = new BitSet(endIndex - startIndex);
        for (int i = 0; i < (endIndex - startIndex); i++) {
            result.set(i, bitSet.get(endIndex - i - 1));
        }
        return result;
    }

    public static int bitSetToInt(BitSet bitSet) {
        int res = 0, pow = 1;
        for (int i = 0; i < 32; i++, pow <<= 1) {
            if (bitSet.get(i)) {
                res |= pow;
            }
        }
        return res;
    }

    public static boolean isKnownServerPort(int port){
        return (port == TFConstantsIF.DESTINATION_PORT_HTTP)||
               (port == TFConstantsIF.DESTINATION_PORT_HTTPS)||
               (port == TFConstantsIF.DESTINATION_PORT_DNS);
    }
}
