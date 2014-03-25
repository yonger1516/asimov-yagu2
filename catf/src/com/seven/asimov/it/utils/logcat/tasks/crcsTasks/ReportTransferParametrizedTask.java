package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ReportTransferWrapperNN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ReportTransferParametrizedTask extends Task<ReportTransferWrapperNN> {
    private static final String TAG = ReportTransferParametrizedTask.class.getSimpleName();

    private String reportTransferParametrizedRegexp =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*status=(%s).*type=%s.*token=([0-9]*).*changed status from: %s to:%s";

    private Pattern reportTransferParametrizedPattern;

    private String type;
    private String status;
    private String fromStatus;
    private String toStatus;

    public ReportTransferParametrizedTask(String status, String type, String fromStatus, String toStatus) {
        this.status = status;
        this.type = type;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;

        reportTransferParametrizedRegexp = String.format(reportTransferParametrizedRegexp, status, type, fromStatus, toStatus);
        Log.v(TAG, "ReportTransferParametrizedRegexp=" + reportTransferParametrizedRegexp);
        reportTransferParametrizedPattern = Pattern.compile(reportTransferParametrizedRegexp, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public ReportTransferWrapperNN parseLine(String line) {
        Matcher matcher = reportTransferParametrizedPattern.matcher(line);
        if (matcher.find()) {
            ReportTransferWrapperNN wrapper = new ReportTransferWrapperNN();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setStatus(this.getStatus());
            wrapper.setFromStatus(this.getFromStatus());
            wrapper.setToStatus(this.getToStatus());
            wrapper.setType(this.getType());
            wrapper.setToken(matcher.group(4));

            return wrapper;
        }

        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }
}
