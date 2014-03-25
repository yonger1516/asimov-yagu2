package com.seven.asimov.test.tool.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.core.*;
import com.seven.asimov.test.tool.core.testjobs.TestJob;
import com.seven.asimov.test.tool.core.testjobs.TestJobToast;
import com.seven.asimov.test.tool.core.testjobs.TestJobType;
import com.seven.asimov.test.tool.preferences.SharedPrefs;
import com.seven.asimov.test.tool.serialization.TestSuite;
import com.seven.asimov.test.tool.zxing.IntentIntegrator;
import com.seven.asimov.test.tool.zxing.IntentResult;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * TestsTab Activity.
 */

public class TestsTab extends Activity {

    private static final String LOG = "TestsTab";

    public static final String ACTION_DISPLAY = "com.seven.asimov.test.tool.intent.action.DISPLAY.TestsTab";

    // Controls
    private AlertDialog mAlertDialogLoadTests;
    private AlertDialog mAlertDialogInstallZxing;
    private AlertDialog mAlertDialogChangeTestSuiteHost;
    private AlertDialog mAlertDialogMoreOptions;

    private EditText mEditTextBelongsTo;
    private EditText mEditTextTestName;
    private EditText mEditTextTestSuiteHost;
    private EditText mEditTextScope;

    private ListView mListViewTests;
    private ListView mListViewScopes;
    private ListView mListViewMoreOptions;

    private ProgressDialog mProgressDialogConnectionProgress;

    private Activity mActivity;

    private static int sFirstVisibleItem;
    private static int sTop = 0;

    private static TestSuiteMap sTestSuiteMap = new TestSuiteMap();

    public static TestSuiteMap getTestSuiteList() {
        return sTestSuiteMap;
    }

    private static List<String> sScopesList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Log.v(LOG, "onCreate()");

        setContentView(R.layout.tests);
        mListViewTests = (ListView) findViewById(R.id.lvTests);
        // mListViewScopes = (ListView) findViewById(R.id.lvTests);
        mListViewTests.setOnItemClickListener(mOnItemClickListenerTests);
        mListViewTests.setOnCreateContextMenuListener(mOnCreateContextMenuListenerTests);
        // mListViewScopes.setOnItemClickListener(mOnItemClickListenerScopes);
        // mListViewScopes.setOnCreateContextMenuListener(mOnCreateContextMenuListenerScopes);
    }

    private void displayBelongsTo(String belongsTo) {
        // Log.v(Tag, "displayBelongsTo()");
        mEditTextBelongsTo.setText(belongsTo);
    }

    private String getBelongsTo() {
        // Log.v(Tag, "getBelongsTo()");
        return mEditTextBelongsTo.getText().toString().replaceAll("\\n", StringUtils.EMPTY);
    }

    private void displayScope(String scope) {
        // Log.v(Tag, "displayScope()");
        mEditTextScope.setText(scope);
    }

    public String getScope() {
        // Log.v(Tag, "getScope()");
        return mEditTextScope.getText().toString().replaceAll("\\n", StringUtils.EMPTY);
    }

    private void displayTestName(String test) {
        // Log.v(Tag, "displayLastTestFromServer()");
        mEditTextTestName.setText(test);
    }

    public String getTestName() {
        // Log.v(Tag, "getLastTestFromServer()");
        return mEditTextTestName.getText().toString().replaceAll("\\n", StringUtils.EMPTY);
    }

    private void refreshTestSuites() {
        try {
            ArrayList<TestSuite> tempList = new ArrayList<TestSuite>();
            for (int i = 0; i < sTestSuiteMap.values().size(); i++) {
                TestSuite ts = sTestSuiteMap.get(i);
                tempList.add(ts);
            }

            Collections.sort(tempList, new Comparator<TestSuite>() {
                public int compare(TestSuite ts1, TestSuite ts2) {
                    return ts1.getName().toLowerCase().compareTo(ts2.getName().toLowerCase());
                }
            });

            sTestSuiteMap.clear();
            for (int i = 0; i < tempList.size(); i++) {
                sTestSuiteMap.put(i, tempList.get(i));
            }

            mListViewTests.setAdapter(new TestsAdapter(this));
            mListViewTests.setSelectionFromTop(sFirstVisibleItem, sTop);
        } catch (Exception e) {
            Log.e(LOG, e.getMessage(), e);
        }
    }

    /**
     * TestsAdapter.
     */
    class TestsAdapter extends ArrayAdapter<Object> {
        private Activity mContext;

        TestsAdapter(Activity context) {
            super(context, R.layout.options, sTestSuiteMap.values().toArray());
            this.mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // Log.v(LOG, "TestsAdapter.getView()");
            LayoutInflater li = LayoutInflater.from(mContext);
            View row = li.inflate(R.layout.listitem2, null);
            CheckedTextView ctv = (CheckedTextView) row.findViewById(R.id.list_content2);
            ctv.setText(sTestSuiteMap.get(position).getName());
            ctv.setChecked(sTestSuiteMap.get(position).isChekedForExecution());
            return (row);
        }
    }

    private void refreshScopes(List<String> items) {
        try {
            if (items != null) {
                sScopesList.clear();
                for (int i = 0; i < items.size(); i++) {
                    sScopesList.add(i, items.get(i));
                }
            }
            Collections.sort(sScopesList);
            mListViewScopes
                    .setAdapter(new ArrayAdapter<String>(this, R.layout.listitem, R.id.list_content, sScopesList));
            mListViewScopes.setSelection(sFirstVisibleItem);
        } catch (Exception exc) {
        }
    }

    private OnItemClickListener mOnItemClickListenerTests = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

            CheckedTextView ctvRequestOptions = (CheckedTextView) v.findViewById(R.id.list_content2);
            ctvRequestOptions.setChecked(!ctvRequestOptions.isChecked());
            sTestSuiteMap.get(position).setChekedForExecution(ctvRequestOptions.isChecked());

            loadTest(null, sTestSuiteMap.get(position).getName());
        }
    };

    private OnItemClickListener mOnItemClickListenerScopes = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            // loadTest(mListViewScopes.getItemAtPosition(position).toString());
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG, "onPause()");

        RootTab.setLastPausedTab(Tabs.TESTS_TAB);

        unregisterReceiver(mUiReceiver);

        markSelection();

        if (mAlertDialogLoadTests != null) {
            if (mAlertDialogLoadTests.isShowing()) {
                mAlertDialogLoadTests.dismiss();
            }
        }
        if (mAlertDialogInstallZxing != null) {
            if (mAlertDialogInstallZxing.isShowing()) {
                mAlertDialogInstallZxing.dismiss();
            }
        }
        if (mProgressDialogConnectionProgress != null) {
            if (mProgressDialogConnectionProgress.isShowing()) {
                mProgressDialogConnectionProgress.dismiss();
            }
        }
        if (mAlertDialogChangeTestSuiteHost != null) {
            if (mAlertDialogChangeTestSuiteHost.isShowing()) {
                mAlertDialogChangeTestSuiteHost.dismiss();
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
        Log.v(LOG, "onResume()");

        // RootTab.getTabList().put(Tabs.TESTS_TAB, true);

        mActivity = this;

        refreshTestSuites();

        registerReceiver(mUiReceiver, new IntentFilter(ACTION_DISPLAY));
    }

    protected static final int CONTEXTMENU_LOADITEM = 0;
    // protected static final int CONTEXTMENU_DELETEITEM = 1;
    private OnCreateContextMenuListener mOnCreateContextMenuListenerTests = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(sTestSuiteMap.get(((AdapterContextMenuInfo) menuInfo).position).getName());
            menu.add(0, CONTEXTMENU_LOADITEM, 0, "Load test suite");
            // menu.add(0, CONTEXTMENU_DELETEITEM, 1, "Delete test");
            /* Add as many context-menu-options as you want to. */
        }
    };

    private OnCreateContextMenuListener mOnCreateContextMenuListenerScopes = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(mListViewScopes.getAdapter().getItem(((AdapterContextMenuInfo) menuInfo).position)
                    .toString());
            menu.add(0, CONTEXTMENU_LOADITEM, 0, "Show test suites from this scope");
            // menu.add(0, CONTEXTMENU_DELETEITEM, 1, "Delete test");
            /* Add as many context-menu-options as you want to. */
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem aItem) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
        /* Switch on the ID of the item, to get what the user selected. */
        switch (aItem.getItemId()) {
            case CONTEXTMENU_LOADITEM:
                loadTest(null, sTestSuiteMap.get(menuInfo.position).getName());
                break;
            // case CONTEXTMENU_DELETEITEM:
            // /* Get the selected item out of the Adapter by its position. */
            // final String name = lvTests.getAdapter().getItem(menuInfo.position)
            // .toString();
            // AlertDialog.Builder builder = new AlertDialog.Builder(
            // TestsTabView.this);
            // builder.setMessage(
            // "Are you sure you want to delete `" + name + "` test?")
            // .setCancelable(true)
            // .setPositiveButton("Yes",
            // new DialogInterface.OnClickListener() {
            // public void onClick(DialogInterface dialog,
            // int id) {
            // // DeleteTest(name);
            // }
            // })
            // .setNegativeButton("No",
            // new DialogInterface.OnClickListener() {
            // public void onClick(DialogInterface dialog,
            // int id) {
            // dialog.cancel();
            // }
            // });
            // AlertDialog alert = builder.create();
            // alert.show();
            // break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.add("itmLoadTest");
        item.setTitle(R.string.itmLoadTest);
        item.setOnMenuItemClickListener(mMenuItemLoadTest);

        item = menu.add("itmScan");
        item.setTitle("Scan");
        item.setOnMenuItemClickListener(mMenuItemScan);

        item = menu.add("itmLoadTests");
        item.setTitle(R.string.itmLoadTests);
        item.setOnMenuItemClickListener(mMenuItemShowTests);
        // item = menu.add("itmLoadScopes");
        // item.setTitle(R.string.itmLoadScopes);
        // item.setOnMenuItemClickListener(mMenuItemLoadScopes);
        MenuItem item13 = menu.add("itmMore");
        item13.setTitle("More...");
        item13.setOnMenuItemClickListener(mMore);
        return true;
    }

    private MenuItem.OnMenuItemClickListener mMenuItemLoadTest = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            loadTestSuiteDialog();
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mMenuItemScan = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {

            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            final List<ResolveInfo> pkgAppsList = getApplicationContext().getPackageManager().queryIntentActivities(
                    mainIntent, 0);

            boolean zxingInstalled = false;

            for (ResolveInfo apk : pkgAppsList) {
                ApplicationInfo appInfo = apk.activityInfo.applicationInfo;
                String packageName = appInfo.packageName;
                if (packageName.equals("com.google.zxing.client.android")) {
                    zxingInstalled = true;
                    break;
                }
            }
            if (!zxingInstalled) {
                installZxingDialog();
            } else {
                IntentIntegrator integrator = new IntentIntegrator(mActivity);
                integrator.initiateScan();
            }

            return true;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            if (StringUtils.isNotEmpty(scanResult.getContents())) {
                Properties testSuiteProps = new Properties();
                try {
                    testSuiteProps.load(new ByteArrayInputStream(scanResult.getContents().getBytes()));
                    if (testSuiteProps.get("TestSuiteName") != null) {
                        loadTest(
                                (String) ((testSuiteProps.get("ServerName") != null) ? testSuiteProps.get("ServerName")
                                        : null), (String) testSuiteProps.get("TestSuiteName"));
                    }
                } catch (Exception e) {
                    Log.v(LOG, e.getMessage());
                }
            }
        }
        // else continue with any other code you need in the method
    }

    private MenuItem.OnMenuItemClickListener mMenuItemShowTests = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            showTestSuitesDialog();
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mMenuItemLoadScopes = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            loadScopesDialog();
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mMore = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            return showMoreOptionsDialog();
        }
    };

    public static final int OPTION_CHANGE_TEST_SUITE_HOST = 0;

    private static final Map<Integer, String> MORE_OPTIONS;

    static {
        MORE_OPTIONS = new HashMap<Integer, String>();
        MORE_OPTIONS.put(OPTION_CHANGE_TEST_SUITE_HOST, "Change test suite host");
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
            Log.v(LOG, "mMoreOptionsClickedHandler.onItemClick() Position: " + position);
            TextView tvOptions = (TextView) v.findViewById(R.id.list_content);
            if (position == OPTION_CHANGE_TEST_SUITE_HOST) {
                changeTestSuiteHostDialog();
            }
            // if (mAlertDialogMoreOptions != null) {
            // if (mAlertDialogMoreOptions.isShowing()) {
            // mAlertDialogMoreOptions.dismiss();
            // }
            // }
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
            tvOptions.setTextColor(getResources().getColor(R.color.black));

            return (row);
        }
    }

    private void loadTestSuiteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TestsTab.this);
        builder.setTitle("Load test suite from server...");
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.save, null);
        builder.setView(view);
        builder.setPositiveButton("Load", mDialogOnClickListenerLoadTest);
        builder.setNegativeButton("Cancel", mDialogOnClickListenerCancelLoadTest);
        TextView lblloadTests = (TextView) view.findViewById(R.id.lblTestName);
        lblloadTests.setText("Enter test suite name:");
        mEditTextTestName = (EditText) view.findViewById(R.id.txtTestName);
        mEditTextTestName.setOnCreateContextMenuListener(mLoadTestSuiteOnCreateContextMenuListener);
        displayTestName(SharedPrefs.getLastTestSuiteFromServer());
        mAlertDialogLoadTests = builder.create();
        mAlertDialogLoadTests.show();
    }

    private OnCreateContextMenuListener mLoadTestSuiteOnCreateContextMenuListener = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Last test suites");
            menu.clear();

            ArrayList<String> latestTestSuiteList = SharedPrefs.getLastTestSuitesFromServer();

            if (latestTestSuiteList.size() == 0) {
                menu.add(0, 10, 10, "No last used test suites");
            } else {
                for (int i = 0; i < latestTestSuiteList.size(); i++) {
                    String testSuite = latestTestSuiteList.get(i);
                    menu.add(0, 100 - i, 100 - i, testSuite).setOnMenuItemClickListener(
                            mEditTextTestSuiteOnMenuItemClickListener);
                }
            }
        }
    };

    private OnMenuItemClickListener mEditTextTestSuiteOnMenuItemClickListener = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            mEditTextTestName.setText(item.toString());
            return true;
        }
    };

    private void showTestSuitesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TestsTab.this);
        builder.setTitle("Show test suites from server...");
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.save2, null);
        builder.setView(view);
        builder.setPositiveButton("Load", mDialogOnClickListenerLoadTests);
        builder.setNegativeButton("Cancel", mDialogOnClickListenerCancelLoadTests);
        TextView lblName = (TextView) view.findViewById(R.id.lblName);
        lblName.setText("Find test suites belong to (all if empty):");
        mEditTextBelongsTo = (EditText) view.findViewById(R.id.txtName);
        mEditTextTestName = (EditText) view.findViewById(R.id.txtName);
        displayBelongsTo(SharedPrefs.getLastBelongsTo());
        TextView lblName2 = (TextView) view.findViewById(R.id.lblName2);
        lblName2.setText("Find test suites from scope (all if empty):");
        mEditTextScope = (EditText) view.findViewById(R.id.txtName2);
        displayScope(SharedPrefs.getLastScope());
        mAlertDialogLoadTests = builder.create();
        mAlertDialogLoadTests.show();
    }

    private boolean installZxingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("7Test");
        builder.setMessage("Zxing bar scanner app should be installed!");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("Install", mDialognClickListenerInstallZxing);
        builder.setNegativeButton("Cancel", mDialognClickListenerCancelInstallZxing);
        mAlertDialogInstallZxing = builder.create();
        mAlertDialogInstallZxing.show();
        return true;
    }

    private void loadScopesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TestsTab.this);
        builder.setTitle("Show scopes from server...");
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.save2, null);
        builder.setView(view);
        builder.setPositiveButton("Load", mDialogOnClickListenerLoadScopes);
        builder.setNegativeButton("Cancel", mDialogOnClickListenerCancelLoadScopes);
        TextView lblName = (TextView) view.findViewById(R.id.lblName);
        lblName.setText("Find scopes belong to (all if empty):");
        mEditTextBelongsTo = (EditText) view.findViewById(R.id.txtName);
        displayBelongsTo(SharedPrefs.getLastBelongsTo());
        TextView lblName2 = (TextView) view.findViewById(R.id.lblName2);
        lblName2.setVisibility(View.GONE);
        mEditTextScope = (EditText) view.findViewById(R.id.txtName2);
        mEditTextScope.setVisibility(View.GONE);
        mAlertDialogLoadTests = builder.create();
        mAlertDialogLoadTests.show();
    }

    private OnMenuItemClickListener mEditTextTestSuiteHostOnMenuItemClickListener = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            mEditTextTestSuiteHost.setText(item.toString());
            return true;
        }
    };

    private boolean changeTestSuiteHostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change test suite host...");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View saveview = inflater.inflate(R.layout.save, null);
        builder.setView(saveview);
        mEditTextTestSuiteHost = (EditText) saveview.findViewById(R.id.txtTestName);
        mEditTextTestSuiteHost.setText(TestFactoryOptions.getsTestSuiteHost());
        mEditTextTestSuiteHost.setOnCreateContextMenuListener(mTestSuiteHostOnCreateContextMenuListener);
        builder.setPositiveButton("OK", mDialogChangeRelayHostOnClickListener);
        builder.setNegativeButton("Cancel", mDialogChangeRelayHostOnClickListener);
        mAlertDialogChangeTestSuiteHost = builder.create();
        mAlertDialogChangeTestSuiteHost.show();
        return true;
    }

    private Dialog.OnClickListener mDialogChangeRelayHostOnClickListener = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                TestFactoryOptions.setsTestSuiteHost(mEditTextTestSuiteHost.getText().toString());
            }
            mAlertDialogChangeTestSuiteHost.cancel();
        }
    };

    protected static final int CONTEXTMENU_10001 = 10001;
    protected static final int CONTEXTMENU_10002 = 10002;
    protected static final int CONTEXTMENU_10003 = 10003;
    protected static final int CONTEXTMENU_10004 = 10004;
    protected static final int CONTEXTMENU_10005 = 10005;
    private OnCreateContextMenuListener mTestSuiteHostOnCreateContextMenuListener = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Choose test suite host");
            menu.clear();
            menu.add(0, CONTEXTMENU_10001, CONTEXTMENU_10001, "tln-dev-testrunner1.7sys.eu")
                    .setOnMenuItemClickListener(mEditTextTestSuiteHostOnMenuItemClickListener);
            menu.add(0, CONTEXTMENU_10002, CONTEXTMENU_10002, "tln-qa-testrunner1.7sys.eu")
                    .setOnMenuItemClickListener(mEditTextTestSuiteHostOnMenuItemClickListener);
            menu.add(0, CONTEXTMENU_10003, CONTEXTMENU_10003, "tln-perf-testrunner1.7sys.eu")
                    .setOnMenuItemClickListener(mEditTextTestSuiteHostOnMenuItemClickListener);
            menu.add(0, CONTEXTMENU_10004, CONTEXTMENU_10004, "hki-dev-testrunner1.7sys.eu")
                    .setOnMenuItemClickListener(mEditTextTestSuiteHostOnMenuItemClickListener);
            menu.add(0, CONTEXTMENU_10005, CONTEXTMENU_10005, "testruner.cybervisiontech.com.ua")
                    .setOnMenuItemClickListener(mEditTextTestSuiteHostOnMenuItemClickListener);
        }
    };

    private Dialog.OnClickListener mDialogOnClickListenerLoadTest = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            loadTest(null, getTestName());
        }
    };

    private Dialog.OnClickListener mDialogOnClickListenerLoadTests = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            SharedPrefs.saveLastBelongsTo(getBelongsTo());
            SharedPrefs.saveLastScope(getScope());
            loadTests();
        }
    };

    private Dialog.OnClickListener mDialogOnClickListenerLoadScopes = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            SharedPrefs.saveLastBelongsTo(getBelongsTo());
            loadScopes();
        }
    };

    private Dialog.OnClickListener mDialogOnClickListenerCancelLoadTest = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            SharedPrefs.saveLastTestSuiteFromServer(getTestName());
            mAlertDialogLoadTests.cancel();
        }
    };

    private Dialog.OnClickListener mDialognClickListenerInstallZxing = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
                    .parse("market://details?id=com.google.zxing.client.android"));
            startActivity(goToMarket);
        }
    };

    private Dialog.OnClickListener mDialognClickListenerCancelInstallZxing = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mAlertDialogInstallZxing.cancel();
        }
    };

    private Dialog.OnClickListener mDialogOnClickListenerCancelLoadTests = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            SharedPrefs.saveLastBelongsTo(getBelongsTo());
            SharedPrefs.saveLastScope(getScope());
            mAlertDialogLoadTests.cancel();
        }
    };

    private Dialog.OnClickListener mDialogOnClickListenerCancelLoadScopes = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            SharedPrefs.saveLastBelongsTo(getBelongsTo());
            mAlertDialogLoadTests.cancel();
        }
    };

    private void markSelection() {
        sFirstVisibleItem = mListViewTests.getFirstVisiblePosition();
        View v = mListViewTests.getChildAt(0);
        sTop = (v == null) ? 0 : v.getTop();
    }

    private ProgressDialog newProgressDialog() {

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setTitle("Please wait!");
        dialog.setMessage("Requesting server...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(true);
        // reset the bar to the default value of 0
        dialog.setProgress(0);

        return dialog;
    }

    private void loadTest(String testSuiteHost, String testSuiteName) {

        if (StringUtils.isNotEmpty(testSuiteHost)) {
            if (testSuiteHost.equalsIgnoreCase("localhost")) {
                testSuiteHost = null;
            }
        }

        SharedPrefs.saveLastTestSuiteFromServer(testSuiteName);

        sendDirectServiceRequest(((testSuiteHost != null) ? testSuiteHost : TestFactoryOptions.getsTestSuiteHost())
                + "/loadTestSuite?name=" + testSuiteName, TestJobType.SERVICE_REQUEST_LOADTEST);
        mProgressDialogConnectionProgress = newProgressDialog();
        mProgressDialogConnectionProgress.show();
    }

    private void loadTests() {
        sendDirectServiceRequest(
                TestFactoryOptions.getsTestSuiteHost() + "/loadTestSuites?belongs=" + SharedPrefs.getLastBelongsTo()
                        + "&scope=" + SharedPrefs.getLastScope(), TestJobType.SERVICE_REQUEST_LOADTESTS);
        mProgressDialogConnectionProgress = newProgressDialog();
        mProgressDialogConnectionProgress.show();
    }

    private void loadScopes() {
        // sendDirectServiceRequest(sTestSuiteHost + "/loadScopes?belongs=" + SharedPrefs.getLastBelongsTo(),
        // TestJobId.SERVICE_REQUEST_LOADSCOPES);
        // mProgressDialogConnectionProgress = ProgressDialog
        // .show(this, "Please Wait", "Requesting server...", true, true);
    }

    private void sendDirectServiceRequest(String uri, TestJobType testJobType) {
        try {
            TestJob testJob = new TestJob(testJobType);
            Request request = new Request();
            request.initService(uri);
            testJob.setRequest(request);
            PipelineFactory.sendServiceRequest(testJob, getApplicationContext());
        } catch (Exception e) {
            Log.e(LOG, e.getMessage());
        }
    }

    private BroadcastReceiver mUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            Set<String> keys = extras.keySet();
            for (String key : keys) {
                if (key.equals(Pipeline.MESSAGE)) {
                    Log.v(LOG, Pipeline.MESSAGE);
                    String toast = null;
                    TestJobToast testJobToast = (TestJobToast) extras.get("Toast");
                    switch (testJobToast.getTestJobType()) {
                        default:
                            break;
                        case SERVICE_REQUEST_LOADTEST:

                            if (testJobToast.getMessage() != null) {
                                String[] message = testJobToast.getMessage().split(":", 2);
                                if (message.length == 2) {
                                    if (message[0].equals("Max")) {
                                        mProgressDialogConnectionProgress.setIndeterminate(false);
                                        mProgressDialogConnectionProgress.setMax(Integer.valueOf(message[1]));
                                    } else if (message[0].equals("Progress")) {
                                        mProgressDialogConnectionProgress.incrementProgressBy(Integer.valueOf(message[1]));
                                    } else if (message[0].equals("Received")) {
                                        mProgressDialogConnectionProgress.setMessage("Applying...");
                                    }
                                }
                            } else if (testJobToast.getHttpStatus() != null) {
                                if (testJobToast.getHttpStatus().contains(Constants.HTTP_STATUS_STRING_200)) {
                                    if (TestFactory.isFreshTestSuiteLoadedFromServer()) {
                                        toast = "Test suite '" + TestFactory.getTestSuite().getName() + "' loaded! It has "
                                                + TestFactory.getTestSuite().getTestItems().size() + " item(s).";
                                        TestFactory.setFreshTestSuiteLoadedFromServer(false);
                                        break;
                                    }
                                    toast = "Could not load test suite '" + SharedPrefs.getLastTestSuiteFromServer() + "'!";
                                    break;
                                }

                            }
                        case SERVICE_REQUEST_LOADTESTS:

                            if (testJobToast.getMessage() != null) {
                                String[] message = testJobToast.getMessage().split(":", 2);
                                if (message.length == 2) {
                                    if (message[0].equals("Max")) {
                                        mProgressDialogConnectionProgress.setIndeterminate(false);
                                        mProgressDialogConnectionProgress.setMax(Integer.valueOf(message[1]));
                                    } else if (message[0].equals("Progress")) {
                                        mProgressDialogConnectionProgress.incrementProgressBy(Integer.valueOf(message[1]));
                                    } else if (message[0].equals("Received")) {
                                        mProgressDialogConnectionProgress.setMessage("Applying...");
                                    }
                                }
                            } else if (testJobToast.getHttpStatus() != null) {
                                if (testJobToast.getHttpStatus().contains(Constants.HTTP_STATUS_STRING_200)) {
                                    refreshTestSuites();
                                    toast = "Got " + sTestSuiteMap.size() + " test suite(s).";
                                }
                            }
                    }

                    if (testJobToast.getHttpStatus() != null || testJobToast.getError() != null) {
                        if (toast == null) {
                            toast = "["
                                    + testJobToast.getConnection()
                                    + "] "
                                    + ((testJobToast.getHttpStatus() != null) ? testJobToast.getHttpStatus()
                                    : StringUtils.EMPTY)
                                    + ((testJobToast.getConnectionStatus() != null) ? testJobToast
                                    .getConnectionStatus() : StringUtils.EMPTY)
                                    + ((testJobToast.getError() != null) ? testJobToast.getError() : StringUtils.EMPTY);
                        }
                    }

                    if (toast != null) {

                        if (mProgressDialogConnectionProgress != null) {
                            mProgressDialogConnectionProgress.dismiss();
                        }

                        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
                    }

                    break;
                }
            }
        }
    };
}
