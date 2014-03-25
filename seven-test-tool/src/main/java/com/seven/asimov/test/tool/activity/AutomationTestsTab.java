package com.seven.asimov.test.tool.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.core.Pipeline;
import com.seven.asimov.test.tool.core.Starter;
import com.seven.asimov.test.tool.core.testjobs.TestJobToast;
import com.seven.asimov.test.tool.testcase.SmokeTestCase;
import com.seven.asimov.test.tool.tests.device.certification.PerformanceTests;
import com.seven.asimov.test.tool.tests.device.certification.StabilityTests;
import com.seven.asimov.test.tool.tests.environmentcertification.EnvironmentCertificationTests;
import com.seven.asimov.test.tool.tests.sanity.SanityTests;
import com.seven.asimov.test.tool.tests.smoke.SmokeTests;
import com.seven.asimov.test.tool.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutomationTestsTab extends ListActivity implements AdapterView.OnItemLongClickListener/*, AdapterView.OnItemClickListener*/ {

    private final String TAG = AutomationTestsTab.class.getSimpleName();
    public static final Object locker = new Object();
    private static final Logger LOG = LoggerFactory.getLogger(AutomationTestsTab.class.getSimpleName());
    public static final String ACTION_DISPLAY = "com.seven.asimov.test.tool.intent.action.DISPLAY.AutomationTestsTab";
    public static Context context;
    public static BrandingLoaderUtil.Branding b;
    public static ActivityManager am;
    public static PackageManager pm;

    private static String HTTP_STATUS_STRING_200 = "200 OK";

    private CustomArrayAdapter adapter;
    private static ArrayList<String> autoTestCases;
    private AutomationTestsTab currentActivity = this;
    private Map<String, String> testResultsMap = new HashMap<String, String>();
    public static ArrayList<String> checkedTest = new ArrayList<String>();
    private String menuName;
    private MenuItem selectOC;

    private ListView list;
    private CheckedTextView chText;
    private TextView testrunner;
    private TextView branding;
    private TextView statusInfo;
    private static Spinner spinner;
    private static TextView apkName;
    private LinearLayout ocApk;

    private boolean started = false;
    private static boolean fileAdded = true;
    private static int suiteID = 0;
    final TestCaseUtil util = new TestCaseUtil(this, testResultsMap);
    Thread testRunningThread = null;

    private List<String> testRunnersList = new ArrayList<String>() {{
        add("hki-dev-testrunner4.7sys.eu");
        add("tln-dev-testrunner1.7sys.eu");
        add("hki-qa-testrunner1.7sys.eu");
        add("hki-dev-testrunner2.7sys.eu");
        add("rwc-qa-testrunner1.7sys.eu");
    }};
    public static String currentTestRunner;

    public final String[] mkTotal = {"su", "-c", "mkdir " + Z7TestUtil.TOTAL_RESULT};
    public final String[] rmOldTestResults = {"su", "-c", "rm -r " + Z7TestUtil.TOTAL_RESULT + "*"};

    public static String message;

    public void createPackage() {

        File mainDir = new File(Z7TestUtil.TOTAL_RESULT);

        try {
            if (!mainDir.exists()) {
                Runtime.getRuntime().exec(mkTotal).waitFor();
                for (String s : Z7TestUtil.SUITES) {
                    String[] mkSuite = {"su", "-c", "mkdir " + Z7TestUtil.TOTAL_RESULT + s};
                    Runtime.getRuntime().exec(mkSuite).waitFor();
                }
            } else {
                File suite;
                boolean isExists = true;
                for (String s : Z7TestUtil.SUITES) {
                    suite = new File(Z7TestUtil.TOTAL_RESULT + s);
                    isExists = suite.exists();
                }
                if (!isExists) {
                    Runtime.getRuntime().exec(rmOldTestResults).waitFor();
                    for (String s : Z7TestUtil.SUITES) {
                        String[] mkSuite = {"su", "-c", "mkdir " + Z7TestUtil.TOTAL_RESULT + s};
                        Runtime.getRuntime().exec(mkSuite).waitFor();
                    }
                }
            }

        } catch (IOException e1) {
            Log.e(TAG, "Error with creating result directory. Exception : " + (e1 != null ? e1.getMessage() : "null"));
        } catch (InterruptedException e2) {
            Log.e(TAG, "Error with creating result directory. Exception : " + (e2 != null ? e2.getMessage() : "null"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        currentTestRunner = testRunnersList.get(0);
        AsimovTestCase.TEST_RESOURCE_HOST = currentTestRunner;

        PropertyLoaderUtil property = new PropertyLoaderUtil(getApplicationContext());
        property.loadProperties();
        LOG.info("Brandings property loaded.");

        Starter.init(this);
        autoTestCases = extractTestNames();

        setContentView(R.layout.automationtablayout);

        spinner = (Spinner) findViewById(R.id.spinner);
        String[] suites = getResources().getStringArray(R.array.suites);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_layout, suites);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(spinnerListener);

        testrunner = (TextView) findViewById(R.id.currenttestrunner);
        testrunner.setTextColor(Color.WHITE);
        testrunner.setText(currentTestRunner);
        branding = (TextView) findViewById(R.id.tvBrandingInfo);
        branding.setTextColor(Color.WHITE);
        branding.setText(R.string.sBrandingInfo);
        TextView brandingMain = (TextView) findViewById(R.id.tvBranding);
        brandingMain.setOnClickListener(selectBranding);
        statusInfo = (TextView) findViewById(R.id.tvStatusInfo);
        statusInfo.setTextColor(Color.WHITE);
        apkName = (TextView) findViewById(R.id.tvApkName);
        apkName.setTextColor(Color.WHITE);
        am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        pm = getPackageManager();
        ocApk = (LinearLayout) findViewById(R.id.llOCApk);
        if (spinner.getSelectedItemPosition() != 0) {
            ocApk.setVisibility(View.GONE);
        }
        list = (ListView) findViewById(android.R.id.list);
        adapter = new CustomArrayAdapter(this, autoTestCases, testResultsMap, true);
        setListAdapter(adapter);
        list.setOnItemLongClickListener(this);

        chText = (CheckedTextView) findViewById(R.id.allTest);
        chText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!started) {
                    if (chText.isChecked()) {
                        chText.setChecked(false);
                        chText.setCheckMarkDrawable(android.R.drawable.checkbox_off_background);
                        globalCheck(false);
                    } else {
                        chText.setChecked(true);
                        chText.setCheckMarkDrawable(android.R.drawable.checkbox_on_background);
                        globalCheck(true);
                    }
                }
            }
        });

        final Button button = (Button) findViewById(R.id.button1);

        button.setClickable(true);
        button.setEnabled(true);
        button.setText(R.string.smokeTestsButtonStart);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!started) {
                    LOG.info("Tests started");
                    switch (spinner.getSelectedItemPosition()) {
                        case 0:
                            Log.e(TAG, "Spinner = " + spinner.getSelectedItemPosition());
                            util.setTestSuite(spinner.getSelectedItemPosition());
                            startInitialTests(button);
                            break;
                        case 1:
                        case 2:
                        case 3:
                            Log.e(TAG, "Spinner = " + spinner.getSelectedItemPosition());
                            util.setTestSuite(spinner.getSelectedItemPosition());
                            startSmokeTests(button);
                            break;
                    }
                } else {
                    LOG.info("Tests stopped.");
                    if (testRunningThread != null) {
                        util.setInterruptTests(new AtomicBoolean(true));
                    }
                    statusInfo.setText("Stopping...");
                    Toast.makeText(getApplicationContext(), "Please, wait. This will take a few minutes.", Toast.LENGTH_SHORT).show();
                    button.setClickable(false);
                    button.setEnabled(false);
                }
            }
        });

        //TODO in this method should be create general package
        createPackage();
    }

    private void startInitialTests(Button button) {
        if (!apkName.getText().equals("Not selected")) {
            startSmokeTests(button);
        } else {
            new AlertDialog.Builder(AutomationTestsTab.this)
                    .setTitle("Start tests")
                    .setMessage("Select OC apk")
                    .setPositiveButton("Select OC",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    LOG.info("OC did not selected");
                                    Intent view = new Intent(AutomationTestsTab.this, ViewActivity.class);
                                    view.putExtra("Key_name", "select");
                                    menuName = "select";
                                    startActivityForResult(view, 1);
                                }
                            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void startSmokeTests(Button button) {
        if (!branding.getText().equals("branding does not detect")) {
            if (b != null) {
                Log.i(TAG, "Select branding " + b.getBrandingName());
                startTests(button);
            } else {
                new AlertDialog.Builder(AutomationTestsTab.this)
                        .setTitle("Start tests")
                        .setMessage("Could not find the branding " + branding.getText().toString() +
                                ". Try to download it again.")

                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        } else {
            if (spinner.getSelectedItemPosition() == 3) {
                b = new BrandingLoaderUtil.Branding();
                startTests(button);
            } else {
                new AlertDialog.Builder(AutomationTestsTab.this)
                        .setTitle("Start tests")
                        .setMessage("Branding did not selected")
                        .setPositiveButton("Select branding",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        LOG.info("Select OC.");
                                        Intent view = new Intent(AutomationTestsTab.this, ViewActivity.class);
                                        view.putExtra("Key_name", "branding");
                                        menuName = "branding";
                                        startActivityForResult(view, 1);
                                    }
                                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }
    }

    //TODO
    private void startTests(Button button) {
        if (!checkedTest.isEmpty()) {
            clearResults();
            SmokeHelperUtil.clearResults();

            button.setText("Stop");
            statusInfo.setText(R.string.smokeTestsButtonCheck);

            started = true;
            spinner.setClickable(false);
            util.setInterruptTests(new AtomicBoolean(false));
            testRunningThread = new Thread(util);
            testRunningThread.start();
        } else {
            new AlertDialog.Builder(AutomationTestsTab.this)
                    .setTitle("Start tests")
                    .setMessage("Select the tests to run")
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                    .show();
        }
    }

    private void clearResults() {
        LOG.info("Clearing results.");
        autoTestCases = extractTestNames();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshPicturesOnTextViewEntries();
            }
        });
    }

    private AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            suiteID = position;
            switch (position) {
                case 0:
                    ocApk.setVisibility(LinearLayout.VISIBLE);
                    branding.setTextColor(Color.WHITE);
                    break;
                case 1:
                case 2:
                case 3:
                    ocApk.setVisibility(View.GONE);
                    branding.setTextColor(Color.WHITE);
                    break;
            }
            selectOCMenuVisibility(position == 0);
            autoTestCases = extractTestNames();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    globalCheck(true);
                }
            });
            chText.setChecked(true);
            chText.setCheckMarkDrawable(android.R.drawable.checkbox_on_background);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        View v1 = list.getChildAt(position);
        int top = (v1 == null) ? 0 : v1.getTop();

        if (!started) {
            CheckedTextView c = (CheckedTextView) v;
            Log.e(TAG, "" + checkedTest.size() + " " + checkedTest.toString());
            Log.e(TAG, c.getText().toString());
            if (checkedTest.contains(c.getText().toString())) {
                Log.e("|", "if");
                c.setCheckMarkDrawable(android.R.drawable.checkbox_off_background);
                chText.setChecked(false);
                chText.setCheckMarkDrawable(android.R.drawable.checkbox_off_background);
                checkedTest.remove(c.getText().toString());
            } else {
                Log.e("|", "else");
                c.setCheckMarkDrawable(android.R.drawable.checkbox_on_background);
                checkedTest.add(c.getText().toString());
            }
            if (autoTestCases.size() == checkedTest.size()) {
                chText.setChecked(true);
                chText.setCheckMarkDrawable(android.R.drawable.checkbox_on_background);
            }
            adapter = new CustomArrayAdapter(this, autoTestCases, testResultsMap);
            list.setAdapter(adapter);
        }

        list.setSelectionFromTop(position, top);
    }

    /**
     * There has to be a conclusion results of the tests
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        LOG.info("Item : " + item + " selected.");

        TextView dialogView = new TextView(getApplicationContext());
        dialogView.setTextSize(12);
        dialogView.setText(testResultsMap.get(item));
        LOG.info("Test info selected : \n" + testResultsMap.get(item));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Results: \n");
        AlertDialog dialog = builder.create();
        dialog.setView(dialogView);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Close", closeButtonListener);
        dialog.show();
        return true;
    }

    DialogInterface.OnClickListener closeButtonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        LOG.info("AutomationTestTab paused.");
        try {
            util.serializableTestCaseEvents();
        } catch (IOException e) {
//            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        RootTab.setLastPausedTab(Tabs.AUTOMATION_TESTS_TAB);
        unregisterReceiver(mUiReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOG.info("AutomationTestTab resumed.");
        try {
            util.deSerializableTestCaseEvents();
        } catch (IOException e) {
//            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        } catch (ClassNotFoundException e2) {
//            Log.e(TAG, ExceptionUtils.getStackTrace(e2));
        }
        registerReceiver(mUiReceiver, new IntentFilter(ACTION_DISPLAY));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("Exit");
            builder.setMessage("Are you sure you want to exit?");
            builder.setPositiveButton("Yes", destroyActivityButtonListener);
            builder.setNegativeButton("No", closeButtonListener);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    DialogInterface.OnClickListener destroyActivityButtonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            currentActivity.finish();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem refreshResults = menu.add(R.string.itmRefreshResults);
        refreshResults.setTitle(R.string.itmRefreshResults);
        refreshResults.setOnMenuItemClickListener(menuRefreshResults);

        MenuItem setupTestRunnerServer = menu.add(R.string.itmSetupTestrunnerServer);
        setupTestRunnerServer.setTitle(R.string.itmSetupTestrunnerServer);
        setupTestRunnerServer.setOnMenuItemClickListener(menuSetupTestrunnerServer);

        selectOC = menu.add(R.string.sSelectOC);
        selectOC.setTitle(R.string.sSelectOC);
        selectOC.setOnMenuItemClickListener(selectOCListener);

        MenuItem addBranding = menu.add(R.string.itmAddNewBranding);
        addBranding.setTitle(R.string.itmAddNewBranding);
        addBranding.setOnMenuItemClickListener(addNewBranding);

        SubMenu help = menu.addSubMenu(R.string.itmHelp);
        help.add(Menu.NONE, 1, Menu.NONE, R.string.itmAboutLog);
        help.add(Menu.NONE, 2, Menu.NONE, R.string.itmAbout7TestTool);

        help.getItem(0).setOnMenuItemClickListener(aboutLogListener);
        help.getItem(1).setOnMenuItemClickListener(about7TestToolListener);

        selectOCMenuVisibility(spinner.getSelectedItemPosition() == 0);

        return true;
    }

    private void selectOCMenuVisibility(boolean visible) {
        if (selectOC != null) {
            selectOC.setVisible(visible);
        }
    }


    private MenuItem.OnMenuItemClickListener menuSetupTestrunnerServer = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return selectTestRunner();
        }
    };

    private AlertDialog testRunnerDialog;

    private boolean selectTestRunner() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select testrunner");
        builder.setPositiveButton("OK", closeButtonListener);

        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.options, null);

        ListView listView = (ListView) view.findViewById(R.id.ListViewOptions);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(testRunnerSelector);
        listView.setAdapter(new OptionsAdapter(this));
        builder.setView(view);

        testRunnerDialog = builder.create();
        testRunnerDialog.show();
        return true;
    }

    private AdapterView.OnItemClickListener testRunnerSelector = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            currentTestRunner = (String) parent.getItemAtPosition(position);
            testrunner.setText(currentTestRunner);
            AsimovTestCase.TEST_RESOURCE_HOST = currentTestRunner;
            Toast.makeText(getApplicationContext(), "Current testrunner is: " + currentTestRunner, Toast.LENGTH_LONG).show();
            if (testRunnerDialog != null) {
                if (testRunnerDialog.isShowing()) {
                    testRunnerDialog.dismiss();
                }
            }
        }
    };

    private AdapterView.OnItemClickListener testBrandingSelector = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = ((BrandingLoaderUtil.Branding) parent.getItemAtPosition(position)).getBrandingName();
            name = name.substring(0, name.lastIndexOf("."));
            b = getBranding(name);
            branding.setText(name.replaceAll("_", "-"));
            if (testRunnerDialog != null) {
                if (testRunnerDialog.isShowing()) {
                    testRunnerDialog.dismiss();
                }
            }
        }
    };

    private MenuItem.OnMenuItemClickListener menuRefreshResults = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            clearResults();
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener selectOCListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            fileAdded = true;
            LOG.info("Select OC.");
            Intent view = new Intent(AutomationTestsTab.this, ViewActivity.class);
            view.putExtra("Key_name", "select");
            menuName = "select";
            startActivityForResult(view, 1);
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener addNewBranding = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            fileAdded = false;
            LOG.info("New Branding.");
            Intent view = new Intent(AutomationTestsTab.this, ViewActivity.class);
            view.putExtra("Key_name", "branding");
            menuName = "branding";
            startActivityForResult(view, 1);
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener aboutLogListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            new AlertDialog.Builder(context)
                    .setTitle("About log")
                    .setMessage("Logs are available in folder\n" +
                            "sdcard/OCIntegrationTestsResults. This folder is created after the first run smoke tests.")
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                    .show();
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener about7TestToolListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            LOG.info("InformationActivity");
            Intent view = new Intent(AutomationTestsTab.this, InformationActivity.class);
            startActivity(view);
            return true;
        }
    };

    private View.OnClickListener selectBranding = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!started) {
                if (!BrandingLoaderUtil.getBrandings().isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Select branding");
                    builder.setPositiveButton("OK", closeButtonListener);

                    LayoutInflater li = LayoutInflater.from(context);
                    View view = li.inflate(R.layout.options, null);

                    ListView listView = (ListView) view.findViewById(R.id.ListViewOptions);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    listView.setOnItemClickListener(testBrandingSelector);
                    listView.setAdapter(new BrandingAdapter(currentActivity));
                    builder.setView(view);

                    testRunnerDialog = builder.create();
                    testRunnerDialog.show();
                } else {
                    new AlertDialog.Builder(AutomationTestsTab.this)
                            .setTitle("Choice of branding")
                            .setMessage("A list of loaded branding empty.")
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                            .show();
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (menuName.equals("branding")) {
            addNewBrandingToList(data);
        } else if (menuName.equals("select")) {
            selectOCApk(data);
        }
    }

    private ArrayList<String> extractTestNames() {
        ArrayList<String> result = new ArrayList<String>();
        Class[] classes;
        switch (suiteID) {
            case 0:
                classes = new Class[]{EnvironmentCertificationTests.class};
                break;
            case 1:
                classes = new Class[]{SmokeTests.class};
                break;
            case 2:
                classes = new Class[]{SanityTests.class};
                break;
            case 3:
                classes = new Class[]{StabilityTests.class, PerformanceTests.class};
                break;
            default:
                classes = new Class[0];
        }

        for (Class clazz : classes) {
            for (Method method : clazz.getDeclaredMethods()) {
                Annotation[] annotations = method.getAnnotations();
                boolean isIgnored = false;
                for (Annotation annotation : annotations) {
                    if (annotation.toString().contains("Ignore")) {
                        isIgnored = true;
                        break;
                    }
                }
                if (!isIgnored && (method.getName().contains("test_"))) {
                    result.add(method.getName());
                }
            }
        }
        for (String entry : result) {
            String info = "TEST INFO: \n" +
                    "Name : " + entry + "\n" +
                    "Status : " + "WASN'T EXECUTED" + "\n";
            testResultsMap.put(entry, info);
            LOG.info("Selected test for execution : " + entry);
        }
        return result;
    }

    private void globalCheck(boolean global) {
        adapter = new CustomArrayAdapter(this, autoTestCases, testResultsMap, global);
        list.setAdapter(adapter);
    }

    public void refreshPicturesOnTextViewEntries() {
        LOG.info("Refreshed GUI.");
        adapter = new CustomArrayAdapter(this, autoTestCases, testResultsMap);
        setListAdapter(adapter);
    }

    private BroadcastReceiver mUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            Set<String> keys = extras.keySet();
            for (String key : keys) {
                if (key.equals(Pipeline.MESSAGE)) {
                    String toast = null;
                    TestJobToast testJobToast = (TestJobToast) extras.get("Toast");
                    switch (testJobToast.getTestJobType()) {
                        default:
                            break;
                        case SERVICE_TESTS_LOAD_AUTOMATED_TESTS:

                            if (testJobToast.getMessage() != null) {
                                String[] message = testJobToast.getMessage().split(":", 2);
                                if (message.length == 2) {
                                }
                            } else if (testJobToast.getHttpStatus() != null) {
                                if (testJobToast.getHttpStatus().contains(HTTP_STATUS_STRING_200)) {
                                    toast = "Got " + autoTestCases.size() + " test suite(s).";
                                }
                            }
                            Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    };

    public void setStarted(boolean started) {
        this.started = started;
    }

    private void addNewBrandingToList(Intent data) {
        if (data != null) {
            if (data.getExtras().containsKey("path")) {
                ExecutorService pool = Executors.newFixedThreadPool(1);
                CompletionService<Integer> compService = new ExecutorCompletionService<Integer>(pool);
                compService.submit(new BrandingThread(data.getStringExtra("path")));
                Future<Integer> fut;
                String brandingName;
                try {
                    String path;
                    fut = compService.take();
                    switch (fut.get()) {
                        case 0:
                            path = data.getStringExtra("path");
                            brandingName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
                            b = getBranding(brandingName);
                            brandingName = brandingName.replaceAll("_", "-");
                            branding.setTextColor(Color.WHITE);
                            branding.setText(brandingName);
//                            }
                            new AlertDialog.Builder(this)
                                    .setTitle("Add Branding")
                                    .setMessage("Branding successfully added")
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                    .show();
                            break;
                        case 1:
                            path = data.getStringExtra("path");
                            brandingName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
                            b = getBranding(brandingName);
                            brandingName = brandingName.replaceAll("_", "-");
                            branding.setTextColor(Color.WHITE);
                            branding.setText(brandingName);
//                            }
                            new AlertDialog.Builder(this)
                                    .setTitle("Add Branding")
                                    .setMessage("This branding already exists")
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                    .show();
                            break;
                        case 2:
                            StringBuilder sb = new StringBuilder();
                            if (!BrandingLoaderUtil.incorrectParameters.isEmpty()) {
                                Log.e("AutomationTestTab", "Number of incorrect parameters = " +
                                        BrandingLoaderUtil.incorrectParameters.size());
                                for (int i = 0; i < BrandingLoaderUtil.incorrectParameters.size(); i++)
                                    sb.append(BrandingLoaderUtil.incorrectParameters.get(i)).append("\n");
                                BrandingLoaderUtil.incorrectParameters.clear();
                            }
                            new AlertDialog.Builder(this)
                                    .setTitle("Add Branding")
                                    .setMessage("This is not correct branding\nMissing the following parameters:\n" + sb)
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                    .show();
                            break;
                        default:
                            sb = new StringBuilder();
                            if (!BrandingLoaderUtil.error.isEmpty()) {
                                Log.e("AutomationTestTab", "Number of errors = " +
                                        BrandingLoaderUtil.error.size());
                                for (int i = 0; i < BrandingLoaderUtil.error.size(); i++)
                                    sb.append(BrandingLoaderUtil.error.get(i)).append("\n");
                                BrandingLoaderUtil.error.clear();
                            }
                            new AlertDialog.Builder(this)
                                    .setTitle("Add Branding")
                                    .setMessage("Error during the addition of branding:\n" + sb)
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                    .show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void selectOCApk(Intent data) {
        if (data != null) {
            if (data.getExtras().containsKey("path")) {
                String path = data.getStringExtra("path");
                if (path != null) {
                    SmokeTestCase.setOcApkPath(path);
                    String name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
                    name = name.replaceAll("_", "-");
                    apkName.setText(name);
                }
            }
        }
    }

    private BrandingLoaderUtil.Branding getBranding(String brandingName) {
        Log.e(TAG, brandingName);
        List<BrandingLoaderUtil.Branding> brandings = BrandingLoaderUtil.getBrandings();
        for (BrandingLoaderUtil.Branding br : brandings) {
            Log.e(TAG, br.getBrandingName());
            if (br.getBrandingName().equals(brandingName + ".target")) {
                return br;
            }
        }
        return null;
    }

    /**
     * OptionsAdapter.
     */
    class OptionsAdapter extends ArrayAdapter<Object> {
        private Activity mContext;

        OptionsAdapter(Activity context) {
            super(context, R.layout.options, testRunnersList.toArray());
            this.mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // Log.v(LOG, "OptionsAdapter.getView()");
            LayoutInflater li = LayoutInflater.from(mContext);
            View row = li.inflate(R.layout.listitem, null);
            TextView tvOptions = (TextView) row.findViewById(R.id.list_content);
            tvOptions.setText(testRunnersList.get(position));
            tvOptions.setTextColor(getResources().getColor(R.color.black));

            return (row);
        }
    }

    public static boolean isFileAdded() {
        return fileAdded;
    }

    public static TextView getApkName() {
        return apkName;
    }

    class BrandingAdapter extends ArrayAdapter<Object> {
        private Activity mContext;

        BrandingAdapter(Activity context) {
            super(context, R.layout.options, BrandingLoaderUtil.getBrandings().toArray());
            this.mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater li = LayoutInflater.from(mContext);
            View row = li.inflate(R.layout.listitem, null);
            TextView tvOptions = (TextView) row.findViewById(R.id.list_content);
            tvOptions.setText(BrandingLoaderUtil.getBrandings().get(position).getBrandingName());
            tvOptions.setTextColor(getResources().getColor(R.color.black));

            return row;
        }
    }

}