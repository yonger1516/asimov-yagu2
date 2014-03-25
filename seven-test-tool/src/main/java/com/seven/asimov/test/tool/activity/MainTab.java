package com.seven.asimov.test.tool.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.controls.NewEditText;
import com.seven.asimov.test.tool.core.Pipeline;
import com.seven.asimov.test.tool.core.TestFactory;
import com.seven.asimov.test.tool.core.testjobs.*;
import com.seven.asimov.test.tool.preferences.SharedPrefs;
import com.seven.asimov.test.tool.receivers.ConnectivityReceiver;
import com.seven.asimov.test.tool.services.MainService;
import com.seven.asimov.test.tool.utils.Z7FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Request handler.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class MainTab extends Activity {

    private static final Logger LOG = LoggerFactory.getLogger(MainTab.class.getSimpleName());

    public static final String ACTION_DISPLAY = "com.seven.asimov.test.tool.intent.action.DISPLAY.MainTab";

    private AlertDialog mAlertDialogSaveTestBeforeExit;
    private AlertDialog mAlertDialogSaveRenameTest;
    private AlertDialog mAlertDialogTestExists;
    private AlertDialog mAlertDialogMoreOptions;

    private Button mButtonSingleRequest;
    private Button mButtonPeriodicRequests;
    private Button mButtonLoopedRequest;
    private Button mButtonAutomatedTest;
    private Button mButtonPreviousRequest;
    private Button mButtonNextRequest;
    private Button mButtonResetRequest;
    private Button mButtonShowRequest;
    private Button mButtonPreviousPattern;
    private Button mButtonNextPattern;
    private Button mButtonResetPattern;

    private EditText mEditTextTestName;
    private NewEditText mEditTextHttpMethod;
    private NewEditText mEditTextUri;
    private NewEditText mEditTextPattern;

    private LinearLayout mLinearLayoutSingleRequest;
    private LinearLayout mLinearLayoutPeriodicRequests;
    private LinearLayout mLinearLayoutLoopedRequests;

    private ListView mListViewMoreOptions;

    private TextView mTextViewConnectionType;
    // private TextView mTextViewConnectionActivity;
    // private TextView mTextViewConnectionStatus;
    private TextView mTextViewTestName;
    private TextView mTextViewTimer;
    private TextView mTextViewPreviousTimer;
    private TextView mTextViewCurrentPatternId;
    private TextView mTextViewPatternSize;
    private TextView mTextViewCurrentDelay;
    private TextView mTextViewCurrentTestId;
    private TextView mTextViewTestsCount;
    private TextView mTextViewCurrentTestReterationId;
    private TextView mTextViewCurrentTestReterations;

    private void displayActiveNetworkType(String activeNetworkType) {
        mTextViewConnectionType.setText(activeNetworkType);
    }

    private void displayActiveNetworkState(String activeNetworkState) {
        if (activeNetworkState.equals(NetworkInfo.State.CONNECTED.toString())) {
            mTextViewConnectionType.setTextColor(getResources().getColor(R.color.green));
        }
        if (activeNetworkState.equals(NetworkInfo.State.DISCONNECTED.toString())) {
            mTextViewConnectionType.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private void displayCurrentPatternId(int currentPatternId) {
        mTextViewCurrentPatternId.setText(String.valueOf(currentPatternId));
    }

    private void displayPatternSize(int patternSize) {
        mTextViewPatternSize.setText(String.valueOf(patternSize));
    }

    private void displayCurrentDelay(int currentDelay) {
        mTextViewCurrentDelay.setText(String.valueOf(currentDelay));
    }

    private void displayCurrentTestId(int currentTestId) {
        mTextViewCurrentTestId.setText(String.valueOf(currentTestId));
    }

    private void displayTestsCount(int testsCount) {
        mTextViewTestsCount.setText(String.valueOf(testsCount));
    }

    private void displayCurrentTestReterationId(int currentTestReterationId) {
        mTextViewCurrentTestReterationId.setText(String.valueOf(currentTestReterationId));
    }

    private void displayCurrentTestReterations(int currentTestReterations) {
        mTextViewCurrentTestReterations.setText(String.valueOf(currentTestReterations));
    }

    // private void displayDataConnectionActivity(String dataConnectionActivity) {
    // mTextViewConnectionActivity.setText(dataConnectionActivity);
    // }
    //
    // private void displayDataConnectionState(String dataConnectionState) {
    // mTextViewConnectionStatus.setText(dataConnectionState);
    // }

    private void displayPattern(ArrayList<String> pattern) {
        pattern.set(TestFactory.getCurrentIntervalId(), "[" + pattern.get(TestFactory.getCurrentIntervalId()) + "]");
        String result = StringUtils.join(pattern.toArray(), ",");
        mEditTextPattern.setText(result);
    }

    private void displayPattern(String pattern) {
        mEditTextPattern.setText(pattern);
    }

    private String getPattern() {
        return mEditTextPattern.getText().toString().replaceAll("\\n", StringUtils.EMPTY);
    }

    private void displayTimers() {
        displayTimer(TestFactory.getTimer());
        displayPreviousTimer(TestFactory.getPreviousTimer());
    }

    private void displayTimer(String timer) {
        mTextViewTimer.setText(timer);
    }

    private void displayPreviousTimer(String previousTimer) {
        mTextViewPreviousTimer.setText(previousTimer);
    }

    private void displayHttpMethod(String httpMethod) {
        mEditTextHttpMethod.setText(httpMethod);
    }

    private String getHttpMethod() {
        return mEditTextHttpMethod.getText().toString().replaceAll("\\n", StringUtils.EMPTY);
    }

    private String getHttpUri() {
        return mEditTextUri.getText().toString().replaceAll("\\n", StringUtils.EMPTY);
    }

    private void displayHttpUri(String httpUri) {
        mEditTextUri.setText(httpUri);
    }

    private void displayTestSuiteName(String testSuiteName) {
        mTextViewTestName.setText(testSuiteName);
    }

    private void colorTestSuiteName(Boolean validatedOk) {
        if (BooleanUtils.isTrue(validatedOk)) {
            mTextViewTestName.setTextColor(getResources().getColor(R.color.green));
        } else {
            mTextViewTestName.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private String getTestSuiteName() {
        return mTextViewTestName.getText().toString();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(Tabs.MAIN_TAB.name(), "onCreate()");

        setContentView(R.layout.maintabview);

        // http://stackoverflow.com/questions/4149415/onscreen-keyboard-opens-automatically-when-i-enter-on-my-activity
        // Android opens the OSK automatically if you have an EditText
        // focused
        // when activity starts.
        // You can prevent that by adding following into your activity's
        // onCreate:
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mButtonSingleRequest = (Button) findViewById(R.id.btnRequest);
        mButtonSingleRequest.setOnClickListener(new MyOnClickListener());
        mButtonPeriodicRequests = (Button) findViewById(R.id.btnRequestWithInterval);
        mButtonPeriodicRequests.setText(R.string.btnRequestWithInterval);
        mButtonPeriodicRequests.setOnClickListener(new MyOnClickListener());
        mButtonLoopedRequest = (Button) findViewById(R.id.btnLoopedRequest);
        mButtonLoopedRequest.setText(R.string.btnLoopedRequest);
        mButtonLoopedRequest.setOnClickListener(new MyOnClickListener());
        mButtonAutomatedTest = (Button) findViewById(R.id.btnAutomatedTest);
        mButtonAutomatedTest.setText(R.string.btnAutomatedTest);
        mButtonAutomatedTest.setOnClickListener(new MyOnClickListener());
        mButtonPreviousRequest = (Button) findViewById(R.id.buttonPrevoiusRequest);
        mButtonPreviousRequest.setOnClickListener(new MyOnClickListener());
        mButtonNextRequest = (Button) findViewById(R.id.buttonNextRequest);
        mButtonNextRequest.setOnClickListener(new MyOnClickListener());
        mButtonResetRequest = (Button) findViewById(R.id.buttonResetRequest);
        mButtonResetRequest.setOnClickListener(new MyOnClickListener());
        mButtonShowRequest = (Button) findViewById(R.id.buttonShowRequest);
        mButtonShowRequest.setOnClickListener(new MyOnClickListener());
        mButtonPreviousPattern = (Button) findViewById(R.id.buttonPrevoiusPattern);
        mButtonPreviousPattern.setOnClickListener(new MyOnClickListener());
        mButtonNextPattern = (Button) findViewById(R.id.buttonNextPattern);
        mButtonNextPattern.setOnClickListener(new MyOnClickListener());
        mButtonResetPattern = (Button) findViewById(R.id.buttonResetPattern);
        mButtonResetPattern.setOnClickListener(new MyOnClickListener());

        mEditTextHttpMethod = (NewEditText) findViewById(R.id.txtMethod);
        mEditTextHttpMethod.addTextChangedListener(mEditTextHttpMethodWatcher);
        mEditTextHttpMethod.setOnCreateContextMenuListener(mMethodContextMenuHandler);
        mEditTextUri = (NewEditText) findViewById(R.id.txtUri);
        mEditTextUri.addTextChangedListener(mEditTextUriWatcher);
        mEditTextPattern = (NewEditText) findViewById(R.id.txtIntervalPattern);
        mEditTextPattern.addTextChangedListener(mEditTextPatternWatcher);
        mEditTextPattern.setOnCreateContextMenuListener(mIntervalContextMenuHandler);

        mLinearLayoutSingleRequest = (LinearLayout) findViewById(R.id.layoutRequest);
        mLinearLayoutPeriodicRequests = (LinearLayout) findViewById(R.id.layoutRequestWithInterval);
        mLinearLayoutLoopedRequests = (LinearLayout) findViewById(R.id.layoutLoopedRequest);

        mTextViewConnectionType = (TextView) findViewById(R.id.lblvarConnectionType);
        // mTextViewConnectionActivity = (TextView) findViewById(R.id.lblvarConnectionActivity);
        // mTextViewConnectionStatus = (TextView) findViewById(R.id.lblvarConnectionStatus);
        mTextViewTestName = (TextView) findViewById(R.id.lblvarTest);
        mTextViewTimer = (TextView) findViewById(R.id.lblvarTimer);
        mTextViewPreviousTimer = (TextView) findViewById(R.id.lblvarprevTimer);
        mTextViewCurrentPatternId = (TextView) findViewById(R.id.textViewCurrentPatternId);
        mTextViewPatternSize = (TextView) findViewById(R.id.textViewPatternSize);
        mTextViewCurrentDelay = (TextView) findViewById(R.id.textViewCurrentDelay);
        mTextViewCurrentTestId = (TextView) findViewById(R.id.lblvarCurrentRequestId);
        mTextViewTestsCount = (TextView) findViewById(R.id.lblvarLoadedRequestsCount);
        mTextViewCurrentTestReterationId = (TextView) findViewById(R.id.textViewCurrentTestReterationId);
        mTextViewCurrentTestReterations = (TextView) findViewById(R.id.textViewCurrentTestReterations);
    }

    // Received events
    public static final String ACTIVE_NETWORK_STATE_MESSAGE = "activeNetworkState";
    public static final String ACTIVE_NETWORK_TYPE_MESSAGE = "activeNetworkType";
    // public static final String DATA_CONNECTION_ACTIVITY_MESSAGE = "dataConnectionActivity";
    // public static final String DATA_CONNECTION_STATE_MESSAGE = "dataConnectionState";

    private BroadcastReceiver mUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            Set<String> keys = extras.keySet();
            for (String key : keys) {
                if (key.equals(ACTIVE_NETWORK_STATE_MESSAGE)) {
                    displayActiveNetworkState(extras.getString(key));
                    break;
                }
                if (key.equals(ACTIVE_NETWORK_TYPE_MESSAGE)) {
                    displayActiveNetworkType(extras.getString(key));
                    break;
                }
                // if (key.equals(DATA_CONNECTION_ACTIVITY_MESSAGE)) {
                // displayDataConnectionActivity(extras.getString(key));
                // break;
                // }
                // if (key.equals(DATA_CONNECTION_STATE_MESSAGE)) {
                // displayDataConnectionState(extras.getString(key));
                // break;
                // }
                if (key.equals(Pipeline.MESSAGE)) {
                    Log.v(Tabs.MAIN_TAB.name(), Pipeline.MESSAGE);
                    TestJobToast testJobToast = (TestJobToast) extras.get("Toast");

                    switch (testJobToast.getTestJobType()) {
                        default:
                            break;
                        case BUTTON_SINGLE_REQUEST:
                            displayButtonSingleRequest();
                            break;
                        case BUTTON_PERIODIC_REQUESTS:
                            displayButtonPeriodicRequest();
                            break;
                        case BUTTON_LOOPED_REQUESTS:
                            displayButtonLoopedRequest();
                            break;
                        case AUTOMATED_TEST:
                            colorTestSuiteName(testJobToast.isValidated());
                            break;
                    }
                    Toast toast = Toast
                            .makeText(
                                    getApplicationContext(),
                                    "["
                                            + testJobToast.getConnection()
                                            + "] "
                                            + ((testJobToast.getHttpStatus() != null) ? testJobToast.getHttpStatus()
                                            : StringUtils.EMPTY)
                                            + ((testJobToast.getConnectionStatus() != null) ? testJobToast
                                            .getConnectionStatus() : StringUtils.EMPTY)
                                            + ((testJobToast.getError() != null) ? testJobToast.getError()
                                            : StringUtils.EMPTY), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
                }
                if (key.equals(TestFactory.MESSAGE)) {
                    // Log.v(LOG, TEST_FACTORY_MESSAGE);

                    displayTestFactory();
                    displayTimers();

                    break;
                }
                if (key.equals(MainService.TIMER_MESSAGE)) {
                    displayTimers();
                    break;
                }
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(Tabs.MAIN_TAB.name(), "onNewIntent()");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(Tabs.MAIN_TAB.name(), "onPause()");

        RootTab.setLastPausedTab(Tabs.MAIN_TAB);

        // http://developer.android.com/reference/android/app/Activity.html
        // Note that it is important to save persistent data in onPause() instead of onSaveInstanceState(Bundle) because
        // the latter is not part of the lifecycle callbacks, so will not be called in every situation as described in
        // its documentation
        // Store UI data
        TestFactory.setHttpMethod(getHttpMethod());
        TestFactory.setUri(getHttpUri());
        TestFactory.setIntervalPattern(getPattern());

        unregisterReceiver(mUiReceiver);

        if (mAlertDialogSaveRenameTest != null) {
            if (mAlertDialogSaveRenameTest.isShowing()) {
                mAlertDialogSaveRenameTest.dismiss();
            }
        }
        if (mAlertDialogSaveTestBeforeExit != null) {
            if (mAlertDialogSaveTestBeforeExit.isShowing()) {
                mAlertDialogSaveTestBeforeExit.dismiss();
            }
        }
        if (mAlertDialogTestExists != null) {
            if (mAlertDialogTestExists.isShowing()) {
                mAlertDialogTestExists.dismiss();
            }
        }

        if (mAlertDialogMoreOptions != null) {
            if (mAlertDialogMoreOptions.isShowing()) {
                mAlertDialogMoreOptions.dismiss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v(Tabs.MAIN_TAB.name(), "onResume()");

        registerReceiver(mUiReceiver, new IntentFilter(ACTION_DISPLAY));

        displayActiveNetworkState(ConnectivityReceiver.getActiveNetworkState());
        displayActiveNetworkType(ConnectivityReceiver.getActiveNetworkType());
        // displayDataConnectionActivity(MyPhoneStateListener.getDataConnectionActivity());
        // displayDataConnectionState(MyPhoneStateListener.getDataConnectionState());

        displayTestFactory();

        displayTimers();
    }

    private void displayButtonSingleRequest() {
        if (TestJobStates.getJobState(TestJobType.BUTTON_SINGLE_REQUEST) == TestJobState.IS_RUNNING) {
            if (mLinearLayoutSingleRequest.getChildAt(1) == null) {
                mLinearLayoutSingleRequest.addView(newProgressBar(), 1);
            }
        } else if (mLinearLayoutSingleRequest.getChildAt(1) != null) {
            mLinearLayoutSingleRequest.removeViewAt(1);
        }
    }

    private void displayButtonPeriodicRequest() {
        if (TestJobStates.getJobState(TestJobType.BUTTON_PERIODIC_REQUESTS) == TestJobState.IS_RUNNING) {
            mButtonPeriodicRequests.setText(R.string.btnStopSending);
            if (mLinearLayoutPeriodicRequests.getChildAt(1) == null) {
                mLinearLayoutPeriodicRequests.addView(newProgressBar(), 1);
            }
        } else {
            mButtonPeriodicRequests.setText(R.string.btnRequestWithInterval);
            if (mLinearLayoutPeriodicRequests.getChildAt(1) != null) {
                mLinearLayoutPeriodicRequests.removeViewAt(1);
            }
        }
    }

    private void displayButtonLoopedRequest() {
        if (TestJobStates.getJobState(TestJobType.BUTTON_LOOPED_REQUESTS) == TestJobState.IS_RUNNING) {
            mButtonLoopedRequest.setText(R.string.btnStopSending);
            if (mLinearLayoutLoopedRequests.getChildAt(1) == null) {
                mLinearLayoutLoopedRequests.addView(newProgressBar(), 1);
            }
        } else {
            mButtonLoopedRequest.setText(R.string.btnLoopedRequest);
            if (mLinearLayoutLoopedRequests.getChildAt(1) != null) {
                mLinearLayoutLoopedRequests.removeViewAt(1);
            }
        }
    }

    private void displayTestFactory() {

        displayButtonSingleRequest();
        displayButtonPeriodicRequest();
        displayButtonLoopedRequest();

        displayHttpMethod(TestFactory.getHttpMethod());
        displayHttpUri(TestFactory.getUri());
        displayPattern(TestFactory.getIntervalPattern());
        displayTestSuiteName(TestFactory.getTestSuite().getName());
        displayCurrentPatternId(TestFactory.getCurrentIntervalId());
        displayPatternSize(TestFactory.getIntervalPatternSize());
        // displayCurrentDelay(currentDelay);
        displayCurrentTestId(TestFactory.getCurrentTestId());
        displayTestsCount(TestFactory.getTestsCount());
        displayCurrentTestReterationId(TestFactory.getCurrentTestReiterationId());
        displayCurrentTestReterations(TestFactory.getCurrentTestReiterations());
    }

    protected static final int CONTEXTMENU_GET = 0;
    protected static final int CONTEXTMENU_POST = 1;
    protected static final int CONTEXTMENU_PUT = 2;
    protected static final int CONTEXTMENU_DELETE = 3;
    protected static final int CONTEXTMENU_HEAD = 4;
    protected static final int CONTEXTMENU_TRACE = 5;
    protected static final int CONTEXTMENU_OPTIONS = 6;
    protected static final int CONTEXTMENU_CONNECT = 7;

    private OnCreateContextMenuListener mMethodContextMenuHandler = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            // Log.v(LOG, "txtMethodContextMenuHandler.onCreateContextMenu()");
            menu.setHeaderTitle("Choose request method");
            menu.clear();
            menu.add(0, CONTEXTMENU_GET, CONTEXTMENU_GET, "GET");
            menu.add(0, CONTEXTMENU_POST, CONTEXTMENU_POST, "POST");
            menu.add(0, CONTEXTMENU_PUT, CONTEXTMENU_PUT, "PUT");
            menu.add(0, CONTEXTMENU_DELETE, CONTEXTMENU_DELETE, "DELETE");
            menu.add(0, CONTEXTMENU_HEAD, CONTEXTMENU_HEAD, "HEAD");
            menu.add(0, CONTEXTMENU_TRACE, CONTEXTMENU_TRACE, "TRACE");
            menu.add(0, CONTEXTMENU_OPTIONS, CONTEXTMENU_OPTIONS, "OPTIONS");
            menu.add(0, CONTEXTMENU_CONNECT, CONTEXTMENU_CONNECT, "CONNECT");
            /* Add as many context-menu-options as you want to. */
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Log.v(Tabs.MAIN_TAB.name(), "onOptionsItemSelected()");
        return false; // no need to call super.onOptionsItemSelected(item)
    }

    @Override
    public boolean onContextItemSelected(MenuItem aItem) {
        super.onContextItemSelected(aItem);
        // Log.v(LOG, "onContextItemSelected()");
        /* Switch on the ID of the item, to get what the user selected. */
        switch (aItem.getItemId()) {
            // HTTP Method
            case CONTEXTMENU_GET:
                mEditTextHttpMethod.setText("GET");
                break;
            case CONTEXTMENU_POST:
                mEditTextHttpMethod.setText("POST");
                break;
            case CONTEXTMENU_PUT:
                mEditTextHttpMethod.setText("PUT");
                break;
            case CONTEXTMENU_DELETE:
                mEditTextHttpMethod.setText("DELETE");
                break;
            case CONTEXTMENU_HEAD:
                mEditTextHttpMethod.setText("HEAD");
                break;
            case CONTEXTMENU_TRACE:
                mEditTextHttpMethod.setText("TRACE");
                break;
            case CONTEXTMENU_OPTIONS:
                mEditTextHttpMethod.setText("OPTIONS");
                break;
            case CONTEXTMENU_CONNECT:
                mEditTextHttpMethod.setText("CONNECT");
                break;
            // Interval
            case CONTEXTMENU_5:
                displayPattern("5");
                break;
            case CONTEXTMENU_15:
                displayPattern("15");
                break;
            case CONTEXTMENU_30:
                displayPattern("30");
                break;
            case CONTEXTMENU_60:
                displayPattern("60");
                break;
            case CONTEXTMENU_300:
                displayPattern("300");
                break;
            case CONTEXTMENU_900:
                displayPattern("900");
                break;
            case CONTEXTMENU_1800:
                displayPattern("1800");
                break;
            case CONTEXTMENU_3600:
                displayPattern("3600");
            default:
                return false;
        }
        return true;
    }

    protected static final int CONTEXTMENU_5 = 99;
    protected static final int CONTEXTMENU_15 = 100;
    protected static final int CONTEXTMENU_30 = 101;
    protected static final int CONTEXTMENU_60 = 102;
    protected static final int CONTEXTMENU_300 = 103;
    protected static final int CONTEXTMENU_900 = 104;
    protected static final int CONTEXTMENU_1800 = 105;
    protected static final int CONTEXTMENU_3600 = 106;
    protected static final int CONTEXTMENU_86400 = 107;

    private OnCreateContextMenuListener mIntervalContextMenuHandler = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            Log.v(Tabs.MAIN_TAB.name(), "txtIntervalContextMenuHandler.onContextItemSelected()");
            menu.setHeaderTitle("Choose pattern");
            menu.clear();
            menu.add(0, CONTEXTMENU_5, CONTEXTMENU_5, "5 sec");
            menu.add(0, CONTEXTMENU_15, CONTEXTMENU_15, "15 sec");
            menu.add(0, CONTEXTMENU_30, CONTEXTMENU_30, "30 sec");
            menu.add(0, CONTEXTMENU_60, CONTEXTMENU_60, "1 min");
            menu.add(0, CONTEXTMENU_300, CONTEXTMENU_300, "5 min");
            menu.add(0, CONTEXTMENU_900, CONTEXTMENU_900, "15 min");
            menu.add(0, CONTEXTMENU_1800, CONTEXTMENU_1800, "30 min");
            menu.add(0, CONTEXTMENU_3600, CONTEXTMENU_3600, "1 hour");
            menu.add(0, CONTEXTMENU_86400, CONTEXTMENU_86400, "24 hours");
            /* Add as many context-menu-options as you want to. */
        }
    };

    private void loadTestSuiteFromSd(String testSuiteName, boolean showToast) {
        Log.d(Tabs.MAIN_TAB.name(), "loadTestSuiteFromSd(): " + testSuiteName);
        boolean resultOk = Z7FileUtils.loadTestSuiteFromSd(testSuiteName);
        if (showToast) {
            if (resultOk) {
                SharedPrefs.saveLastTestSuiteFromSd(testSuiteName);
                Toast.makeText(getApplicationContext(), "Test suite '" + testSuiteName + "' loaded!",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Could not load test suite '" + testSuiteName + "'!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ProgressBar newProgressBar() {
        // Log.v(LOG, "newProgressBar()");
        return new ProgressBar(this, null, android.R.attr.progressBarStyle);
    }

    private TextWatcher mEditTextHttpMethodWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Prevent text change if this tab is not active
            if (RootTab.getCurrentTab() == Tabs.MAIN_TAB) {
                TestFactory.setHttpMethod(s.toString());
            }
        }
    };

    private TextWatcher mEditTextUriWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Prevent text change if this tab is not active
            if (RootTab.getCurrentTab() == Tabs.MAIN_TAB) {
                TestFactory.setUri(s.toString());
            }
        }
    };

    private TextWatcher mEditTextPatternWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Prevent text change if this tab is not active
            if (RootTab.getCurrentTab() == Tabs.MAIN_TAB) {
                TestFactory.setIntervalPattern(s.toString());
            }
        }
    };

    /**
     * MyOnClickListener.
     */
    private class MyOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (v instanceof Button) {
                Button button = (Button) v;
                if (TestJobType.get(button.getId()) != null) {
                    LOG.debug(TestJobType.get(button.getId()).toString());
                    if (MainService.getPackageVersionName() != null) {
                        LOG.info("Package version name: " + MainService.getPackageVersionName());
                    }
                }
                TestJobType tyType = TestJobType.get(button.getId());
                switch (tyType) {
                    case BUTTON_SINGLE_REQUEST:
                        TestJobStates.setJobState(tyType, TestJobState.IS_RUNNING);
                        sendTestJob(tyType);
                        break;
                    case BUTTON_PERIODIC_REQUESTS:
                        if (TestJobStates.getJobState(tyType) == TestJobState.IS_RUNNING) {
                            TestJobStates.setJobState(tyType, TestJobState.NOT_RUNNING);
                            TestFactory.removeTestJobs(tyType);
                        } else {
                            TestJobStates.setJobState(tyType, TestJobState.IS_RUNNING);
                            sendTestJob(tyType);
                        }
                        displayButtonPeriodicRequest();
                        break;
                    case BUTTON_LOOPED_REQUESTS:
                        if (TestJobStates.getJobState(tyType) == TestJobState.IS_RUNNING) {
                            TestJobStates.setJobState(tyType, TestJobState.NOT_RUNNING);
                            TestFactory.removeTestJobs(tyType);
                        } else {
                            TestJobStates.setJobState(tyType, TestJobState.IS_RUNNING);
                            sendTestJob(tyType);
                        }
                        displayButtonLoopedRequest();
                        break;
                    case AUTOMATED_TEST:
                        if (TestJobStates.getJobState(getTestSuiteName()) == TestJobState.IS_RUNNING) {
                            TestJobStates.setJobState(getTestSuiteName(), TestJobState.NOT_RUNNING);
                            TestFactory.removeTestJobs(getTestSuiteName());
                        } else {
                            TestJobStates.setJobState(getTestSuiteName(), TestJobState.IS_RUNNING);
                            sendTestJob(getTestSuiteName(), tyType);
                        }
                        break;
                    case BUTTON_PREVIOUS_REQUEST:
                        TestFactory.moveToPreviousTest();
                        displayTestFactory();
                        break;
                    case BUTTON_NEXT_REQUEST:
                        TestFactory.moveToNextTest();
                        displayTestFactory();
                        break;
                    case BUTTON_RESET_REQUEST:
                        TestFactory.resetTests();
                        displayTestFactory();
                        break;
                    case BUTTON_PREVIOUS_PATTERN:
                        TestFactory.moveToPreviousInterval();
                        displayTestFactory();
                        break;
                    case BUTTON_NEXT_PATTERN:
                        TestFactory.moveToNextInterval();
                        displayTestFactory();
                        break;
                    case BUTTON_RESET_PATTERN:
                        TestFactory.resetIntervalPattern();
                        TestFactory.resetCounters();
                        displayTestFactory();
                        break;
                    case BUTTON_SHOW_REQUEST:
                        Intent intent = new Intent(getApplicationContext(), TestInfoActivity.class);
                        startActivityForResult(intent, 1);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    ;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        Log.v(Tabs.MAIN_TAB.name(), "onKeyDown()");
        // Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Ask the user if they want to quit
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("Exit");
            builder.setMessage("Save test suite?");
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Exit the activity
                    MainTab.this.finish();
                }
            });
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Exit the activity
                    saveAsTestDialog(true);
                }
            });
            mAlertDialogSaveTestBeforeExit = builder.create();
            mAlertDialogSaveTestBeforeExit.show();
            // Say that we've consumed the event
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.v(Tabs.MAIN_TAB.name(), "onCreateOptionsMenu()");
        // SubMenu toolsMenu = menu.addSubMenu("Tools");
        // SubMenu helpMenu = menu.addSubMenu("Help");
        MenuItem item7 = menu.add("itmSaveTest");
        item7.setTitle(R.string.itmSaveTest);
        item7.setOnMenuItemClickListener(mMenuItemSaveTestSuite);
        MenuItem item4 = menu.add("itmSaveTest2");
        item4.setTitle(R.string.itmSaveTest2);
        item4.setOnMenuItemClickListener(mMenuItemSaveAsTestSuite);
        MenuItem item6 = menu.add("itmRenameTest");
        item6.setTitle(R.string.itmRenameTest);
        item6.setOnMenuItemClickListener(mMenuItemRenameTestSuite);
        // MenuItem item3 = menu.add("itmShowTests");
        // item3.setTitle(R.string.itmShowTests);
        // item3.setOnMenuItemClickListener(mShowTestsFromSd);
        MenuItem item13 = menu.add("itmMore");
        item13.setTitle("More...");
        item13.setOnMenuItemClickListener(mMore);
        return true;
    }

    // private MenuItem.OnMenuItemClickListener mResetStat = new MenuItem.OnMenuItemClickListener() {
    // public boolean onMenuItemClick(MenuItem item) {
    // Log.v(LOG, "mResetStat.onMenuItemClick()");
    // return true;
    // }
    // };

    private MenuItem.OnMenuItemClickListener mMenuItemSaveTestSuite = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            prepareToSaveTestSuite(false);
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mMenuItemSaveAsTestSuite = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            saveAsTestDialog(false);
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mMenuItemRenameTestSuite = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            renameTestSuiteDialog();
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mMore = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            return showMoreOptionsDialog();
        }
    };

    public static final int OPTION_SHOW_TEST_FROM_SD = 0;

    private static final Map<Integer, String> MORE_OPTIONS;

    static {
        MORE_OPTIONS = new HashMap<Integer, String>();
        MORE_OPTIONS.put(OPTION_SHOW_TEST_FROM_SD, "Show test suites stored on SD card");
        // MORE_OPTIONS.put(OPTION_ZIP_AND_UPLOADLOGS, "Zip + Upload logs");
        // MORE_OPTIONS.put(OPTION_REBOOT_DEVICE, "Reboot device");
    }

    private boolean showMoreOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("More options");
        builder.setPositiveButton("OK", null);
        LayoutInflater li = LayoutInflater.from(this);
        View tere = li.inflate(R.layout.options, null);
        mListViewMoreOptions = (ListView) tere.findViewById(R.id.ListViewOptions);
        mListViewMoreOptions.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListViewMoreOptions.setOnItemClickListener(mMoreOptionsClickedHandler);
        mListViewMoreOptions.setAdapter(new OptionsAdapter(this));
        builder.setView(tere);
        mAlertDialogMoreOptions = builder.create();
        mAlertDialogMoreOptions.show();
        return true;
    }

    private OnItemClickListener mMoreOptionsClickedHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            Log.v(Tabs.MAIN_TAB.name(), "mMoreOptionsClickedHandler.onItemClick() Position: " + position);
            TextView tvOptions = (TextView) v.findViewById(R.id.list_content);
            if (position == OPTION_SHOW_TEST_FROM_SD) {
                Intent intent = new Intent(getApplicationContext(), SdTestsActivity.class);
                startActivityForResult(intent, 1);
            }
            if (mAlertDialogMoreOptions != null) {
                if (mAlertDialogMoreOptions.isShowing()) {
                    mAlertDialogMoreOptions.dismiss();
                }
            }
        }
    };

    /**
     * OptionsAdapter.
     */
    class OptionsAdapter extends ArrayAdapter<Object> {
        private Activity mContext;

        OptionsAdapter(Activity context) {
            super(context, R.layout.options, MORE_OPTIONS.values().toArray());
            this.mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // Log.v(LOG, "OptionsAdapter.getView()");
            LayoutInflater li = LayoutInflater.from(mContext);
            View row = li.inflate(R.layout.listitem, null);
            TextView tvOptions = (TextView) row.findViewById(R.id.list_content);
            tvOptions.setText(MORE_OPTIONS.get(position));
            if (position == OPTION_SHOW_TEST_FROM_SD) {
                tvOptions.setTextSize(16);
            }
            tvOptions.setTextColor(getResources().getColor(R.color.black));

            return (row);
        }
    }

    private void saveAsTestDialog(boolean exitApp) {
        // Log.v(LOG, "saveAsTestDialog()");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainTab.this);
        builder.setTitle("Save current test suite as...");
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.save, null);
        builder.setView(view);
        TextView lblTestName = (TextView) view.findViewById(R.id.lblTestName);
        lblTestName.setVisibility(View.GONE);
        mEditTextTestName = (EditText) view.findViewById(R.id.txtTestName);
        mEditTextTestName.setText(getTestSuiteName());
        // }
        if (exitApp) {
            builder.setPositiveButton("Save", mSaveAsTestSuiteAndQuit);
        } else {
            builder.setPositiveButton("Save", mSaveAsTestSuite);
        }
        builder.setNegativeButton("Cancel", mCancelSaveTest);
        mAlertDialogSaveRenameTest = builder.create();
        mAlertDialogSaveRenameTest.show();
    }

    private void prepareToSaveTestSuite(boolean exitApp) {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File dir = new File(sdcard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_TESTSUITE);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, getTestSuiteName() + Z7FileUtils.TESTSUITE_FILE_EXTENSION);
            if (Z7FileUtils.saveTestSuiteToSd(file, getTestSuiteName())) {
                Toast.makeText(getApplicationContext(), "Test suite '" + getTestSuiteName() + "' saved!",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Could not save suite '" + getTestSuiteName() + "'!",
                        Toast.LENGTH_SHORT).show();
            }
            displayTestSuiteName(TestFactory.getTestSuite().getName());
            if (mAlertDialogSaveRenameTest != null) {
                if (mAlertDialogSaveRenameTest.isShowing()) {
                    mAlertDialogSaveRenameTest.dismiss();
                }
            }
            if (exitApp) {
                MainTab.this.finish();
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Could not save!", Toast.LENGTH_SHORT).show();
        }
    }

    private Dialog.OnClickListener mSaveAsTestSuite = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            prepareToSaveAsTestSuite(false);
        }
    };

    private Dialog.OnClickListener mSaveAsTestSuiteAndQuit = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Log.v(LOG, "mSaveAsTestSuiteAndQuit.onClick()");
            prepareToSaveAsTestSuite(true);
        }
    };

    private void prepareToSaveAsTestSuite(boolean exitApp) {
        Log.v(Tabs.MAIN_TAB.name(), "prepareToSaveAsTestSuite()");
        if (!StringUtils.isEmpty(mEditTextTestName.getText().toString())) {
            try {
                File sdcard = Environment.getExternalStorageDirectory();
                File dir = new File(sdcard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_TESTSUITE);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, mEditTextTestName.getText().toString().replaceAll("\\n", StringUtils.EMPTY)
                        + Z7FileUtils.TESTSUITE_FILE_EXTENSION);
                // createNewFile returns false if file exists
                if (!file.createNewFile()
                        && mEditTextTestName.getText().toString().replaceAll("\\n", StringUtils.EMPTY)
                        .equalsIgnoreCase(getTestSuiteName())) {
                    testSuiteExistsDialog("save", exitApp, file,
                            mEditTextTestName.getText().toString().replaceAll("\\n", StringUtils.EMPTY), null);
                } else {
                    if (Z7FileUtils.saveTestSuiteToSd(file,
                            mEditTextTestName.getText().toString().replaceAll("\\n", StringUtils.EMPTY))) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Test suite '"
                                        + mEditTextTestName.getText().toString().replaceAll("\\n", StringUtils.EMPTY)
                                        + "' saved!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(
                                getApplicationContext(),
                                "Could not save suite '"
                                        + mEditTextTestName.getText().toString().replaceAll("\\n", StringUtils.EMPTY)
                                        + "'!", Toast.LENGTH_SHORT).show();
                    }
                    displayTestSuiteName(TestFactory.getTestSuite().getName());
                    if (mAlertDialogSaveRenameTest != null) {
                        if (mAlertDialogSaveRenameTest.isShowing()) {
                            mAlertDialogSaveRenameTest.dismiss();
                        }
                    }
                    if (exitApp) {
                        MainTab.this.finish();
                    }
                }
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "Could not save!", Toast.LENGTH_SHORT).show();
                if (mAlertDialogSaveRenameTest != null) {
                    if (mAlertDialogSaveRenameTest.isShowing()) {
                        mAlertDialogSaveRenameTest.dismiss();
                    }
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please enter some name!", Toast.LENGTH_LONG).show();
        }
    }

    private void renameTestSuiteDialog() {
        Log.v(Tabs.MAIN_TAB.name(), "renameTestSuiteDialog()");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainTab.this);
        builder.setTitle("Rename test suite to...");
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.save, null);
        builder.setView(view);
        TextView lblTestName = (TextView) view.findViewById(R.id.lblTestName);
        lblTestName.setVisibility(View.GONE);
        mEditTextTestName = (EditText) view.findViewById(R.id.txtTestName);
        mEditTextTestName.setText(getTestSuiteName());
        String uri = getHttpUri();
        if (uri.contains("http://")) {
            uri = uri.substring(("http://").length());
        }
        uri = uri.replace("/", "_");
        if (uri.lastIndexOf("_") == uri.length() - 1) {
            uri = uri.substring(0, uri.length() - 1);
        }
        mEditTextTestName.setText(uri);
        builder.setPositiveButton(R.string.btnRenameTest, mRenameTestPrepare);
        builder.setNegativeButton("Cancel", mCancelSaveTest);
        mAlertDialogSaveRenameTest = builder.create();
        mAlertDialogSaveRenameTest.show();
    }

    private void testSuiteExistsDialog(final String method, final boolean quit, final File file,
                                       final String newTestSuiteName, final String oldTestSuiteName) {
        Log.v(Tabs.MAIN_TAB.name(), "testSuiteExistsDialog()");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainTab.this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("Overwrite?");
        builder.setMessage("Test suite with same name already exists, overwrite it?");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Z7FileUtils.saveTestSuiteToSd(file, newTestSuiteName)) {
                    if (method.equalsIgnoreCase("save")) {
                        Toast.makeText(getApplicationContext(), "Test suite '" + newTestSuiteName + "' saved!",
                                Toast.LENGTH_SHORT).show();
                    }
                    if (method.equalsIgnoreCase("rename")) {
                        Toast.makeText(getApplicationContext(),
                                "Test suite '" + oldTestSuiteName + "' renamed to '" + newTestSuiteName + "'!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (method.equalsIgnoreCase("save")) {
                        Toast.makeText(getApplicationContext(), "Could not save suite '" + newTestSuiteName + "'!",
                                Toast.LENGTH_SHORT).show();
                    }
                    if (method.equalsIgnoreCase("rename")) {
                        Toast.makeText(getApplicationContext(), "Could not rename suite '" + newTestSuiteName + "'!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                displayTestSuiteName(newTestSuiteName);
                if (mAlertDialogSaveRenameTest != null) {
                    if (mAlertDialogSaveRenameTest.isShowing()) {
                        mAlertDialogSaveRenameTest.dismiss();
                    }
                }
                if (quit) {
                    MainTab.this.finish();
                }
            }
        });
        mAlertDialogTestExists = builder.create();
        mAlertDialogTestExists.show();
    }

    private Dialog.OnClickListener mRenameTestPrepare = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            prepareToRenameTestSuite();
        }
    };

    private void prepareToRenameTestSuite() {
        Log.v(Tabs.MAIN_TAB.name(), "prepareToRenameTestSuite()");
        if (mEditTextTestName.getText().length() != 0) {
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_TESTSUITE);
                if (dir.exists()) {
                    File oldFile = new File(dir, getTestSuiteName() + Z7FileUtils.TESTSUITE_FILE_EXTENSION);
                    File newFile = new File(dir, mEditTextTestName.getText() + Z7FileUtils.TESTSUITE_FILE_EXTENSION);
                    if (oldFile.exists()) {
                        if (!newFile.exists()
                                || mEditTextTestName.getText().toString().equalsIgnoreCase(getTestSuiteName())) {
                            if (oldFile.renameTo(newFile)) {
                                if (Z7FileUtils.saveTestSuiteToSd(newFile, mEditTextTestName.getText().toString()
                                        .replaceAll("\\n", StringUtils.EMPTY))) {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Test suite '"
                                                    + getTestSuiteName()
                                                    + "' renamed to '"
                                                    + mEditTextTestName.getText().toString()
                                                    .replaceAll("\\n", StringUtils.EMPTY) + "'!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Could not rename test suite '" + getTestSuiteName() + "'!",
                                            Toast.LENGTH_SHORT).show();
                                }
                                displayTestSuiteName(TestFactory.getTestSuite().getName());
                                if (mAlertDialogSaveRenameTest != null) {
                                    if (mAlertDialogSaveRenameTest.isShowing()) {
                                        mAlertDialogSaveRenameTest.dismiss();
                                    }
                                }
                            }
                        } else {
                            testSuiteExistsDialog("rename", false, newFile, mEditTextTestName.getText().toString()
                                    .replaceAll("\\n", StringUtils.EMPTY), getTestSuiteName());
                        }
                    } else {
                        newFile.createNewFile();
                        if (Z7FileUtils.saveTestSuiteToSd(newFile,
                                mEditTextTestName.getText().toString().replaceAll("\\n", StringUtils.EMPTY))) {
                            Toast.makeText(getApplicationContext(), "Renamed!", Toast.LENGTH_SHORT).show();
                        }
                        if (mAlertDialogSaveRenameTest != null) {
                            if (mAlertDialogSaveRenameTest.isShowing()) {
                                mAlertDialogSaveRenameTest.dismiss();
                            }
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Could not rename!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "Could not rename!", Toast.LENGTH_SHORT).show();
                if (mAlertDialogSaveRenameTest != null) {
                    if (mAlertDialogSaveRenameTest.isShowing()) {
                        mAlertDialogSaveRenameTest.dismiss();
                    }
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please enter some name!", Toast.LENGTH_LONG).show();
        }
    }

    private Dialog.OnClickListener mCancelSaveTest = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mAlertDialogSaveRenameTest != null) {
                if (mAlertDialogSaveRenameTest.isShowing()) {
                    mAlertDialogSaveRenameTest.dismiss();
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Log.v(LOG, "onActivityResult()");
        if (resultCode == RESULT_OK && requestCode == 1) {
            String msg = data.getStringExtra("loadTestSuiteFromSd");
            if (!StringUtils.isEmpty(msg)) {
                loadTestSuiteFromSd(msg, true);
            }
        }
    }

    private void sendTestJob(TestJobType testJobType) {
        // Log.v(LOG, "sendTestJob()");
        try {

            TestJob newTestJob = new TestJob(testJobType);
            TestFactory.addTestJob(newTestJob);

            displayTestFactory();

        } catch (Exception e) {
            Log.e(Tabs.MAIN_TAB.name(), "sendTestJob(): " + e.getMessage());
        }
    }

    private void sendTestJob(String testSuiteName, TestJobType testJobType) {
        // Log.v(LOG, "sendTestJob()");
        try {

            TestJob newTestJob = new TestJob(testSuiteName, testJobType);
            TestFactory.addTestJob(newTestJob);

            displayTestFactory();

        } catch (Exception e) {
            Log.e(Tabs.MAIN_TAB.name(), "sendTestJob(): " + e.getMessage());
        }
    }
}
