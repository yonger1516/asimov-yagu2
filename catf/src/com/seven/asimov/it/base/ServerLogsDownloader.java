package com.seven.asimov.it.base;

import android.content.Context;
import android.util.Log;
import ch.ethz.ssh2.SCPClient;
import com.seven.asimov.it.IntegrationTestRunnerGa;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.SSHUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ServerLogsDownloader {
    private static final Logger logger = LoggerFactory.getLogger(ServerLogsDownloader.class.getSimpleName());
    private static SSHUtil sshUtil = SSHUtil.getInstance();
    private static final String splitRegexp = "@";
    private static final String SCP = "scp";
    private static final String ROOT = "root";
    private static final String RM_R = "rm -r";
    private static final String ZIP = "zip";
    private static final String[] eng002apsPartOne = TFConstantsIF.ENG002_APS_PART_ONE.split(splitRegexp);
    private static final String[] eng002apsPartTwo = TFConstantsIF.ENG002_APS_PART_TWO.split(splitRegexp);
    private static final String[] eng004apsPartOne = TFConstantsIF.ENG004_APS_PART_ONE.split(splitRegexp);
    private static final String[] eng004apsPartTwo = TFConstantsIF.ENG004_APS_PART_TWO.split(splitRegexp);
    private static final String SERVER_LOGS_FOLDER = ":/usr/local/seven/";
    private static final String E2E_AUTO_SERVER_TMP_FOLDER_HKIADM02 = "/tmp/E2E-Auto-Server-Tmp/";
    private static final String E2E_AUTO_SERVER_TMP_FOLDER_HKIADM02_TMP = "Tmp/";
    private static final String LOGS_ZIP = "logs.zip";
    private static String serverName;
    private static String[] apsPartOne;
    private static String[] apsPartTwo;

    private ServerLogsDownloader() {
    }

    private static void downloadServerLogs() throws Exception {
        try {
            if (!sshUtil.isLogined())
                sshUtil.login(TFConstantsIF.HKI_ADM02_HOST, new String(new byte[]{109, 112, 105, 115, 111, 116, 115, 107, 105, 121}), new String(new byte[]{67, 104, 97, 110, 103, 101, 77, 101}));
        } catch (IOException e) {
            logger.error("Failed connect to " + TFConstantsIF.HKI_ADM02_HOST);
            logger.error("Server logs was not copied to " + IntegrationTestRunnerGa.RESULTS_DIR);
            logger.error(ExceptionUtils.getMessage(e));
            return;
        }
        scpLogsToTmp(TFConstantsIF.SERVER_LOG_NAMES_PART_ONE.split(splitRegexp), apsPartOne);
        scpLogsToTmp(TFConstantsIF.SERVER_LOG_NAMES_PART_TWO.split(splitRegexp), apsPartTwo);
        zipLogs();
        copyLogsToDevice();
        cleanTempFilesAfterCopy();
        sshUtil.closeConnection();
    }

    private static boolean checkHost() {
        serverName = PMSUtil.getPmsServerIp().substring(0, PMSUtil.getPmsServerIp().indexOf("."));
        if (serverName == null) return false;
        if (serverName.endsWith("002")) {
            apsPartOne = eng002apsPartOne;
            apsPartTwo = eng002apsPartTwo;
        } else if (serverName.endsWith("004")) {
            apsPartOne = eng004apsPartOne;
            apsPartTwo = eng004apsPartTwo;
        } else {
            if (TFConstantsIF.CUSTOM_SERVER_APS_PART_ONE == null || TFConstantsIF.CUSTOM_SERVER_APS_PART_TWO == null) {
                logger.error("Check test.property file for CUSTOM_SERVER_APS parameters for your target. Your server is " + PMSUtil.getPmsServerIp());
                logger.error("Server logs downloading fail");
                return false;
            }
            apsPartOne = TFConstantsIF.CUSTOM_SERVER_APS_PART_ONE.split(splitRegexp);
            apsPartTwo = TFConstantsIF.CUSTOM_SERVER_APS_PART_TWO.split(splitRegexp);
            if (apsPartOne == null || apsPartOne.length == 0 || apsPartTwo == null || apsPartTwo.length == 0) {
                logger.error("Incorrect CUSTOM_SERVER_APS parameters at test.property file for your target. Your server is " + PMSUtil.getPmsServerIp());
                logger.error("Correct example: CUSTOM_SERVER_APS=ap1@ap2");
                logger.error("Server logs downloading fail");
                return false;
            }
        }
        logger.info("Will get server logs for " + serverName);
        return true;
    }

    private static void scpLogsToTmp(String[] logNames, String[] aps) throws IOException {
        for (String apNubmer : aps) {
            for (String serverLogFile : logNames) {
                sshUtil.execute(String.valueOf(new StringBuilder().append(SCP).append(" ").append(ROOT).append(splitRegexp).append(apNubmer).append(SERVER_LOGS_FOLDER).append(serverName).append("-").append(apNubmer).append("/logs/").append(serverLogFile).append(" ").append(E2E_AUTO_SERVER_TMP_FOLDER_HKIADM02).append(E2E_AUTO_SERVER_TMP_FOLDER_HKIADM02_TMP).append(apNubmer).append(serverLogFile)));
            }
        }
    }

    private static void zipLogs() throws IOException {
        sshUtil.execute(String.valueOf(new StringBuilder().append(ZIP).append(" ").append(E2E_AUTO_SERVER_TMP_FOLDER_HKIADM02).append("logs ").append(E2E_AUTO_SERVER_TMP_FOLDER_HKIADM02).append(E2E_AUTO_SERVER_TMP_FOLDER_HKIADM02_TMP).append("*")));
    }

    private static void copyLogsToDevice() throws IOException {
        SCPClient scpClient = sshUtil.getConn().createSCPClient();
        scpClient.get(E2E_AUTO_SERVER_TMP_FOLDER_HKIADM02 + LOGS_ZIP, IntegrationTestRunnerGa.RESULTS_DIR + IntegrationTestRunnerGa.LOGCATS + IntegrationTestRunnerGa.SERVER_LOGS);
    }

    private static void cleanTempFilesAfterCopy() throws IOException {
        if (sshUtil.isLogined()) {
            sshUtil.execute(String.valueOf(new StringBuilder().append(RM_R).append(" ").append(E2E_AUTO_SERVER_TMP_FOLDER_HKIADM02).append(E2E_AUTO_SERVER_TMP_FOLDER_HKIADM02_TMP).append("*")));
            sshUtil.execute(String.valueOf(new StringBuilder().append(RM_R).append(" ").append(E2E_AUTO_SERVER_TMP_FOLDER_HKIADM02).append(LOGS_ZIP)));
        }
    }

    public static void getServerLogs(Context context) {
        if (TFConstantsIF.SERVER_LOG_DOWNLOAD_ENABLED == null || !TFConstantsIF.SERVER_LOG_DOWNLOAD_ENABLED.contains("1")) {
            logger.info("Server logs downloading is not enabled");
            return;
        }
        if (!checkHost()) {
            logger.info("Server logs downloading failed. Check your pms host and test.property file");
            return;
        }
        boolean was3G = false;
        MobileNetworkUtil helper = MobileNetworkUtil.init(context);
        if (helper.isMobileDataEnabled()) {
            helper.switchWifiOnOff(true);
            was3G = true;
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                logger.error("Error while switching to WIFI network, server logs are not downloaded");
                logger.error(ExceptionUtils.getMessage(e));
            }
        }
        try {
            ServerLogsDownloader.downloadServerLogs();
        } catch (Exception e) {
            logger.error("Error while server logs downloading");
            logger.error(ExceptionUtils.getMessage(e));
        }
        if (was3G) helper.switchWifiOnOff(false);
    }
}