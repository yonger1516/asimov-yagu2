package com.seven.asimov.test.tool.core;

import com.seven.asimov.test.tool.preferences.SharedPrefs;
import org.apache.commons.lang.StringUtils;


/**
 * TestFactoryOptions.
 */

public abstract class TestFactoryOptions {

    public static void setsTestSuiteHost(String testSuiteHost) {
        SharedPrefs.saveTestSuiteServer(testSuiteHost);
    }

    public static String getsTestSuiteHost() {
        String sTestSuiteHost = SharedPrefs.getTestSuiteServer();
        if (StringUtils.isEmpty(sTestSuiteHost)) {
            sTestSuiteHost = "tln-dev-testrunner1.7sys.eu";
        }
        return sTestSuiteHost;
    }

    private static boolean sRequestKeepAlive = true;
    private static boolean sRequestExpectContinue;
    private static boolean sRequestChunked;
    private static boolean sHandleRedirect = true;
    private static boolean sRequestAcceptGzip;
    private static boolean sRequestAcceptDeflate;
    private static boolean sRequestAcceptCompress;

    public static void setRequestKeepAlive(boolean keepAlive) {
        sRequestKeepAlive = keepAlive;
    }

    public static boolean isRequestKeepAlive() {
        return sRequestKeepAlive;
    }

    public static void setRequestExpectContinue(boolean expectContinue) {
        sRequestExpectContinue = expectContinue;
    }

    public static boolean isRequestExpectContinue() {
        return sRequestExpectContinue;
    }

    public static void setRequestChunked(boolean chunked) {
        sRequestChunked = chunked;
    }

    public static boolean isRequestChunked() {
        return sRequestChunked;
    }

    public static void setHandleRedirect(boolean handleRedirect) {
        sHandleRedirect = handleRedirect;
    }

    public static boolean isHandleRedirect() {
        return sHandleRedirect;
    }

    public static void setRequestAcceptGzip(boolean acceptGzip) {
        sRequestAcceptGzip = acceptGzip;
    }

    public static boolean isRequestAcceptGzip() {
        return sRequestAcceptGzip;
    }

    public static void setRequestAcceptDeflate(boolean acceptDeflate) {
        sRequestAcceptDeflate = acceptDeflate;
    }

    public static boolean isRequestAcceptDeflate() {
        return sRequestAcceptDeflate;
    }

    public static void setRequestAcceptCompress(boolean acceptCompress) {
        sRequestAcceptCompress = acceptCompress;
    }

    public static boolean isRequestAcceptCompress() {
        return sRequestAcceptCompress;
    }
}
