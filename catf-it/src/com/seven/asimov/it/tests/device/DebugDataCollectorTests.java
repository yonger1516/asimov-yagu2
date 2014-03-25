package com.seven.asimov.it.tests.device;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.debugDataCollectorTasks.DebugDataSizeTriggerTask;
import com.seven.asimov.it.utils.logcat.tasks.debugDataCollectorTasks.DebugDataTimeTriggerTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.http.client.methods.HttpGet;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DebugDataCollectorTests extends TcpDumpTestCase {
    private static final String TAG = DebugDataCollectorTests.class.getSimpleName();
    private static final String TCP_DUMP_GENERATION_REST_PROPERTY_PATH = "@asimov@debug_data@tcpdump";
    private static final String TCP_DUMP_GENERATION_REST_PROPERTY_NAME = "enabled";
    private static final String IPTABLES_DUMP_GENERATION_REST_PROPERTY_PATH = "@asimov@debug_data@iptables";
    private static final String IPTABLES_DUMP_GENERATION_REST_PROPERTY_NAME = "enabled";

    private static final String DEBUG_DATA_REST_PROPERTY_PATH = "@asimov@debug_data";
    private static final String DEBUG_DATA_ENABLE_REST_PROPERTY_NAME = "enabled";
    private static final String DEBUG_DATA_UPLOAD_INTERVAL_REST_PROPERTY_NAME = "upload_interval";
    private static final String DEBUG_DATA_LOG_SIZE_REST_PROPERTY_NAME = "log_size";

    /**
     * <p> Test steps:</p>
     * <ol>
     * <li> Send policy to turn on generation of tcp dump logs (enabled=1). </li>
     * <li> Send 3 request requests.  </li>
     * <li> Send policy to turn off generation of tcp dump logs (enabled=0). </li>
     * <li> Send 3 request requests to another resourse </li>
     * </ol>
     *  <p> Expected result:</p>
     *  <ol>
     * <li> Policy should be received </li>
     * <li> The directory /data/misc/openchannel/capture/ should be increased in the size.  </li>
     * <li> Second policy should be received </li>
     * <li> The directory /data/misc/openchannel/capture/ should not be increased in the size. </li>
     * </ol>
     */
    public void test_001_tcpDumpEnabled () throws Exception {
        final String RESOURCE_URI = "asimov_it_cv_test_001_tcpDumpGeneration";
        int size = 524030;
        List<HttpRequest> requests = new ArrayList<HttpRequest>(3);
        String[] bodies = new String[]{"a", "b", "c"};

        String policyValue = "1";
        for (int i = 0; i < 3; i++){
            requests.add(createRequest().setUri(createTestResourceUri(RESOURCE_URI + i))
                    .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .addHeaderField("X-OC-ResponseContentSize", size + "," + bodies[i]).getRequest());
        }

        try {
            String[] chmodData = {"su", "-c", "chmod -R 777 /data/"};
            Runtime.getRuntime().exec(chmodData).waitFor();
            File F = new File("/data/misc/openchannel/capture/");

            PMSUtil.addPoliciesWithCheck (new Policy[]{new Policy(TCP_DUMP_GENERATION_REST_PROPERTY_NAME, policyValue, TCP_DUMP_GENERATION_REST_PROPERTY_PATH, true)});

            long captureSizebefore = getDirSize(F);

            for (int i = 0; i < 3; i++) {
                checkMiss(requests.get(i), requestId++);
                logSleeping(5 * 1000);
            }

            long captureSizeafter = getDirSize(F);
            assertTrue("Tcp dump should be generated and log http sessions, packagesize shoulb be more that " + captureSizebefore + " but was " + captureSizeafter, captureSizeafter > captureSizebefore);
            policyValue = "0";
            //stop tcpDump to awake Data Debug Collector
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TCP_DUMP_GENERATION_REST_PROPERTY_NAME, policyValue, TCP_DUMP_GENERATION_REST_PROPERTY_PATH, true)});
            captureSizebefore = getDirSize(F);

            for (int i = 0; i < 3; i++) {
                sendRequest2(requests.get(i));
                logSleeping(5 * 1000);
            }
            captureSizeafter = getDirSize(F);
            assertTrue("Tcp dump should not be generated , package size should be " + captureSizebefore + " but was " + captureSizeafter, captureSizeafter <= captureSizebefore);

        } finally {
            PMSUtil.cleanPaths(new String[]{TCP_DUMP_GENERATION_REST_PROPERTY_PATH});
        }
    }

    /**
     * <p> Test steps:</p>
     * <ol>
     * <li> Send policy to turn on generation of nflogs (enabled=1). </li>
     * <li> Wait for 6 minutes.  </li>
     * <li> Send policy to turn off generation of tcp dump logs (enabled=0). </li>
     * <li> Wait for 6 minutes. </li>
     * </ol>
     *  <p> Expected result:</p>
     * <ol>
     * <li> Policy should be received </li>
     * <li> The directory /data/misc/openchannel/capture/ should be increased in the size.  </li>
     * <li> Second policy should be received </li>
     * <li> The directory /data/misc/openchannel/capture/ should not be increased in the size. </li>
     * </ol>
     */
    public void test_002_iptablesDumpGeneration () throws Exception {

        String policyValue = "1";
        try {
            String[] chmodData = {"su", "-c", "chmod -R 777 /data/"};
            Runtime.getRuntime().exec(chmodData).waitFor();
            File F = new File("/data/misc/openchannel/iptables_dump/");

            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(IPTABLES_DUMP_GENERATION_REST_PROPERTY_NAME, policyValue, IPTABLES_DUMP_GENERATION_REST_PROPERTY_PATH, true)});
            long iptablesSizeBefore = getDirSize(F);
            logSleeping(6*60*1000);
            long iptablesSizeAfter = getDirSize(F);

            assertTrue("Iptables dump should be generated, package size should be more that " + iptablesSizeBefore + " but was " + iptablesSizeAfter, iptablesSizeAfter > iptablesSizeBefore);

            policyValue = "0";
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(IPTABLES_DUMP_GENERATION_REST_PROPERTY_NAME, policyValue, IPTABLES_DUMP_GENERATION_REST_PROPERTY_PATH, true)});

            iptablesSizeBefore = getDirSize(F);
            logSleeping(6 * 60 * 1000);
            iptablesSizeAfter = getDirSize(F);
            assertTrue("Iptables dump should not be generated, package size should be " + iptablesSizeBefore + " but was " + iptablesSizeAfter , iptablesSizeAfter  <= iptablesSizeBefore);
        } finally {
            PMSUtil.cleanPaths(new String[]{IPTABLES_DUMP_GENERATION_REST_PROPERTY_PATH});
        }
    }

    /**
     * <p> Test steps:</p>
     * <ol>
     * <li> Send such policies:
     asimov@debug_data@upload_interval=3
     asimov@debug_data@enabled=1
     asimov@tcpdump@enabled=1 </li>
     * <li> Wait for 4 minutes.</li>
     * <li> Send policy asimov@tcpdump@enabled=0  </li>
     * </ol>
     * <p> Expected result:</p>
     * <ol>
     * <li> Policies should be received.</li>
     * <li> Time trigger should become active and should try to upload logs after receiving asimov@tcpdump@enabled=0.</li>
     * </ol>
     */
    public void test_003_debugDataUploadInterval () throws Exception {

        String debugDataEnabled = "1";
        String uploadInterval = "3";
        String tcpDumpEnabled = "1";
        DebugDataTimeTriggerTask debugDataTimeTriggerTask = new DebugDataTimeTriggerTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), debugDataTimeTriggerTask);

        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(DEBUG_DATA_ENABLE_REST_PROPERTY_NAME, debugDataEnabled, DEBUG_DATA_REST_PROPERTY_PATH, true),
                    new Policy(DEBUG_DATA_UPLOAD_INTERVAL_REST_PROPERTY_NAME, uploadInterval, DEBUG_DATA_REST_PROPERTY_PATH, true),
                    new Policy(TCP_DUMP_GENERATION_REST_PROPERTY_NAME, tcpDumpEnabled, TCP_DUMP_GENERATION_REST_PROPERTY_PATH, true)});
            //stop tcpDump to awake Data Debug Collector
            logcatUtil.start();
            logSleeping(3 * 60 * 1000);
            tcpDumpEnabled = "0";
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TCP_DUMP_GENERATION_REST_PROPERTY_NAME, tcpDumpEnabled, TCP_DUMP_GENERATION_REST_PROPERTY_PATH, true)});
            logSleeping(30 * 1000);
            logcatUtil.stop();

            assertTrue("Time trigger should be active, but was not", !debugDataTimeTriggerTask.getLogEntries().isEmpty());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            PMSUtil.cleanPaths(new String[]{DEBUG_DATA_REST_PROPERTY_PATH, TCP_DUMP_GENERATION_REST_PROPERTY_PATH});
            logcatUtil.stop();
        }
    }

    /**
     * <p> Test steps:</p>
     * <ol>
     * <li> Send such policies:
     asimov@debug_data@upload_interval=3
     asimov@debug_data@enabled=0
     asimov@tcpdump@enabled=1  </li>
     * <li> Wait for 4 minutes. </li>
     * <li> Send policy asimov@tcpdump@enabled=0  </li>
     * </ol>
     * <p> Expected result:</p>
     * <ol>
     * <li> Policies should be received.</li>
     * <li> Time trigger should not become active after receiving asimov@tcpdump@enabled=0.</li>
     * </ol>
     */
    public void test_004_debugDataDisabled () throws Exception {

        String debugDataEnabled = "0";
        String uploadInterval = "3";
        String tcpDumpEnabled = "1";
        DebugDataTimeTriggerTask debugDataTimeTriggerTask = new DebugDataTimeTriggerTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), debugDataTimeTriggerTask);

        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(DEBUG_DATA_ENABLE_REST_PROPERTY_NAME, debugDataEnabled, DEBUG_DATA_REST_PROPERTY_PATH, true),
                    new Policy(DEBUG_DATA_UPLOAD_INTERVAL_REST_PROPERTY_NAME, uploadInterval, DEBUG_DATA_REST_PROPERTY_PATH, true),
                    new Policy(TCP_DUMP_GENERATION_REST_PROPERTY_NAME, tcpDumpEnabled, TCP_DUMP_GENERATION_REST_PROPERTY_PATH, true)});

            //stop tcpDump to awake Data Debug Collector
            logcatUtil.start();
            logSleeping(3 * 60 * 1000);
            tcpDumpEnabled = "0";
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TCP_DUMP_GENERATION_REST_PROPERTY_NAME, tcpDumpEnabled, TCP_DUMP_GENERATION_REST_PROPERTY_PATH, true)});
            logSleeping(30 * 1000);
            logcatUtil.stop();

            assertTrue("Time trigger should not be active, but was not", debugDataTimeTriggerTask.getLogEntries().isEmpty());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            PMSUtil.cleanPaths(new String[]{DEBUG_DATA_REST_PROPERTY_PATH, TCP_DUMP_GENERATION_REST_PROPERTY_PATH});
            logcatUtil.stop();
        }
    }

    /**
     * <p> Test steps:</p>
     * <ol>
     * <li> Send such policies:
     asimov@debug_data@log_size=50
     asimov@debug_data@enabled=1
     asimov@tcpdump@enabled=1 </li>
     * <li> Send policy asimov@tcpdump@enabled=0  </li>
     * </ol>
     * <p> Expected result:</p>
     * <ol>
     * <li> Policies should be received.</li>
     * <li> Size trigger should become active after receiving asimov@tcpdump@enabled=0 and should try to upload logs.</li>
     * </ol>
     */
    public void test_005_debugDataLogSize () throws Exception {
        final String RESOURCE_URI = "asimov_it_cv_test_005_debugDataLogSize";
        int size = 524030;
        List<HttpRequest> requests = new ArrayList<HttpRequest>(5);
        String[] bodies = new String[]{"a", "b", "c", "d", "e"};

        for (int i = 0; i < 5; i++){
            requests.add(createRequest().setUri(createTestResourceUri(RESOURCE_URI + i))
                    .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .addHeaderField("X-OC-ResponseContentSize", size + "," + bodies[i]).getRequest());
        }

        String debugDataLogSize = "50";
        String tcpDumpEnabled = "1";
        DebugDataSizeTriggerTask debugDataSizeTriggerTask = new DebugDataSizeTriggerTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), debugDataSizeTriggerTask);

        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(DEBUG_DATA_LOG_SIZE_REST_PROPERTY_NAME, debugDataLogSize, DEBUG_DATA_REST_PROPERTY_PATH, true),
                    new Policy(TCP_DUMP_GENERATION_REST_PROPERTY_NAME, tcpDumpEnabled, TCP_DUMP_GENERATION_REST_PROPERTY_PATH, true)});
            //stop tcpDump to awake Data Debug Collector
            tcpDumpEnabled = "0";
            logcatUtil.start();
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(TCP_DUMP_GENERATION_REST_PROPERTY_NAME, tcpDumpEnabled, TCP_DUMP_GENERATION_REST_PROPERTY_PATH, true)});
            logcatUtil.stop();

            assertTrue("Size trigger should be active, but was not ",!debugDataSizeTriggerTask.getLogEntries().isEmpty());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            debugDataSizeTriggerTask.reset();
            PMSUtil.cleanPaths(new String[]{DEBUG_DATA_REST_PROPERTY_PATH, TCP_DUMP_GENERATION_REST_PROPERTY_PATH});
            logcatUtil.stop();
        }
    }
}
