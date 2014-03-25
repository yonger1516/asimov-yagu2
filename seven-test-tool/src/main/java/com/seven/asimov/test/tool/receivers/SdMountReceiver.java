package com.seven.asimov.test.tool.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SdMountReceiver BroadcastReceiver.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class SdMountReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(SdMountReceiver.class.getSimpleName());

    private static Boolean sSdMounted;

    public static boolean isSdMounted() {
        if (sSdMounted == null) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                sSdMounted = true;
            } else if (Environment.MEDIA_UNMOUNTED.equals(state)) {
                sSdMounted = false;
            } else if (Environment.MEDIA_SHARED.equals(state)) {
                sSdMounted = false;
            } else if (Environment.MEDIA_CHECKING.equals(state)) {
                sSdMounted = true;
            } else {
                sSdMounted = true;
                LOG.info("state: " + state);
                LOG.info("Media mounted: " + sSdMounted);
            }
        }
        return sSdMounted;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
            sSdMounted = true;
            LOG.info("Media mounted: " + sSdMounted);
        } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_SHARED.equals(state)) {
                sSdMounted = false;
                LOG.info("Media mounted: " + sSdMounted);
            }
        }
    }
}
