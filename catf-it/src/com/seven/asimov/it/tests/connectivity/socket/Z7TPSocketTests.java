package com.seven.asimov.it.tests.connectivity.socket;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.testcases.ConnectivitySocketTestCase;

public class Z7TPSocketTests extends ConnectivitySocketTestCase {

    /**
     * /**
     * <p>Verify, that OC provides invalidate with cache due to inactivity trigger after long poll start
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Start long poll
     * 2. Send third request with the same URL with new body
     * 4. Wait 30 sec
     * 5. Observe client log
     * </p>
     * <p>Expected reults:
     * 1. Response for the third request should be sent from cache with new body.
     * </p>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_001_Z7TPInactivityTrigger() throws Throwable {
        final String resourceUri = "asimov_it_cv_zconn_0011";
        executeTestZ7TPInactivityTrigger(resourceUri, SLEEP_TIME, WAIT_FOR_INACTIVITY_TRIGGER);
    }

    /**
     * /**
     * <p>Verify, that OC provides invalidate with cache due to inactivity trigger after long poll start
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Start two rapid manual polls one by one
     * 2. Send two requests with the same URLs but new bodies
     * 3. Wait for 3 minutes
     * 4. Observe client log
     * </p>
     * <p>Expected reults:
     * 1. The both fourth responses should be sent from cache with new bodies.
     * </p>
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_002_Z7TPFloatClosure() throws Throwable {
        String resourceUri1 = "asimov_it_cv_zconn_0021";
        String resourceUri2 = "asimov_it_cv_zconn_0022";
        executeTestZ7TPFloatClosure(resourceUri1, resourceUri2, WAIT_FOR_INACTIVITY_TRIGGER, SLEEP_TIME);
    }
}

