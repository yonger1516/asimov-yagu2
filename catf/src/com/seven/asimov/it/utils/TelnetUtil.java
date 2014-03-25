package com.seven.asimov.it.utils;

import android.util.Log;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.base.androidtest.FixtureSharingTestRunner;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class TelnetUtil extends FixtureSharingTestRunner {
    private static final Logger logger = LoggerFactory.getLogger(TelnetUtil.class.getSimpleName());

    private TelnetUtil() {
    }

    public static String getOutPutFromTelnetCommand(String host, Integer port) throws IOException {
        String toReturn = null;
        TelnetClient tc = new TelnetClient();
        tc.connect(host, port);
        InputStream instr = tc.getInputStream();
        try {
            byte[] buff = new byte[1024];
            int ret_read = 0;
            ret_read = instr.read(buff);
            if (ret_read > 0) {
                toReturn = new String(buff, 0, ret_read);
            }
        } catch (IOException e) {
            logger.error("Exception while reading socket: " + ExceptionUtils.getStackTrace(e));
        }
        try {
            tc.disconnect();
        } catch (IOException e) {
            logger.error("Exception while closing telnet: " + ExceptionUtils.getStackTrace(e));
        }
        return toReturn;
    }

    public static String findOutServerVersion(String host, Integer port) {
        if (TFConstantsIF.SUPPORTED_SERVER_VERSIONS != null) {
            String[] versions = TFConstantsIF.SUPPORTED_SERVER_VERSIONS.split("@");
            logger.debug("Supported server versions: " + Arrays.toString(versions));
            try {
                for (int i = 0; i < 3; i++) {
                    String response = getOutPutFromTelnetCommand(host, port);
                    for (String version : versions) {
                        if (response != null && response.contains(version)) {
                            logger.debug("Current server version is: " + version + " Expected server version is: " + TFConstantsIF.EXPECTED_SERVER_VERSION);
                            return version;
                        }
                    }
                }
            } catch (IOException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
        logger.warn("Can not find out server version! Use by default server version from property file: " + TFConstantsIF.EXPECTED_SERVER_VERSION);
        return TFConstantsIF.EXPECTED_SERVER_VERSION;
    }
}