package com.seven.asimov.it.tests.dispatchers.interception;

import android.util.Log;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks.DispatcherCheckTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAppliedTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import java.util.Map;

import static com.seven.asimov.it.utils.pms.PMSUtil.addPolicies;
import static com.seven.asimov.it.utils.pms.PMSUtil.cleanPaths;

public class CheckInterceptionTests extends TcpDumpTestCase {

    private static final String TAG = CheckInterceptionTests.class.getSimpleName();
    private static final String OCDNSD = "@asimov@interception@ocdnsd";
    private static final String OCHTTPD = "@asimov@interception@ochttpd";
    private static final String OCSHTTPD = "@asimov@interception@ocshttpd";
    private static final String OCTCPD = "@asimov@interception@octcpd";
    private static final String PROPERTY = "enabled";

    /**
     * The test checks that dispatcher is shutdown when policy "enabled" for current dispatcher is "false"
     *
     * 1. Check if the dns dispatcher is running -  should be true
     * 2. Policy should be received and applied.
     * 3. Check if dispatcher is running - should be false
     * @throws Throwable
     */
    public void test_001_ocdnsd() throws Throwable {
        try {
            assertTrue("DNS dispatcher shude be running", checkDispacher("ocdnsd"));
            PolicyAppliedTask policyAppliedTask = new PolicyAppliedTask(PROPERTY, "false");
            LogcatUtil logcat = new LogcatUtil(getContext(), policyAppliedTask);
            logcat.start();
            Log.d(TAG, "before policy");
            final Policy dnsDisable = new Policy(PROPERTY, "false", OCDNSD, true);
            addPolicies(new Policy[]{dnsDisable});
            Log.d(TAG, "after policy update");
            logSleeping(45 * 1000);
            logcat.stop();
            assertFalse("policy wasn't resived", policyAppliedTask.getLogEntries().isEmpty());
            assertFalse("DNS dispatcher shude not be running", checkDispacher("ocdnsd"));
        } finally {
            cleanPaths(new String[]{OCDNSD});
            logSleeping(45 * 1000);
        }
    }

    /**
     * The test checks that dispatcher is shutdown when policy "enabled" for current dispatcher is "false" and checks the traffic interception by tcp dispatcher
     *
     * 1. Check if http dispatcher is running - should be true
     * 2. Send http request
     * 3. Policy should be received and applied.
     * 4. Check if http dispatcher is running - should be false
     * 5. Send http request and check the it will be intercept by tcp dispatcher
     *
     * @throws Throwable
     */
    public void test_002_ochttpd() throws Throwable {
        final String uri = createTestResourceUri("test_ochttpd_uri");
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        PolicyAppliedTask policyAppliedTask = new PolicyAppliedTask(PROPERTY, "false");
        DispatcherCheckTask dispatcherCheckTask = new DispatcherCheckTask();
        LogcatUtil logcat;
        try {
            assertTrue("HTTP dispatcher should be running", checkDispacher("ochttpd"));
            logcat = new LogcatUtil(getContext(), policyAppliedTask);
            logcat.start();
            sendRequest(request);
            Log.d(TAG, "before policy");
            final Policy httpdDisable = new Policy(PROPERTY, "false", OCHTTPD, true);
            addPolicies(new Policy[]{httpdDisable});
            Log.d(TAG, "after policy update");
            logSleeping(45 * 1000);
            logcat.stop();
            assertFalse("policy wasn't resived", policyAppliedTask.getLogEntries().isEmpty());
            assertFalse("HTTP dispatcher shouldn't be running", checkDispacher("ochttpd"));

            logcat = new LogcatUtil(getContext(), dispatcherCheckTask);
            logcat.start();
            logSleeping(10 * 1000);
            sendRequest(request);
            logcat.stop();

            assertFalse("log entries are empty", dispatcherCheckTask.getLogEntries().isEmpty());
            Log.d(TAG, "entries size = " + String.valueOf(dispatcherCheckTask.getLogEntries().size()));
            assertTrue("wrong dispatcher", dispatcherCheckTask.getLogEntries().get(0).getDispatcherName().equals("octcpd"));


        } finally {
            cleanPaths(new String[]{OCHTTPD});
            logSleeping(45 * 1000);
        }
    }

    /**
     * The test checks that dispatcher is shutdown when policy "enabled" for current dispatcher is "false" and checks the traffic interception by tcp dispatcher
     * 1. Check if https dispatcher is running - should be true
     * 2. Send https request
     * 3. Policy should be received and applied.
     * 4. Check if https dispatcher is running - should be false
     * 5. Send https request and check the it will be intercept by tcp dispatcher
     * @throws Throwable
     */
    public void test_003_ocshttpd() throws Throwable {
        final String uri = createTestResourceUri("test_ocshttpd_uri", true);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        PolicyAppliedTask policyAppliedTask = new PolicyAppliedTask(PROPERTY, "false");
        DispatcherCheckTask dispatcherCheckTask = new DispatcherCheckTask();
        LogcatUtil logcat;
        try {
            assertTrue("HTTPS dispatcher should be running", checkDispacher("ocshttpd"));
            logcat = new LogcatUtil(getContext(), policyAppliedTask);
            logcat.start();
            sendHttpsRequest(request);
            Log.d(TAG, "before policy");
            final Policy shttpdDisable = new Policy(PROPERTY, "false", OCSHTTPD, true);
            addPolicies(new Policy[]{shttpdDisable});
            Log.d(TAG, "after policy update");
            logSleeping(45 * 1000);
            logcat.stop();
            assertFalse("policy wasn't resived", policyAppliedTask.getLogEntries().isEmpty());
            assertFalse("HTTPS dispatcher shouldn't be running", checkDispacher("ocshttpd"));

            logcat = new LogcatUtil(getContext(), dispatcherCheckTask);
            logcat.start();
            logSleeping(10 * 1000);
            sendHttpsRequest(request);
            logcat.stop();
            assertFalse("log entries are empty", dispatcherCheckTask.getLogEntries().isEmpty());
            Log.d(TAG, "entries size = " + String.valueOf(dispatcherCheckTask.getLogEntries().size()));
            assertTrue("wrong dispatcher", dispatcherCheckTask.getLogEntries().get(0).getDispatcherName().equals("octcpd"));

        } finally {
            cleanPaths(new String[]{OCSHTTPD});
            logSleeping(45 * 1000);
        }
    }


    /**
     * The test checks that dispatcher is shutdown when policy "enabled" for current dispatcher is "false"
     *
     * 1. Check if the dns dispatcher is running -  should be true
     * 2. Policy should be received and applied.
     * 3. Check if dispatcher is running - should be false
     * @throws Throwable
     */
    public void test_004_octcpd() throws Throwable {
        try {
            assertTrue("TCP dispatcher should be running", checkDispacher("octcpd"));
            PolicyAppliedTask policyAppliedTask = new PolicyAppliedTask(PROPERTY, "false");
            LogcatUtil logcat = new LogcatUtil(getContext(), policyAppliedTask);
            logcat.start();
            Log.d(TAG, "before policy");
            final Policy tcpdDisable = new Policy(PROPERTY, "false", OCTCPD, true);
            PMSUtil.addPolicies(new Policy[]{tcpdDisable});
            Log.d(TAG, "after policy update");
            logSleeping(45 * 1000);
            logcat.stop();
            assertFalse("policy wasn't resived", policyAppliedTask.getLogEntries().isEmpty());
            assertFalse("TCP dispatcher should not be running", checkDispacher("octcpd"));
        } finally {
            cleanPaths(new String[]{OCTCPD});
            logSleeping(45 * 1000);
        }
    }

    private boolean checkDispacher(String name) {
        Map<String, Integer> processes = OCUtil.getOcProcesses(true);
        Log.d(TAG, "Name " + name + " id " + String.valueOf(processes.get(name) == null ? "--" : processes.get(name)));
        Log.d(TAG, "isContain " + processes.containsKey(name));
        return processes.containsKey(name);

    }
}
