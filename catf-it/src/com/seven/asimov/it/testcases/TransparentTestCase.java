package com.seven.asimov.it.testcases;


import android.content.Context;
import com.seven.asimov.it.base.*;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.HtrxNotOptimizingTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CERemovedFromBDTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CERemovedFromCashTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CERemovedFromFSTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CsaTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.CrcsTransferSuccessTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAppliedTask;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollParamsTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class TransparentTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(TransparentTestCase.class.getSimpleName());
    protected static final String CRCS_SENT = "CRCS_SENT";
    protected static final String FCL_NONE = "FCL_NONE";
    protected static final String NO_POLLING = "NO_POLLING";
    protected static final String NO_CACHING = "NO_CACHING";
    protected final static String TRANSPARENT_CHECK = "TRANSPARENT_CHECK";

    private HttpResponse response;
    private String uri;
    List<TestCaseThread> customThreads = new ArrayList<TestCaseThread>();

    protected final long RMP_PERIOD = 35 * 1000;
    protected final long RI_PERIOD = 70 * 1000;
    protected final long LP_PERIOD = 70 * 1000;
    protected final long LP_DELAY = 65 * 1000;
    protected final long RLP_PERIOD = 35 * 1000;
    protected final long CC_PERIOD = 30 * 1000;
    private final String POLICY_NAMESPACE = "@asimov";
    private final String POLICY_NAME = "transparent";

    protected void funcForSwithchingTransparentMode(final String uri, final String encodedRawHeadersDef, final String encodedRawHeadersDef304, final HttpRequest request, final long sleepOn, final long sleepOff,
                                                    int reqCount, long sleepIT) throws Throwable {
        CERemovedFromBDTask cebd = new CERemovedFromBDTask();
        CERemovedFromCashTask cecash = new CERemovedFromCashTask();
        CERemovedFromFSTask fsTask = new CERemovedFromFSTask();
        LogcatUtil logcat = new LogcatUtil(getContext(), cebd, cecash, fsTask);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        //Thread with change policy
        Callable<Void> setPolicy = transparentModeOn(sleepOn);
        Callable<Void> trasparentOff = transparentModeOff(sleepOff);

        Runnable status304 = new Runnable() {
            @Override
            public void run() {
                try {
                    PrepareResourceUtil.prepareResource304(uri, encodedRawHeadersDef304);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        };
        Runnable status200 = new Runnable() {
            @Override
            public void run() {
                try {
                    PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        };
        Thread status;
        try {
            logcat.start();
            if (sleepOff > 0) {
                PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);
            } else {
                PrepareResourceUtil.prepareResource(uri, false);
            }
            int requestId = 1;
            if (encodedRawHeadersDef304 != null) {
                // 1 MISS - 200 OK
                checkMiss(request, requestId++);
                status = new Thread(status304);
                status.start();
                logSleeping(40 * 1000);

                // 2 MISS - 304
                checkMiss304(request, requestId++);
                logSleeping(40 * 1000);

                // 3 MISS - 304
                checkMiss304(request, requestId++);
                logSleeping(40 * 1000);

                // 4 HIT - 304
                checkHit304(request, requestId++);
            } else {
                for (int i = 0; i < reqCount; i++) {
                    response = checkMiss(request, requestId++);
                    TestUtil.sleep(sleepIT - response.getDuration());
                }
                response = checkHit(request, requestId++);
            }
            //start changing policy thread
            Future<Void> policyResult = executorService.submit(setPolicy);
            executorService.shutdown();
            logger.info("get future");
            policyResult.get();

            if (sleepOff > 0) {
                // transparent OFF
                executorService = Executors.newSingleThreadExecutor();
                Future<Void> offResult = executorService.submit(trasparentOff);
                executorService.shutdown();
                TestUtil.sleep(60 * 1000);
                logger.info("get \"OFF\"future");
                offResult.get();
            }
            checkMiss(request, requestId);
            TestUtil.sleep(60 * 1000);
            logcat.stop();

            assertFalse("CE should be deleted", cecash.getLogEntries().isEmpty());
            assertTrue("CE wasn't removed from all resources " + cecash.getLogEntries().get(0), cecash.getLogEntries().get(0).isAllRemoved());

        } finally {
            if (logcat.isRunning()) {
                logcat.stop();
            }
            final Policy transparent = new Policy(POLICY_NAME, "0", POLICY_NAMESPACE, true);
            PMSUtil.addPolicies(new Policy[]{transparent});
            SmsUtil.sendPolicyUpdate(getContext(), (byte) 1);
            TestUtil.sleep(60 * 1000);
            executorService.shutdownNow();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    protected String base64Encode(String expected) {
        return URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
    }

    private Callable<Void> transparentModeOn(final long sleep) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PolicyAppliedTask policyAppliedTask = new PolicyAppliedTask("transparent", "1");
                LogcatUtil chekPolicy = new LogcatUtil(getContext(), policyAppliedTask);
                logger.info("transparentModeOn log start");
                chekPolicy.start();

                final Policy transparent = new Policy(POLICY_NAME, "1", POLICY_NAMESPACE, true);
                PMSUtil.addPolicies(new Policy[]{transparent});
                SmsUtil.sendPolicyUpdate(getContext(), (byte) 1);
                logSleeping(sleep);

                logger.info("transparentModeOn log stop");
                chekPolicy.stop();
                assertFalse("policies wasn't set right", policyAppliedTask.getLogEntries().isEmpty());
                logger.info("transparentModeOn end");
                return null;
            }
        };
    }

    private Callable<Void> transparentModeOff(final long sleep) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PolicyAppliedTask policyAppliedTask = new PolicyAppliedTask("transparent", "0");
                LogcatUtil chekPolicy = new LogcatUtil(getContext(), policyAppliedTask);
                logger.info("transparentModeOff log start");
                chekPolicy.start();

                final Policy transparent = new Policy(POLICY_NAME, "0", POLICY_NAMESPACE, true);
                PMSUtil.addPolicies(new Policy[]{transparent});
                SmsUtil.sendPolicyUpdate(getContext(), (byte) 1);
                logSleeping(sleep);

                logger.info("transparentModeOff log stop");
                chekPolicy.stop();
                assertFalse("policies wasn't set right", policyAppliedTask.getLogEntries().isEmpty());
                logger.info("transparentModeOff end");
                return null;
            }
        };
    }


    protected boolean checkTransparent(String uri, int requests, int interval, boolean bypassing, Context ctx, String checkParams) throws InterruptedException {
        final HtrxNotOptimizingTask htrxNOTask = new HtrxNotOptimizingTask();
        final CrcsTransferSuccessTask ctst = new CrcsTransferSuccessTask();
        final CsaTask csa = new CsaTask();
        final StartPollParamsTask sppTask = new StartPollParamsTask();
        final LogcatUtil logcat = new LogcatUtil(ctx, ctst, csa, htrxNOTask, sppTask);

        try {
            logcat.start();

            for (int i = 0; i < requests; i++) {
                logger.info("");
                sendRequest2(createRequest().setUri(uri)
                        .addHeaderField("X-OC-Encoding", "identity")
                        .getRequest());
                Thread.sleep(interval);
            }
            logcat.stop();

        } finally {
            logcat.stop();
        }

        boolean isTransparentEnabled = (!htrxNOTask.getLogEntries().isEmpty()) &&
                (htrxNOTask.getLogEntries().get(0).isTransparent());

        assertTrue("Expected transparent mode " + (bypassing ? "enabled" : "disabled") + " but it is " +
                (isTransparentEnabled ? "enabled" : "disabled"),
                isTransparentEnabled == bypassing);


        if (checkParams.equals(CRCS_SENT)) {
            if (!ctst.getLogEntries().isEmpty() == bypassing) {
                return true;
            }
        } else if (checkParams.equals(NO_CACHING)) {
            if (csa.getLogEntries().isEmpty() == bypassing) {
                return true;
            }
        } else if (checkParams.equals(NO_POLLING)) {
            if (sppTask.getLogEntries().isEmpty() == bypassing) {
                return true;
            }
        } else if (checkParams.equals(TRANSPARENT_CHECK)) {
            if ((csa.getLogEntries().isEmpty() == bypassing)) {
                return true;
            }
        }

        return false;
    }


}
