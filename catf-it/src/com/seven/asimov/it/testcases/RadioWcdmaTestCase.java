package com.seven.asimov.it.testcases;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.AirplaneModeUtil;
import com.seven.asimov.it.utils.logcat.wrappers.RadioLogEntry;
import com.seven.asimov.it.utils.logcat.wrappers.RadioStateType;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RadioWcdmaTestCase extends TcpDumpTestCase {


    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected List<RadioLogEntry> getWcdmaEntries(List<RadioLogEntry> input){
        List<RadioLogEntry> result = new ArrayList<RadioLogEntry>();
        for (RadioLogEntry entry : input){
            if (entry.getCurrentState() == RadioStateType.cell_dch ||
                    entry.getCurrentState() == RadioStateType.cell_fach ||
                    entry.getCurrentState() == RadioStateType.cell_pch ||
                    entry.getCurrentState() == RadioStateType.idle){
                result.add(entry);
            }
        }
        return result;
    }

    protected void setAirplaneMode(Context context,boolean mode){
        AirplaneModeUtil airplaneModeUtil = new AirplaneModeUtil(context);
        airplaneModeUtil.setEnabled(mode);
    }


    protected HttpResponse sendSimpleRequest() throws URISyntaxException,IOException {
        String resource = "asimov_wcdma_simple_request";
        String uri = createTestResourceUri(resource);
        HttpRequest request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
        HttpResponse response = checkMiss(request,1) ;
        return response;
    }

    protected void enableMobileNetwork() throws NetworkErrorException {
        System.out.println("NetworkConnection = " + getActiveNetworkType());
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
        }
        System.out.println("NetworkConnection = " + getActiveNetworkType());
        int counter = 0;
        do{
            System.out.println("NetworkConnection = " + getActiveNetworkType());
            try {
                Thread.sleep(1000,1);
            }catch (InterruptedException ignored){
                throw new NetworkErrorException("Can't stop wifi and|or get mobile network! Interrupted exception occurred while waiting available network.");
            }
            counter++;
            if (counter > 100){
                throw new NetworkErrorException("Can't stop wifi and|or get mobile network!!!");
            }

        }while (!checkMobileNetwork());
    }

    protected void disableMobileNetwork() throws NetworkErrorException {
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()==false){
            wifiManager.setWifiEnabled(true);
        }
        int counter = 0;
        do{
            try {
                Thread.sleep(1000,1);
            }catch (InterruptedException ignored){
                throw new NetworkErrorException("Can't start wifi and|or get mobile network! Interrupted exception occurred while waiting available network.");
            }
            counter++;
            if (counter > 100){
                throw new NetworkErrorException("Can't start wifi and|or get mobile network!!!");
            }
        }while (getActiveNetworkType() != 2);
    }

    public boolean checkMobileNetwork(){
        TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager.getDataState()==2){
            return true;
        }
        return false;
    }


    public int getActiveNetworkType() {
        int ret = 0; // IF_UNKNOWN
        try {
            ConnectivityManager conMgr = (ConnectivityManager) getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
            ret = netInfo.getType();
            if( ret == ConnectivityManager.TYPE_MOBILE ) {
                ret = 1; // IF_MOBILE
                // check sub_type here
                TelephonyManager telephonyManager = (TelephonyManager) getContext().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                int type = telephonyManager.getNetworkType();
                switch(type) {
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN: ret = 4;  break; // -> MOBILE_NETWORK_TYPE_NAME_UNKNOWN
                    case TelephonyManager.NETWORK_TYPE_GPRS:    ret = 5;  break; // -> MOBILE_NETWORK_TYPE_NAME_GPRS
                    case TelephonyManager.NETWORK_TYPE_EDGE:    ret = 6;  break; // -> MOBILE_NETWORK_TYPE_NAME_EDGE
                    case TelephonyManager.NETWORK_TYPE_UMTS:    ret = 7;  break; // -> MOBILE_NETWORK_TYPE_NAME_UMTS
                    case TelephonyManager.NETWORK_TYPE_HSDPA:   ret = 8;  break; // -> MOBILE_NETWORK_TYPE_NAME_HSDPA
                    case TelephonyManager.NETWORK_TYPE_HSUPA:   ret = 9;  break; // -> MOBILE_NETWORK_TYPE_NAME_HSUPA
                    case TelephonyManager.NETWORK_TYPE_HSPA:    ret = 10; break; // -> MOBILE_NETWORK_TYPE_NAME_HSPA
                    case TelephonyManager.NETWORK_TYPE_CDMA:    ret = 11; break; // -> MOBILE_NETWORK_TYPE_NAME_CDMA
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:  ret = 12; break; // -> MOBILE_NETWORK_TYPE_NAME_EVDO_0
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:  ret = 13; break; // -> MOBILE_NETWORK_TYPE_NAME_EVDO_A
                    /* Since: API Level 9 */
                    // case TelephonyManager.NETWORK_TYPE_EVDO_B: ret= 14; break; // -> MOBILE_NETWORK_TYPE_NAME_EVDO_B
                    case TelephonyManager.NETWORK_TYPE_1xRTT:   ret = 15; break; // -> MOBILE_NETWORK_TYPE_NAME_EVDO_1xRTT
                    case TelephonyManager.NETWORK_TYPE_IDEN:    ret = 1;  break; // No code in Asimov for this
                    /* Since: API Level 11 */
                    // case TelephonyManager.NETWORK_TYPE_LTE:
                    // case TelephonyManager.NETWORK_TYPE_EHRPD:
                    default: break;
                }
            } else if( ret == ConnectivityManager.TYPE_WIFI ) {
                ret = 2; // IF_WIFI
                //WifiManager wifiManager = (WifiManager)Z7Shared.context.getSystemService(Context.WIFI_SERVICE);
            } else if( ret == ConnectivityManager.TYPE_WIMAX ) {
                ret = 16; // IF_WIMAX
            }
            // NOTE, there could be also TYPE_BLUETOOTH, TYPE_DUMMY, TYPE_ETHERNET, TYPE_MOBILE_DUN,
            //                            TYPE_MOBILE_HIPRI, TYPE_MOBILE_MMS, TYPE_MOBILE_SUPL
        } catch( Exception e ) {
            e.printStackTrace();
        }
        return ret;
    }
}
