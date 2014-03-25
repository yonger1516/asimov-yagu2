package com.seven.asimov.it.lib.connectivity;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrdinaryNoStartPollNegative extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(OrdinaryNoStartPollNegative.class.getSimpleName());

    @LargeTest
    public void runTest() throws Exception {
        String[] LOCK_DROP = {"su", "-c", "iptables -t filter -A INPUT -p tcp --sport 7735 -j DROP"};
        String[] UNLOCK_DROP = {"su", "-c", "iptables -t filter -D INPUT -p tcp --sport 7735 -j DROP"};

        final String RESOURCE_URI = "asimov_it_cv_ordinary_no_start_poll";
        String uri = createTestResourceUri(RESOURCE_URI);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").getRequest();

        Runtime.getRuntime().exec(LOCK_DROP).waitFor();
        PrepareResourceUtil.prepareResource(uri, false);

        try {
            // 1.1
            HttpResponse response = checkMiss(request, 1, VALID_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.2
            response = checkMiss(request, 2, VALID_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.3
            response = checkMiss(request, 3, VALID_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.4 this response should be served from OC cache
            response = checkHit(request, 4, VALID_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.5
            response = checkMiss(request, 5, VALID_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.6
            response = checkMiss(request, 6, VALID_RESPONSE);
            logSleeping(MIN_NON_RMP_PERIOD - response.getDuration());

            // 1.7
            checkMiss(request, 7, VALID_RESPONSE);

        } finally {
            logger.info("Ivalidating resource");
            // invalidate resource to stop server polling
            PrepareResourceUtil.prepareResource(uri, true);
            Runtime.getRuntime().exec(UNLOCK_DROP).waitFor();
        }
    }
}
