package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpRequest.Builder;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatChecks;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import com.seven.asimov.it.utils.pms.Policy;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CachingRevalidationTestCase extends TcpDumpTestCase {

    protected final String[] LOCK_Z7TP = {"su", "-c", "iptables -t filter -A INPUT -p tcp --sport 7735 -j DROP"};
    protected final String[] UNLOCK_Z7TP = {"su", "-c", "iptables -t filter -D INPUT -p tcp --sport 7735 -j DROP"};
    protected final String ETAG_DEFAULT = "42f8-4bccc642f14c0";
    protected final String LAST_MODIFIED_DEFAULT = "Wed, 11 Jan 1984 08:00:00 GMT";
    protected final int MAX_AGE_DEFAULT = 100;
    protected final long DELAY = 29 * 1000;
    protected final boolean KEEP_ALIVE = true;

    protected static final String PACKAGE_NAME = "com.seven.asimov.it";
    protected static final String PACKAGES_PROPERTY_NAME = "packages";
    protected static final String BLACKLIST_PATH = "@asimov@http@revalidation_blacklist";

    protected static final Policy policy = new Policy(PACKAGES_PROPERTY_NAME, PACKAGE_NAME, BLACKLIST_PATH, true);
    protected static final int WAIT_FOR_POLICIES = 30 * 1000;

    protected void executeRevalidation(String uri, String encoded, String encodedNotModified, HttpRequest request1, HttpRequest request2) throws Exception {
        radioKeep.start();

        if (encoded != null) {
            PrepareResourceUtil.prepareResource200(uri, false, encoded);
        }
        try {
            // R1.1 miss, cached by RFC
            HttpResponse response = checkMiss(request1, 1, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.2 hit
            response = checkHit(request2, 2, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.3 hit
            response = checkHit(request2, 3, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.4 hit
            response = checkHit(request2, 4, VALID_RESPONSE);
            PrepareResourceUtil.prepareResource304(uri, encodedNotModified);
            logSleeping(DELAY - response.getDuration());

            // R1.5 hit, but send revalidation request
            response = checkMiss(request2, 5, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.6 hit by revalidation subscription
            response = checkHit(request2, 6, VALID_RESPONSE);
            logSleeping(DELAY - response.getDuration());

            // R1.7 hit
            checkHit(request2, 7, VALID_RESPONSE);

        } finally {
            // Stop server polling
            PrepareResourceUtil.invalidateResourceSafely(uri);
            radioKeep.interrupt();
        }
    }

    protected void executeServerRevalidation(String uri, String headers, boolean keepAlive, long delay) throws Exception {
        radioKeep.start();
        String encodedRawHeadersDef = base64Encode(headers);
        final HttpRequest request = buildDefaultRequest(uri).getRequest();
        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);

        try {
            // R1.1 miss, cached by RFC
            HttpResponse response = checkMiss(request, 1, VALID_RESPONSE, keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            // R1.2 hit
            response = checkHit(request, 2, VALID_RESPONSE, keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            // R1.3 hit
            response = checkHit(request, 3, VALID_RESPONSE, keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            // R1.4 after this request revalidation time expires
            response = checkHit(request, 4, VALID_RESPONSE, keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            // R1.5 on this request must be HTTP activity
            response = checkMiss(request, 5, VALID_RESPONSE, keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            // R1.6 hit
            response = checkHit(request, 6, VALID_RESPONSE, keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            // R1.7 hit
            checkHit(request, 7, VALID_RESPONSE, keepAlive, TIMEOUT);

        } finally {
            // invalidate resource to stop server polling
            PrepareResourceUtil.invalidateResourceSafely(uri);
            radioKeep.interrupt();
        }
    }

    protected void executeRevalidationThreadEtagHttps(final String uri, final String encodedRawHeadersDef, final String encodedRawHeadersReval,
                                                      final HttpRequest request) throws Exception {
        radioKeep.start();
        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);
        try {
            int requestId = 1;
            HttpResponse response = checkMiss(request, requestId++);
            logSleeping(DELAY - response.getDuration());

            // R1.2 hit
            response = checkHit(request, requestId++);
            logSleeping(DELAY - response.getDuration());

            // R1.3 hit
            response = checkHit(request, requestId++);
            logSleeping(DELAY - response.getDuration());

            // R1.4 after this request revalidation time expires
            response = checkHit(request, requestId++);
            PrepareResourceUtil.prepareResource304(uri, encodedRawHeadersReval);
            logSleeping(DELAY - response.getDuration());

            // R1.5 on this request must be HTTP activity
            response = checkMiss(request, requestId++);
            PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);
            logSleeping(DELAY - response.getDuration());

            // R1.6 hit
            response = checkHit(request, requestId++);
            logSleeping(DELAY - response.getDuration());

            // R1.7 hit
            checkHit(request, requestId++);
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            radioKeep.interrupt();
        }
    }

    protected String base64Encode(String expected) {
        return URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
    }

    protected Builder buildDefaultRequest(String uri) {
        return createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeaderField("Accept-Language", "ru-ru,ru;q=0.8,en-us;q=0.5,en;q=0.")
                .addHeaderField("Accept-Encoding", "identity");
    }

    protected Builder buildLMRequest(String uri, String date) {
        return buildDefaultRequest(uri).addHeaderField("If-Modified-Since", date);
    }

    protected Thread radioKeep = new Thread() {
        @Override
        public void run() {
            final String uri = createTestResourceUri("asimov_it_test_radio_on", false);
            while (true) {
                try {
                    sendRequestWithoutLogging(createRequest().setUri(uri).setMethod("GET").getRequest());
                    TestUtil.sleep(TFConstantsIF.RADIO_UP_DELAY);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    };


    public class RrWithCmTask extends Task<RrWithCmEntry> {

        private String REGEXP = "(201[2-9].[0-1][0-9].[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*).*RR \\[\\d+\\] is being constructed with CM, expiration time is \\d+";
        private Pattern pattern = Pattern.compile(REGEXP);

        @Override
        protected RrWithCmEntry parseLine(String line) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                return new RrWithCmEntry(LogcatChecks.getUnixTimeFromString(matcher.group(1)));
            }
            return null;
        }
    }

    public class RiStartPollTask extends Task<RiStartPollEntry> {

        private String REGEXP = "(201[2-9].[0-1][0-9].[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*).*Polling class: 1";
        private Pattern pattern = Pattern.compile(REGEXP);

        @Override
        protected RiStartPollEntry parseLine(String line) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                return new RiStartPollEntry(LogcatChecks.getUnixTimeFromString(matcher.group(1)));
            }
            return null;
        }
    }

    protected class RrWithCmEntry extends LogEntryWrapper {

        private long timestamp;

        public RrWithCmEntry(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }
    }

    protected class RiStartPollEntry extends LogEntryWrapper {

        private long timestamp;

        public RiStartPollEntry(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }
    }
}
