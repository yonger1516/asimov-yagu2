package com.seven.asimov.it.tests.caching.rfc.revalidation;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.testcases.CachingRevalidationTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;

import java.net.URLEncoder;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;

/**
 * IGNORED due to OC version 2.4, where revalidation is not enabled. Should be un-ignored and checked in release 3.0
 * Logic of the test wasn't checked from release 2.3 because revalidation had switched off.
 */

public class RevalidationBlacklistTests extends CachingRevalidationTestCase {

    @LargeTest
    public void test_001_RevalidationBlacklistRmToCm() throws Exception {
        final String uri = createTestResourceUri("asimov_revalidation_blacklist_rm_to_cm");
        final HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity").getRequest();
        RrWithCmTask task = new RrWithCmTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), task);
        try {
            logcatUtil.start();
            PMSUtil.addPolicies(new Policy[]{policy});
            logSleeping(WAIT_FOR_POLICIES);
            int requestId = 0;
            String expected = "HTTP/1.1 200 OK" + CRLF
                    + "Connection: close" + CRLF
                    + "Cache-Control: max-age=60" + CRLF
                    + "Etag: 686505555g456f54" + CRLF
                    + "Last-Modified:  Fri, 23 Mar 2015 20:12:01" + CRLF
                    + CRLF
                    + "tere";
            String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
            request.addHeaderField(new HttpHeaderField("X-OC-Raw", expectedEncoded));
            checkMiss(request, ++requestId);
            logSleeping(MIN_NON_RMP_PERIOD / 2);
            checkHit(request, ++requestId);
            logcatUtil.stop();
            assertTrue("RR should be constructed with CM, not RM", task.getLogEntries().size() > 0);
        } finally {
            PMSUtil.cleanPaths(new String[]{BLACKLIST_PATH});
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logcatUtil.stop();
        }
    }

    @LargeTest
    public void test_002_RevalidationBlacklistRiPolling() throws Exception {
        final String uri = createTestResourceUri("asimov_revalidation_blacklist_ri_polling");
        final HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity").getRequest();
        RiStartPollTask task = new RiStartPollTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), task);
        try {
            PMSUtil.addPolicies(new Policy[]{policy});
            logSleeping(WAIT_FOR_POLICIES);
            logcatUtil.start();
            int requestId = 0;
            String expected = "HTTP/1.1 200 OK" + CRLF
                    + "Connection: close" + CRLF
                    + "Cache-Control: max-age=120" + CRLF
                    + "Etag: 686505555g456f54" + CRLF
                    + "Last-Modified:  Fri, 23 Mar 2015 20:12:01" + CRLF
                    + CRLF
                    + "tere";
            String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
            request.addHeaderField(new HttpHeaderField("X-OC-Raw", expectedEncoded));
            checkMiss(request, ++requestId);
            logSleeping(MIN_NON_RMP_PERIOD);
            checkHit(request, ++requestId);
            logSleeping(MIN_NON_RMP_PERIOD);
            checkMiss(request, ++requestId);
            logSleeping(MIN_NON_RMP_PERIOD);
            checkHit(request, ++requestId);
            logSleeping(MIN_NON_RMP_PERIOD);
            checkHit(request, ++requestId);
            logcatUtil.stop();
            assertTrue("RI based polling should be detected (not revalidation polling)", task.getLogEntries().size() > 0);
        } finally {
            PMSUtil.cleanPaths(new String[]{BLACKLIST_PATH});
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logcatUtil.stop();
        }
    }
}
