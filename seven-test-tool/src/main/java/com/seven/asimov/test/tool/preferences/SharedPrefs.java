package com.seven.asimov.test.tool.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import com.seven.asimov.test.tool.utils.Z7HttpsUtil;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * SharedPrefs - place to store persisting variables.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public abstract class SharedPrefs {
    private static final String HTTPS_PROTOCOLS = "HttpsProtocols";
    private static final String HTTPS_CIPHERS = "HttpsCiphers";

    // Shared preferences
    private static final String PREFS_NAME = "7TestPrefs";
    private static SharedPreferences sSharedPrefs;
    private static SharedPreferences.Editor sSharedPrefsEditor;

    private static boolean sInitiated;

    public static void setInitiated(boolean initiated) {
        sInitiated = initiated;
    }

    public static boolean isInitiated() {
        return sInitiated;
    }

    public static void init(Context context) {
        sSharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sSharedPrefsEditor = sSharedPrefs.edit();
    }

    // MainTab preferences
    public static void saveLastTestSuiteFromSd(String testSuiteName) {
        sSharedPrefsEditor.putString("LastTestSuiteFromSd", testSuiteName);
        sSharedPrefsEditor.commit();
    }

    public static String getLastTestSuiteFromSd() {
        return sSharedPrefs.getString("LastTestSuiteFromSd", null);
    }

    // TestsTab prefs
    public static void saveLastTestSuitesFromServer(ArrayList<String> testSuiteNameList) {

        resetLastTestSuitesFromServer();

        for (int i = 0; i < testSuiteNameList.size(); i++) {
            sSharedPrefsEditor.putString("LastTestSuitesFromServer_" + i, testSuiteNameList.get(i));
            sSharedPrefsEditor.commit();
        }
    }

    public static void saveLastTestSuiteFromServer(String testSuite) {

        ArrayList<String> latestTestSuiteList = SharedPrefs.getLastTestSuitesFromServer();

        boolean testSuiteAdded = false;

        for (int i = 0; i < latestTestSuiteList.size(); i++) {
            if (latestTestSuiteList.get(i).equalsIgnoreCase(testSuite)) {
                latestTestSuiteList.remove(i);
                latestTestSuiteList.add(testSuite);
                testSuiteAdded = true;
            }
        }

        if (!testSuiteAdded) {
            latestTestSuiteList.add(testSuite);
        }

        // Limit list to 10
        if (latestTestSuiteList.size() > 10) {
            latestTestSuiteList.remove(0);
        }

        SharedPrefs.saveLastTestSuitesFromServer(latestTestSuiteList);
    }

    public static void resetLastTestSuitesFromServer() {

        int count = getLastTestSuitesFromServer().size();

        for (int i = 0; i < count; i++) {
            sSharedPrefsEditor.putString("LastTestSuitesFromServer_", null);
            sSharedPrefsEditor.commit();
        }
    }

    public static ArrayList<String> getLastTestSuitesFromServer() {

        ArrayList<String> testSuites = new ArrayList<String>();

        int i = 0;
        while (true) {
            String result = sSharedPrefs.getString("LastTestSuitesFromServer_" + i, null);
            if (result == null) {
                break;
            }
            testSuites.add(result);
            i++;
        }
        return testSuites;
    }

    public static String getLastTestSuiteFromServer() {

        ArrayList<String> latestTestSuiteList = SharedPrefs.getLastTestSuitesFromServer();

        if (latestTestSuiteList.size() > 0) {
            return latestTestSuiteList.get(latestTestSuiteList.size() - 1);
        } else {
            return StringUtils.EMPTY;
        }
    }

    public static void saveLastBelongsTo(String belongsTo) {
        sSharedPrefsEditor.putString("LastBelongsTo", belongsTo);
        sSharedPrefsEditor.commit();
    }

    public static String getLastBelongsTo() {
        return sSharedPrefs.getString("LastBelongsTo", StringUtils.EMPTY);
    }

    public static void saveLastScope(String scope) {
        sSharedPrefsEditor.putString("LastScope", scope);
        sSharedPrefsEditor.commit();
    }

    public static String getLastScope() {
        return sSharedPrefs.getString("LastScope", StringUtils.EMPTY);
    }

    public static void saveTestSuiteServer(String testSuiteServer) {
        sSharedPrefsEditor.putString("TestSuiteServer", testSuiteServer);
        sSharedPrefsEditor.commit();
    }

    public static String getTestSuiteServer() {
        return sSharedPrefs.getString("TestSuiteServer", StringUtils.EMPTY);
    }

    public static void saveIsTcpDumpIsLaunched(boolean isTcpDumpIsLaunched) {
        sSharedPrefsEditor.putBoolean("TcpDumpAutoLaunch", isTcpDumpIsLaunched);
        sSharedPrefsEditor.commit();
    }

    public static void saveIsLogCatIsLaunched(boolean isLogCatIsLaunched) {
        sSharedPrefsEditor.putBoolean("LogCatAutoLaunch", isLogCatIsLaunched);
        sSharedPrefsEditor.commit();
    }

    public static boolean isTcpDumpIsLaunched() {
        return sSharedPrefs.getBoolean("TcpDumpAutoLaunch", false);
    }

    public static boolean isLogCatIsLaunched() {
        return sSharedPrefs.getBoolean("LogCatAutoLaunch", false);
    }

    public static void saveHttpsProtocols(String... protocols) {
        sSharedPrefsEditor.putString(HTTPS_PROTOCOLS, StringUtils.join(protocols, ","));
        sSharedPrefsEditor.commit();
    }

    public static String[] getHttpsProtocols() {
        String protocols = sSharedPrefs.getString(HTTPS_PROTOCOLS, null);
        return (protocols == null) ? null : protocols.split("\\s*,\\s*");
    }

    public static void saveHttpsCiphers(Map<String, Set<String>> ciphers) {
        sSharedPrefsEditor.putString(HTTPS_CIPHERS, Z7HttpsUtil.convertCiphersMapToString(ciphers));
        sSharedPrefsEditor.commit();
    }

    public static Map<String, Set<String>> getHttpsCiphers() {
        return Z7HttpsUtil.convertCiphersStringToMap(sSharedPrefs.getString(HTTPS_CIPHERS, null));
    }
}
