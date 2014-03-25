package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.ThTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollParamsTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollParamsWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.ThWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class E2EPollingTestCase extends E2ETestCase {
    private static final Logger logger = LoggerFactory.getLogger(E2EPollingTestCase.class.getSimpleName());
    protected static final String SUITE_NAME = "E2E-Polling";
    protected static final String HTTP_PMS_PATH = "@asimov@http";
    protected static final String PMS_PATH = "@asimov";
    protected static final String TRANSPARENT = "transparent";
    protected static final HttpRequest request = createRequest().addHeaderField("X-OC-Encoding", "identity").getRequest();
    protected static final long LOGCAT_ENTRY_WAIT_TIMEOUT = 25000;
    protected static final long SERVER_LONG_POLLING_INTERVAL = 0L;

    protected static volatile AssertionError error;
    protected static final int operationTimeOut = 350;
    protected static final int testTimeOut = 600;

    protected class ExchangeParameters {
        private StartPollWrapper startPollWrapper;
        private StartPollParamsWrapper startPollParamsWrapper;

        public ExchangeParameters(StartPollWrapper startPollWrapper, StartPollParamsWrapper startPollParamsWrapper) {
            this.startPollWrapper = startPollWrapper;
            this.startPollParamsWrapper = startPollParamsWrapper;
        }

        public StartPollWrapper getStartPollWrapper() {
            return startPollWrapper;
        }

        public StartPollParamsWrapper getStartPollParamsWrapper() {
            return startPollParamsWrapper;
        }
    }

    protected String createUri() {
        UUID guid = UUID.randomUUID();
        String pathEnd = "asimov_" + guid.toString() + "_" + Thread.currentThread().getStackTrace()[4].getMethodName().toLowerCase();
        return createTestResourceUri(pathEnd);
    }

    protected void prepareResource(HttpRequest request) throws Exception {
        String uri = createUri();
        request.setUri(uri);
        PrepareResourceUtil.prepareResource(uri, false);
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
            //properties.remove(id);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected void addProperty(String path, String name, String value) {
        try {
            properties.add(PMSUtil.createPersonalScopeProperty(name, path, value, true));
            TestUtil.sleep(10 * 1000);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected void setResourceDelay(final long delaySeconds, final HttpRequest request) throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Setting response delay to " + delaySeconds + " sec");
                HttpRequest modificationRequest = request.copy();
                modificationRequest.addHeaderField(new HttpHeaderField("X-OC-Stateless-Sleep", "true"));
                modificationRequest.addHeaderField(new HttpHeaderField("X-OC-ChangeSleep", Long.toString(delaySeconds)));
                sendRequest2(modificationRequest, false, true);
            }
        }).start();
        Thread.sleep(5 * 1000);
    }

    protected void advancedCheckOfStartPoll(final StartPollTask startPollTask, final StartPollParamsTask startPollParamsTask, final double delay, final double rpRi, final int pollClass, final int ri,
                                            final int it, final int tempPoll) {
        assertTrue("Start of the polling should be reported in client log", !startPollTask.getLogEntries().isEmpty());
        StartPollParamsWrapper params = startPollParamsTask.getLogEntries().get(startPollParamsTask.getLogEntries().size() - 1);
        assertEquals("Incorrect polling class was started", pollClass, params.getPollClass());
        if (ri >= 0) {
            assertTrue("Incorrect RI parameter", isInRange(0.8 * ri, 1.2 * ri, params.getRi()));
        } else {
            assertTrue("Incorrect RI parameter", isInRange(1.2 * ri, 0.8 * ri, params.getRi()));
        }
        assertEquals("Incorrect IT parameter", it, params.getIt());
        assertTrue("Incorrect TO parameter", isInRange(0.8 * delay, 1.2 * delay, params.getTo() * DateUtil.SECONDS));
        assertEquals("Incorrect Temp Poll", tempPoll, params.getTempPoll());
        assertTrue("Incorrect RP RI", isInRange(0.8 * rpRi, 1.2 * rpRi, params.getRpRi() * DateUtil.SECONDS));
    }

    protected boolean isInRange(final double start, final double end, final double currentValue) {
        if (start > end) throw new IllegalArgumentException("Incorrect parameters while checking value in range.");
        return currentValue <= end && currentValue >= start;
    }

    protected void stopLogcat(LogcatUtil logcatUtil) {
        try {
            if (logcatUtil != null) logcatUtil.stop();
        } catch (InterruptedException e) {
            logger.debug(ExceptionUtils.getStackTrace(e));
        }
    }

    protected ThWrapper checkThEntriesListForReceivedInvalidateNotification(ThTask thTask, StartPollTask startPollTask) {
        if (!thTask.getLogEntries().isEmpty() && !startPollTask.getLogEntries().isEmpty()) {
            logger.info("Received two correct tasks");
            for (ThWrapper wrapper : thTask.getLogEntries()) {
                logger.info(String.format("Current wrapper parameters: id = %d, current start poll parameters: id = %s", wrapper.getId(), startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1).getSubscriptionId()));
                if (Integer.toString(wrapper.getId()).contains(startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1).getSubscriptionId()))
                    return wrapper;
            }
        }
        return null;
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
