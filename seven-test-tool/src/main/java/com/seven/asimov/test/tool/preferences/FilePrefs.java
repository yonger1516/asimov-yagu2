package com.seven.asimov.test.tool.preferences;

import android.os.Environment;
import com.seven.asimov.it.utils.IOUtil;
import com.seven.asimov.test.tool.receivers.SdMountReceiver;
import com.seven.asimov.test.tool.utils.Z7FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * FilePrefs - place to store configuration on SD card.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public abstract class FilePrefs {

    private static Properties sProps;

    private static final Logger LOG = LoggerFactory.getLogger(FilePrefs.class.getSimpleName());

    public static final String CONFIG_KEY_IS_TCPDUMP_IS_LAUNCHED = "IsTcpDumpIsLaunched";
    public static final String CONFIG_KEY_IS_LOGCAT_IS_LAUNCHED = "IsLogCatIsLaunched";
    public static final String CONFIG_KEY_STORE_LOGCAT_LOGS_ON_SD = "StoreLogCatLogsOnSD";
    public static final String CONFIG_KEY_LOGCAT = "LogCat";

    private static boolean sInitiated;

    public static void setInitiated(boolean initiated) {
        sInitiated = initiated;
    }

    public static boolean isInitiated() {
        return sInitiated;
    }

    public static void init() {

        sProps = new Properties();

        loadPrefsFromSd();
    }

    public static boolean loadPrefsFromSd() {

        FileInputStream in = null;

        try {

            if (!SdMountReceiver.isSdMounted()) {
                return false;
            }

            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_CONFIG + Z7FileUtils.CONFIG_FILE);

            if (!file.exists()) {
                saveDefaultPrefsToSd();
            } else {

                in = new FileInputStream(file);

                sProps.load(in);
                in.close();
            }

            return true;
        } catch (Exception e) {
            LOG.error("Could not load prefs from SD card!", e);

        } finally {
            IOUtil.safeClose(in);
        }
        return false;
    }

    public static boolean saveDefaultPrefsToSd() {

        boolean result = false;

        FileOutputStream out = null;

        try {

            while (true) {

                if (!SdMountReceiver.isSdMounted()) {
                    break;
                }

                File sdcard = Environment.getExternalStorageDirectory();

                File dir = new File(sdcard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_CONFIG);

                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        break;
                    }
                }

                File file = new File(sdcard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_CONFIG + Z7FileUtils.CONFIG_FILE);

                // Default configuration properties
                sProps.setProperty(CONFIG_KEY_IS_TCPDUMP_IS_LAUNCHED, String.valueOf(false));
                sProps.setProperty(CONFIG_KEY_IS_LOGCAT_IS_LAUNCHED, String.valueOf(false));
                sProps.setProperty(CONFIG_KEY_STORE_LOGCAT_LOGS_ON_SD, String.valueOf(true));
                sProps.setProperty(CONFIG_KEY_LOGCAT, "com.seven.asimov,com.seven.Z7.service");

                out = new FileOutputStream(file);
                sProps.store(out, "---No Comment---");
                out.close();

                result = true;
                break;

            }
        } catch (Exception e) {
            LOG.error(e.toString());
        } finally {
            IOUtil.safeClose(out);
        }

        if (!result) {
            LOG.warn("Could not save default prefs to SD card!");
        }

        return result;
    }

    // public static void saveIsTcpDumpIsLaunched(boolean isTcpDumpIsLaunched) {
    // mPrefsMap.put("IsTcpDumpIsLaunched", String.valueOf(isTcpDumpIsLaunched));
    // savePrefsToSd();
    // }

    public static boolean isTcpDumpIsLaunched() {
        return Boolean.valueOf(sProps.getProperty(CONFIG_KEY_IS_TCPDUMP_IS_LAUNCHED, "FALSE"));
    }

    public static boolean isLogCatIsLaunched() {
        return Boolean.valueOf(sProps.getProperty(CONFIG_KEY_IS_LOGCAT_IS_LAUNCHED, "FALSE"));
    }

    public static boolean isLogCatLogsStoredOnSD() {
        return Boolean.valueOf(sProps.getProperty(CONFIG_KEY_STORE_LOGCAT_LOGS_ON_SD, "TRUE"));
    }

    public static String[] getLogCatProcesses() {
        String result = null;
        result = sProps.getProperty(CONFIG_KEY_LOGCAT);
        if (result != null) {
            return result.split(",");
        }
        return null;
    }
}
