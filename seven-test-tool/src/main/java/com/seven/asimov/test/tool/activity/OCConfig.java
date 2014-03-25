package com.seven.asimov.test.tool.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.utils.BrandingLoaderUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCConfig extends Activity {

    private static final String TAG = OCConfig.class.getSimpleName();
    private EditText eBrandigName, eRelayHost, eRelayPort, eRedirHost, eRedirPort;
    private AlertDialog brandingDialog;
    private List<String> defaultBranding = new ArrayList<String>() {{
        add("c004_default");
        add("demo001_default");
        add("demo008_default");
        add("demo020_default");
        add("eng002_default");
        add("eng004_default");
    }};
    public static String currentBranding;

    final ArrayList<String> hosts = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.occonfig);
        eBrandigName = (EditText) findViewById(R.id.eBrandingName);
        eRelayHost = (EditText) findViewById(R.id.eRelayHost);
        eRelayPort = (EditText) findViewById(R.id.eRelayPort);
        eRedirHost = (EditText) findViewById(R.id.eRedirHost);
        eRedirPort = (EditText) findViewById(R.id.eRedirPort);
        readDefaultBranding();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem loadDefaultBranding = menu.add(R.string.addDefaultBranding);
        loadDefaultBranding.setTitle(R.string.addDefaultBranding);
        loadDefaultBranding.setOnMenuItemClickListener(lAddDefaultBranding);

        MenuItem saveBranding = menu.add(R.string.saveBranding);
        saveBranding.setTitle(R.string.saveBranding);
        saveBranding.setOnMenuItemClickListener(lSaveBranding);

        return true;
    }

    private void readDefaultBranding() {
        readDefaultBranding("eng002");
    }

    private void readDefaultBranding(String name) {
        AssetManager assetManager = this.getBaseContext().getAssets();
        InputStream in = null;
        try {
            in = assetManager.open("default/" + name + ".xml");
            Properties prop = new Properties();
            prop.loadFromXML(in);
            eBrandigName.setText(prop.getProperty("brandingName"));
            eRelayHost.setText(prop.getProperty("relayHost"));
            eRelayPort.setText(prop.getProperty("relayPort"));
            eRedirHost.setText(prop.getProperty("redirHost"));
            eRedirPort.setText(prop.getProperty("redirPort"));
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    DialogInterface.OnClickListener closeButtonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    private boolean selectDefaultBranding() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select branding");
        builder.setPositiveButton("OK", closeButtonListener);

        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.options, null);

        ListView listView = (ListView) view.findViewById(R.id.ListViewOptions);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(brandingSelector);
        listView.setAdapter(new OptionsAdapter(this));
        builder.setView(view);

        brandingDialog = builder.create();
        brandingDialog.show();
        return true;
    }

    private AdapterView.OnItemClickListener brandingSelector = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            currentBranding = (String) parent.getItemAtPosition(position);
            Toast.makeText(getApplicationContext(), "Current branding is: " + currentBranding, Toast.LENGTH_LONG).show();
            readDefaultBranding(currentBranding.substring(0, currentBranding.indexOf("_")));
            if (brandingDialog != null) {
                if (brandingDialog.isShowing()) {
                    brandingDialog.dismiss();
                }
            }
        }
    };

    private void saveNewBranding() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        if (!eBrandigName.getText().toString().equals("") && !eRelayHost.getText().toString().equals("") &&
                !eRelayPort.getText().toString().equals("") && !eRedirHost.getText().toString().equals("") &&
                !eRedirPort.getText().toString().equals("")) {
            Pattern p = Pattern.compile("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}");
            Matcher m = p.matcher(eRelayHost.getText().toString());
            if (!m.matches()) {
                if (eRelayHost.getText().toString().equals(eRedirHost.getText().toString()))
                    hosts.add(eRelayHost.getText().toString());
                else {
                    hosts.add(eRelayHost.getText().toString());
                    hosts.add(eRedirHost.getText().toString());
                }
                ExecutorService pool = Executors.newFixedThreadPool(1);
                CompletionService<Boolean> compService = new ExecutorCompletionService<Boolean>(pool);
                compService.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        InetAddress address;
                        for (int i = 0; i < hosts.size(); i++) {
                            try {
                                address = InetAddress.getByName(hosts.get(i));
                                hosts.set(i, address.toString().substring(address.toString().indexOf("/") + 1));
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                                hosts.set(i, null);
                                return false;
                            }
                        }
                        return true;
                    }
                });
                boolean temp = true;
                try {
                    Future<Boolean> future = compService.take();
                    temp = future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (temp)
                    createNewBranding();
                else {
                    hosts.clear();
                    dialog.setTitle("Save new branding");
                    dialog.setMessage("UnknownHostException! Please check the relay host and redirection host.");
                    dialog.setPositiveButton("OK", closeButtonListener);
                    dialog.show();
                }
            } else {
                if (eRelayHost.getText().toString().equals(eRedirHost.getText().toString()))
                    hosts.add(eRelayHost.getText().toString());
                else {
                    hosts.add(eRelayHost.getText().toString());
                    hosts.add(eRedirHost.getText().toString());
                }
                createNewBranding();
            }
        } else {
            hosts.clear();
            dialog.setTitle("Save new branding");
            dialog.setMessage("Please check that all the parameters have been set and their values are correct.");
            dialog.setPositiveButton("OK", closeButtonListener);
            dialog.show();
        }
    }

    private void createNewBranding() {
        BrandingLoaderUtil.Branding branding = new BrandingLoaderUtil.Branding();
        branding.setBrandingName(eBrandigName.getText().toString());
        if (hosts.size() == 1) {
            branding.setSystemRelayHost(hosts.get(0));
            branding.setClientRedirectionServer1Host(hosts.get(0));
        } else {
            branding.setSystemRelayHost(hosts.get(0));
            branding.setClientRedirectionServer1Host(hosts.get(1));
        }
        branding.setSystemClientRelayPort(Integer.parseInt(eRelayPort.getText().toString()));
        branding.setClientRedirectionServer1Port(Integer.parseInt(eRedirPort.getText().toString()));
        Log.i("OC Configuration", branding.toString());
        hosts.clear();
        boolean result = BrandingLoaderUtil.setNewBranding(branding);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Save new branding");
        dialog.setMessage(result ? "Branding successfully added." : "This branding already exists");
        dialog.setPositiveButton("OK", closeButtonListener);
        dialog.show();
    }

    private MenuItem.OnMenuItemClickListener lAddDefaultBranding = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return selectDefaultBranding();
        }
    };

    private MenuItem.OnMenuItemClickListener lSaveBranding = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            saveNewBranding();
            return true;
        }
    };

    class OptionsAdapter extends ArrayAdapter<Object> {
        private Activity mContext;

        OptionsAdapter(Activity context) {
            super(context, R.layout.options, defaultBranding.toArray());
            this.mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater li = LayoutInflater.from(mContext);
            View row = li.inflate(R.layout.listitem, null);
            TextView tvOptions = (TextView) row.findViewById(R.id.list_content);
            tvOptions.setText(defaultBranding.get(position));
            tvOptions.setTextColor(getResources().getColor(R.color.black));

            return (row);
        }
    }
}
