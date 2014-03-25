package com.seven.asimov.test.tool.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import com.seven.asimov.test.tool.activity.MainTab;
import com.seven.asimov.test.tool.activity.RootTab;
import com.seven.asimov.test.tool.activity.Tabs;
import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.receivers.BatteryReceiver;
import com.seven.asimov.test.tool.receivers.MyPhoneStateListener;
import com.seven.asimov.test.tool.receivers.ScreenOrientationReceiver;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EventService service.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class MainService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(MainService.class.getSimpleName());

    private static boolean sServiceStarted;

    public static boolean isServiceStarted() {
        return sServiceStarted;
    }

    public static void setServiceStarted(boolean serviceStarted) {
        MainService.sServiceStarted = serviceStarted;
    }

    private final Handler mHandlerRunTimers = new Handler();

    private final int mNotifId = 8099;

    private static String sPackageVersionName;

    public static String getPackageVersionName() {
        return sPackageVersionName;
    }

    ;

    @Override
    public void onCreate() {
        super.onCreate();

        LOG.info("onCreate()");

        // put us as a foreground service
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String msg = "7Test \u00ae " + packageInfo.versionName;

            Notification notif = new Notification(0, msg, System.currentTimeMillis());
            notif.flags |= Notification.FLAG_AUTO_CANCEL;
            Intent intent = new Intent("com.seven.asimov.test.tool.service.FG");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent contentIntent = PendingIntent
                    .getActivity(this, mNotifId, intent, PendingIntent.FLAG_ONE_SHOT);

            notif.setLatestEventInfo(this, "SEVEN Networks", msg, contentIntent);

            startForeground(mNotifId, notif);

        } catch (Exception e) {
            LOG.error("onCreate()", e);
        }

        doServiceInit();
    }

    private void doServiceInit() {

        LOG.info("//////////////////////////");
        LOG.info("Starting 7Test tool service!!!");
        LOG.info("//////////////////////////");

        // Run timers
        mHandlerRunTimers.removeCallbacks(mRunTimers);
        mHandlerRunTimers.post(mRunTimers);

        // Show app info
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            LOG.info("7Test info - Package name: " + packageInfo.packageName);
            LOG.info("7Test info - Uid: " + packageInfo.applicationInfo.uid);
            LOG.info("7Test info - Version code: " + String.valueOf(packageInfo.versionCode));
            LOG.info("7Test info - Version name: " + packageInfo.versionName);
            sPackageVersionName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            LOG.info("Cannot load Version!");
        }

        // Show device info
        LOG.info("Device info - Build.BRAND: " + Build.BRAND);
        LOG.info("Device info - Build.MANUFACTURER: " + Build.MANUFACTURER);
        LOG.info("Device info - Build.PRODUCT: " + Build.PRODUCT);
        LOG.info("Device info - Build.MODEL: " + Build.MODEL);
        LOG.info("Device info - Build.VERSION.RELEASE: " + Build.VERSION.RELEASE);
        LOG.info("Device info - Build.VERSION.SDK: " + Build.VERSION.SDK);
        LOG.info("Device info - Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
        LOG.info("Device info - Build.VERSION.INCREMENTAL: " + Build.VERSION.INCREMENTAL);
        LOG.info("Device info - Build.TYPE: " + Build.TYPE);
        LOG.info("Device info - Build.USER: " + Build.USER);

        // Initialize listener
        MyPhoneStateListener phoneStateListener = new MyPhoneStateListener(this);
        phoneStateListener.launchListener();

        // Initialize receivers
        BatteryReceiver.init(this);
        ScreenOrientationReceiver.init(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LOG.info("onStartCommand()");

        return START_STICKY;
    }

    private Runnable mRunTimers = new Runnable() {
        public void run() {
            runTimers();
            mHandlerRunTimers.postDelayed(this, Constants.MILL_IN_SEC); // 1 second
        }
    };

    public static final String TIMER_MESSAGE = "timerMessage";

    private void runTimers() {
        if (RootTab.getCurrentTab() == Tabs.MAIN_TAB) {
            Intent intent = new Intent(MainTab.ACTION_DISPLAY);
            intent.putExtra(TIMER_MESSAGE, StringUtils.EMPTY);
            sendIntent(intent);
        }
    }

    private void sendIntent(Intent intent) {
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LOG.debug("onBind " + intent.getAction());
        // nothing here to bind to
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOG.info("//////////////////////////");
        LOG.info("Stopping 7Test tool service!!!");
        LOG.info("//////////////////////////");

        try {
            stopForeground(true);
        } catch (Exception ex) {
        }

        mHandlerRunTimers.removeCallbacks(mRunTimers);
    }
}
