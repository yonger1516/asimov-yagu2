package com.seven.asimov.it.tests.communication.relay.http;

import android.test.suitebuilder.annotation.SmallTest;
import com.seven.asimov.it.annotation.Execute;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.IWCTask;
import com.seven.asimov.it.utils.logcat.wrappers.IWCWrapper;
import org.apache.http.HttpStatus;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>Branding target, required for this test case, should include next parameters:</p>
 * <ul>
 * <li>client.msisdn_validation_protocol=http</li>
 * <li>client.msisdn_validation_enabled=1</li>
 * </ul>
 */
public class RelayCommunicationHttp extends TcpDumpTestCase {

    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final int RADIO_KEEPER_DELAY_MS = 2 * 1000;

    private TestCaseThread radioUpKeeperThread = new TestCaseThread() {
        @Override
        public void run() throws Throwable {
            final String uri = createTestResourceUri("asimov_it_test_radio_up", false);
            while (!isInterruptedSoftly()) {
                sendRequestWithoutLogging(createRequest().setUri(uri).setMethod("GET").getRequest());
                TestUtil.sleep(RADIO_KEEPER_DELAY_MS);
            }
        }
    };

    private void updateFor3gOnly() {
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        mobileNetworkUtil.on3gOnly();
    }

    private void updateForWifiOnly() {
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        mobileNetworkUtil.onWifiOnly();
    }

    /**
     * <p>Received notification with invalidate when radio UP and 7ztp connection to Relay is closed</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests
     * and 1 other responses for next requests</p>
     * <p>Pattern [0,95,95,95,95]</p>
     * <p>Steps:</p>
     * <ul>
     * <li>Ensure that only 3G is active</li>
     * <li>Ensure that radio status will be UP</li>
     * <li>Send 3 requests with some RI. RI-based polling should start.</li>
     * <li>Send 4th request</li>
     * <li>Change response body</li>
     * <li>Send 5th request</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be with verdict HIT.</li>
     * <li>Radio state always is UP due to requests are sending</li>
     * <li>Notification with invalidate come to client  in interval 95 s. after 4th request was sent</li>
     * <li>OC should send get cached request to TH immediately after receiving of notification</li>
     * <li>5th request should be HITed with new response from TH</li>
     * </ul>
     */
    @Execute
    @SmallTest
    public void test_RelayCommunicationHttp_001() throws Throwable {
        updateFor3gOnly();
        final int RI = 95 * 1000;
        executorService.submit(radioUpKeeperThread);
        String resource = "test_RelayCommunication_001_Http";
        final String uri = createTestResourceUri(resource);
        int requestId = 0;
        IWCTask iwcTask = new IWCTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), iwcTask);
        logcatUtil.start();
        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).
                setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        try {
            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.2
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.3
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.4
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            PrepareResourceUtil.prepareResource(uri, true);
            logSleeping(RI - response.getDuration());
            // 1.5
            checkHit(request, ++requestId, HttpStatus.SC_OK, INVALIDATED_RESPONSE);
            logcatUtil.stop();

            List<IWCWrapper> iwcWrapperList = iwcTask.getLogEntries();
            assertFalse("IWC message should be received", iwcWrapperList.isEmpty());
        } finally {
            logcatUtil.stop();
            executorService.shutdownNow();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>Received notification with invalidate when radio DOWN and 7ztp connection to Relay is closed</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests
     * and 3 other responses for next requests</p>
     * <p>Pattern [0,95,95,95,95,95,95]</p>
     * <p>Steps:</p>
     * <ul>
     * <li>Ensure that only 3G is active</li>
     * <li>Send 3 requests with some RI. RI-based polling should start.</li>
     * <li>Send 4th request</li>
     * <li>Change response body</li>
     * <li>Send 5th, 6th and 7th requests</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be with verdict HIT.</li>
     * <li>Notification with invalidate come to client  in interval 95 s. after 4th request was sent</li>
     * <li>OC should send get cached request to TH immediately after receiving of notification</li>
     * <li>6th request should be sent to network, after receiving of response, polling should start.</li>
     * <li>7th request is HIT with new cache.</li>
     * </ul>
     */

    @SmallTest
    public void test_RelayCommunicationHttp_002() throws Exception {
        updateFor3gOnly();
        final int RI = 95 * 1000;
        String resource = "test_RelayCommunication_002_Http";
        final String uri = createTestResourceUri(resource);
        HttpRequest changedRequest = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE)
                .addHeaderField("X-OC-Sleep", "30")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();
        int requestId = 0;
        IWCTask iwcTask = new IWCTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), iwcTask);
        logcatUtil.start();
        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).
                setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        try {
            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.2
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.3
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.4
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            sendRequest(changedRequest, false, true);
            logSleeping(RI - response.getDuration());
            // 1.5
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, INVALIDATED_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.6
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, INVALIDATED_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.7
            checkHit(request, ++requestId, HttpStatus.SC_OK, INVALIDATED_RESPONSE);

            logcatUtil.stop();

            List<IWCWrapper> iwcWrapperList = iwcTask.getLogEntries();
            assertFalse("IWC message should be received", iwcWrapperList.isEmpty());
        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>Change radio state to UP after receive notification invalidate in Long Poll</p>
     * <p>A test resource is needed for this test case that returns the same responses with delay 95
     * for first 2 requests and 1 other responses with delay 95 for next requests</p>
     * <p>Pattern [0,100,100]</p>
     * <p>Delay: [95,95,95]</p>
     * <p>Steps:</p>
     * <ul>
     * <li>Ensure that only 3G is active</li>
     * <li>Send 2 requests. LP-based polling should start.</li>
     * <li>Send 3rd request</li>
     * <li>After some time-interval change response body</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     * <li>After 2nd request start poll should start.</li>
     * <li>After 3rd request SMS invalidate should come.</li>
     * <li>OC should send get cached request to TH immediately after receiving of notification.</li>
     * <li>3rd request should be HITed with new response from TH.</li>
     * </ul>
     */

    @SmallTest
    public void test_RelayCommunicationHttp_003() throws Exception {
        updateFor3gOnly();
        final int RI = 100 * 1000;

        String resource = "test_RelayCommunication_003_Http";
        final String uri = createTestResourceUri(resource);
        int requestId = 0;
        IWCTask iwcTask = new IWCTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), iwcTask);
        logcatUtil.start();
        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).
                setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").
                addHeaderField("X-OC-Sleep", "95").getRequest();

        try {
            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.2
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);

            logSleeping(RI - response.getDuration());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5 * 1000);
                        PrepareResourceUtil.prepareResource(uri, true);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            // 1.3
            checkHit(request, ++requestId, HttpStatus.SC_OK, INVALIDATED_RESPONSE);
            logcatUtil.stop();

            List<IWCWrapper> iwcWrapperList = iwcTask.getLogEntries();
            assertFalse("IWC message should be received", iwcWrapperList.isEmpty());
        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>OC should send get cached data request in case when no any requests for RR and radio state is UP</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests
     * and 2 other responses for next requests</p>
     * <p>Pattern [0,95,95,95]</p>
     * <p>Steps:</p>
     * <ul>
     * <li>Ensure that only 3G is active</li>
     * <li>Ensure that radio status will be UP</li>
     * <li>Send 3 requests with some RI. RI-based polling should start.</li>
     * <li>Send 4th request</li>
     * <li>Change response body</li>
     * <li>Check, that IWC-message should be in logs.</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be with verdict HIT.</li>
     * <li>Radio state always is UP due to requests are sending</li>
     * <li>Notification with invalidate come to client  in interval 95 s. after 4th request was sent</li>
     * <li>OC should send get cached request to TH immediately after receiving of notification</li>
     * </ul>
     */

    @SmallTest
    public void test_RelayCommunicationHttp_004() throws Throwable {
        updateFor3gOnly();
        final int RI = 95 * 1000;
        executorService.submit(radioUpKeeperThread);
        String resource = "test_RelayCommunication_001_Http";
        final String uri = createTestResourceUri(resource);
        int requestId = 0;
        IWCTask iwcTask = new IWCTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), iwcTask);
        logcatUtil.start();
        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).
                setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        try {
            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.2
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.3
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.4
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            PrepareResourceUtil.prepareResource(uri, true);
            logSleeping(RI - response.getDuration());
            logcatUtil.stop();

            List<IWCWrapper> iwcWrapperList = iwcTask.getLogEntries();
            assertFalse("IWC message should be received", iwcWrapperList.isEmpty());
        } finally {
            logcatUtil.stop();
            executorService.shutdownNow();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>OC should send get cached data request as soon as radio state will be UP</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests
     * and 2 other responses for next requests</p>
     * <p>Pattern [0,95,95,95]</p>
     * <p>Steps:</p>
     * <ul>
     * <li>Ensure that only 3G is active</li>
     * <li>Ensure that radio status will be UP</li>
     * <li>Send 3 requests with some RI. RI-based polling should start.</li>
     * <li>Send 4th request</li>
     * <li>Change response body</li>
     * <li>Check, that IWC-message should be in logs.</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be with verdict HIT.</li>
     * <li>Radio state always is UP due to requests are sending</li>
     * <li>Notification with invalidate should come to client after 4th request was sent.</li>
     * <li>In 5 sec after receiving of SMS radio state should be UP due to requests from browser</li>
     * <li>OC should send get cached request to TH immediately after radio state become UP</li>
     * </ul>
     */

    @SmallTest
    public void test_RelayCommunicationHttp_005() throws Exception {
        updateFor3gOnly();
        final int RI = 95 * 1000;
        final double halfInterval = 0.5;
        String resource = "test_RelayCommunication_001_Http";
        final String uri = createTestResourceUri(resource);
        int requestId = 0;
        IWCTask iwcTask = new IWCTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), iwcTask);
        logcatUtil.start();
        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).
                setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        try {
            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.2
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.3
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.4
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping((long) ((RI - response.getDuration()) * halfInterval));
            long beginTime = System.currentTimeMillis();

            PrepareResourceUtil.prepareResource(uri, true);

            long endTime = System.currentTimeMillis();
            long delay = endTime - beginTime;

            logSleeping((long) ((RI - response.getDuration()) * halfInterval) - delay);
            logcatUtil.stop();

            List<IWCWrapper> iwcWrapperList = iwcTask.getLogEntries();
            assertTrue("IWC message should be received", iwcWrapperList.size() != 0);
        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * <p>Presence of notification with invalidate using WIFI interface</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests
     * and 1 other responses for next requests</p>
     * <p>Pattern [0,95,95,95,95]</p>
     * <p>Steps:</p>
     * <ul>
     * <li>Ensure that only Wifi is active</li>
     * <li>Send 3 requests with some RI. RI-based polling should start.</li>
     * <li>Send 4th request</li>
     * <li>Change response body</li>
     * <li>Send 5th request</li>
     * </ul>
     * <p>Expected results:</p>
     * <ul>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be with verdict HIT.</li>
     * <li>Notification with invalidate come to client  in interval 95 s. after 4th request was sent</li>
     * <li>OC should send get cached request to TH immediately after receiving of notification</li>
     * <li>5th request should be HITed with new response from TH</li>
     * </ul>
     */

    @SmallTest
    public void test_RelayCommunicationHttp_006() throws Exception {
        updateForWifiOnly();
        final int RI = 95 * 1000;
        String resource = "test_RelayCommunication_001_Http";
        final String uri = createTestResourceUri(resource);
        int requestId = 0;
        IWCTask iwcTask = new IWCTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), iwcTask);
        logcatUtil.start();
        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).
                setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        try {
            // 1.1
            HttpResponse response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.2
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.3
            response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(RI - response.getDuration());
            // 1.4
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            PrepareResourceUtil.prepareResource(uri, true);
            logSleeping(RI - response.getDuration());
            // 1.5
            checkHit(request, ++requestId, HttpStatus.SC_OK, INVALIDATED_RESPONSE);
            logcatUtil.stop();

            List<IWCWrapper> iwcWrapperList = iwcTask.getLogEntries();
            assertFalse("IWC message should be received", iwcWrapperList.isEmpty());
        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }
}
