package com.seven.asimov.it.utils;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.StreamedHash;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Random;

public final class TestUtil {

    private static final String TAG = TestUtil.class.getSimpleName();
    private static final Logger logger = LoggerFactory.getLogger(TestUtil.class.getSimpleName());

    private static Random random = new Random();
    protected static final String[] S = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
            "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6",
            "7", "8", "9", "0"};

    private TestUtil() {
    }

    public static void sleep(long millis, long msSince) {
        sleep(millis - (System.currentTimeMillis() - msSince));
    }

    public static void sleep(long time) {
        if (time >= 0) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String generationRandomString() {
        return generationRandomString(30);
    }

    public static String generationRandomString(int length) {
        char[] sb = new char[length];
        for (int i = 0; i < length; i++) {
            sb[i] = S[random.nextInt(S.length - 1)].charAt(0);
        }
        return String.valueOf(sb);
    }

    private static boolean runRadioUpThread;
    private static int RADIO_UP_PING_INTERVAL = 4 * 1000;

    public static byte[] merge(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    public static void switchRadioUpStart() throws Exception {
        runRadioUpThread = true;
        runRadioUpThread();
        logger.trace(TAG, "RadioUp thread has been started");
    }

    public static void switchRadioUpStop() throws Exception {
        runRadioUpThread = false;
    }

    private static void runRadioUpThread() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (runRadioUpThread) {
                    try {
                        Runtime.getRuntime().exec("ping -w1 " + "1.2.3.4");
                        Thread.sleep(RADIO_UP_PING_INTERVAL);
                    } catch (Exception e) {
                        logger.error(TAG, "RadioUp Thread interrupted " + e.getMessage());
                    }
                }
                logger.trace(TAG, "RadioUp thread has been stopped");
            }
        }).start();
    }

    public static String generateString(Random rng, String characters, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

    public static String generateString(int length) {
        Random random = new Random();
        return generateString(random, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz", length);

    }

    public static void doCmd(String cmd) throws Exception {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream os = new DataOutputStream(process.getOutputStream());

        os.writeBytes(cmd + "\n");
        os.writeBytes("exit\n");
        os.flush();
        os.close();
    }

    /**
     * Calculates streamed hash of specified string (string is filled by once character, like "aaaaaaaaa").
     *
     * @param c      character to fill
     * @param length length of string
     * @return streamed hash of string
     */
    public static byte[] getStreamedHash(char c, int length) {
        StreamedHash sh = new StreamedHash();
        int bufSize = 1024;
        int fullBufs = length / bufSize;
        if (fullBufs > 0) {
            char[] chars = new char[bufSize];
            Arrays.fill(chars, c);
            byte[] bytes = new String(chars).getBytes();
            for (int i = 0; i < fullBufs; i++) {
                sh.append(bytes);
            }
        }
        int lastBufLen = length - fullBufs * bufSize;
        char[] chars = new char[lastBufLen];
        Arrays.fill(chars, c);
        sh.append(new String(chars).getBytes());
        return sh.getHash();
    }

    /**
     * Send command to OC control to change OC behavior
     *
     * @param command
     * @return
     */
    public static boolean sendOCControlCommand(String command) {
        final String OC_CONTROL_COMMAND_BASE_URL = "http://localhost/oc/";
        String uriString = OC_CONTROL_COMMAND_BASE_URL + command;

        boolean result = false;
        HttpRequest request = AsimovTestCase.createRequest().setUri(uriString).setMethod("GET").getRequest();
        HttpResponse response = null;
        try {
            response = AsimovTestCase.sendRequest(request);
            if (HttpStatus.SC_OK == response.getStatusCode()) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
