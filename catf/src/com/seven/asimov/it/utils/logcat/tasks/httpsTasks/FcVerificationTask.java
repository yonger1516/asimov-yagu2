package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import android.util.Log;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FcVerificationWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.TimeZones;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FcVerificationTask extends Task<FcVerificationWrapper> {

    private static final String TAG = FcVerificationTask.class.getName();

    private static final String FC_VERIFICATION_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*FC.*FCK.*: (.*)";
    private static final Pattern fcVerificationPattern = Pattern.compile(FC_VERIFICATION_REGEXP, Pattern.CASE_INSENSITIVE);

    public FcVerificationWrapper parseLine(String line) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Matcher matcher = fcVerificationPattern.matcher(line);
        if (matcher.find()) {
            int hour = 0;
            try {
                hour = TimeZones.valueOf(matcher.group(2)).getId();
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getMessage());
            }
            long timestamp;
            try {
                timestamp = DateUtil.format(matcher.group(1).replaceAll("/", "-")) + hour * 3600 * 1000;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            if (matcher.group(3).contains("FC is not expired")) {
                return new FcVerificationWrapper(0, timestamp);
            } else {
                Pattern fcVerificationResultPattern = Pattern.compile("verification result - (\\d)");
                matcher = fcVerificationResultPattern.matcher(matcher.group(3));
                if (matcher.find()) {
                    return new FcVerificationWrapper(Integer.parseInt(matcher.group(1)), timestamp);
                }
            }
        }
        return null;
    }
}
