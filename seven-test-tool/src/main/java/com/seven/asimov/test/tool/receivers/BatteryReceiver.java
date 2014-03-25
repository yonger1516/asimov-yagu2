package com.seven.asimov.test.tool.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BatteryReceiver BroadcastReceiver.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class BatteryReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(BatteryReceiver.class.getSimpleName());

    private static Context sContext;

    public static void init(Context context) {
        sContext = context;
        registerBroadcast();
    }

    public static void registerBroadcast() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        sContext.registerReceiver(new BatteryReceiver(), filter);
    }

    private static int sPlugged = -1;
    private int mPlugged = -1;
    private int mScale = -1;
    private static int sLevel = -1;
    private int mLevel = -1;
    private int mVoltage = -1;
    private int mTemp = -1;

    @Override
    public void onReceive(Context context, Intent intent) {

        mPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        mLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        mScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        mTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        mVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

        // Log on plugged state change or battery level change
        if (mPlugged != sPlugged || mLevel != sLevel) {
            sPlugged = mPlugged;
            sLevel = mLevel;
            LOG.info("Plugged = " + sPlugged + ", battery level is " + sLevel + "/" + mScale + ", temp is " + mTemp
                    + ", voltage is " + mVoltage);

        }
    }
}
