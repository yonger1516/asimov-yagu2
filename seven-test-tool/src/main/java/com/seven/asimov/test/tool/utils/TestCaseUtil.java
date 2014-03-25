package com.seven.asimov.test.tool.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.test.AssertionFailedError;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.activity.AutomationTestsTab;
import com.seven.asimov.test.tool.tests.device.certification.PerformanceTests;
import com.seven.asimov.test.tool.tests.device.certification.StabilityTests;
import com.seven.asimov.test.tool.tests.environmentcertification.EnvironmentCertificationTests;
import com.seven.asimov.test.tool.tests.sanity.SanityTests;
import com.seven.asimov.test.tool.tests.smoke.SmokeTests;
import com.seven.asimov.test.tool.utils.logcat.wrapper.TestCaseEvents;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestCaseUtil extends Z7TestUtil implements Runnable {

    private final String TAG = TestCaseUtil.class.getSimpleName();

    private static final String TEST = "test_";

    public static String TCPDUMP_PATH;
    public static final String FILENAME_TEST_CASE_EVENTS = RESULTS_DIR + "testCaseEvents.txt";
    private Method ivn;
    protected TcpDumpUtil tcpDump;

    private AutomationTestsTab activity;
    private Map<String, String> results;

    private TestCaseEvents testCaseEvents = new TestCaseEvents();

    private int testSuite;

    public void setTestSuite(int suite) {
        this.testSuite = suite;
    }

    public TestCaseUtil(AutomationTestsTab activity, Map<String, String> results) {
        super(activity, results);
        this.activity = activity;
        this.results = results;
    }

    public void serializableTestCaseEvents() throws IOException {
        FileOutputStream fos = new FileOutputStream(FILENAME_TEST_CASE_EVENTS);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(testCaseEvents);
        oos.flush();
        oos.close();
    }

    public void deSerializableTestCaseEvents() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(FILENAME_TEST_CASE_EVENTS);
        ObjectInputStream oin = new ObjectInputStream(fis);
        testCaseEvents = (TestCaseEvents) oin.readObject();
    }

    public void run() {
        Z7TFProperties.init(activity.getApplicationContext(), AutomationTestsTab.b.getPath());
        startTcpdump(testSuite);

        final SmokeLogcatUtil slu = new SmokeLogcatUtil(activity.getBaseContext());
        slu.start(testSuite);
        List<Class> classes = new ArrayList<Class>();
        switch (testSuite) {
            case 0:
                classes.add(EnvironmentCertificationTests.class);
                break;
            case 1:
                classes.add(SmokeTests.class);
                break;
            case 2:
                classes.add(SanityTests.class);
                break;
            case 3:
                classes.add(StabilityTests.class);
                classes.add(PerformanceTests.class);
                break;
        }

        List<Method> methods = new ArrayList<Method>();
        for (Class clazz : classes) {
            for (Method method : clazz.getDeclaredMethods()) {
                Annotation[] annotations = method.getAnnotations();
                boolean isIgnored = false;
                for (Annotation annotation : annotations) {
                    if (annotation.toString().contains("Ignore")) {
                        String invCheck = "invCheck";
                        if (method.getName().contains(invCheck))
                            ivn = method;
                        isIgnored = true;
                        break;
                    }
                }
                if (!isIgnored && method.getName().contains(TEST)) {
                    if (AutomationTestsTab.checkedTest != null && !AutomationTestsTab.checkedTest.isEmpty()) {
                        for (String aCheckedTest : AutomationTestsTab.checkedTest) {
                            if (method.getName().contains(aCheckedTest)) {
                                methods.add(method);
                                break;
                            }
                        }
                    } else methods.add(method);
                }
            }
        }
        try {
            ivn.invoke(ivn.getDeclaringClass().newInstance(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final boolean check = SmokeHelperUtil.invReady();
        if (check) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final TextView statusInfo = (TextView) activity.findViewById(R.id.tvStatusInfo);
                    statusInfo.setText(R.string.smokeTestsButtonRunning);
                }
            });

            try {
                runTests(methods);
            } catch (IOException e) {
                Log.e(TAG, ExceptionUtils.getStackTrace(e));
            } catch (InterruptedException e) {
                Log.e(TAG, ExceptionUtils.getStackTrace(e));
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, ExceptionUtils.getStackTrace(e));
            }

        }

        ArrayList<String> problem = SmokeHelperUtil.getEnvProblems();
        final StringBuilder sb = new StringBuilder();
        for (String p : problem) {
            sb.append(p).append("\n");
        }
        problem.clear();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!check) {
                    AlertDialog.Builder ad = new AlertDialog.Builder(AutomationTestsTab.context);
                    ad.setTitle("ERROR")
                            .setMessage(sb.toString())
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            synchronized (AutomationTestsTab.locker) {
                                                AutomationTestsTab.locker.notifyAll();
                                            }
                                        }
                                    }
                            ).show();

                }
                activity.setStarted(false);
                activity.refreshPicturesOnTextViewEntries();
                try {
                    slu.stop();
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    throw new AssertionFailedError(e.getMessage());
                }

//                AutomationTestsTab.selectSuiteSetVisible(true);
            }
        });
        if (!check && testSuite == 0) {
            synchronized (AutomationTestsTab.locker) {
                try {
                    AutomationTestsTab.locker.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                    throw new AssertionFailedError(e.getMessage());
                }
            }
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Button button = (Button) activity.findViewById(R.id.button1);
                button.setText(R.string.smokeTestsButtonStart);
                button.setEnabled(true);
                final TextView statusInfo = (TextView) activity.findViewById(R.id.tvStatusInfo);
                statusInfo.setText("Finished");
            }

        });

        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    createScreenshot();
                } catch (IOException e) {
                    ExceptionUtils.getStackTrace(e);
                }
                Toast toast = Toast.makeText(AutomationTestsTab.context, "Screenshot has been created", Toast.LENGTH_LONG);
                toast.show();
                if (testSuite == 0 && check && SmokeHelperUtil.isApkInstalled()) {
                    AlertDialog.Builder ad = new AlertDialog.Builder(AutomationTestsTab.context);
                    ad.setTitle("Uninstall OC apk")
                            .setMessage("Do you want to uninstall OC apk?")
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String[] ocUninstallCommand = {"su", "-c", "pm uninstall " + AutomationTestsTab.b.getAndroidPackageName()};
                                            String[] killAllOcc = {"su", "-c", "killall -9 occ"};
                                            String[] clearDataMiscOpenchannel = {"su", "-c", "rm -r /data/misc/openchannel/*"};
                                            try {
                                                Runtime.getRuntime().exec(ocUninstallCommand).waitFor();
                                                Runtime.getRuntime().exec(killAllOcc).waitFor();
                                                Runtime.getRuntime().exec(clearDataMiscOpenchannel).waitFor();
                                                SmokeHelperUtil.setApkInstalled(false);
                                                TextView apkName = (TextView) activity.findViewById(R.id.tvApkName);
                                                apkName.setText("Not selected");
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            dialog.dismiss();
                                        }
                                    }
                            ).setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
                final Button button = (Button) activity.findViewById(R.id.button1);
                button.setClickable(true);
                final Spinner spinner = (Spinner) activity.findViewById(R.id.spinner);
                spinner.setClickable(true);
            }
        });

        TCPDUMP_PATH = FILES_DIR + File.separator + Z7ShellUtil.PCAP_BASE_FILENAME;
        ShellUtil.killAll(TCPDUMP_PATH, 9);
        ShellUtil.killAll(FILES_DIR + File.separator + "tcpdump-new", 9);
    }


    public void setInterruptTests(AtomicBoolean interruptTests) {
        isInterruptTests = interruptTests;
    }

}