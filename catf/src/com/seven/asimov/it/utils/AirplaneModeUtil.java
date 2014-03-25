package com.seven.asimov.it.utils;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirplaneModeUtil {

    private Context mContext;
    private final String TAG = AirplaneModeUtil.class.getSimpleName();
    private static final Logger logger = LoggerFactory.getLogger(AirplaneModeUtil.class.getSimpleName());

    public AirplaneModeUtil(Context mContext) {
        this.mContext = mContext;
    }

    public boolean isEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    public void setEnabled(boolean isEnabled) {
        // Toggle airplane mode.
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, isEnabled ? 1 : 0);

        // Post an intent to reload.
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", isEnabled);
        mContext.sendBroadcast(intent);

        if (isEnabled) logger.trace(TAG, "AIRPLANE_MODE_ON");
        else logger.trace(TAG, "AIRPLANE_MODE_OFF");
    }
}
