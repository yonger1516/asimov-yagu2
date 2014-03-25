package com.seven.asimov.it.utils.logcat.wrappers;

import com.seven.asimov.it.utils.logcat.wrappers.ReportTransferWrapperNN;

public class CrcsTaskInQueueWrapper extends ReportTransferWrapperNN {
    private static final String TAG = CrcsTaskInQueueWrapper.class.getSimpleName();

    @Override
    protected String getTAG() {
        return TAG;
    }
}
