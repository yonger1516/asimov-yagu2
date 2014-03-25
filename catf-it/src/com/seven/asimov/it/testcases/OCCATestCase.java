package com.seven.asimov.it.testcases;

import android.util.Log;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.CCRTask;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.FCLHttpsTask;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.FclPostponed;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.VerdictForFCLTask;
import com.seven.asimov.it.utils.logcat.tasks.streamTasks.StreamTask;
import com.seven.asimov.it.utils.logcat.wrappers.StreamWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.VerdictForFCLWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class OCCATestCase extends TcpDumpTestCase {
    private static final String TAG = OCCATestCase.class.getSimpleName();
    protected static final String OC_APK_FILENAME = "/sdcard/asimov-signed.apk";
    protected static final String OC_APK_FILENAME_NEW = "/sdcard/asimov-signed-newer.apk";

    protected boolean presentNSQ = false;
    protected boolean presentNSR = false;
    protected boolean presentNAQ = false;
    protected boolean presentNAR = false;

    public void installOC() throws Exception {
        String[] uninstallOc = {"su", "-c", "pm uninstall com.seven.asimov"};
        String[] installOc = {"su", "-c", "pm install -r " + OC_APK_FILENAME};
        String[] killAllOcc = {"su", "-c", "killall -9 occ"};
        String[] startService = new String[]{"su", "-c", "am startservice com.seven.asimov/.ocengine.OCEngineService"};
        Runtime.getRuntime().exec(uninstallOc).waitFor();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        File file = new File(OC_APK_FILENAME);
        if (!file.exists()) {
            throw new FileNotFoundException("OC apk doesn't exist ob SD card or name should be asimov-signed.apk");
        }
        Log.d(TAG, "Installing OC Client");
        Runtime.getRuntime().exec(installOc).waitFor();
        Thread.sleep(20 * 1000);
        Runtime.getRuntime().exec(killAllOcc).waitFor();
        Thread.sleep(20 * 1000);
        Log.d(TAG, "Sending intent for start OC Engine");
        Runtime.getRuntime().exec(startService).waitFor();
        Thread.sleep(1 * 60 * 1000);
    }

    public void updateOC() throws Exception {
        String[] installOc = {"su", "-c", "pm install -r asimov-signed-newer" + OC_APK_FILENAME_NEW};
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        File file = new File(OC_APK_FILENAME_NEW);
        if (!file.exists()) {
            throw new FileNotFoundException("OC apk doesn't exist ob SD card or name should be asimov-signed-newer.apk");
        }
        Log.d(TAG, "Updating OC Client");
        Runtime.getRuntime().exec(installOc).waitFor();
        Thread.sleep(20 * 1000);
    }

    protected void checkStreamMode(StreamTask streamTask) {
        for (StreamWrapper entry : streamTask.getLogEntries()) {
            if (entry.getTask().equals("NAQ")) {
                presentNAQ = true;
            }
            if (entry.getTask().equals("NAR")) {
                presentNAR = true;
            }
            if (entry.getTask().equals("NSQ")) {
                presentNSQ = true;
            }
            if (entry.getTask().equals("NSR")) {
                presentNSR = true;
            }
        }
        assertTrue("Dispatcher should send NAQ to controller", presentNAQ);
        assertTrue("Dispatcher should received NAR from controller", presentNAR);
        assertTrue("Dispatcher should send NSQ to controller", presentNSQ);
        assertTrue("Dispatcher should received NSR from controller", presentNSR);
    }

    protected void checkVerdictForFCL(VerdictForFCLTask verdictForFCLTask) {
        if (verdictForFCLTask.getFCP()) {
            for (VerdictForFCLWrapper entry : verdictForFCLTask.getLogEntries()) {
                assertTrue("FCL task should receive verdict FCP, but did not", entry.getVerdict().equals("FCP"));
            }
        } else {
            for (VerdictForFCLWrapper entry : verdictForFCLTask.getLogEntries()) {
                assertTrue("FCL task should receive verdict FCN, but did not", entry.getVerdict().equals("FCN"));
            }
        }
    }

    protected void resetValues() {
        presentNSQ = false;
        presentNSR = false;
        presentNAQ = false;
        presentNAR = false;
    }

    protected Thread rebootOpenChannel() throws Exception {
        return new Thread() {
            @Override
            public void run() {
                Integer pid1;
                Integer pid2;
                Map<String, Integer> processes = OCUtil.getRunningProcesses(true);
                if (processes.get("occ") != null || processes.get("com.seven.asimov") != null) {
                    pid1 = processes.get("occ");
                    Log.i(TAG, "Process occ = " + pid1);
                    String[] killPid1 = {"su", "-c", "kill " + pid1};
                    pid2 = processes.get("com.seven.asimov");
                    //Log.i(TAG, "Process com.seven.asimov = " + pid2);
                    //String[] killPid2 = {"su", "-c", "kill " + pid2};

                    try {
                        Runtime.getRuntime().exec(killPid1);
                        //Runtime.getRuntime().exec(killPid2);
                    } catch (IOException io) {
                        Log.e(TAG, "Killing process is failed due to : " + ExceptionUtils.getStackTrace(io));
                    }
                }
            }
        };
    }

    protected void resetDB() throws Exception {
        List<String> command = new ArrayList<String>();
        command.add("rm /data/misc/openchannel/oc_engine.db");
        ShellUtil.execWithCompleteResult(command, true);
    }

    protected void checkPostponing(String uri) throws Exception {
        FclPostponed fclPostponed = new FclPostponed();
        FCLHttpsTask fclHttpsTask = new FCLHttpsTask(false);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), fclPostponed, fclHttpsTask);
        String policyName = "enabled";
        String policyPath = "@asimov@application@com.seven.asimov.it@ssl";
        Policy[] policiesToAdd = new Policy[]{new Policy(policyName, "1", policyPath, true),
                new Policy("enabled", "0", "@asimov@failovers@restart@engine@enabled", true),
                new Policy("enabled", "0", "@asimov@failovers@restart@controller@enabled", true),
                new Policy("enabled", "0", "@asimov@failovers@restart@dispatchers@enabled", true)};
        HttpRequest httpRequest = createRequest().setUri(uri).getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(policiesToAdd);
            logcatUtil.start();
            Thread requestThread1 = new Thread(new RequestThread(httpRequest));
            Thread requestThread2 = new Thread(new RequestThread(httpRequest));
            requestThread1.start();
            requestThread2.start();
            requestThread1.join();
            requestThread2.join();
            logcatUtil.stop();

            assertTrue("It should be detected 2 or more constructed FCL tasks", fclHttpsTask.getLogEntries().size() >= 2);
            String firstCSM = fclHttpsTask.getLogEntries().get(0).getCSM();
            String secondCSM = fclHttpsTask.getLogEntries().get(1).getCSM();
            Log.i(TAG, "First csm = " + firstCSM + "Second csm = " + secondCSM);

            assertTrue("Postponed task should not be empty", fclPostponed.getLogEntries().size() != 0);
            String csm = fclPostponed.getLogEntries().get(0).getForCsm();
            String postponedCsm = fclPostponed.getLogEntries().get(0).getCsm();
            Log.i(TAG, "Postponed csm = " + postponedCsm + "First csm = " + csm);

            assertTrue("Second FCL should be postponed for first FCL task", (firstCSM.equals(csm) && secondCSM.equals(postponedCsm)));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
            PMSUtil.cleanPaths(new String[]{policyPath, "@asimov@failovers@restart"});
        }
    }

    protected void checkFCLVerdict(String uri, int verdict) throws Exception {
        VerdictForFCLTask verdictForFCLTask = new VerdictForFCLTask(false);
        CCRTask ccrTask = new CCRTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), verdictForFCLTask, ccrTask);
        String policyName = "enabled";
        String policyPath = "@asimov@application@com.seven.asimov.it@ssl";
        Policy[] policiesToAdd = new Policy[]{new Policy(policyName, "1", policyPath, true),
                new Policy("enabled", "0", "@asimov@failovers@restart@engine@enabled", true),
                new Policy("enabled", "0", "@asimov@failovers@restart@controller@enabled", true),
                new Policy("enabled", "0", "@asimov@failovers@restart@dispatchers@enabled", true)};
        HttpRequest httpRequest = createRequest().setUri(uri).getRequest();

        try {
            PMSUtil.addPoliciesWithCheck(policiesToAdd);
            logcatUtil.start();
            sendHttpsRequest(httpRequest);
            checkVerdictForFCL(verdictForFCLTask);
            verdictForFCLTask.reset();
            logcatUtil.stop();

            verdictForFCLTask = new VerdictForFCLTask(true);
            logcatUtil = new LogcatUtil(getContext(), verdictForFCLTask, ccrTask);
            logcatUtil.start();
            for (int i = 0; i < 2; i++) {
                sendHttpsRequest(httpRequest);
                checkVerdictForFCL(verdictForFCLTask);
                verdictForFCLTask.reset();
            }
            logcatUtil.stop();
            int ccrAmount = ccrTask.getLogEntries().size();
            assertTrue("CCV task was not constructed or did not give verdict", ccrAmount != 0);
            for (int i = 0; i < ccrAmount; i++) {
                assertTrue("CCR verdict should be: " + verdict, verdict == 0 ? ccrTask.getLogEntries().get(i).getCodeVerdict() == 0 : ccrTask.getLogEntries().get(i).getCodeVerdict() == 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
            PMSUtil.cleanPaths(new String[]{policyPath, "@asimov@failovers@restart"});
        }

    }

    protected void checkExpiration(String uri) throws Exception {
        final int csa = IpTablesUtil.getApplicationUid(getContext(), "com.seven.asimov");
        final int csat = IpTablesUtil.getApplicationUid(getContext(), "com.seven.asimov.it");
        FCLHttpsTask fclHttpsTask = new FCLHttpsTask(false);
        FCLHttpsTask fcGeneration = new FCLHttpsTask(true);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), fclHttpsTask, fcGeneration);
        String policyName = "enabled";
        String policyPath = "@asimov@application@com.seven.asimov.it@ssl";
        Policy[] policiesToAdd = new Policy[]{new Policy(policyName, "1", policyPath, true),
                new Policy("enabled", "0", "@asimov@failovers@restart@engine@enabled", true),
                new Policy("enabled", "0", "@asimov@failovers@restart@controller@enabled", true),
                new Policy("enabled", "0", "@asimov@failovers@restart@dispatchers@enabled", true)};
        HttpRequest httpRequest = createRequest().setUri(uri).getRequest();
        Calendar c = GregorianCalendar.getInstance();
        c.set(2025, 10, 30);
        try {
            PMSUtil.addPoliciesWithCheck(policiesToAdd);
            sendHttpsRequest(httpRequest);
            IpTablesUtil.banNetworkForAllApplications(true);
            IpTablesUtil.allowNetworkForApplication(true, csa);
            IpTablesUtil.allowNetworkForApplication(true, csat);
            Thread.sleep(5 * 1000);
            DateUtil.setTimeOnDevice(c.getTimeInMillis());
            logcatUtil.start();
            sendHttpsRequest(httpRequest);
            logcatUtil.stop();
            assertTrue("FCL task should be generated", fclHttpsTask.getLogEntries().size() != 0);
            assertTrue("New FC should be generated", fcGeneration.getLogEntries().size() != 0);
            String csm = fclHttpsTask.getLogEntries().get(0).getCSM();
            String generatedFCforCsm = fcGeneration.getLogEntries().get(0).getCSM();
            assertTrue("FC should be generated for csm " + csm + "but was for " + generatedFCforCsm, csm.equals(generatedFCforCsm));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IpTablesUtil.banNetworkForAllApplications(false);
            IpTablesUtil.allowNetworkForApplication(false, csa);
            IpTablesUtil.allowNetworkForApplication(false, csat);
            if (logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
            PMSUtil.cleanPaths(new String[]{policyPath, "@asimov@failovers@restart"});
        }
    }

    protected void checkStreamMode(Policy[] policiesToAdd) throws Exception {
        StreamTask streamTask = new StreamTask();
        VerdictForFCLTask verdictForFCLTask = new VerdictForFCLTask(false);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), streamTask, verdictForFCLTask);
        String policyPath = "@asimov@application@com.seven.asimov.it@ssl";
        String policyPNPath = "@asimov@pn";
        HttpRequest httpRequest = createRequest().setUri(createTestResourceUri("test_001_OCCA", true)).getRequest();
        try {
            PMSUtil.addPoliciesWithCheck(policiesToAdd);
            logcatUtil.start();
            sendHttpsRequest(httpRequest);
            logcatUtil.stop();
            checkVerdictForFCL(verdictForFCLTask);
            checkStreamMode(streamTask);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
            PMSUtil.cleanPaths(new String[]{policyPath, policyPNPath});
            streamTask.reset();
            verdictForFCLTask.reset();
            resetValues();
        }
    }

    class RequestThread implements Runnable {
        HttpRequest httpRequest;

        public RequestThread(HttpRequest httpRequest) {
            this.httpRequest = httpRequest;
        }

        @Override
        public void run() {
            try {
                Log.i(TAG, "Sending request in thread");
                sendHttpsRequest(httpRequest);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
