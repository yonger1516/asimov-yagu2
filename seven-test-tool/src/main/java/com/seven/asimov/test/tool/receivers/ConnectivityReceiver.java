package com.seven.asimov.test.tool.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import com.seven.asimov.test.tool.activity.MainTab;
import com.seven.asimov.test.tool.activity.RootTab;
import com.seven.asimov.test.tool.activity.Tabs;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ConnectivityReceiver BroadcastReceiver.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class ConnectivityReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectivityReceiver.class.getSimpleName());

    private static String sActiveNetworkType = StringUtils.EMPTY;
    private static String sActiveNetworkState = StringUtils.EMPTY;

    public static String getActiveNetworkType() {
        return sActiveNetworkType;
    }

    public static String getActiveNetworkState() {
        return sActiveNetworkState;
    }

    public static final String ACTION_GET_NETWORK_INFO = "com.seven.asimov.test.tool.intent.action.GET_NETWORK_INFO";

    private Context mContext;

    private static final int TYPE_UNKNOWN = -1;
    private Integer mActiveNetworkType = TYPE_UNKNOWN;
    private Integer mActiveNetworkSubType = TYPE_UNKNOWN;
    private State mActiveNetworkState;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;

        if (intent.getAction().equals(ACTION_GET_NETWORK_INFO)) {
            LOG.debug("ConnectivityReceiver.ACTION_GET_NETWORK_INFO");
            getNetworkInfo(null);
        } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            LOG.debug("ConnectivityManager.CONNECTIVITY_ACTION");
            getNetworkInfo(intent);
        } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            LOG.debug("WifiManager.NETWORK_STATE_CHANGED_ACTION");
            getWifiNetworkInfo(intent);
        }
    }

    private void getNetworkInfo(Intent intent) {
        NetworkInfo networkInfo = null;
        if (intent != null) {
            networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        } else {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = cm.getActiveNetworkInfo();
        }
        if (networkInfo == null) {
            LOG.info("Active network info - Type: " + State.DISCONNECTED);
            displayActiveNetworkState(State.DISCONNECTED.toString());
            displayActiveNetworkType(State.DISCONNECTED.toString());
            return;
        }
        switch (networkInfo.getType()) {
            case ConnectivityManager.TYPE_MOBILE:
                if (mActiveNetworkType != networkInfo.getType() || mActiveNetworkSubType != networkInfo.getSubtype()) {
                    TelephonyManager telephonyManager = (TelephonyManager) mContext
                            .getSystemService(Context.TELEPHONY_SERVICE);
                    displayActiveNetworkType(networkInfo.getTypeName() + "/" + networkInfo.getSubtypeName() + " ("
                            + telephonyManager.getNetworkOperatorName() + ")");
                }
                if (mActiveNetworkType != networkInfo.getType()) {
                    LOG.info("Active network info - Type: " + networkInfo.getTypeName());
                    mActiveNetworkType = networkInfo.getType();
                }
                if (mActiveNetworkSubType != networkInfo.getSubtype()) {
                    LOG.info("Active network info - Subtype: " + networkInfo.getSubtypeName());
                    mActiveNetworkSubType = networkInfo.getSubtype();

                }
                if (mActiveNetworkState != networkInfo.getState()) {
                    LOG.info("Active network info - State: " + networkInfo.getState());
                    mActiveNetworkState = networkInfo.getState();
                    displayActiveNetworkState(networkInfo.getState().toString());
                }
                break;
            case ConnectivityManager.TYPE_WIFI:
                // Handle initial state
                if (mActiveNetworkType == TYPE_UNKNOWN && mActiveNetworkState == null) {
                    LOG.info("Active network info - Type: " + networkInfo.getTypeName());
                    mActiveNetworkType = networkInfo.getType();
                    WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    displayActiveNetworkType(networkInfo.getTypeName() + " (" + wifiInfo.getSSID() + ")");

                    LOG.info("Active network info - State: " + networkInfo.getState());
                    mActiveNetworkState = networkInfo.getState();
                    displayActiveNetworkState(networkInfo.getState().toString());
                    break;
                }
                if (mActiveNetworkState != networkInfo.getState()) {
                    LOG.info("Active network info - State: " + networkInfo.getState());
                    mActiveNetworkState = networkInfo.getState();
                    displayActiveNetworkState(networkInfo.getState().toString());
                }
                if (mActiveNetworkType != networkInfo.getType()) {
                    LOG.info("Active network info - Type: " + networkInfo.getTypeName());
                    mActiveNetworkType = networkInfo.getType();
                    WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    displayActiveNetworkType(networkInfo.getTypeName() + " (" + wifiInfo.getSSID() + ")");
                }
                if (mActiveNetworkState != networkInfo.getState()) {
                    LOG.info("Active network info - State: " + networkInfo.getState());
                    mActiveNetworkState = networkInfo.getState();
                    displayActiveNetworkState(networkInfo.getState().toString());
                }
                break;
            default:
                break;
        }
    }

    private void getWifiNetworkInfo(Intent intent) {
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (mActiveNetworkType != networkInfo.getType()) {
            LOG.info("Active network info - Type: " + networkInfo.getTypeName());
            mActiveNetworkType = networkInfo.getType();
        }
        if (mActiveNetworkState != networkInfo.getState()) {
            LOG.info("Active network info - State: " + networkInfo.getState());
            mActiveNetworkState = networkInfo.getState();
            displayActiveNetworkState(networkInfo.getState().toString());
            if (mActiveNetworkState == State.CONNECTED) {
                WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                LOG.info("WIFI info - SSID: " + wifiInfo.getSSID());
                displayActiveNetworkType(networkInfo.getTypeName() + " (" + wifiInfo.getSSID() + ")");
                LOG.info("WIFI info - BSSID: " + wifiInfo.getBSSID());
                LOG.info("WIFI info - NetworkId: " + wifiInfo.getNetworkId());
                LOG.info("WIFI info - IpAddress: " + intToIp(wifiInfo.getIpAddress()));
                LOG.info("WIFI info - Rssi: " + wifiInfo.getRssi());
                LOG.info("WIFI info - LinkSpeed: " + wifiInfo.getLinkSpeed() + " Mbps");
                LOG.info("WIFI info - MacAddress: " + wifiInfo.getMacAddress());
                List<WifiConfiguration> e = wifiManager.getConfiguredNetworks();
                for (WifiConfiguration wifiConfiguration : e) {
                    String ssid = wifiConfiguration.SSID.substring(1, wifiConfiguration.SSID.length() - 1);
                    if (ssid.equals(wifiInfo.getSSID())) {
                        LOG.info("WIFI info - WPA_PSK: " + wifiConfiguration.allowedKeyManagement.get(KeyMgmt.WPA_PSK));
                        LOG.info("WIFI info - WPA_EAP: " + wifiConfiguration.allowedKeyManagement.get(KeyMgmt.WPA_EAP));
                        LOG.info("WIFI info - IEEE8021X: "
                                + wifiConfiguration.allowedKeyManagement.get(KeyMgmt.IEEE8021X));
                        LOG.info("WIFI info - NONE: " + wifiConfiguration.allowedKeyManagement.get(KeyMgmt.NONE));
                    }
                }
            }
        }
    }

    private String intToIp(int i) {
        return ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + (i & 0xFF);
    }

    // private String getConnectionState(State state) {
    // switch (state) {
    // case CONNECTED:
    // return "CONNECTED";
    // case CONNECTING:
    // return "CONNECTING";
    // case DISCONNECTED:
    // return "DISCONNECTED";
    // case DISCONNECTING:
    // return "DISCONNECTING";
    // case SUSPENDED:
    // return "SUSPENDED";
    // case UNKNOWN:
    // return "UNKNOWN";
    // default:
    // break;
    // }
    // return null;
    // }

    // Display methods

    private void displayActiveNetworkState(String networkState) {
        sActiveNetworkState = networkState;
        if (RootTab.getCurrentTab() == Tabs.MAIN_TAB) {
            Intent intent = new Intent(MainTab.ACTION_DISPLAY);
            intent.putExtra(MainTab.ACTIVE_NETWORK_STATE_MESSAGE, networkState);
            displayIntent(intent);
        }
    }

    private void displayActiveNetworkType(String networkType) {
        sActiveNetworkType = networkType;
        if (RootTab.getCurrentTab() == Tabs.MAIN_TAB) {
            Intent intent = new Intent(MainTab.ACTION_DISPLAY);
            intent.putExtra(MainTab.ACTIVE_NETWORK_TYPE_MESSAGE, networkType);
            displayIntent(intent);
        }
    }

    private void displayIntent(Intent intent) {
        // Log.v(LOG, "displayIntent()");
        mContext.sendBroadcast(intent);
    }

}
