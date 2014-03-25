package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import org.apache.http.HttpStatus;

public class RlpTestCase extends TcpDumpTestCase {
    protected HttpResponse[] checkMissHitResponses(int[] RI, int[] D, int[] RESPONSE_TIMEOUT, String RESOURCE_URI, int... hitNumber) throws Exception {

        //check that test-case configuration is correct
        assertTrue("TC issue, bad configuration of test-case", RI.length == D.length && RI.length == RESPONSE_TIMEOUT.length && RESOURCE_URI != null);
        for (int i : hitNumber) {
            assertTrue("TC issue, bad configuration of test-case", i >= 0 && i < RI.length);
        }

        final int N = RI.length;
        final HttpRequest[] REQUEST = new HttpRequest[N];
        final HttpResponse[] RESPONSE = new HttpResponse[N];
        final String URI = createTestResourceUri(RESOURCE_URI);

        final int additionalResponseTime = 30 * 1000;

        try {
            PrepareResourceUtil.prepareResource(URI, false);
            TestUtil.sleep(5 * 1000);
            for (int i = 0; i < N; i++) {
                REQUEST[i] = createRequest().setUri(URI).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-Stateless-Sleep", "true")
                        .addHeaderField("X-OC-Sleep", Integer.toString(D[i])).getRequest();
            }

            outer:
            for (int i = 0; i < N; i++) {
                for (int j : hitNumber) {
                    if (i == j) {
                        RESPONSE[i] = checkHit(REQUEST[i], i + 1, HttpStatus.SC_OK, VALID_RESPONSE, false, RESPONSE_TIMEOUT[i] * 1000 + additionalResponseTime);
                        logSleeping(RI[i] * 1000 - RESPONSE[i].getDuration());
                        continue outer;
                    }
                }

                RESPONSE[i] = checkMiss(REQUEST[i], i + 1, HttpStatus.SC_OK, VALID_RESPONSE, false, RESPONSE_TIMEOUT[i] * 1000 + additionalResponseTime);
                logSleeping(RI[i] * 1000 - RESPONSE[i].getDuration());
            }

        } finally {
            PrepareResourceUtil.prepareResource(URI, true);
        }
        return RESPONSE;
    }
}
