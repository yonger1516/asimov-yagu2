package com.seven.asimov.test.tool.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PackageReceiver BroadcastReceiver.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class PackageReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(PackageReceiver.class.getSimpleName());

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            String addedPackage = intent.getData().toString();
            LOG.info("Package added = " + addedPackage);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            String removedPackage = intent.getData().toString();
            LOG.info("Package removed = " + removedPackage);
        }
    }
}
