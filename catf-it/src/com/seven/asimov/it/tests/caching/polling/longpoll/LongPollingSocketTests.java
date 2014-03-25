package com.seven.asimov.it.tests.caching.polling.longpoll;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.testcases.LongPollingTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollParamsTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.apache.http.HttpStatus;

import java.net.SocketTimeoutException;

public class LongPollingSocketTests extends LongPollingTestCase {

    @DeviceOnly
    @LargeTest
    public void test_001_LPSocketCloseIWC() throws Throwable {

        final long SLEEP = 70 * 1000;
        final String RESOURCE_URI = "test_socket_close_2";

        final String uri = createTestResourceUri(RESOURCE_URI);

        PrepareResourceUtil.prepareResource(uri, false);
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "65").getRequest();

        try {
            int requestId = 1;
            HttpResponse response;

            for (int i = 0; i < 2; i++) {
                // this response shall be returned from network
                response = checkMiss(request, requestId++, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(SLEEP - response.getDuration());
            }

            for (int i = 0; i < 2; i++) {
                // this response shall be returned from cache
                response = checkHit(request, requestId++, -1, null, false, 1000);
                logSleeping(SLEEP - response.getDuration());
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Pattern [0, 70, 70, 70]
     * Delay [65, 65, 65, 65]
     * <p/>
     * 1. 1st and 2nd requests should be MISS. Long poll should be detected, polling will start.
     * 2. 3rd request should be HIT with socket timeout of 1 second and IN socket
     * error should occur.
     * 3. 4th request should be HIT due to active RR after one error rule.
     *
     * @throws Throwable
     */
    @DeviceOnly
    @LargeTest
    public void test_002_LPSocketCloseIWOC() throws Throwable {
        final long SLEEP = 70 * 1000;
        final String RESOURCE_URI = "test_socket_close_2";
        final String uri = createTestResourceUri(RESOURCE_URI);
        StartPollParamsTask startPollParamsTask = new StartPollParamsTask();
        StartPollTask startPollTask = new StartPollTask();
        StartPollWrapper startPoll;
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), startPollTask, startPollParamsTask);
        logcatUtil.start();

        PrepareResourceUtil.prepareResource(uri, false);
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "65").getRequest();

        try {
            int requestId = 1;
            HttpResponse response;

            for (int i = 0; i < 2; i++) {
                // this response shall be returned from network
                response = checkMiss(request, requestId++, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(SLEEP - response.getDuration());
            }

            for (int i = 0; i < 2; i++) {
                response = checkHit(request, requestId++, -1, null, false, 1000);
                logSleeping(SLEEP - response.getDuration());

            }
            logcatUtil.stop();
            startPoll = startPollTask.getLogEntries().get(startPollTask.getLogEntries().size() - 1);
            SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(startPoll.getSubscriptionId()),
                    SmsUtil.InvalidationType.INVALIDATE_WITHOUT_CACHE.byteVal);

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logcatUtil.stop();
        }
    }
}