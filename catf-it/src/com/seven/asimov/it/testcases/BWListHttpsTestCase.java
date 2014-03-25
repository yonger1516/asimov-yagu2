package com.seven.asimov.it.testcases;


import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.customservice.CustomService;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.FCLTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollParamsTask;
import com.seven.asimov.it.utils.logcat.wrappers.FCLWrapper;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BWListHttpsTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(BWListHttpsTestCase.class.getSimpleName());

    /**
     * <p>
     * Method sends requests to Custom Service and checks if polling starts or not after the last request.
     * </p>
     *
     * @param pathEnd             Text appended to resource uri.
     * @param service             CustomService to send request to.
     * @param customSessions      Array of session identifiers for Custom Services,
     *                            method adds these ids as header to each request to keep
     *                            corresponding Custom Services alive. This needs to be done because
     *                            default timeout for Custom Service is 5 min which is not enough
     *                            for some tests.
     * @param requestCount        Number of requests sent to Custom Service.
     * @param requestInterval     Interval between requests. Total method execution time should not exceed
     *                            Custom Service timeout value is 5 min.
     * @param shouldBeIntercepted If sent requests should be intercepted.
     * @throws Exception
     */
    protected void BWListHostsAndPortsTester(String pathEnd, CustomService service, String[] customSessions, int requestCount, int requestInterval, boolean shouldBeIntercepted) throws Exception {
        final StartPollParamsTask sppTask = new StartPollParamsTask();
        final FCLTask fclTask = new FCLTask();
        final LogcatUtil logcat = new LogcatUtil(getContext(), sppTask, fclTask);
        String uri = "";

        try {
            uri = createTestResourceUri(pathEnd, true, service.getPort());
            PrepareResourceUtil.prepareResource(uri, false);

            final HttpRequest request = createRequest().setUri(uri)
                    .setMethod("GET").getRequest();

            for (String sessionID : customSessions) {
                request.addHeaderField(new HttpHeaderField("X-OC-Reserve-Custom-Service", sessionID)); //Prolongation Custom Services reservation
            }

            logcat.start();

            for (int i = 0; i < requestCount; i++) {
                TestUtil.sleep(requestInterval);
                HttpResponse response = sendHttpsRequest(request);
                assertEquals("HttpRequest to custom service failed with code:" + response.getStatusCode() +
                        " expected code:" + HttpStatus.SC_OK, response.getStatusCode(), HttpStatus.SC_OK);
            }

            logcat.stop();

            int matchedFcls = 0;
            for (FCLWrapper fcl : fclTask.getLogEntries()) {
                logger.info("found FCL record timestamp:" + fcl.getTimestamp() + " ip:" + fcl.getIp() + " port:" + fcl.getPort());
                if (fcl.getPort() == service.getPort()) {
                    logger.info("FCL with timestamp:" + fcl.getTimestamp() + " matched");
                    matchedFcls++;
                }
            }

            assertEquals("Expected to have: " + requestCount +
                    " HTTPS FCL records, but found: " + matchedFcls,
                    requestCount, matchedFcls);
            assertEquals("Expected ssl requests to be " + (shouldBeIntercepted ? "intercepted" : "ignored") +
                    " but they were " + (!sppTask.getLogEntries().isEmpty() ? "intercepted" : "ignored"),
                    shouldBeIntercepted, !sppTask.getLogEntries().isEmpty());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }
}
