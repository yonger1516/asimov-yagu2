package com.seven.asimov.test.tool.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScreenOrientationReceiver BroadcastReceiver.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class ScreenOrientationReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenOrientationReceiver.class.getSimpleName());

    private static Context sContext;

    public static void init(Context context) {
        sContext = context;
        registerBroadcast();
    }

    public static void registerBroadcast() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        sContext.registerReceiver(new ScreenOrientationReceiver(), filter);
    }

    @Override
    public void onReceive(Context context, Intent myIntent) {

        if (myIntent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)) {

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // it's Landscape
                LOG.info("LANDSCAPE");
            } else {
                LOG.info("PORTRAIT");
            }
        }
    }
}
