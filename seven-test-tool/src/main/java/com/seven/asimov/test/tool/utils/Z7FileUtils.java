package com.seven.asimov.test.tool.utils;

import android.os.Environment;
import android.util.Log;
import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.core.TestFactory;
import com.seven.asimov.test.tool.preferences.SharedPrefs;
import com.seven.asimov.test.tool.serialization.TestItem;
import com.seven.asimov.test.tool.serialization.TestSuite;

import java.io.*;

/**
 * FileUtils.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public abstract class Z7FileUtils {

    private static final String LOG = "FileUtils";

    public static final String EXTERNAL_DIR_TESTSUITE = "/7Test/tests/";
    public static final String EXTERNAL_DIR_DDMS_LOG = "/7Test/logs/ddms/";
    public static final String EXTERNAL_DIR_PCAP_LOG = "/7Test/logs/pcap/";
    public static final String EXTERNAL_DIR_LOGCAT_LOG = "/7Test/logs/logcat/";
    public static final String EXTERNAL_DIR_CONFIG = "/7Test/config/";

    public static final String CONFIG_FILE = "7Test.properties";

    public static final String INTERNAL_DIR_DDMS_LOG = "logs/ddms";
    public static final String INTERNAL_DIR_PCAP_LOG = "logs/pcap";
    public static final String INTERNAL_DIR_LOGCAT_LOG = "logs/logcat";
    public static final String INTERNAL_DIR_7TP = "config/7tp";

    public static final String TESTSUITE_FILE_EXTENSION = ".xml";

    public static boolean loadTestSuiteFromSd(String testSuiteName) {
        boolean result = false;
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File sdcard = Environment.getExternalStorageDirectory();
                File dir = new File(sdcard.getAbsolutePath() + EXTERNAL_DIR_TESTSUITE);
                if (!dir.exists()) {
                    return result;
                }
                File file = new File(dir, testSuiteName + TESTSUITE_FILE_EXTENSION);
                if (!file.exists()) {
                    return result;
                }

                ByteArrayOutputStream xmlArray = new ByteArrayOutputStream();
                BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = is.read(buffer)) != Constants.EOF) {
                    xmlArray.write(buffer, 0, bytesRead);
                }
                is.close();

                TestSuite testSuite = (TestSuite) Util.deserializeXMLToObject(TestSuite.class, xmlArray.toByteArray());
                for (TestItem test : testSuite.getTestItems()) {
                    if (!test.getRequestHeaders().contains(Constants.HTTP_NEW_LINE)) {
                        test.setRequestHeaders(test.getRequestHeaders().replace(Constants.HTTP_NEW_LINE_CHROME,
                                Constants.HTTP_NEW_LINE));
                    }
                    test.setHttpMethod(Z7HttpUtil.getHttpMethodFromHeaders(test.getRequestHeaders()));
                    test.setUri(Z7HttpUtil.getUriFromHeaders(test.getRequestHeaders()));
                }
                TestFactory.setTestSuite(testSuite);

                result = true;
                return result;
            }
        } catch (Exception e) {
            Log.e(LOG, e.getMessage());
        }
        return result;
    }

    public static boolean saveTestSuiteToSd(File file, String testSuiteName) {
        Log.v(LOG, "saveTestSuiteToSd(): " + testSuiteName);
        try {
            TestFactory.getTestSuite().setName(testSuiteName);
            PrintWriter out = new PrintWriter(new FileWriter(file, false));
            out.print(new String(Util.serializeObjectToXML(TestFactory.getTestSuite())));
            out.close();
            SharedPrefs.saveLastTestSuiteFromSd(testSuiteName);
            return true;
        } catch (Exception ex) {
            Log.e(LOG, ex.getMessage());
            return false;
        }
    }

    public static boolean deleteTestSuiteFromSd(String testSuiteName) {
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + EXTERNAL_DIR_TESTSUITE);
        if (dir.exists()) {
            File file = new File(dir, testSuiteName + TESTSUITE_FILE_EXTENSION);
            if (file.exists()) {
                if (file.delete()) {
                    return true;
                }
            }
        }
        return false;
    }
}
