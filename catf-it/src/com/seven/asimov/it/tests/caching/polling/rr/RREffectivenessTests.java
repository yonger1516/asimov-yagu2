package com.seven.asimov.it.tests.caching.polling.rr;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.pollingTasks.StartPollTask;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.apache.http.HttpStatus;

import java.util.LinkedList;

public class RREffectivenessTests extends TcpDumpTestCase {
    static final String TERE = "tere";
    static final String ERET = "eret";
    protected static String TEST_RESOURCE_OWNER = "asimov_it";
    public final static int WAIT_FOR_POLICY_UPDATE = 25 * 1000;

    static final int SC_OK = HttpStatus.SC_OK;
    static final int RI_65 = 65 * 1000;
    static final int RI_80 = 80 * 1000;
    static final int RTO_20 = 20 * 1000;
    static final int RTO_120 = 120 * 1000;

    /**
     * Pattern: [0,65,65,....]
     * <p/>
     * Expected results:
     * 1.  RI should be detected after 3rd request and polling should start after receiving response
     * 2.  4th request should be HITed
     * 3.  IWC notification should be received before 5th request.
     * 4.  5th request should be HITed with new response.
     * 5.  6th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 6.  7th request should be HITed
     * 7.  IWC notification should be received before 8th request.
     * 8.  8th request should be HITed with new response.
     * 9.  9th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 10. 10th request should be HITed
     * 11. IWC notification should be received before 11th request.
     * 12. 11th request should be HITed with new response.
     * 13. Average hitcount since miss = '2' that is equal to min_effective_hitcount so OCEngine makes decision that RR is effective
     * 14. 12th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 15. 13th request should be HITed
     *
     * @throws Throwable
     */
    public void test_001_RREffect() throws Throwable {
        String resource = "test_asimov_rr_effect_001";
        String uri = createTestResourceUri(resource);

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        HttpRequest invRequest = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", ERET)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        HttpRequest invRequestToValid = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", TERE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        PMSUtil.addPolicies(new Policy[]{new Policy("min_effective_hitcount", "2", "@asimov@http", true)});
        logSleeping(WAIT_FOR_POLICY_UPDATE);

        final LinkedList<RequestResponse> RR = new LinkedList<RequestResponse>();
        RR.add(new RequestResponse(request, 1, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 2, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 3, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 4, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new RequestResponse(request, 5, RTO_20, RI_65, SC_OK, TERE, 0, invRequest, RTO_20)); // hit TERE, inv. to ERET
        RR.add(new RequestResponse(request, 6, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new RequestResponse(request, 7, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new RequestResponse(request, 8, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new RequestResponse(request, 9, RTO_20, RI_65, SC_OK, ERET, 0, invRequestToValid, RTO_20)); // hit ERET, inv. to TERE
        RR.add(new RequestResponse(request, 10, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new RequestResponse(request, 11, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 12, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new RequestResponse(request, 13, RTO_20, RI_65, SC_OK, TERE, 0, invRequest, RTO_20)); // hit TERE, inv. to ERET
        RR.add(new RequestResponse(request, 14, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new RequestResponse(request, 15, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new RequestResponse(request, 16, RTO_20, 0, SC_OK, ERET, 0)); // hit ERET

        StartPollTask startPollTask = null;
        LogcatUtil logcatUtil = null;
        try {
            for (int i = 1; i <= RR.size(); i++) {
                if (i == 3 || i == 7 || i == 11 || i == 15) {
                    startPollTask = new StartPollTask();
                    logcatUtil = new LogcatUtil(getContext(), startPollTask);
                    logcatUtil.start();
                }

                if (i == 5 || i == 9 || i == 13) {
                    final LogcatUtil fLogcat = logcatUtil;
                    final StartPollTask fStartpol = startPollTask;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                logSleeping(55 * 1000);
                                fLogcat.stop();
                                if (!fStartpol.getLogEntries().isEmpty()) {
                                    final StartPollWrapper ourWrapper =
                                            fStartpol.getLogEntries().get(fStartpol.getLogEntries().size() - 1);
                                    SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(ourWrapper.getSubscriptionId()),
                                            SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }).start();
                }

                executeConncetion(RR.get(i - 1)); // execute request number i

                if (i == 5 || i == 9 || i == 12 || i == 15) {
                    if (logcatUtil.isRunning())
                        logcatUtil.stop();

                    assertTrue("Start of polling should be reported in client log on " +
                            "request R0." + i, !startPollTask.getLogEntries().isEmpty());
                }
            }
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Pattern: [0,65,65,....]
     * <p/>
     * Expected results:
     * 1.  RI should be detected after 3rd request and polling should start after receiving response
     * 2.  4th request should be HITed
     * 3.  IWC notification should be received before 5th request.
     * 4.  5th request should be HITed with new response.
     * 5.  6th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 6.  IWC notification should be received before 7th request.
     * 8.  7th request should be HITed with new response.
     * 9.  8th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 10. IWC notification should be received before 9th request.
     * 11. 9th request should be HITed with new response.
     * 12. Average hitcount since miss less then min_effective_hitcount so OCEngine makes decision that RR is not effective
     * 13. 10th – 11th requests should be MISSed.
     *
     * @throws Throwable
     */
    public void test_002_RREffect() throws Throwable {
        String resource = "test_asimov_rr_effect_002";
        String uri = createTestResourceUri(resource);

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        HttpRequest invRequest = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        HttpRequest invRequestToValid = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", VALID_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        PMSUtil.addPolicies(new Policy[]{new Policy("min_effective_hitcount", "2", "@asimov@http", true)});
        logSleeping(WAIT_FOR_POLICY_UPDATE);

        final LinkedList<RequestResponse> RR = new LinkedList<RequestResponse>();
        RR.add(new RequestResponse(request, 1, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 2, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 3, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 4, RTO_20, RI_65, SC_OK, TERE, 0, invRequest, RTO_20)); // hit TERE, inv. to ERET
        RR.add(new RequestResponse(request, 5, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new RequestResponse(request, 6, RTO_20, RI_65, SC_OK, ERET, 1, invRequestToValid, RTO_20)); // miss ERET, inv. to TERE
        RR.add(new RequestResponse(request, 7, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new RequestResponse(request, 8, RTO_20, RI_65, SC_OK, TERE, 1, invRequest, RTO_20)); // miss TERE, inv. to ERET
        RR.add(new RequestResponse(request, 9, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new RequestResponse(request, 10, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new RequestResponse(request, 11, RTO_20, 0, SC_OK, ERET, 1)); // miss ERET

        StartPollTask startPollTask = null;
        LogcatUtil logcatUtil = null;
        try {
            for (int i = 1; i <= RR.size(); i++) {
                if (i == 3 || i == 6 || i == 8) {
                    startPollTask = new StartPollTask();
                    logcatUtil = new LogcatUtil(getContext(), startPollTask);
                    logcatUtil.start();
                }

                if (i == 4 || i == 6 || i == 8) {
                    final LogcatUtil fLogcat = logcatUtil;
                    final StartPollTask fStartpol = startPollTask;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                logSleeping(55 * 1000);
                                fLogcat.stop();
                                if (!fStartpol.getLogEntries().isEmpty()) {
                                    final StartPollWrapper ourWrapper =
                                            fStartpol.getLogEntries().get(fStartpol.getLogEntries().size() - 1);
                                    SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(ourWrapper.getSubscriptionId()), SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }).start();
                }

                executeConncetion(RR.get(i - 1)); // execute request number i

                if (i == 4 || i == 6 || i == 8) {
                    if (logcatUtil.isRunning())
                        logcatUtil.stop();

                    assertTrue("Start of polling should be reported in client log on " +
                            "request R0." + i, !startPollTask.getLogEntries().isEmpty());
                }
            }
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Pattern: [0,65,65,....]
     * <p/>
     * Expected results:
     * 1.  RI should be detected after 3rd request and polling should start after receiving response
     * 2.  4th request should be HITed
     * 3.  IWC notification should be received before 5th request.
     * 4.  5th request should be HITed with new response.
     * 5.  6th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 6.  IWC notification should be received before 7th request.
     * 8.  7th request should be HITed with new response.
     * 9.  8th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 10. IWC notification should be received before 9th request.
     * 11. 9th request should be HITed with new response.
     * 12. Average hitcount since miss less then min_effective_hitcount so OCEngine makes decision that RR is not effective
     * 13. 10th – 11th requests should be MISSed. After 11th response OC should detect that RR can be effective again
     * 14. 12th request should be MISSed, RI should be re-detected and polling should start after receiving response
     *
     * @throws Throwable
     */
    public void test_003_RREffect() throws Throwable {
        String resource = "test_asimov_rr_effect_003";
        String uri = createTestResourceUri(resource);

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        HttpRequest invRequest = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        HttpRequest invRequestToValid = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", VALID_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        PMSUtil.addPolicies(new Policy[]{new Policy("min_effective_hitcount", "2", "@asimov@http", true)});
        logSleeping(WAIT_FOR_POLICY_UPDATE);

        final LinkedList<RequestResponse> RR = new LinkedList<RequestResponse>();
        RR.add(new RequestResponse(request, 1, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 2, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 3, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 4, RTO_20, RI_65, SC_OK, TERE, 0, invRequest, RTO_20)); // hit TERE, inv. to ERET
        RR.add(new RequestResponse(request, 5, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new RequestResponse(request, 6, RTO_20, RI_65, SC_OK, ERET, 1, invRequestToValid, RTO_20)); // miss ERET, inv. to TERE
        RR.add(new RequestResponse(request, 7, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new RequestResponse(request, 8, RTO_20, RI_65, SC_OK, TERE, 1, invRequest, RTO_20)); // miss TERE, inv. to ERET
        RR.add(new RequestResponse(request, 9, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new RequestResponse(request, 10, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new RequestResponse(request, 11, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new RequestResponse(request, 12, RTO_20, 0, SC_OK, ERET, 1)); // miss ERET

        StartPollTask startPollTask = null;
        LogcatUtil logcatUtil = null;
        try {
            for (int i = 1; i <= RR.size(); i++) {
                if (i == 3 || i == 6 || i == 8 || i == 12) {
                    startPollTask = new StartPollTask();
                    logcatUtil = new LogcatUtil(getContext(), startPollTask);
                    logcatUtil.start();
                }

                if (i == 4 || i == 6 || i == 8) {
                    final LogcatUtil fLogcat = logcatUtil;
                    final StartPollTask fStartpol = startPollTask;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                logSleeping(55 * 1000);
                                fLogcat.stop();
                                if (!fStartpol.getLogEntries().isEmpty()) {
                                    final StartPollWrapper ourWrapper =
                                            fStartpol.getLogEntries().get(fStartpol.getLogEntries().size() - 1);
                                    SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(ourWrapper.getSubscriptionId()), SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }).start();
                }

                executeConncetion(RR.get(i - 1)); // execute request number i

                if (i == 4 || i == 6 || i == 8) {
                    if (logcatUtil.isRunning())
                        logcatUtil.stop();

                    assertTrue("Start of polling should be reported in client log on " +
                            "request R0." + i, !startPollTask.getLogEntries().isEmpty());
                }
            }
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Pattern: [0,65,65,...]
     * <p/>
     * Expected results:
     * 1. RI should be detected after 3rd request and polling should start after receiving response
     * 2. 4th request should be HITed
     * 3. IWC notification should be received before 5th request.
     * 4. 5th request should be HITed with new response.
     * 5. 6th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 6. IWC notification should be received before 7th request.
     * 8.  7th request should be HITed with new response.
     * 9.  8th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 10. IWC notification should be received before 9th request.
     * 11. 9th request should be HITed with new response.
     * 12. Average hitcount since miss less then min_effective_hitcount so OCEngine makes decision that RR is not effective
     * 13. 10th – 12th requests should be MISSed, RI should not be re-detected and polling should not start after receiving response.
     *
     * @throws Throwable
     */
    public void test_004_RREffect() throws Throwable {
        String resource = "test_asimov_rr_effect_004";
        String uri = createTestResourceUri(resource);

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        HttpRequest invRequest = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        HttpRequest invRequestToValid = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", VALID_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        PMSUtil.addPolicies(new Policy[]{new Policy("min_effective_hitcount", "2", "@asimov@http", true)});
        logSleeping(WAIT_FOR_POLICY_UPDATE);

        final LinkedList<RequestResponse> RR = new LinkedList<RequestResponse>();
        RR.add(new RequestResponse(request, 1, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 2, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 3, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new RequestResponse(request, 4, RTO_20, RI_65, SC_OK, TERE, 0, invRequest, RTO_20)); // hit TERE, inv. to ERET
        RR.add(new RequestResponse(request, 5, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new RequestResponse(request, 6, RTO_20, RI_65, SC_OK, ERET, 1, invRequestToValid, RTO_20)); // miss ERET, inv. to TERE
        RR.add(new RequestResponse(request, 7, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new RequestResponse(request, 8, RTO_20, RI_65, SC_OK, TERE, 1, invRequest, RTO_20)); // miss TERE, inv. to ERET
        RR.add(new RequestResponse(request, 9, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new RequestResponse(request, 10, RTO_20, RI_65, SC_OK, ERET, 1, invRequestToValid, RTO_20)); // miss ERET, inv. to TERE
        RR.add(new RequestResponse(request, 11, RTO_20, RI_65, SC_OK, TERE, 1, invRequest, RTO_20)); // miss TERE, inv. to ERET
        RR.add(new RequestResponse(request, 12, RTO_20, 25 * 1000, SC_OK, ERET, 1)); // miss ERET

        StartPollTask startPollTask = null;
        LogcatUtil logcatUtil = null;
        try {
            for (int i = 1; i <= RR.size(); i++) {
                if (i == 3 || i == 6 || i == 8 || i == 12) {
                    startPollTask = new StartPollTask();
                    logcatUtil = new LogcatUtil(getContext(), startPollTask);
                    logcatUtil.start();
                }

                if (i == 4 || i == 6 || i == 8 || i == 10 || i == 11) {
                    final LogcatUtil fLogcat = logcatUtil;
                    final StartPollTask fStartpol = startPollTask;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                logSleeping(55 * 1000);
                                fLogcat.stop();
                                if (!fStartpol.getLogEntries().isEmpty()) {
                                    final StartPollWrapper ourWrapper =
                                            fStartpol.getLogEntries().get(fStartpol.getLogEntries().size() - 1);
                                    SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(ourWrapper.getSubscriptionId()), SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }).start();
                }

                executeConncetion(RR.get(i - 1)); // execute request number i

                if (i == 4 || i == 6 || i == 8 || i == 10 || i == 11) {
                    if (logcatUtil.isRunning())
                        logcatUtil.stop();

                    assertTrue("Start of polling should be reported in client log on " +
                            "request R0." + i, !startPollTask.getLogEntries().isEmpty());
                }

                if (i == 12) {
                    if (logcatUtil.isRunning())
                        logcatUtil.stop();

                    assertTrue("Start of polling SHOULD NOT be reported in client log on " +
                            "request R0." + i, startPollTask.getLogEntries().isEmpty());
                }
            }
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Pattern: [0,65,65,65,65,65,35,30]
     * <p/>
     * Set such policy on PMS server:
     * asimov@http@min_effective_hitcount=3
     * <p/>
     * Expected results:
     * 1. RI should be detected after 3rd request and polling should start after receiving response
     * 2. 4th  - 6th requests should be HITed
     * 3. 7th request should be MISSed (out-of-order)
     * 4. 8th request should be HITed
     * 3. IWC notification should be received before 9th request.
     * 4. 9th request should be HITed with new response.
     * 5. 10th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 6. 11th – 13th requests should be HITed
     * 7. 14th request should be MISSed (out-of-order)
     * 8. 15th request should be HITed
     * 9. IWC notification should be received before 16th request.
     * 10. 16th request should be HITed with new response.
     * 11.  17th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 12. 18th – 20th request should be HITed
     * 13. 21st  request should be MISSed (out-of-order)
     * 14. 22nd  request should be HITed
     * 15. IWC notification should be received before 23th request.
     * 16. 23rd  request should be HITed with new response.
     * 17. OC should make decision that RR is effective
     * 18. 24th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 19. 25th request should be HITed
     *
     * @throws Throwable
     */
    public void test_005_RREffect() throws Throwable {
        String resource = "test_asimov_rr_effect_005";
        String uri = createTestResourceUri(resource);

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        HttpRequest invRequest = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        HttpRequest invRequestToValid = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", VALID_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        PMSUtil.addPolicies(new Policy[]{new Policy("min_effective_hitcount", "3", "@asimov@http", true),
                new Policy("out_of_order_aggressiveness", "0", "@asimov@http", true)});
        logSleeping(WAIT_FOR_POLICY_UPDATE);

        final LinkedList<TcpDumpTestCase.RequestResponse> RR = new LinkedList<TcpDumpTestCase.RequestResponse>();
        RR.add(new TcpDumpTestCase.RequestResponse(request, 1, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 2, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 3, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 4, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 5, RTO_20, 35 * 1000, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 6, RTO_20, 30 * 1000, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 7, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 8, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 9, RTO_20, RI_65, SC_OK, TERE, 0, invRequest, RTO_20)); // hit TERE, inv. to ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 10, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 11, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 12, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 13, RTO_20, 35 * 1000, SC_OK, ERET, 0)); // hit ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 14, RTO_20, 30 * 1000, SC_OK, ERET, 1)); // miss ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 15, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 16, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 17, RTO_20, RI_65, SC_OK, ERET, 0, invRequestToValid, RTO_20)); // hit ERET, inv. to TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 18, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 19, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 20, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 21, RTO_20, 35 * 1000, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 22, RTO_20, 30 * 1000, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 23, RTO_20, RI_65, SC_OK, TERE, 0));
        RR.add(new TcpDumpTestCase.RequestResponse(request, 24, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 25, RTO_20, RI_65, SC_OK, TERE, 0, invRequest, RTO_20)); // hit TERE, inv. to ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 26, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 27, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 28, RTO_20, 25 * 1000, SC_OK, ERET, 0)); // hit ERET

        StartPollTask startPollTask = null;
        LogcatUtil logcatUtil = null;
        try {
            for (int i = 1; i <= RR.size(); i++) {
                if (i == 3 || i == 11 || i == 18 || i == 27) {
                    startPollTask = new StartPollTask();
                    logcatUtil = new LogcatUtil(getContext(), startPollTask);
                    logcatUtil.start();
                }

                if (i == 9 || i == 17 || i == 25) {
                    final LogcatUtil fLogcat = logcatUtil;
                    final StartPollTask fStartpol = startPollTask;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                logSleeping(55 * 1000);
                                fLogcat.stop();
                                if (!fStartpol.getLogEntries().isEmpty()) {
                                    final StartPollWrapper ourWrapper =
                                            fStartpol.getLogEntries().get(fStartpol.getLogEntries().size() - 1);
                                    SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(ourWrapper.getSubscriptionId()), SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }).start();
                }

                executeConncetion(RR.get(i - 1)); // execute request number i


                if (i == 8 || i == 16 || i == 24 || i == 27) {
                    if (logcatUtil.isRunning())
                        logcatUtil.stop();

                    assertTrue("Start of polling should be reported in client log on " +
                            "request R0." + i, !startPollTask.getLogEntries().isEmpty());
                }
            }
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Pattern: [0,65,65]
     * <p/>
     * Expected results:
     * 1. RI should be detected after 3rd request and polling should start after receiving response
     * 2. 4th request should be HITed
     * 3. IWC notification should be received before 5th request.
     * 4. 5th request should be HITed with new response.
     * 5. 6th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 6. 7th request should be HITed
     * 7. IWC notification should be received before 8th request.
     * 8.  8th request should be HITed with new response.
     * 9.  9th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 10. 10th request should be HITed
     * 11. IWC notification should be received before 11th request.
     * 12. 11th request should be HITed with new response.
     * 13. Average hitcount since miss = '2' that is equal to min_effective_hitcount so OC should make decision that RR is effective
     * 14. 12th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 15. IWC notification should be received before 13th request.
     * 16. 13th request should be HITed  with new response.
     * 17. Average hitcount since miss is less then min_effective_hitcount so OC should make decision that RR is not effective
     * 18. 14th request should be MISSed, RI should not be re-detected and polling should not start after receiving response.
     *
     * @throws Throwable
     */
    public void test_006_RREffect() throws Throwable {
        String resource = "test_asimov_rr_effect_006";
        String uri = createTestResourceUri(resource);

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        HttpRequest invRequest = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        HttpRequest invRequestToValid = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", VALID_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        PMSUtil.addPolicies(new Policy[]{new Policy("min_effective_hitcount", "2", "@asimov@http", true)});
        logSleeping(WAIT_FOR_POLICY_UPDATE);

        final LinkedList<TcpDumpTestCase.RequestResponse> RR = new LinkedList<TcpDumpTestCase.RequestResponse>();
        RR.add(new TcpDumpTestCase.RequestResponse(request, 1, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 2, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 3, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 4, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 5, RTO_20, RI_65, SC_OK, TERE, 0, invRequest, RTO_20)); // hit TERE, inv. to ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 6, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 7, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 8, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 9, RTO_20, RI_65, SC_OK, ERET, 0, invRequestToValid, RTO_20)); // hit ERET, inv. to TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 10, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 11, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 12, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 13, RTO_20, RI_65, SC_OK, TERE, 0, invRequest, RTO_20)); // hit TERE, inv. to ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 14, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 15, RTO_20, 25 * 1000, SC_OK, ERET, 1)); // miss ERET // 25 sec wait for start poll
        RR.add(new TcpDumpTestCase.RequestResponse(request, 16, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 17, RTO_20, 25 * 1000, SC_OK, TERE, 1)); // miss TERE

        StartPollTask startPollTask = null;
        LogcatUtil logcatUtil = null;
        try {
            for (int i = 1; i <= RR.size(); i++) {
                if (i == 3 || i == 7 || i == 11 || i == 15 || i == 17) {
                    startPollTask = new StartPollTask();
                    logcatUtil = new LogcatUtil(getContext(), startPollTask);
                    logcatUtil.start();
                }

                if (i == 5 || i == 9 || i == 13) {
                    final LogcatUtil fLogcat = logcatUtil;
                    final StartPollTask fStartpol = startPollTask;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                logSleeping(55 * 1000);
                                fLogcat.stop();
                                if (!fStartpol.getLogEntries().isEmpty()) {
                                    final StartPollWrapper ourWrapper =
                                            fStartpol.getLogEntries().get(fStartpol.getLogEntries().size() - 1);
                                    SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(ourWrapper.getSubscriptionId()), SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }).start();
                }

                executeConncetion(RR.get(i - 1)); // execute request number i

                if (i == 15) {  // it is done to wait 25 secs for start poll in the log
                    // and then invalidate the resource. see "25 sec wait for start poll" above
                    long start = System.currentTimeMillis();
                    sendRequest2(invRequestToValid, false, true);
                    long end = System.currentTimeMillis();
                    long intv = end - start;
                    logSleeping(RI_65 - 25 * 1000 - intv);
                }

                if (i == 4 || i == 8 || i == 12 || i == 15) {
                    if (logcatUtil.isRunning())
                        logcatUtil.stop();

                    assertTrue("Start of polling should be reported in client log on " +
                            "request R0." + i, !startPollTask.getLogEntries().isEmpty());
                }

                if (i == 17) {
                    if (logcatUtil.isRunning())
                        logcatUtil.stop();

                    assertTrue("Start of polling SHOULD NOT be reported in client log on " +
                            "request R0." + i, startPollTask.getLogEntries().isEmpty());
                }
            }
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    /**
     * Pattern: [0,65,65]
     * <p/>
     * Set such policy on PMS server:
     * asimov@http@min_effective_hitcount=3
     * <p/>
     * Expected results:
     * 1.  RI should be detected after 3rd request and polling should start after receiving response
     * 2.  4th request should be HITed
     * 3.  IWC notification should be received before 5th request.
     * 4.  5th request should be HITed with new response.
     * 5.  6th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 6.  7th request should be HITed
     * 7.  IWC notification should be received before 8th request.
     * 8.  8th request should be HITed with new response.
     * 9.  9th request should be MISSed, RI should be re-detected and polling should start after receiving response
     * 10. 10th request should be HITed
     * 11. IWC notification should be received before 11th request.
     * 12. 11th request should be HITed with new response.
     * 13. Average hitcount since is less then min_effective_hitcount so OC should make decision that RR is not effective
     * 14. 12th – 14th requests should be MISSed. After 14th response OC should detect that RR can be effective again
     * 15. 15th request should be MISSed, RI should be re-detected and polling should start after receiving response
     *
     * @throws Throwable
     */
    public void test_007_RREffect() throws Throwable {
        String resource = "test_asimov_rr_effect_007";
        String uri = createTestResourceUri(resource);

        PrepareResourceUtil.prepareResource(uri, false);

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        HttpRequest invRequest = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", INVALIDATED_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        HttpRequest invRequestToValid = createRequest()
                .setMethod("GET").setUri(uri)
                .addHeaderField("X-OC-ChangeResponseContent", VALID_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", TEST_RESOURCE_OWNER).getRequest();

        PMSUtil.addPolicies(new Policy[]{new Policy("min_effective_hitcount", "3", "@asimov@http", true)});
        logSleeping(WAIT_FOR_POLICY_UPDATE);

        final LinkedList<TcpDumpTestCase.RequestResponse> RR = new LinkedList<TcpDumpTestCase.RequestResponse>();
        RR.add(new TcpDumpTestCase.RequestResponse(request, 1, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 2, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 3, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 4, RTO_20, RI_65, SC_OK, TERE, 0, invRequest, RTO_20)); // hit TERE, inv. to ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 5, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 6, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 7, RTO_20, RI_65, SC_OK, ERET, 0, invRequestToValid, RTO_20)); // hit ERET, inv. to TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 8, RTO_20, RI_65, SC_OK, TERE, 0)); // hit TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 9, RTO_20, RI_65, SC_OK, TERE, 1)); // miss TERE
        RR.add(new TcpDumpTestCase.RequestResponse(request, 10, RTO_20, RI_65, SC_OK, TERE, 0, invRequest, RTO_20)); // hit TERE, inv. to ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 11, RTO_20, RI_65, SC_OK, ERET, 0)); // hit ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 12, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 13, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 14, RTO_20, RI_65, SC_OK, ERET, 1)); // miss ERET
        RR.add(new TcpDumpTestCase.RequestResponse(request, 15, RTO_20, 25 * 1000, SC_OK, ERET, 1)); // miss ERET

        StartPollTask startPollTask = null;
        LogcatUtil logcatUtil = null;
        try {
            for (int i = 1; i <= RR.size(); i++) {
                if (i == 3 || i == 6 || i == 9 || i == 15) {
                    startPollTask = new StartPollTask();
                    logcatUtil = new LogcatUtil(getContext(), startPollTask);
                    logcatUtil.start();
                }

                if (i == 4 || i == 7 || i == 10) {
                    final LogcatUtil fLogcat = logcatUtil;
                    final StartPollTask fStartpol = startPollTask;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                logSleeping(55 * 1000);
                                fLogcat.stop();
                                if (!fStartpol.getLogEntries().isEmpty()) {
                                    final StartPollWrapper ourWrapper =
                                            fStartpol.getLogEntries().get(fStartpol.getLogEntries().size() - 1);
                                    SmsUtil.sendInvalidationSms(getContext(), Integer.parseInt(ourWrapper.getSubscriptionId()), SmsUtil.InvalidationType.INVALIDATE_WITH_CACHE.byteVal);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }).start();
                }

                executeConncetion(RR.get(i - 1)); // execute request number i


                if (i == 4 || i == 7 || i == 10 || i == 15) {
                    if (logcatUtil.isRunning())
                        logcatUtil.stop();

                    assertTrue("Start of polling should be reported in client log on " +
                            "request R0." + i, !startPollTask.getLogEntries().isEmpty());
                }
            }
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }
}
