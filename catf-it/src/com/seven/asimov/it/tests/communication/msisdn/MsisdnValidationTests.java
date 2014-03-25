package com.seven.asimov.it.tests.communication.msisdn;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.MsisdnValidationTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.PropertyLoadUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.msisdnTasks.MsisdnSendingSmsValidationTask;
import com.seven.asimov.it.utils.logcat.tasks.msisdnTasks.MsisdnValidationHttpRequestTask;
import com.seven.asimov.it.utils.logcat.tasks.msisdnTasks.MsisdnValidationSuccessTask;
import com.seven.asimov.it.utils.logcat.wrappers.MsisdnSendingSmsValidationWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.MsisdnValidationHttpRequestWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.MsisdnValidationSuccessWrapper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * <h2>This class tests MSISDN validation.</h2>
 * <p>Use target test_framework-MSISDN_validation to build TF.</p>
 * <p>Each test needs specific OCC apk placed into device /sdcard/apks/</p>
 * <p>Use next brandings for building the apks:</p>
 * <ol>
 * <li>test_framework-MSISDN_no_validation_protocol</li>
 * <li>test_framework-MSISDN_validation_disabled</li>
 * <li>test_framework-MSISDN_HTTP_rooted</li>
 * <li>test_framework-MSISDN_SMS_rooted_no_http_https</li>
 * <li>test_framework-MSISDN_SMS_wrong_validation_phone</li>
 * <li>test_framework-MSISDN_HTTP_wrong_validation_url</li>
 * </ol>
 * <p>and name each apk according to its branding:</p>
 * <ol>
 * <li>asimov-signed-MSISDN_no_validation_protocol.apk</li>
 * <li>asimov-signed-MSISDN_validation_disabled.apk</li>
 * <li>asimov-signed-MSISDN_HTTP_rooted.apk</li>
 * <li>asimov-signed-MSISDN_SMS_rooted_no_http_https.apk</li>
 * <li>asimov-signed-MSISDN_SMS_wrong_validation_phone.apk</li>
 * <li>asimov-signed-MSISDN_HTTP_wrong_validation_url.apk</li>
 * </ol>
 * <p>These brandings use eng002 server. You can make brandings that use other server, but all params that present in these brandings must be present and set properly.</p>
 * <p>Brandings features:</p>
 * <ol>
 * <li>test_framework-MSISDN_no_validation_protocol must contain client.msisdn_validation_protocol=</li>
 * <li>test_framework-MSISDN_validation_disabled must contain client.msisdn_validation_enabled=0</li>
 * <li>test_framework-MSISDN_SMS_wrong_validation_phone must contain some wrong phone: system.msisdn_validation_phonenumber=+380123456789</li>
 * <li>test_framework-MSISDN_HTTP_wrong_validation_url must contain client.msisdn_validation_url=http://nohost.com/msisdnapi/msisdn_validation_api/setAndGetMsisdnInfo.do?content=</li>
 * </ol>
 * <p>In Ukraine use MTS sim card or ignore all tests that use http validation.!!!</p>
 */

public class MsisdnValidationTests extends MsisdnValidationTestCase {

    private final String appFolder = "/sdcard" + File.separator + "apks";
    private static final Logger logger = LoggerFactory.getLogger(MsisdnValidationTests.class.getSimpleName());

    /**
     * <p>Tests that MSISDN validation is not performed when no validation protocol is set.</p>
     *
     * @throws Exception
     */
    @DeviceOnly
    @LargeTest
    public void test_001_msisdnGeneral_1() throws Exception {
        final String ocFileName = "asimov-signed-MSISDN_no_validation_protocol.apk";
        final String appPath = appFolder + File.separator + ocFileName;

        MsisdnSendingSmsValidationTask msisdnSendingSmsValidationTask = new MsisdnSendingSmsValidationTask();
        msisdnSendingSmsValidationTask.setChangeTimestampToGMT(false);
        MsisdnValidationHttpRequestTask msisdnValidationHttpRequestTask = new MsisdnValidationHttpRequestTask();
        msisdnValidationHttpRequestTask.setChangeTimestampToGMT(false);
        MsisdnValidationSuccessTask msisdnValidationSuccessTask = new MsisdnValidationSuccessTask();
        msisdnValidationSuccessTask.setChangeTimestampToGMT(false);

        LogcatUtil logcatUtil = new LogcatUtil(getContext()
                , msisdnSendingSmsValidationTask
                , msisdnValidationHttpRequestTask
                , msisdnValidationSuccessTask
        );
        logcatUtil.start();
        try {
            long timestamp = System.currentTimeMillis();

            reinstallOcc(appPath);

            logcatUtil.stop();

            logger.trace(msisdnSendingSmsValidationTask.toString());
            logger.trace(msisdnValidationHttpRequestTask.toString());
            logger.trace(msisdnValidationSuccessTask.toString());

            MsisdnSendingSmsValidationWrapper msisdnSendingSmsValidationLogEntry = msisdnSendingSmsValidationTask.getEntryAfterTimestamp(timestamp);
            assertNull("Msisdn validation shouldn't be performed!", msisdnSendingSmsValidationLogEntry);
            logger.trace("1-1 " + msisdnSendingSmsValidationLogEntry);
            MsisdnValidationHttpRequestWrapper msisdnValidationHttpRequestLogEntry = msisdnValidationHttpRequestTask.getEntryAfterTimestamp(timestamp);
            assertNull("Msisdn validation shouldn't be performed!", msisdnValidationHttpRequestLogEntry);
            logger.trace("1-2 " + msisdnValidationHttpRequestLogEntry);
            MsisdnValidationSuccessWrapper msisdnValidationSuccessLogEntry = msisdnValidationSuccessTask.getEntryAfterTimestamp(timestamp);
            assertNull("Msisdn validation shouldn't be done!", msisdnValidationSuccessLogEntry);
            logger.trace("1-3 " + msisdnValidationSuccessLogEntry);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e.getMessage());
        } finally {
            logcatUtil.stop();
        }
    }

    /**
     * <p>Tests that MSISDN validation is not performed when disabled.</p>
     *
     * @throws Exception
     */
    @DeviceOnly
    @LargeTest
    public void test_002_msisdnGeneral_2() throws Exception {
        final String ocFileName = "asimov-signed-MSISDN_validation_disabled.apk";
        final String appPath = appFolder + File.separator + ocFileName;

        MsisdnSendingSmsValidationTask msisdnSendingSmsValidationTask = new MsisdnSendingSmsValidationTask();
        msisdnSendingSmsValidationTask.setChangeTimestampToGMT(false);
        MsisdnValidationHttpRequestTask msisdnValidationHttpRequestTask = new MsisdnValidationHttpRequestTask();
        msisdnValidationHttpRequestTask.setChangeTimestampToGMT(false);
        MsisdnValidationSuccessTask msisdnValidationSuccessTask = new MsisdnValidationSuccessTask();
        msisdnValidationSuccessTask.setChangeTimestampToGMT(false);

        LogcatUtil logcatUtil = new LogcatUtil(getContext()
                , msisdnSendingSmsValidationTask
                , msisdnValidationHttpRequestTask
                , msisdnValidationSuccessTask
        );
        logcatUtil.start();

        try {
            long timestamp = System.currentTimeMillis();

            reinstallOcc(appPath);

            logcatUtil.stop();

            logger.trace(msisdnSendingSmsValidationTask.toString());
            logger.trace(msisdnValidationHttpRequestTask.toString());
            logger.trace(msisdnValidationSuccessTask.toString());

            MsisdnSendingSmsValidationWrapper msisdnSendingSmsValidationLogEntry = msisdnSendingSmsValidationTask.getEntryAfterTimestamp(timestamp);
            assertNull("Msisdn validation shouldn't be performed!", msisdnSendingSmsValidationLogEntry);
            logger.trace("2-1 " + msisdnSendingSmsValidationLogEntry);
            MsisdnValidationHttpRequestWrapper msisdnValidationHttpRequestLogEntry = msisdnValidationHttpRequestTask.getEntryAfterTimestamp(timestamp);
            assertNull("Msisdn validation shouldn't be performed!", msisdnValidationHttpRequestLogEntry);
            logger.trace("2-2 " + msisdnValidationHttpRequestLogEntry);
            MsisdnValidationSuccessWrapper msisdnValidationSuccessLogEntry = msisdnValidationSuccessTask.getEntryAfterTimestamp(timestamp);
            assertNull("Msisdn validation shouldn't be done!", msisdnValidationSuccessLogEntry);
            logger.trace("2-3 " + msisdnValidationSuccessLogEntry);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e.getMessage());
        } finally {
            logcatUtil.stop();
        }
    }

    /**
     * <p>Tests that MSISDN validation is NOT performed when there is no data coverage.</p>
     *
     * @throws Exception
     */
    @DeviceOnly
    @LargeTest
    public void test_003_msisdnGeneral_3() throws Exception {
        final String ocFileName = "asimov-signed-MSISDN_HTTP_rooted.apk";
        final String appPath = appFolder + File.separator + ocFileName;

        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        int status = mobileNetworkUtil.switchWifiOnOff(false);
        assertEquals("Can't off wifi!", status, 1);
        status = mobileNetworkUtil.switchMobileDataOnOff(false);
        assertEquals("Can't off 3G!", status, 1);

        MsisdnValidationHttpRequestTask msisdnValidationHttpRequestTask = new MsisdnValidationHttpRequestTask();
        msisdnValidationHttpRequestTask.setChangeTimestampToGMT(false);
        MsisdnValidationSuccessTask msisdnValidationSuccessTask = new MsisdnValidationSuccessTask();
        msisdnValidationSuccessTask.setChangeTimestampToGMT(false);

        LogcatUtil logcatUtil = new LogcatUtil(getContext()
                , msisdnValidationHttpRequestTask
                , msisdnValidationSuccessTask
        );
        logcatUtil.start();

        try {
            long timestamp = System.currentTimeMillis();

            reinstallOcc(appPath);

            logcatUtil.stop();

            logger.trace(msisdnValidationHttpRequestTask.toString());
            logger.trace(msisdnValidationSuccessTask.toString());

            MsisdnValidationHttpRequestWrapper msisdnValidationHttpRequestLogEntry = msisdnValidationHttpRequestTask.getEntryAfterTimestamp(timestamp);
            assertNull("Msisdn validation shouldn't be performed!", msisdnValidationHttpRequestLogEntry);
            logger.trace("3-1 " + msisdnValidationHttpRequestLogEntry);
            MsisdnValidationSuccessWrapper msisdnValidationSuccessLogEntry = msisdnValidationSuccessTask.getEntryAfterTimestamp(timestamp);
            assertNull("Msisdn validation shouldn't be done!", msisdnValidationSuccessLogEntry);
            logger.trace("3-2 " + msisdnValidationSuccessLogEntry);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e.getMessage());
        } finally {
            mobileNetworkUtil.switchMobileDataOnOff(true);
            logcatUtil.stop();
        }
    }

    /**
     * <p>Tests MSISDN validation using SMS.</p>
     *
     * @throws Exception
     */
    @DeviceOnly
    @LargeTest
    public void test_004_msisdnSms_1() throws Exception {
        final String ocFileName = "asimov-signed-MSISDN_SMS_rooted_no_http_https.apk";
        final String appPath = appFolder + File.separator + ocFileName;

        MsisdnSendingSmsValidationTask msisdnSendingSmsValidationTask = new MsisdnSendingSmsValidationTask();
        msisdnSendingSmsValidationTask.setChangeTimestampToGMT(false);
        MsisdnValidationSuccessTask msisdnValidationSuccessTask = new MsisdnValidationSuccessTask();
        msisdnValidationSuccessTask.setChangeTimestampToGMT(false);

        LogcatUtil logcatUtil = new LogcatUtil(getContext(), msisdnSendingSmsValidationTask, msisdnValidationSuccessTask);
        logcatUtil.start();

        try {
            long timestamp = System.currentTimeMillis();

            reinstallOcc(appPath);

            logcatUtil.stop();

            logger.trace(msisdnSendingSmsValidationTask.toString());
            logger.trace(msisdnValidationSuccessTask.toString());

            MsisdnSendingSmsValidationWrapper msisdnSendingSmsValidationLogEntry = msisdnSendingSmsValidationTask.getEntryAfterTimestamp(timestamp);
            String phoneToSend = msisdnSendingSmsValidationLogEntry.getPhoneToSend();
            assertEquals("Msisdn validation: phonenumber is wrong! Must be " + TFConstantsIF.MSISDN_VALIDATION_PHONENUMBER + " but is " + phoneToSend,
                    TFConstantsIF.MSISDN_VALIDATION_PHONENUMBER, phoneToSend);
            logger.trace("4-1 " + msisdnSendingSmsValidationLogEntry);
            MsisdnValidationSuccessWrapper msisdnValidationSuccessLogEntry = msisdnValidationSuccessTask.getEntryAfter(msisdnSendingSmsValidationLogEntry.getEntryNumber());
            assertNotNull("Msisdn validation has not been done!", msisdnValidationSuccessLogEntry);
            logger.trace("4-2 " + msisdnValidationSuccessLogEntry);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e.getMessage());
        } finally {
            logcatUtil.stop();
        }
    }

    /**
     * <p>Tests MSISDN SMS validation timeout if validation phonenumber is wrong.</p>
     *
     * @throws Exception
     */
    @DeviceOnly
    @LargeTest
    public void test_005_msisdnSms_2() throws Exception {
        final String ocFileName = "asimov-signed-MSISDN_SMS_wrong_validation_phone.apk";
        final String appPath = appFolder + File.separator + ocFileName;

        MsisdnSendingSmsValidationTask msisdnSendingSmsValidationTask = new MsisdnSendingSmsValidationTask();
        msisdnSendingSmsValidationTask.setChangeTimestampToGMT(false);
        MsisdnValidationSuccessTask msisdnValidationSuccessTask = new MsisdnValidationSuccessTask();
        msisdnValidationSuccessTask.setChangeTimestampToGMT(false);

        LogcatUtil logcatUtil = new LogcatUtil(getContext(), msisdnSendingSmsValidationTask, msisdnValidationSuccessTask);
        logcatUtil.start();

        try {
            long timestamp = System.currentTimeMillis();

            reinstallOcc(appPath);

            logcatUtil.stop();

            logger.trace(msisdnSendingSmsValidationTask.toString());
            logger.trace(msisdnValidationSuccessTask.toString());

            MsisdnSendingSmsValidationWrapper msisdnSendingSmsValidationLogEntry = msisdnSendingSmsValidationTask.getEntryAfterTimestamp(timestamp);
            assertNotNull("SMS validation message must be sent!", msisdnSendingSmsValidationLogEntry);
            logger.trace("5-1 " + msisdnSendingSmsValidationLogEntry);
            MsisdnValidationSuccessWrapper msisdnValidationSuccessLogEntry = msisdnValidationSuccessTask.getEntryAfter(msisdnSendingSmsValidationLogEntry.getEntryNumber());
            assertNull("Msisdn validation must not has been done!", msisdnValidationSuccessLogEntry);
            logger.trace("5-2 " + msisdnValidationSuccessLogEntry);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e.getMessage());
        } finally {
            logcatUtil.stop();
        }
    }

    /**
     * <p>Tests MSISDN validation through HTTP.</p>
     *
     * @throws Exception
     */
    @DeviceOnly
    @LargeTest
    public void test_006_msisdnHttp_1() throws Exception {
        final String ocFileName = "asimov-signed-MSISDN_HTTP_rooted.apk";
        final String appPath = appFolder + File.separator + ocFileName;

        MsisdnValidationHttpRequestTask msisdnValidationHttpRequestTask = new MsisdnValidationHttpRequestTask();
        msisdnValidationHttpRequestTask.setChangeTimestampToGMT(false);
        MsisdnValidationSuccessTask msisdnValidationSuccessTask = new MsisdnValidationSuccessTask();
        msisdnValidationSuccessTask.setChangeTimestampToGMT(false);

        LogcatUtil logcatUtil = new LogcatUtil(getContext(), msisdnValidationHttpRequestTask
                , msisdnValidationSuccessTask
        );
        logcatUtil.start();

        try {
            long timestamp = System.currentTimeMillis();

            reinstallOcc(appPath);

            logcatUtil.stop();

            logger.trace(msisdnValidationHttpRequestTask.toString());
            logger.trace(msisdnValidationSuccessTask.toString());

            MsisdnValidationHttpRequestWrapper msisdnValidationHttpRequestLogEntry = msisdnValidationHttpRequestTask.getEntryAfterTimestamp(timestamp);
            String url = msisdnValidationHttpRequestLogEntry.getUrl();

            logger.trace("url=" + url);
            logger.trace("targetUrl=" + PropertyLoadUtil.getProperty("client.msisdn_validation_url"));
            String targetUrl = PropertyLoadUtil.getProperty("client.msisdn_validation_url").replace("@system.baseurl@", PropertyLoadUtil.getProperty("system.baseurl"));
            logger.trace("targetUrl after replace=" + targetUrl);
            assertEquals("Msisdn validation: url is wrong! Must be " + targetUrl + " but is " + url, targetUrl, url);
            logger.trace("6-1 " + msisdnValidationHttpRequestLogEntry);

            MsisdnValidationSuccessWrapper msisdnValidationSuccessLogEntry = msisdnValidationSuccessTask.getEntryAfter(msisdnValidationHttpRequestLogEntry.getEntryNumber());
            assertNotNull("Msisdn validation has not been done!", msisdnValidationSuccessLogEntry);
            logger.trace("6-2 " + msisdnValidationSuccessLogEntry);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e.getMessage());
        } finally {
            logcatUtil.stop();
        }
    }

    /**
     * <p>Tests that MSISDN validation with incorrect url fails.</p>
     *
     * @throws Exception
     */
    @DeviceOnly
    @LargeTest
    public void test_007_msisdnHttp_2() throws Exception {
        final String ocFileName = "asimov-signed-MSISDN_HTTP_wrong_validation_url.apk";
        final String appPath = appFolder + File.separator + ocFileName;

        MsisdnValidationHttpRequestTask msisdnValidationHttpRequestTask = new MsisdnValidationHttpRequestTask();
        msisdnValidationHttpRequestTask.setChangeTimestampToGMT(false);
        MsisdnValidationSuccessTask msisdnValidationSuccessTask = new MsisdnValidationSuccessTask();
        msisdnValidationSuccessTask.setChangeTimestampToGMT(false);

        LogcatUtil logcatUtil = new LogcatUtil(getContext(), msisdnValidationHttpRequestTask
                , msisdnValidationSuccessTask
        );
        logcatUtil.start();

        try {
            long timestamp = System.currentTimeMillis();

            reinstallOcc(appPath);

            logcatUtil.stop();

            logger.trace(msisdnValidationHttpRequestTask.toString());
            logger.trace(msisdnValidationSuccessTask.toString());

            MsisdnValidationHttpRequestWrapper msisdnValidationHttpRequestLogEntry = msisdnValidationHttpRequestTask.getEntryAfterTimestamp(timestamp);
            assertNotNull("Msisdn validation request should been done!", msisdnValidationHttpRequestLogEntry);
            logger.trace("7-1 " + msisdnValidationHttpRequestLogEntry);

            MsisdnValidationSuccessWrapper msisdnValidationSuccessLogEntry = msisdnValidationSuccessTask.getEntryAfter(msisdnValidationHttpRequestLogEntry.getEntryNumber());
            assertNull("Msisdn validation should not been done!", msisdnValidationSuccessLogEntry);
            logger.trace("7-2 " + msisdnValidationSuccessLogEntry);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e.getMessage());
        } finally {
            logcatUtil.stop();
        }
    }
}