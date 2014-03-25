package com.seven.asimov.test.tool.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.seven.asimov.test.tool.core.Starter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BootCompleteReceiver BroadcastReceiver.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(BootCompleteReceiver.class.getSimpleName());

    @Override
    public void onReceive(Context context, Intent intent) {

        LOG.info("Boot completed!");

        Starter.init(context);

    }
}
