package com.seven.asimov.it.utils.datausage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import com.seven.asimov.it.utils.tcpdump.Direction;

public class DataUsageUtil {
    private static final String TAG = DataUsageUtil.class.getSimpleName();

    private static PackageManager packageManager;

    public static long getDataUsage(Context context, String packageName, Direction direction, int infoFlag) throws Exception {
        packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, infoFlag);
        PackageInfo packageInfo = packageManager.getPackageInfo(packageName, infoFlag);
        TrafficStats trafficStats = new TrafficStats();
//        Log.v(TAG,
//                "packageName:" + packageName + "\n" +
//                        "date:" + new Date() + "\n" +
//                        "getUidRxBytes:" + trafficStats.getUidRxBytes(applicationInfo.uid) + "\n" +
//                        "getUidTxBytes:" + trafficStats.getUidTxBytes(applicationInfo.uid)
//        );
        switch (direction) {
            case TO_US:
                return trafficStats.getUidRxBytes(applicationInfo.uid);
            case FROM_US:
                return trafficStats.getUidTxBytes(applicationInfo.uid);
            case BOTH:
                return trafficStats.getUidRxBytes(applicationInfo.uid) + trafficStats.getUidTxBytes(applicationInfo.uid);
            default:
                return 0;
        }
    }
    /*
    public static long getDataUsage_OC(Context context, Direction direction) throws Exception {
        return getDataUsage(context, TFConstants.OC_PACKAGE_NAME, direction, PackageManager.GET_META_DATA);
    }

    public static long getDataUsage_IT(Context context, Direction direction) throws Exception {
        return getDataUsage(context, TFConstants.IT_PACKAGE_NAME, direction, PackageManager.GET_META_DATA);
    }
    */
}

