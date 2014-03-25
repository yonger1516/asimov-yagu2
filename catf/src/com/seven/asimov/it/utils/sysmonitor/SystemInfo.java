package com.seven.asimov.it.utils.sysmonitor;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public enum SystemInfo {

    INSTANCE;

    private static final String TAG = SystemInfo.class.getSimpleName();

    public String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getModel() {
        return Build.MODEL;
    }

    public String getNetworkInterface(Context context) {
        ConnectivityManager conMng = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMng.getActiveNetworkInfo();
        return networkInfo.getTypeName();
    }

    public String getOCVersion(Context context, String packageName) throws PackageManager.NameNotFoundException {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Catch");
            e.printStackTrace();
        }
        return "000000";
    }
}
