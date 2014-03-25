package com.seven.asimov.it.utils.logcat.wrappers;

public class CRCSReportSendingFailedWrapper extends ReportTransferWrapperNN {
    private static final String TAG = CRCSReportSendingFailedWrapper.class.getSimpleName();

    @Override
    protected String getTAG() {
        return TAG;
    }
}
