package com.seven.asimov.test.tool.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.test.AssertionFailedError;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.sysmonitor.SystemMonitorUtil;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.activity.AutomationTestsTab;
import com.seven.asimov.test.tool.utils.logcat.wrapper.TestCaseEvents;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Z7TestUtil {

    private final String TAG = Z7TestUtil.class.getSimpleName();
    private final Info info = new Info();
    private TestCaseEvents testCaseEvents = new TestCaseEvents();

    public static final String RESULTS_DIR = "/sdcard/OCIntegrationTestsResults/";

    protected static String FILES_DIR;
    private static String TCPDUMP_PATH;
    private static int restart = 0;

    protected AtomicBoolean isInterruptTests = new AtomicBoolean(false);

    private AutomationTestsTab activity;
    private Map<String, String> results;

    private static String additionalInfo = "";
    private static TestCaseEvents.TESTCASE_EVENT result = null;
    public static final String TOTAL_RESULT = "/sdcard/7TestToolTestResults/";
    public static final String[] SUITES = {"EnvironmentCertificationSuite/", "SmokeTestSuite/", "SunityTestSuite/", "DeviceCertificationSuite/"};
    public static String pcapPath = "";

    public static void setAdditionalInfo(String addInfo, TestCaseEvents.TESTCASE_EVENT typeOfExc) {
        additionalInfo = addInfo;
        result = typeOfExc;
    }

    public Z7TestUtil(AutomationTestsTab activity, Map<String, String> results) {
        this.activity = activity;
        this.results = results;
    }

    protected void startTcpdump(int suiteCounter) {

        FILES_DIR = activity.getFilesDir().getAbsolutePath();
        TCPDUMP_PATH = FILES_DIR + File.separator + Z7ShellUtil.PCAP_BASE_FILENAME;
        Z7ShellUtil.killAll(TCPDUMP_PATH, 9);
        Z7ShellUtil.killAll(FILES_DIR + File.separator + "tcpdump-new", 9);

        String SUITE_DIR = TOTAL_RESULT + SUITES[suiteCounter];

        pcapPath = SUITE_DIR;

        File mainDir = new File(SUITE_DIR);
        String[] rmOldTestResults = {"su", "-c", "rm -r " + SUITE_DIR + "*"};
        String[] mkPcaps = {"su", "-c", "mkdir " + SUITE_DIR + "pcaps/"};
        String[] startFullTcpdump = {"su", "-c", FILES_DIR + File.separator + "tcpdump-new" + " -i any -s 0 -Uw " + SUITE_DIR + "tcpdump_log.trace not tcp port 5555 &"};
        Log.e(TAG, Arrays.asList(startFullTcpdump).toString());
        try {
            if (mainDir.exists()) {
                Runtime.getRuntime().exec(rmOldTestResults).waitFor();
            }
            Runtime.getRuntime().exec(mkPcaps).waitFor();
            Runtime.getRuntime().exec(startFullTcpdump).waitFor();
//            Runtime
            TestUtil.sleep(10 * 1000);
        } catch (IOException e1) {
            //Log.e(TAG, LOG, e1.getMessage());
            Log.e(TAG, "Error with starting tcpdump. Exception : " + (e1 != null ? e1.getMessage() : "null"));
        } catch (InterruptedException e2) {
            Log.e(TAG, "Error with starting tcpdump. Exception : " + (e2 != null ? e2.getMessage() : "null"));
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException trough sleeping after tcpdump starting. Exception : " + (e != null ? e.getMessage() : "null"));
        }
    }

    protected void runTests(List<Method> methods) throws IOException, PackageManager.NameNotFoundException, InterruptedException {
        boolean wait = false;
        final Spinner spinner = (Spinner) activity.findViewById(R.id.spinner);
        int spinnerId = spinner.getSelectedItemPosition();
        SystemMonitorUtil systemMonitorUtil = null;
        if (spinnerId == 3) {
            systemMonitorUtil = SystemMonitorUtil.getInstance(activity.getApplicationContext(),
                    AutomationTestsTab.b.getAndroidPackageName(),
                    spinner.getSelectedItem().toString().replaceAll(" +", ""), Z7TestUtil.TOTAL_RESULT +
                    Z7TestUtil.SUITES[spinner.getSelectedItemPosition()]);
            systemMonitorUtil.start();
        }
        Log.e(TAG, "" + methods.size());
        TcpDumpUtil tcpDump = TcpDumpUtil.getInstance(activity.getApplicationContext());
        tcpDump.setTcpDumpTempPath(Z7TestUtil.pcapPath);
        try {
            for (int i = 0; i < methods.size(); i++) {
                Method method = methods.get(i);
                if (isInterruptTests.get()) {
                    //System.out.println("Tests stopped by user.");
                    Log.i(TAG, "Tests stopped by user.");
                    break;
                }
                Log.i(TAG, "Test started : " + method.getName());
                TestCaseEvents.TestStatus testStatus = new TestCaseEvents.TestStatus();
                testStatus.setName(method.getName());
                testStatus.setStartTime(System.currentTimeMillis());
                results.put(method.getName(), "TEST INFO: \n" +
                        "Name : " + method.getName() + "\n" +
                        "Status : " + "RUNNING" + "\n" +
                        "Started  : " + getTime(testStatus.getStartTime()) + "\n");

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.refreshPicturesOnTextViewEntries();
                    }
                });

                try {
                    tcpDump.start();
                    if (spinnerId == 3) {
                        systemMonitorUtil.startedNewTest(method.getName());
                    }
                    method.invoke(method.getDeclaringClass().newInstance(), null);
                    testStatus.setEndTime(System.currentTimeMillis());

                    tcpDump.stop();
                    //executeChecks();
                    testStatus.setTestCaseStatus(TestCaseEvents.TESTCASE_EVENT.PASSED);
                    if (!additionalInfo.equals("")) {
                        testStatus.setAdditionalInfo(true);
                        testStatus.setTestCaseStatus(result);
                    }
                    Log.i(TAG, "Test passed : " + method.getName());
                } catch (IOException e) {
                    Log.e(TAG, "Was detected IOException from TcpDump stop method in the " + method.getName());
                } catch (InstantiationException e1) {
                    Log.e(TAG, "InstantiationException for test : " + method.getName());
                } catch (IllegalAccessException e2) {
                    Log.e(TAG, "IllegalAccessException for test : " + method.getName());
                } catch (InvocationTargetException e3) {
                    if (e3.getTargetException() != null) {
                        Log.e(TAG, getErr(e3.getTargetException()));
                        if (e3.getTargetException().getMessage() != null) {
                            String message = returnErrorMessage(e3.getTargetException().getMessage());
//                        testStatus.setError(e3.getTargetException().getMessage());
                            Log.i(TAG, e3.getTargetException().getMessage());
                            Log.i(TAG, message);
                            testStatus.setError(message);
                        } else {
                            String[] messages = e3.getTargetException().getClass().toString().split("\\s");
                            if (messages.length == 2) {
                                testStatus.setError(messages[1]);
                            } else {
                                testStatus.setError("Unknown exception!");
                                Log.e(TAG, "Unknown exception!");
                            }
                        }
                    }
                    if (testStatus.getError().contains("Unable to resolve host") ||
                            testStatus.getError().contains("was with status code -1") ||
                            testStatus.getError().contains("The destination testrunner is unreacheable") ||
                            testStatus.getError().contains("The connection to testrunner was disrupted")) {
                        wait = true;
                    }
                    testStatus.setTestCaseStatus(TestCaseEvents.TESTCASE_EVENT.FAILED);
                    testStatus.setEndTime(System.currentTimeMillis());
                } catch (Throwable e) {
                    if (e != null) {
                        Log.e(TAG, getErr(e));
                        if (e.getMessage() != null) {
                            testStatus.setError(e.getMessage());
                        } else {
                            String[] messages = e.getClass().toString().split("\\s");
                            if (messages.length == 2) {
                                testStatus.setError(messages[1]);
                            } else {
                                testStatus.setError("Unknown exception!");
                                Log.e(TAG, "Unknown exception!");
                            }
                        }
                    }

                    testStatus.setTestCaseStatus(TestCaseEvents.TESTCASE_EVENT.FAILED);
                    testStatus.setEndTime(System.currentTimeMillis());
                }

                if (spinnerId == 3) {
                    systemMonitorUtil.endedTest();
                }

                testCaseEvents.addNewTestCase(method.getName(), testStatus);

                results.put(method.getName(), returnResultInfo(testStatus));

                additionalInfo = "";

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.refreshPicturesOnTextViewEntries();
                    }
                });

                Log.i(TAG, results.get(method.getName()));
                Log.i(TAG, "Test finished : " + method.getName());
                //checks.clear();
                if (wait) {
                    info.setTestName(testStatus.getName());
                    info.setTestErr(testStatus.getError());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder ad = new AlertDialog.Builder(AutomationTestsTab.context);
                            ad.setTitle("ERROR")
                                    .setMessage("Were found connection problems during the run " + info.getTestName() +
                                            ":\n" + info.getTestErr())
                                    .setPositiveButton("Restart",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    synchronized (AutomationTestsTab.locker) {
                                                        restart = 1;
                                                        AutomationTestsTab.locker.notifyAll();
                                                    }
                                                }
                                            })
                                    .setNeutralButton("Continue", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            synchronized (AutomationTestsTab.locker) {
                                                restart = 0;
                                                AutomationTestsTab.locker.notifyAll();
                                            }
                                        }
                                    })
                                    .setNegativeButton("Stop", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            isInterruptTests = new AtomicBoolean(true);
                                            dialog.dismiss();
                                            synchronized (AutomationTestsTab.locker) {
                                                AutomationTestsTab.locker.notifyAll();
                                            }
                                        }
                                    }).show();
                        }
                    });
                    synchronized (AutomationTestsTab.locker) {
                        try {
                            AutomationTestsTab.locker.wait();
                            i -= restart;
                            wait = false;
                        } catch (InterruptedException e) {
                            Log.e(TAG, ExceptionUtils.getStackTrace(e));
                            throw new AssertionFailedError(e.getMessage());
                        }
                    }
                }
            }
        } finally {
            if (systemMonitorUtil != null) systemMonitorUtil.stop();
        }
    }

    protected void createScreenshot() throws IOException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        String temp = "screenshot_" + date + ".jpg";
        String name = temp.replace(" ", "_");
        name = name.replaceAll(":", "-");
        System.out.println(name);
        String mPath = pcapPath + name;

        Bitmap bitmap;
        View v1 = activity.findViewById(android.R.id.content).getRootView();
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        OutputStream fout = null;
        File imageFile = new File(mPath);

        try {
            fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                fout.close();
            }
        }

    }

    protected String getTime(long timestamp) {
        DateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS");
        java.util.Date date = new Date(timestamp);
        return sdf.format(date);
    }

    public String getErr(Throwable e) {
        final StringBuilder result = new StringBuilder();

        PrintStream st = new PrintStream(new OutputStream() {
            @Override
            public void write(int i) throws IOException {
                //p(i);
                result.append(new String(new byte[]{(byte) i}));
            }
        });

        System.setErr(st);
        e.printStackTrace();
        System.setErr(System.err);
        return result.toString();
    }

    public static String getTCPDUMP_PATH() {
        return TCPDUMP_PATH;
    }

    public String returnResultInfo(TestCaseEvents.TestStatus testStatus) {
        String result;
        if (testStatus.getError() == null && !testStatus.getAdditionalInfo()) {
            result = "TEST INFO: \n" +
                    "Name : " + testStatus.getName() + "\n" +
                    "Status : " + testStatus.getTestCaseStatus().toString() + "\n" +
                    "Started  : " + getTime(testStatus.getStartTime()) + "\n" +
                    "Finished :" + getTime(testStatus.getEndTime());
        } else if (testStatus.getError() == null && testStatus.getAdditionalInfo()) {
            result = "TEST INFO: \n" +
                    "Name : " + testStatus.getName() + "\n" +
                    "Status : " + testStatus.getTestCaseStatus().toString() + "\n" +
                    "Additional : " + additionalInfo + "\n" +
                    "Started  : " + getTime(testStatus.getStartTime()) + "\n" +
                    "Finished :" + getTime(testStatus.getEndTime());
        } else {
            result = "TEST INFO: \n" +
                    "Name : " + testStatus.getName() + "\n" +
                    "Status : " + testStatus.getTestCaseStatus().toString() + "\n" +
                    "Started  : " + getTime(testStatus.getStartTime()) + "\n" +
                    "Finished : " + getTime(testStatus.getEndTime()) + "\n" +
                    "Error : " + testStatus.getError();
        }
        return result;
    }

    private String returnErrorMessage(String message) {
        if (message.endsWith("(Connection timed out)")) {
            Log.i(TAG, "The destination host is unreacheable");
            return "We cannot establish connection. The destination testrunner is unreacheable";
        } else if (message.endsWith("(Connection reset by peer)")) {
            Log.i(TAG, "(Connection reset by peer)");
            return "The connection to testrunner was disrupted";
        } else if (message.equals("SSL handshake timed out")) {
            Log.i(TAG, "SSL handshake timed out");
            return "We cannot establish secure connection between application and testrunner. The destination testrunner is unreacheable.";
        } else if (message.equals("SSLSocketFactory is null")) {
            Log.i(TAG, "SSLSocketFactory is null");
            return "We cannot establish secure connection between application and testrunner. The destination testrunner is unreacheable.";
        }
        return message;
    }

    private class Info {
        private String testName;
        private String testErr;

        public String getTestErr() {
            return testErr;
        }

        public void setTestErr(String testErr) {
            this.testErr = testErr;
        }

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }
    }
}
