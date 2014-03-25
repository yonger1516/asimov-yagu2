package com.seven.asimov.test.tool.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.core.HttpsOptions;
import com.seven.asimov.test.tool.core.TestFactory;
import com.seven.asimov.test.tool.core.TestFactoryOptions;
import com.seven.asimov.test.tool.serialization.TestItem;
import com.seven.asimov.test.tool.utils.Z7HttpsUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * TestInfoActivity Activity.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class TestInfoActivity extends Activity {

    private static final Logger LOG = LoggerFactory.getLogger(TestInfoActivity.class.getSimpleName());

    private AlertDialog mAlertDialogTestProxy;
    private AlertDialog mAlertDialogRequestOptions;
    private AlertDialog mAlertDialogSaveTestBeforeExit;

    private EditText mEditTextTestInfo;
    private EditText mEditTextTestProxy;
    private EditText mEditTextTestProxyPort;

    private ListView mListViewTestOptions;
    private ListView mListViewTestHttpsProtocol;
    private ListView mListViewTestHttpsCiphers;

    private static String sLoadedRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testinfo);
        mEditTextTestInfo = (EditText) findViewById(R.id.EditTextTestInfo);
    }

    private void displayTestProxy(String proxy) {
        mEditTextTestProxy.setText(proxy);
    }

    private void displayTestProxyPort(Integer proxyPort) {
        if (proxyPort != null) {
            mEditTextTestProxyPort.setText(proxyPort.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LOG.debug("onPause()");

        if (mAlertDialogTestProxy != null) {
            if (mAlertDialogTestProxy.isShowing()) {
                mAlertDialogTestProxy.dismiss();
            }
        }

        if (mAlertDialogRequestOptions != null) {
            if (mAlertDialogRequestOptions.isShowing()) {
                mAlertDialogRequestOptions.dismiss();
            }
        }

        if (mAlertDialogSaveTestBeforeExit != null) {
            if (mAlertDialogSaveTestBeforeExit.isShowing()) {
                mAlertDialogSaveTestBeforeExit.dismiss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOG.debug("onResume()");

        TestItem test = TestFactory.getCurrentTest();
        sLoadedRequest = test.getRequestHeaders() + Constants.HTTP_DOUBLE_NEW_LINE
                + ((test.getRequestContent() != null) ? test.getRequestContent() : StringUtils.EMPTY);
        mEditTextTestInfo.setText(sLoadedRequest);
    }

    private static String sNewRequest;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        LOG.debug("onKeyDown()");
        // Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            sNewRequest = mEditTextTestInfo.getText().toString();
            sNewRequest = sNewRequest.replace(Constants.HTTP_NEW_LINE, Constants.HTTP_NEW_LINE_CHROME);
            sNewRequest = sNewRequest.replace(Constants.HTTP_NEW_LINE_CHROME, Constants.HTTP_NEW_LINE);
            // EditText issue
            if (!sNewRequest.contains(Constants.HTTP_DOUBLE_NEW_LINE)) {
                sNewRequest = sNewRequest.replace("\n\r\n", Constants.HTTP_DOUBLE_NEW_LINE);
            }
            String[] parts = sNewRequest.split(Constants.HTTP_DOUBLE_NEW_LINE, -1);
            if (parts.length == 2) {
                String headers = parts[0];
                String body = parts[1].replace(Constants.HTTP_NEW_LINE, Constants.HTTP_NEW_LINE_CHROME);
                sNewRequest = headers + Constants.HTTP_DOUBLE_NEW_LINE + body;
            }
            if (!sLoadedRequest.equals(sNewRequest)) {
                // Ask the user if they want to quit
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setTitle("Return to main tab");
                builder.setMessage("Request has changed, overwrite?");
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit to main activity
                        backToMainTab();
                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Save test and exit to main activity
                        int index = sNewRequest.indexOf(Constants.HTTP_DOUBLE_NEW_LINE);
                        if (index != -1) {
                            String newRequestHeaders = sNewRequest.substring(0,
                                    sNewRequest.indexOf(Constants.HTTP_DOUBLE_NEW_LINE));
                            TestFactory.setRequestHeaders(newRequestHeaders);
                            String newnewRequestContent = sNewRequest.substring(sNewRequest
                                    .indexOf(Constants.HTTP_DOUBLE_NEW_LINE) + Constants.HTTP_DOUBLE_NEW_LINE.length());
                            TestFactory.setRequestContent(newnewRequestContent);
                        }
                        backToMainTab();
                    }
                });
                mAlertDialogSaveTestBeforeExit = builder.create();
                mAlertDialogSaveTestBeforeExit.show();
            }
            // Say that we've consumed the event
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        LOG.debug("onCreateOptionsMenu()");

        MenuItem item1 = menu.add("itmOptions");
        item1.setTitle("Options");
        item1.setOnMenuItemClickListener(mMenuItemTestOptions);
        MenuItem item2 = menu.add("itmProxy");
        item2.setTitle("Proxy");
        item2.setOnMenuItemClickListener(mMenuItemTestProxy);

        MenuItem item3 = menu.add("itmHttpsProtocol");
        item3.setTitle("HTTPS Protocol");
        item3.setOnMenuItemClickListener(mMenuItemTestHttpsProtocol);
        MenuItem item4 = menu.add("itmHttpsCiphers");
        item4.setTitle("HTTPS Ciphers");
        item4.setOnMenuItemClickListener(mMenuItemTestHttpsCiphers);

        return true;
    }

    private MenuItem.OnMenuItemClickListener mMenuItemTestOptions = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            showTestOptionsDialog();
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mMenuItemTestProxy = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            showTestProxyDialog();
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mMenuItemTestHttpsProtocol = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            showTestHttpsProtocolDialog();
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mMenuItemTestHttpsCiphers = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            showTestHttpsCiphersDialog();
            return true;
        }
    };

    private void showTestOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request options");
        builder.setPositiveButton("OK", null);
        LayoutInflater li = LayoutInflater.from(this);
        View tere = li.inflate(R.layout.options, null);
        mListViewTestOptions = (ListView) tere.findViewById(R.id.ListViewOptions);
        mListViewTestOptions.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListViewTestOptions.setOnItemClickListener(mRequestOptionsClickedHandler);
        mListViewTestOptions.setAdapter(new OptionsAdapter(this));
        builder.setView(tere);
        mAlertDialogRequestOptions = builder.create();
        mAlertDialogRequestOptions.show();
    }

    private void showTestProxyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request proxy...");
        builder.setPositiveButton("OK", null);
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.save2, null);
        builder.setView(view);
        builder.setPositiveButton("Save", mDialogOnClickListenerSaveTestProxy);
        builder.setNegativeButton("Cancel", mDialogOnClickListenerSaveTestProxy);
        TextView lblName = (TextView) view.findViewById(R.id.lblName);
        lblName.setText("Host:");
        mEditTextTestProxy = (EditText) view.findViewById(R.id.txtName);
        mEditTextTestProxy.setOnCreateContextMenuListener(mTestProxyHostContextMenuHandler);
        displayTestProxy(TestFactory.getCurrentTest().getProxy());
        TextView lblName2 = (TextView) view.findViewById(R.id.lblName2);
        lblName2.setText("Port (default - 80):");
        mEditTextTestProxyPort = (EditText) view.findViewById(R.id.txtName2);
        mEditTextTestProxyPort.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mEditTextTestProxyPort.setInputType(InputType.TYPE_CLASS_NUMBER);
        displayTestProxyPort(TestFactory.getCurrentTest().getProxyPort());
        mAlertDialogTestProxy = builder.create();
        mAlertDialogTestProxy.show();
    }

    private void showTestHttpsProtocolDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("HTTPS protocols").setCancelable(true)
                .setPositiveButton("OK", mRequestHttpsProtocolSaveHandler)
                .setNegativeButton("Cancel", null);
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.https_protocol, null);
        mListViewTestHttpsProtocol = (ListView) view.findViewById(R.id.lvRequestHttpsProtocol);
        mListViewTestHttpsProtocol.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListViewTestHttpsProtocol.setOnItemClickListener(mRequestHttpsProtocolClickedHandler);
        mListViewTestHttpsProtocol.setAdapter(new HttpsProtocolOptionsAdapter(this));
        builder.setView(view);
        mAlertDialogRequestOptions = builder.create();
        mAlertDialogRequestOptions.show();
    }

    private void showTestHttpsCiphersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("HTTPS ciphers").setCancelable(true)
                .setPositiveButton("OK", mRequestHttpsCiphersSaveHandler)
                .setNeutralButton("Reset", mRequestHttpsCiphersResetHandler)
                .setNegativeButton("Cancel", null);
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.https_ciphers, null);
        mListViewTestHttpsCiphers = (ListView) view.findViewById(R.id.lvRequestHttpsCiphers);
        mListViewTestHttpsCiphers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListViewTestHttpsCiphers.setOnItemClickListener(mRequestHttpsCiphersClickedHandler);
        mListViewTestHttpsCiphers.setAdapter(new HttpsCiphersOptionsAdapter(this));
        builder.setView(view);
        mAlertDialogRequestOptions = builder.create();
        mAlertDialogRequestOptions.show();
    }

    protected static final int CONTEXTMENU_PROXY_0 = 0;
    protected static final int CONTEXTMENU_PROXY_1 = 1;

    private OnCreateContextMenuListener mTestProxyHostContextMenuHandler = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            // Log.v(LOG, "txtMethodContextMenuHandler.onCreateContextMenu()");
            menu.setHeaderTitle("Choose proxy host");
            menu.clear();
            menu.add(0, CONTEXTMENU_PROXY_0, CONTEXTMENU_PROXY_0, "No proxy").setOnMenuItemClickListener(
                    mEditTextTestProxyOnMenuItemClickListener);
            menu.add(0, CONTEXTMENU_PROXY_1, CONTEXTMENU_PROXY_1, "rel10700.seven.com:7888")
                    .setOnMenuItemClickListener(mEditTextTestProxyOnMenuItemClickListener);
            /* Add as many context-menu-options as you want to. */
        }
    };

    private OnMenuItemClickListener mEditTextTestProxyOnMenuItemClickListener = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            // Proxy hosts
            switch (item.getItemId()) {
                default:
                    mEditTextTestProxy.setText(item.toString());
                    break;
                case CONTEXTMENU_PROXY_0:
                    mEditTextTestProxy.setText(StringUtils.EMPTY);
                    mEditTextTestProxyPort.setText(StringUtils.EMPTY);
                    break;
                case CONTEXTMENU_PROXY_1:
                    mEditTextTestProxy.setText("rel10700.seven.com");
                    mEditTextTestProxyPort.setText("7888");
                    break;
            }
            return true;
        }
    };

    private Dialog.OnClickListener mDialogOnClickListenerSaveTestProxy = new Dialog.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                TestFactory.setProxy(mEditTextTestProxy.getText().toString());
                if (StringUtils.isNotEmpty(mEditTextTestProxyPort.getText().toString())) {
                    TestFactory.setProxyPort(Integer.valueOf(mEditTextTestProxyPort.getText().toString()));
                }
                Toast.makeText(getApplicationContext(), "Proxy saved!", Toast.LENGTH_SHORT).show();
            }
            mAlertDialogTestProxy.cancel();
        }
    };

    public static final int REQUEST_OPTION_KEEP_ALIVE = 0;
    public static final int REQUEST_OPTION_EXPECT_CONTINUE = 1;
    public static final int REQUEST_OPTION_CHUNKED = 2;
    public static final int REQUEST_OPTION_HANDLE_REDIRECT = 3;
    public static final int REQUEST_OPTION_ACCEPT_GZIP = 4;
    public static final int REQUEST_OPTION_ACCEPT_DEFLATE = 5;
    public static final int REQUEST_OPTION_ACCEPT_COMPRESS = 6;

    private static final Map<Integer, String> REQUEST_OPTIONS;

    static {
        REQUEST_OPTIONS = new HashMap<Integer, String>();
        REQUEST_OPTIONS.put(REQUEST_OPTION_KEEP_ALIVE, "Keep-alive");
        REQUEST_OPTIONS.put(REQUEST_OPTION_EXPECT_CONTINUE, "Expect-Continue");
        REQUEST_OPTIONS.put(REQUEST_OPTION_CHUNKED, "Chunked");
        REQUEST_OPTIONS.put(REQUEST_OPTION_HANDLE_REDIRECT, "Handle redirect");
        REQUEST_OPTIONS.put(REQUEST_OPTION_ACCEPT_GZIP, "Accept-Encoding: gzip");
        REQUEST_OPTIONS.put(REQUEST_OPTION_ACCEPT_DEFLATE, "Accept-Encoding: deflate");
        REQUEST_OPTIONS.put(REQUEST_OPTION_ACCEPT_COMPRESS, "Accept-Encoding: compress");
    }

    private OnItemClickListener mRequestOptionsClickedHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            LOG.debug("mRequestOptionsClickedHandler.onItemClick() Position: %d", position);
            CheckedTextView ctvRequestOptions = (CheckedTextView) v.findViewById(R.id.list_content2);
            if (position == REQUEST_OPTION_KEEP_ALIVE) {
                TestFactoryOptions.setRequestKeepAlive(!TestFactoryOptions.isRequestKeepAlive());
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestKeepAlive());
            }
            if (position == REQUEST_OPTION_EXPECT_CONTINUE) {
                TestFactoryOptions.setRequestExpectContinue(!TestFactoryOptions.isRequestExpectContinue());
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestExpectContinue());
            }
            if (position == REQUEST_OPTION_CHUNKED) {
                TestFactoryOptions.setRequestChunked(!TestFactoryOptions.isRequestChunked());
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestChunked());
            }
            if (position == REQUEST_OPTION_HANDLE_REDIRECT) {
                TestFactoryOptions.setHandleRedirect(!TestFactoryOptions.isHandleRedirect());
                ctvRequestOptions.setChecked(TestFactoryOptions.isHandleRedirect());
            }
            if (position == REQUEST_OPTION_ACCEPT_GZIP) {
                TestFactoryOptions.setRequestAcceptGzip(!TestFactoryOptions.isRequestAcceptGzip());
                if (TestFactoryOptions.isRequestAcceptGzip()) {
                    TestFactoryOptions.setRequestAcceptDeflate(false);
                    TestFactoryOptions.setRequestAcceptCompress(false);
                }
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestAcceptGzip());
            }
            if (position == REQUEST_OPTION_ACCEPT_DEFLATE) {
                TestFactoryOptions.setRequestAcceptDeflate(!TestFactoryOptions.isRequestAcceptDeflate());
                if (TestFactoryOptions.isRequestAcceptDeflate()) {
                    TestFactoryOptions.setRequestAcceptGzip(false);
                    TestFactoryOptions.setRequestAcceptCompress(false);
                }
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestAcceptDeflate());
            }
            if (position == REQUEST_OPTION_ACCEPT_COMPRESS) {
                TestFactoryOptions.setRequestAcceptCompress(!TestFactoryOptions.isRequestAcceptCompress());
                if (TestFactoryOptions.isRequestAcceptCompress()) {
                    TestFactoryOptions.setRequestAcceptGzip(false);
                    TestFactoryOptions.setRequestAcceptDeflate(false);
                }
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestAcceptCompress());
            }
        }
    };

    /**
     * OptionsAdapter.
     */
    class OptionsAdapter extends ArrayAdapter<Object> {
        private Activity mContext;

        OptionsAdapter(Activity context) {
            super(context, R.layout.options, REQUEST_OPTIONS.values().toArray());
            this.mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // Log.v(LOG, "OptionsAdapter.getView()");
            LayoutInflater li = LayoutInflater.from(mContext);
            View row = li.inflate(R.layout.listitem2, null);
            CheckedTextView ctvRequestOptions = (CheckedTextView) row.findViewById(R.id.list_content2);
            ctvRequestOptions.setText(REQUEST_OPTIONS.get(position));
            ctvRequestOptions.setTextColor(getResources().getColor(R.color.black));

            if (position == REQUEST_OPTION_KEEP_ALIVE) {
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestKeepAlive());
            }
            if (position == REQUEST_OPTION_EXPECT_CONTINUE) {
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestExpectContinue());
            }
            if (position == REQUEST_OPTION_CHUNKED) {
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestChunked());
            }
            if (position == REQUEST_OPTION_HANDLE_REDIRECT) {
                ctvRequestOptions.setChecked(TestFactoryOptions.isHandleRedirect());
            }
            if (position == REQUEST_OPTION_ACCEPT_GZIP) {
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestAcceptGzip());
            }
            if (position == REQUEST_OPTION_ACCEPT_DEFLATE) {
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestAcceptDeflate());
            }
            if (position == REQUEST_OPTION_ACCEPT_COMPRESS) {
                ctvRequestOptions.setChecked(TestFactoryOptions.isRequestAcceptCompress());
            }
            if (TestFactory.getHttpMethod().equals("POST") || TestFactory.getHttpMethod().equals("PUT")) {
                return (row);
            } else {
                if (position == REQUEST_OPTION_EXPECT_CONTINUE) {
                    ctvRequestOptions.setFocusable(false);
                    ctvRequestOptions.setClickable(false);
                    ctvRequestOptions.setEnabled(false);
                    ctvRequestOptions.setTextColor(getResources().getColor(R.color.dark_grey));
                    row.setFocusable(false);
                    row.setClickable(false);
                    row.setEnabled(false);
                    return (row);
                }
                if (position == REQUEST_OPTION_CHUNKED) {
                    ctvRequestOptions.setFocusable(false);
                    ctvRequestOptions.setClickable(false);
                    ctvRequestOptions.setEnabled(false);
                    ctvRequestOptions.setTextColor(getResources().getColor(R.color.dark_grey));
                    row.setFocusable(false);
                    row.setClickable(false);
                    row.setEnabled(false);
                    return (row);
                }
            }
            return (row);
        }
    }

    class HttpsProtocolOptionsAdapter extends ArrayAdapter<Object> {
        private Activity mContext;

        HttpsProtocolOptionsAdapter(Activity context) {
            super(context, R.layout.https_protocol, Z7HttpsUtil.getSupportedProtocols());
            mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String[] supportedProtocols = Z7HttpsUtil.getSupportedProtocols();
            LayoutInflater li = LayoutInflater.from(mContext);
            View row = li.inflate(R.layout.listitem2, null);
            CheckedTextView ctvRequestHttpsProtocol = (CheckedTextView) row.findViewById(R.id.list_content2);
            ctvRequestHttpsProtocol.setText(supportedProtocols[position]);
            ctvRequestHttpsProtocol.setTextColor(getResources().getColor(R.color.black));
            ctvRequestHttpsProtocol.setChecked(HttpsOptions.isProtocolEnabled(supportedProtocols[position]));
            return (row);
        }
    }

    private OnItemClickListener mRequestHttpsProtocolClickedHandler = new OnItemClickListener() {
        private String[] mSupportedProtocols = Z7HttpsUtil.getSupportedProtocols();

        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            LOG.debug("mRequestHttpsProtocolClickedHandler.onItemClick() Position: %d", position);
            CheckedTextView ctvRequestHttpsProtocol = (CheckedTextView) v.findViewById(R.id.list_content2);

            String protocol = mSupportedProtocols[position];
            if (!ctvRequestHttpsProtocol.isChecked()) {
                LOG.debug("Protocol %s clicked - it was not checked, enabling...", protocol);
                HttpsOptions.addProtocols(protocol);
            } else {
                LOG.debug("Protocol %s clicked - it was checked, disabling...", protocol);
                HttpsOptions.removeProtocols(protocol);
            }
        }
    };

    class HttpsCiphersOptionsAdapter extends ArrayAdapter<Object> {
        private Activity mContext;

        HttpsCiphersOptionsAdapter(Activity context) {
            super(context, R.layout.https_ciphers, Z7HttpsUtil.getSupportedCiphers(HttpsOptions.getProtocolsAsArray()));
            this.mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String[] ciphers = Z7HttpsUtil.getSupportedCiphers(HttpsOptions.getProtocolsAsArray());

            LayoutInflater li = LayoutInflater.from(mContext);
            View row = li.inflate(R.layout.listitem2, null);
            CheckedTextView ctvRequestHttpsCiphers = (CheckedTextView) row.findViewById(R.id.list_content2);
            ctvRequestHttpsCiphers.setText(ciphers[position]);
            ctvRequestHttpsCiphers.setTextColor(getResources().getColor(R.color.black));

            boolean selected = false;
            for (String chosen : HttpsOptions.getCiphersAsSet(HttpsOptions.getProtocolsAsArray())) {
                if (chosen.equals(ciphers[position])) {
                    selected = true;
                    break;
                }
            }
            ctvRequestHttpsCiphers.setChecked(selected);

            return (row);
        }
    }

    private OnItemClickListener mRequestHttpsCiphersClickedHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            String[] protocols = HttpsOptions.getProtocolsAsArray();
            String[] ciphers = Z7HttpsUtil.getSupportedCiphers(protocols);

            LOG.debug("mRequestHttpsProtocolClickedHandler.onItemClick() Position: %d", position);
            CheckedTextView ctvRequestHttpsCiphers = (CheckedTextView) v.findViewById(R.id.list_content2);

            boolean exists = ctvRequestHttpsCiphers.isChecked();
            if (exists) {
                LOG.debug("Cipher unclicked, removing: %s", ciphers[position]);
                for (String p : protocols) {
                    HttpsOptions.removeCiphers(p, ciphers[position]);
                }
            } else {
                LOG.debug("Cipher clicked, adding: %s", ciphers[position]);
                for (String p : protocols) {
                    HttpsOptions.addCiphers(p, ciphers[position]);
                }
            }
            ctvRequestHttpsCiphers.setChecked(!exists);
        }
    };

    private OnClickListener mRequestHttpsProtocolSaveHandler = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            HttpsOptions.persistProtocol();
        }
    };

    private OnClickListener mRequestHttpsCiphersSaveHandler = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            HttpsOptions.persistCiphers();
        }
    };

    private OnClickListener mRequestHttpsCiphersResetHandler = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            for (String p : HttpsOptions.getProtocolsAsArray()) {
                HttpsOptions.setCiphers(p, Z7HttpsUtil.getDefaultCiphers(p));
            }
        }
    };

    private void backToMainTab() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("loadTestSuite", TestFactory.getTestSuite().getName());
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
