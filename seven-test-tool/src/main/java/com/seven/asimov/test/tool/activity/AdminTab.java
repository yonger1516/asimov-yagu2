package com.seven.asimov.test.tool.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.core.Pipeline;
import com.seven.asimov.test.tool.core.PipelineFactory;
import com.seven.asimov.test.tool.core.Request;
import com.seven.asimov.test.tool.core.testjobs.TestJob;
import com.seven.asimov.test.tool.core.testjobs.TestJobToast;
import com.seven.asimov.test.tool.core.testjobs.TestJobType;
import com.seven.asimov.test.tool.utils.Z7LoggerUtil;
import com.seven.asimov.test.tool.utils.Z7ShellUtil;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * AdminTab Activity.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class AdminTab extends Activity {

    private static final String LOG = "AdminTab";

    public static final String ACTION_DISPLAY = "com.seven.asimov.test.tool.intent.action.DISPLAY.AdminTab";

    // Controls
    private AlertDialog mAlertDialogChangeProxyHost;
    private AlertDialog mAlertDialogChangeRelayHost;
    private AlertDialog mAlertDialogMoreOptions;

    private Button mButtonScreenOn;
    private Button mButtonScreenOff;
    private Button mButtonTcpDump;
    private Button mButtonLogCat;
    private Button mButtonTestDatabase;
    private Button mButtonTestDatabase2;
    private Button mButtonTest;

    private CheckBox mCheckBoxWifi;
    private CheckBox mCheckBoxAirplane;

    private EditText mEditTextRelayHost;
    private EditText mEditTextProxyHost;

    private ImageButton mImageButtonRefreshActivePipelines;

    private ListView mListViewActivePipelines;
    private ListView mListViewMoreOptions;

    private TextView mTextViewScreenOnOff;
    private TextView mTextViewTcpDumpOnOff;
    private TextView mTextViewLogCatOnOff;

    // private static boolean sProcess1IsRunning;
    private static String sProxyHost = "rel10700.seven.com";
    private static String sRelayHost = "rel10700.seven.com";

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG, "onCreate()");
        setContentView(R.layout.admintabview);
        // Initiate controls
        mButtonScreenOn = (Button) findViewById(R.id.ButtonScreenOn);
        mButtonScreenOn.setOnClickListener(new MyOnClickListener());
        mButtonScreenOff = (Button) findViewById(R.id.ButtonScreenOff);
        mButtonScreenOff.setOnClickListener(new MyOnClickListener());
        mButtonTcpDump = (Button) findViewById(R.id.ButtonTcpDump);
        mButtonTcpDump.setOnClickListener(new MyOnClickListener());
        mButtonLogCat = (Button) findViewById(R.id.ButtonLogCat);
        mButtonLogCat.setOnClickListener(new MyOnClickListener());
        mButtonTestDatabase = (Button) findViewById(R.id.btnTestDatabase);
        mButtonTestDatabase.setOnClickListener(new MyOnClickListener());
        mButtonTestDatabase2 = (Button) findViewById(R.id.btnTestDatabase2);
        mButtonTestDatabase2.setOnClickListener(new MyOnClickListener());
        mButtonTest = (Button) findViewById(R.id.btnTest);
        mButtonTest.setOnClickListener(new MyOnClickListener());

        mCheckBoxWifi = (CheckBox) findViewById(R.id.chbxWiFi);
        mCheckBoxAirplane = (CheckBox) findViewById(R.id.chbxAirplane);

        mImageButtonRefreshActivePipelines = (ImageButton) findViewById(R.id.ImageButtonRefreshActivePipelines);
        mImageButtonRefreshActivePipelines.setOnClickListener(new MyOnClickListener());

        mListViewActivePipelines = (ListView) findViewById(R.id.ListViewActivePipelines);
        mListViewActivePipelines.setOnCreateContextMenuListener(mContextMenuActivePipelines);

        mTextViewScreenOnOff = (TextView) findViewById(R.id.TextViewScreenOnOff);
        mTextViewTcpDumpOnOff = (TextView) findViewById(R.id.TextViewTcpDumpOnOff);
        mTextViewTcpDumpOnOff.setOnLongClickListener(new MyOnLongClickListener());
        mTextViewLogCatOnOff = (TextView) findViewById(R.id.TextViewLogCatOnOff);
        mTextViewLogCatOnOff.setOnLongClickListener(new MyOnLongClickListener());
        // Other
        // mDataHelper = new DataHelper(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG, "onPause()");

        RootTab.setLastPausedTab(Tabs.ADMIN_TAB);

        unregisterReceiver(mUiReceiver);

        sFirstVisibleItem = mListViewActivePipelines.getFirstVisiblePosition();

        if (mAlertDialogChangeProxyHost != null) {
            if (mAlertDialogChangeProxyHost.isShowing()) {
                mAlertDialogChangeProxyHost.dismiss();
            }
        }
        if (mAlertDialogChangeRelayHost != null) {
            if (mAlertDialogChangeRelayHost.isShowing()) {
                mAlertDialogChangeRelayHost.dismiss();
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

        // RootTab.getTabList().put(Tabs.ADMIN_TAB, true);

        // Get Wifi services
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        mCheckBoxWifi.setChecked(wifiManager.isWifiEnabled());
        mCheckBoxWifi.setOnCheckedChangeListener(mCheckBoxWifiOnCheckedChangeListener);
        // Get Airplane mode state
        mCheckBoxAirplane
                .setChecked(Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1);
        mCheckBoxAirplane.setOnCheckedChangeListener(mCheckBoxAirplaneOnCheckedChangeListener);

        registerReceiver(mUiReceiver, new IntentFilter(ACTION_DISPLAY));

        displayScreenOnOff();

        displayTcpDumpOnOff();

        displayLogCatOnOff();

        showActivePipelines();
    }

    private OnCheckedChangeListener mCheckBoxWifiOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                mCheckBoxWifi.setChecked(true);
            } else {
                wifiManager.setWifiEnabled(false);
                mCheckBoxWifi.setChecked(false);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item10 = menu.add("itmInvalidateAll");
        item10.setTitle(R.string.itmInvalidateAll);
        item10.setOnMenuItemClickListener(mInvalidateAll);
        MenuItem item11 = menu.add("itmChangeRelay");
        item11.setTitle(R.string.itmChangeRelay);
        item11.setOnMenuItemClickListener(mChangeRelay);
        MenuItem item12 = menu.add("itmChangeProxy");
        item12.setTitle(R.string.ChangeProxy);
        item12.setOnMenuItemClickListener(mChangeProxy);
        MenuItem item13 = menu.add("itmMore");
        item13.setTitle("More...");
        item13.setOnMenuItemClickListener(mMore);
        return true;
    }

    private MenuItem.OnMenuItemClickListener mInvalidateAll = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            sendDirectProxyServiceRequest("http://localhost/oc/invalidate",
                    TestJobType.SERVICE_PROXY_REQUEST_INVALIDATEALL);
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mChangeRelay = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            return changeRelayHostDialog();
        }
    };

    private MenuItem.OnMenuItemClickListener mChangeProxy = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            return changeProxyHostDialog();
        }
    };

    private MenuItem.OnMenuItemClickListener mMore = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            return showMoreOptionsDialog();
        }
    };

    protected static final int CONTEXTMENU_CLOSE_PIPELINE = 0;
    // protected static final int CONTEXTMENU_DELETEITEM = 1;
    private OnCreateContextMenuListener mContextMenuActivePipelines = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(mListViewActivePipelines.getAdapter()
                    .getItem(((AdapterContextMenuInfo) menuInfo).position).toString());
            menu.add(0, CONTEXTMENU_CLOSE_PIPELINE, 0, "Close pipeline");
            // menu.add(0, CONTEXTMENU_DELETEITEM, 1, "Delete test");
            /* Add as many context-menu-options as you want to. */
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem aItem) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
        /* Switch on the ID of the item, to get what the user selected. */
        switch (aItem.getItemId()) {
            case CONTEXTMENU_CLOSE_PIPELINE:
                closePipeline(mListViewActivePipelines.getAdapter().getItem(menuInfo.position).toString());
                break;
            default:
                return false;
        }
        return true;
    }

    private void sendDirectProxyServiceRequest(String uri, TestJobType testJobType) {
        try {
            TestJob testJob = new TestJob(testJobType);
            Request request = new Request();
            request.initProxyService(uri);
            testJob.setRequest(request);
            PipelineFactory.sendServiceRequest(testJob, getApplicationContext());
        } catch (Exception e) {
            Log.e(LOG, e.getMessage());
        }
    }

    private void closePipeline(String name) {
        ArrayList<Pipeline> tempItems = PipelineFactory.getActivePipelines();
        for (Pipeline pipeline : tempItems) {
            if (pipeline.getName().equals(name)) {
                try {
                    pipeline.setShutDown(true);
                    if (pipeline.getSocket().isConnected()) {
                        pipeline.getSocket().close();
                    }
                } catch (Exception e) {
                    Log.e(LOG, "Could not shut down pipeline", e);
                }
            }
        }
        showActivePipelines();
    }

    public static final int OPTION_ZIP_LOGS = 0;
    public static final int OPTION_ZIP_AND_UPLOADLOGS = 1;
    public static final int OPTION_REBOOT_DEVICE = 2;

    private static final Map<Integer, String> MORE_OPTIONS;

    static {
        MORE_OPTIONS = new HashMap<Integer, String>();
        MORE_OPTIONS.put(OPTION_ZIP_LOGS, "Zip logs");
        MORE_OPTIONS.put(OPTION_ZIP_AND_UPLOADLOGS, "Zip + Upload logs");
        MORE_OPTIONS.put(OPTION_REBOOT_DEVICE, "Reboot device");
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

    /**
     * ZipLogsTask AsyncTask.
     *
     * @author Maksim Selivanov (mselivanov@seven.com)
     */
    private class ZipLogsTask extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... params) {

            for (Boolean param : params) {
                zipLogs(param);
            }

            return null;
        }
    }

    private OnItemClickListener mMoreOptionsClickedHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            Log.v(LOG, "mMoreOptionsClickedHandler.onItemClick() Position: " + position);
            TextView tvOptions = (TextView) v.findViewById(R.id.list_content);
            if (position == OPTION_ZIP_LOGS) {
                ZipLogsTask task = new ZipLogsTask();
                task.execute(new Boolean[]{false});
            }
            if (position == OPTION_ZIP_AND_UPLOADLOGS) {
                ZipLogsTask task = new ZipLogsTask();
                task.execute(new Boolean[]{true});
            }
            if (position == OPTION_REBOOT_DEVICE) {
                ShellUtil.rebootDevice();
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
            tvOptions.setTextColor(getResources().getColor(R.color.black));

            return (row);
        }
    }

    private void zipLogs(boolean uploadLogs) {
        Z7LoggerUtil.rollLogs(uploadLogs);
    }

    private static ArrayList<String> sActivePipelinesList = new ArrayList<String>();
    private static int sFirstVisibleItem;

    private void showActivePipelines() {
        try {
            sActivePipelinesList.clear();
            ArrayList<Pipeline> tempItems = PipelineFactory.getActivePipelines();
            if (tempItems.size() != 0) {
                for (Pipeline pipeline : tempItems) {
                    if (!pipeline.isShutDown()) {
                        sActivePipelinesList.add(pipeline.getName());
                    }
                }
                tempItems.clear();
                Collections.sort(sActivePipelinesList);
            }
            mListViewActivePipelines.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem, R.id.list_content,
                    sActivePipelinesList));
            mListViewActivePipelines.setSelection(sFirstVisibleItem);
        } catch (Exception e) {
        }
    }

    private boolean changeProxyHostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change proxy host...");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View saveview = inflater.inflate(R.layout.save, null);
        builder.setView(saveview);
        mEditTextProxyHost = (EditText) saveview.findViewById(R.id.txtTestName);
        mEditTextProxyHost.setText(sProxyHost);
        mEditTextProxyHost.setOnCreateContextMenuListener(mProxyHostOnCreateContextMenuListener);
        builder.setPositiveButton("OK", mDialogChangeProxyHostOnClickListener);
        builder.setNegativeButton("Cancel", mDialogChangeProxyHostOnClickListener);
        mAlertDialogChangeProxyHost = builder.create();
        mAlertDialogChangeProxyHost.show();
        return true;
    }

    private boolean changeRelayHostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change relay host...");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View saveview = inflater.inflate(R.layout.save, null);
        builder.setView(saveview);
        mEditTextRelayHost = (EditText) saveview.findViewById(R.id.txtTestName);
        mEditTextRelayHost.setText(sRelayHost);
        mEditTextRelayHost.setOnCreateContextMenuListener(mRelayHostOnCreateContextMenuListener);
        builder.setPositiveButton("OK", mDialogChangeRelayHostOnClickListener);
        builder.setNegativeButton("Cancel", mDialogChangeRelayHostOnClickListener);
        mAlertDialogChangeRelayHost = builder.create();
        mAlertDialogChangeRelayHost.show();
        return true;
    }

    protected static final int CONTEXTMENU_10001 = 10001;
    protected static final int CONTEXTMENU_10002 = 10002;
    protected static final int CONTEXTMENU_10003 = 10003;
    protected static final int CONTEXTMENU_10004 = 10004;
    protected static final int CONTEXTMENU_10005 = 10005;
    protected static final int CONTEXTMENU_10006 = 10006;
    protected static final int CONTEXTMENU_10007 = 10007;
    private OnCreateContextMenuListener mRelayHostOnCreateContextMenuListener = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Choose relay host");
            menu.clear();
            menu.add(0, CONTEXTMENU_10001, CONTEXTMENU_10001, "rel10700.seven.com").setOnMenuItemClickListener(
                    mEditTextRelayHostOnMenuItemClickListener);
            menu.add(0, CONTEXTMENU_10002, CONTEXTMENU_10002, "rel11600.seven.com").setOnMenuItemClickListener(
                    mEditTextRelayHostOnMenuItemClickListener);
            menu.add(0, CONTEXTMENU_10007, CONTEXTMENU_10007, "carrier11800.seven.com").setOnMenuItemClickListener(
                    mEditTextRelayHostOnMenuItemClickListener);
            menu.add(0, CONTEXTMENU_10003, CONTEXTMENU_10003, "demo14000.seven.com").setOnMenuItemClickListener(
                    mEditTextRelayHostOnMenuItemClickListener);
            menu.add(0, CONTEXTMENU_10004, CONTEXTMENU_10004, "tln-asimov-bal.eng.7networks.eu")
                    .setOnMenuItemClickListener(mEditTextRelayHostOnMenuItemClickListener);
            menu.add(0, CONTEXTMENU_10005, CONTEXTMENU_10005, "asimov-dev1").setOnMenuItemClickListener(
                    mEditTextRelayHostOnMenuItemClickListener);
            menu.add(0, CONTEXTMENU_10006, CONTEXTMENU_10006, "asimov-dev2").setOnMenuItemClickListener(
                    mEditTextRelayHostOnMenuItemClickListener);
        }
    };

    protected static final int CONTEXTMENU_20001 = 20001;
    protected static final int CONTEXTMENU_20002 = 20002;
    private OnCreateContextMenuListener mProxyHostOnCreateContextMenuListener = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Choose proxy host");
            menu.clear();
            menu.add(0, CONTEXTMENU_20001, CONTEXTMENU_20001, "rel10700.seven.com").setOnMenuItemClickListener(
                    mProxyHostCustomMenu);
            menu.add(0, CONTEXTMENU_20002, CONTEXTMENU_20002, "rel11600.seven.com").setOnMenuItemClickListener(
                    mProxyHostCustomMenu);
        }
    };

    private OnMenuItemClickListener mEditTextRelayHostOnMenuItemClickListener = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            // Relay hosts
            sRelayHost = item.toString();
            mEditTextRelayHost.setText(item.toString());
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mProxyHostCustomMenu = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            // Relay hosts
            sProxyHost = item.toString();
            mEditTextProxyHost.setText(item.toString());
            return true;
        }
    };

    private Dialog.OnClickListener mDialogChangeProxyHostOnClickListener = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                sProxyHost = mEditTextProxyHost.getText().toString();
                sendDirectProxyServiceRequest("http://localhost/oc/set?proxyhost=" + sProxyHost,
                        TestJobType.SERVICE_PROXY_REQUEST_CHANGEPROXY);
            }
            mAlertDialogChangeProxyHost.cancel();
        }
    };

    private Dialog.OnClickListener mDialogChangeRelayHostOnClickListener = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                sRelayHost = mEditTextRelayHost.getText().toString();
                sendDirectProxyServiceRequest("http://localhost/oc/set?relayhost=" + sRelayHost,
                        TestJobType.SERVICE_PROXY_REQUEST_CHANGERELAY);
            }
            mAlertDialogChangeRelayHost.cancel();
        }
    };

    private OnCheckedChangeListener mCheckBoxAirplaneOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // toggle airplane mode
            Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON,
                    Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1 ? 0 : 1);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state",
                    Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1 ? 0 : 1);
            sendBroadcast(intent);
        }
    };

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
                    toast = "["
                            + testJobToast.getConnection()
                            + "] "
                            + ((testJobToast.getHttpStatus() != null) ? testJobToast.getHttpStatus()
                            : StringUtils.EMPTY)
                            + ((testJobToast.getConnectionStatus() != null) ? testJobToast.getConnectionStatus()
                            : StringUtils.EMPTY)
                            + ((testJobToast.getError() != null) ? testJobToast.getError() : StringUtils.EMPTY);
                    Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
                    break;
                }
//                if (key.equals(ScreenOnOffReceiver.MESSAGE)) {
//                    displayScreenOnOff();
//                    break;
//                }
                if (key.equals(Z7LoggerUtil.MESSAGE)) {
                    String toast = (String) extras.get("Toast");
                    if (toast != null) {
                        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
        }
    };

    private void displayScreenOnOff() {
//        if (ScreenOnOffReceiver.isScreenOn()) {
//            mTextViewScreenOnOff.setTextColor(getResources().getColor(R.color.green));
//        } else {
//            mTextViewScreenOnOff.setTextColor(getResources().getColor(R.color.red));
//        }
    }

    private void displayTcpDumpOnOff() {
        if (Z7ShellUtil.isTcpDumpIsRunning()) {
            mButtonTcpDump.setText("Off");
            mTextViewTcpDumpOnOff.setTextColor(getResources().getColor(R.color.green));
        } else {
            mButtonTcpDump.setText("On");
            mTextViewTcpDumpOnOff.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private void displayLogCatOnOff() {
        if (Z7ShellUtil.isLogCatIsRunning()) {
            mButtonLogCat.setText("Off");
            mTextViewLogCatOnOff.setTextColor(getResources().getColor(R.color.green));
        } else {
            mButtonLogCat.setText("On");
            mTextViewLogCatOnOff.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private void loginToOauth2() {
        // Call the webbrowser with the Foursquare OAuth login URL
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://foursquare.com/oauth2/authenticate" + "?client_id="
                + "HRDA2BCXXHW4CNNW4G2JCOMYONEE1GFHT1QAWODYBJSE1EM4" + "&response_type=token"
                + "&redirect_uri=http://mselivanov.7sys.eu/oauthcallback"));
        startActivity(intent);
    }

    private static final AtomicInteger CHANNEL_COUNTER = new AtomicInteger(1);

    /**
     * MyOnClickListener.
     *
     * @author Maksim Selivanov (mselivanov@seven.com)
     */
    private class MyOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (v instanceof Button) {
                Button button = (Button) v;
                switch (button.getId()) {
                    case R.id.ButtonScreenOn:
                        try {

                            // ProxyManager mProxyMgr = new ProxyManager(getApplicationContext());
                            // mProxyMgr.init();
                            // mProxyMgr.startProxy();

                        } catch (Exception e) {
                            Log.e("", e.getMessage());
                        }
//                        ScreenOnOffReceiver.screenOn();
                        break;
                    case R.id.ButtonScreenOff:
//                        ScreenOnOffReceiver.screenOff();
                        break;
                    case R.id.ButtonTcpDump:
                        if (Z7ShellUtil.isTcpDumpIsRunning()) {
                            try {
                                Z7ShellUtil.killTcpDump();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (!Z7ShellUtil.isTcpDumpInstalled()) {
                                Z7ShellUtil.installTcpDump();
                            }
                            if (!Z7ShellUtil.isTcpDumpIsRunning()) {
                                Z7ShellUtil.launchTcpDump();
                            }
                        }
                        displayTcpDumpOnOff();
                        break;
                    case R.id.ButtonLogCat:
                        if (Z7ShellUtil.isLogCatIsRunning()) {
                            try {
                                Z7ShellUtil.killLogCat();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (!Z7ShellUtil.isLogCatIsRunning()) {
                                Z7ShellUtil.launchLogCat(getApplicationContext(), null);
                            }
                        }
                        displayLogCatOnOff();
                        break;
                    case R.id.btnTestDatabase:
                        try {
                            ShellUtil.rebootDevice();
                            // Log.d("Sqlite test", "Begin test");
                            // mDataHelper.deleteAll();
                            // Log.d("Sqlite test", "All entries deleted");
                            // int entries = 10000;
                            // for (int i = 0; i < entries; i++) {
                            // mDataHelper.insertStmt.bindString(1,
                            // RandomStringUtils.randomAlphabetic(20));
                            // mDataHelper.insertStmt.execute();
                            // }
                            // Log.d("Sqlite test", entries + " entries inserted");
                            // List<String> names = mDataHelper.selectAll();
                            // Log.d("Sqlite test", names.size() +
                            // " entries selected");
                            // // }
                            // Log.d("Sqlite test", "Test completed");
                            // mTextViewOutput.setText("Test completed");
                        } catch (Exception e) {
                        }
                        break;
                    case R.id.btnTestDatabase2:
                        try {
                            // Log.d("Sqlite test", "Begin test2");
                            // mDataHelper.deleteAll();
                            // Log.d("Sqlite test", "All entries deleted");
                            // int entries = 10000;
                            // mDataHelper.db.beginTransaction();
                            // for (int i = 0; i < entries; i++) {
                            // mDataHelper.stmtInsert.bindString(1,
                            // RandomStringUtils.random(50, "http://"));
                            // mDataHelper.stmtInsert.execute();
                            // }
                            // mDataHelper.stmtInsert.close();
                            // mDataHelper.db.setTransactionSuccessful();
                            // Log.d("Sqlite test", entries + " entries inserted");
                            // List<String> names = mDataHelper.selectAll();
                            // Log.d("Sqlite test", names.size() +
                            // " entries selected");
                        } finally {
                            // mDataHelper.db.endTransaction();
                            // Log.d("Sqlite test", "Test completed");
                            // mTextViewOutput.setText("Test completed");
                        }
                        break;
                    default:
                        break;
                }
            }

            if (v instanceof ImageButton) {
                ImageButton imageButton = (ImageButton) v;
                switch (imageButton.getId()) {
                    default:
                        break;
                    case R.id.ImageButtonRefreshActivePipelines:
                        showActivePipelines();
                        break;
                }
            }

        }
    }

    /**
     * MyOnLongClickListener.
     *
     * @author Maksim Selivanov (mselivanov@seven.com)
     */
    private class MyOnLongClickListener implements OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            if (v instanceof TextView) {
                TextView item = (TextView) v;
                switch (item.getId()) {
                    default:
                        break;
                    case R.id.TextViewTcpDumpOnOff:
                        Z7ShellUtil.copyPcapLogs();
                        Toast.makeText(getApplicationContext(), "Tcpdump logs rolled!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            return true;
        }
    }
}
