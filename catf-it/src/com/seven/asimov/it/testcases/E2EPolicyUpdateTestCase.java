package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyMGMTUpdateTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicySMSnotificationTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.sms.SmsUtil;
import junit.framework.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class E2EPolicyUpdateTestCase extends E2ETestCase {
    private static final Logger logger = LoggerFactory.getLogger(E2EPolicyUpdateTestCase.class.getSimpleName());

    protected static final String SUITE_NAME = "e2e-policy-update";
    protected static LogcatUtil logcatUtil;
    protected static final String COM_SEVEN_TEST = "com.seven.asimov.test.tool";
    protected static final String TEST_TOOLS_APK_FILENAME = "/sdcard/asimov-7test-tool.apk";
    protected static final String TEST_TOOLS_PACKAGE_NAME = "com.seven.asimov.test.tool";
    protected static final long RADIO_UP_PING_INTERVAL = 5 * 1000;
    protected static final String NORMALIZATION_POLICY_PATH = "@asimov@normalization@header@com.seven.asimov.test.tool@*@.*";
    protected static final String RESPONSE_HEADER_RULES = "response_header_rules";
    protected static String TRUE = "t";
    protected static int ITERATIONS = 2;
    protected static int sleepMs = 60 * 1000;
    protected static final int COUNTER = 96;
    protected static SmsUtil smppEmulator;
    protected volatile boolean radioUp = false;

    protected static final List<String> properties = new ArrayList<String>();

    protected String scheduleSetPolicyWithDelay(String path, String name, String value, String delay, String operationType, boolean important)
            throws IOException, URISyntaxException {
        HttpRequest policyRequest = PMSUtil.getHttpRequestToPms(REST_BATCH_PATH_END, PMSUtil.personalScopePropertyRequestBody(name, path, value, important), false);
        byte[] byteArray = policyRequest.getFullRequest(true).getBytes("UTF8");
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : byteArray) {
            stringBuilder.append(b).append(" ");
        }
        String requestBody = String.format(POLICY_CREATE_SCHEDULING_BODY_PATTERN, getName(), operationType,
                z7TpId, stringBuilder, TFConstantsIF.mPmsServerIp, TFConstantsIF.mPmsServerPort, delay);
        String response = sendPostRequestToRest(POLICY_SET_PATH_END, requestBody);
        boolean isSuccess = response != null && response.toLowerCase().contains(SUCCESS);
        assertTrue("Policy set scheduling should be successfull, check REST logs and functionality", isSuccess);
        if (operationType.equals(POLICY_SENT_TO_PMS_OPERATION_TYPE) & response != null)
            return response.substring(response.indexOf("objectId") + 11, response.indexOf("objectName") - 3);
        return null;
    }

    protected void addProperty(String path, String name, String value) {
        try {
            properties.add(PMSUtil.createPersonalScopeProperty(name, path, value, true));
            TestUtil.sleep(10 * 1000);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected void clearProperties() {
        for (String property : properties) {
            deleteProperty(property);
        }
        properties.clear();
        TestUtil.sleep(10 * 1000);
    }

    protected void deleteProperty(String id) {
        try {
            PMSUtil.deleteProperty(id);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected void switchRadioUp() throws Exception {
        radioUp = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (radioUp) {
                    try {
                        Runtime.getRuntime().exec("ping -c 1 " + AsimovTestCase.TEST_RESOURCE_HOST);
                        logSleeping(RADIO_UP_PING_INTERVAL);
                    } catch (Exception e) {
                        logger.debug("RadioUp Thread interrupted " + e.getMessage());
                    }
                }
                logger.info("RadioUp thread has stopped");
            }
        }).start();
    }

    protected String generatePolicyName() {
        int randomValue = (int) (Math.random() * 1000);
        return randomValue + "SVRNAME:.*\r\n";
    }

    protected boolean waitForSms(PolicyMGMTUpdateTask policyMGMTUpdateTask, PolicySMSnotificationTask policySMSnotificationTask, int iterations) {
        int counter = 0;
        do {
            logSleeping(5 * 1000);
            ++counter;
        }
        while (((policyMGMTUpdateTask.getLogEntries().isEmpty() & policySMSnotificationTask.getLogEntries().isEmpty())) & counter < iterations);
        return counter < iterations;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (PMSUtil.getDeviceZ7TpAddress() != null &&
                PMSUtil.getDeviceZ7TpAddress().length() > 2) {
            z7TpId = PMSUtil.getDeviceZ7TpAddress().substring(2, PMSUtil.getDeviceZ7TpAddress().length());
        } else {
            throw new AssertionFailedError("Some problems with OC. File transport_settings not found or corrupted.");
        }
    }

    @Override
    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Exception e) {
            throw new AssertionFailedError(e.getMessage());
        }
    }
}
