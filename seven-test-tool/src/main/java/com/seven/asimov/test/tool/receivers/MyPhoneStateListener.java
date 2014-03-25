package com.seven.asimov.test.tool.receivers;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MyPhoneStateListener.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public class MyPhoneStateListener extends PhoneStateListener {

    public MyPhoneStateListener(Context context) {
        this.sContext = context;
    }

    private static String sDataConnectionActivity = StringUtils.EMPTY;
    private static String sDataConnectionState = StringUtils.EMPTY;

    public static String getDataConnectionActivity() {
        return sDataConnectionActivity;
    }

    public static String getDataConnectionState() {
        return sDataConnectionState;
    }

    private static final Logger LOG = LoggerFactory.getLogger(MyPhoneStateListener.class.getSimpleName());

    private static Context sContext;
    private static final int TYPE_UNKNOWN = -1;
    private Integer mDataConnectionActivity = TYPE_UNKNOWN;
    private Integer mDataConnectionState = TYPE_UNKNOWN;

    public void launchListener() {
        TelephonyManager telephonyManager = (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new MyPhoneStateListener(sContext), PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        telephonyManager.listen(new MyPhoneStateListener(sContext), PhoneStateListener.LISTEN_DATA_ACTIVITY);
        telephonyManager.listen(new MyPhoneStateListener(sContext), PhoneStateListener.LISTEN_SERVICE_STATE);
        getNetworkInfo();
    }

    @Override
    public void onServiceStateChanged(ServiceState state) {

        // Log.v(LOG, "MyPhoneStateListener.onServiceStateChanged()");

        String serviceState = getServiceState(state.getState());
        LOG.info("Service state: " + serviceState);
        if (state.getState() == ServiceState.STATE_IN_SERVICE) {
            // Show registered network info
            TelephonyManager telephonyManager = (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
            // SIM operator�s MNC + MCC number,
            LOG.info("Registered network info - SimOperator: " + telephonyManager.getSimOperator());
            LOG.info("Registered network info - SimCountryIso: " + telephonyManager.getSimCountryIso());
            LOG.info("Registered network info - SimOperatorName: " + telephonyManager.getSimOperatorName());
            LOG.info("Registered network info - SimState: " + getSimState(telephonyManager.getSimState()));
            LOG.info("Registered network info - SimSerialNumber: " + telephonyManager.getSimSerialNumber());
            // Returns the MCC + MNC of the registered network operator
            LOG.info("Registered network info - NetworkOperator: " + telephonyManager.getNetworkOperator());
            LOG.info("Registered network info - NetworkOperatorName: " + telephonyManager.getNetworkOperatorName());
            LOG.info("Registered network info - NetworkCountryIso: " + telephonyManager.getNetworkCountryIso());
            // IMEI/MEID
            LOG.info("Registered network info - DeviceId: " + telephonyManager.getDeviceId());
            // Device phone number MSISDN
            LOG.info("Registered network info - Line1Number: " + telephonyManager.getLine1Number());
            // IMSI
            LOG.info("Registered network info - SubscriberId: " + telephonyManager.getSubscriberId());
            // Roaming?
            LOG.info("Registered network info - isNetworkRoaming: " + telephonyManager.isNetworkRoaming());
        }
    }

    @Override
    public void onDataConnectionStateChanged(int state) {

        // Log.v(LOG, "MyPhoneStateListener.onDataConnectionStateChanged()");

        if (mDataConnectionState != state) {
            LOG.info("Data connection state: " + getDataConnectionState(state));
            mDataConnectionState = state;
            // displayDataConnectionState(getDataConnectionState(state));
        }
    }

    @Override
    public void onDataActivity(int direction) {

        // Log.v(LOG, "MyPhoneStateListener.onDataActivity()");

        if (mDataConnectionActivity != direction) {
            LOG.info("Data activity: " + getDataActivity(direction));
            mDataConnectionActivity = direction;
            // displayDataConnectionActivity(getDataActivity(direction));
        }
    }

    private String getDataConnectionState(int state) {
        switch (state) {
            case TelephonyManager.DATA_DISCONNECTED:
                return "DATA_DISCONNECTED";
            case TelephonyManager.DATA_CONNECTING:
                return "DATA_CONNECTING";
            case TelephonyManager.DATA_CONNECTED:
                return "DATA_CONNECTED";
            case TelephonyManager.DATA_SUSPENDED:
                return "DATA_SUSPENDED";
            default:
                break;
        }
        return null;
    }

    private String getDataActivity(int direction) {
        switch (direction) {
            case TelephonyManager.DATA_ACTIVITY_NONE:
                return "DATA_ACTIVITY_NONE";
            case TelephonyManager.DATA_ACTIVITY_IN:
                return "DATA_ACTIVITY_IN";
            case TelephonyManager.DATA_ACTIVITY_OUT:
                return "DATA_ACTIVITY_OUT";
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                return "DATA_ACTIVITY_INOUT";
            // Data connection is active, but physical link is down
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                return "DATA_ACTIVITY_DORMANT";
            default:
                break;
        }
        return null;
    }

    private String getServiceState(int state) {
        switch (state) {
            case ServiceState.STATE_IN_SERVICE:
                return "STATE_IN_SERVICE";
            case ServiceState.STATE_OUT_OF_SERVICE:
                return "STATE_OUT_OF_SERVICE";
            case ServiceState.STATE_EMERGENCY_ONLY:
                return "STATE_EMERGENCY_ONLY";
            case ServiceState.STATE_POWER_OFF:
                return "STATE_POWER_OFF";
            default:
                break;
        }
        return null;
    }

    private String getSimState(int simState) {
        switch (simState) {
            case TelephonyManager.SIM_STATE_UNKNOWN:
                return "SIM_STATE_UNKNOWN";
            case TelephonyManager.SIM_STATE_ABSENT:
                return "SIM_STATE_ABSENT";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return "SIM_STATE_PIN_REQUIRED";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return "SIM_STATE_PUK_REQUIRED";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return "SIM_STATE_NETWORK_LOCKED";
            case TelephonyManager.SIM_STATE_READY:
                return "SIM_STATE_READY";
            default:
                break;
        }
        return null;
    }

    // Display methods

    // private void displayDataConnectionActivity(String dataConnectionActivity) {
    // sDataConnectionActivity = dataConnectionActivity;
    // if (RootTab.getCurrentTab() == 0) {
    // Intent intent = new Intent(MainTab.ACTION_DISPLAY);
    // intent.putExtra(MainTab.DATA_CONNECTION_ACTIVITY_MESSAGE, dataConnectionActivity);
    // sendIntent(intent);
    // }
    // }
    //
    // private void displayDataConnectionState(String dataConnectionState) {
    // sDataConnectionState = dataConnectionState;
    // if (RootTab.getCurrentTab() == 0) {
    // Intent intent = new Intent(MainTab.ACTION_DISPLAY);
    // intent.putExtra(MainTab.DATA_CONNECTION_STATE_MESSAGE, dataConnectionState);
    // sendIntent(intent);
    // }
    // }

    private void getNetworkInfo() {
        Intent intent = new Intent(ConnectivityReceiver.ACTION_GET_NETWORK_INFO);
        sendIntent(intent);
    }

    private void sendIntent(Intent intent) {
        // Log.v(LOG, "displayIntent()");
        sContext.sendBroadcast(intent);
    }

    public static String getLine1Number() {
        TelephonyManager telephonyManager = (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }

    public static String getSubscriberId() {
        TelephonyManager telephonyManager = (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getSubscriberId();
    }

    public static String getDeviceId() {
        TelephonyManager telephonyManager = (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}
