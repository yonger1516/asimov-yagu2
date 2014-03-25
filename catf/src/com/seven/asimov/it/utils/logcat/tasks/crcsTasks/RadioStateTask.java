package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.RadioStateWrapper;

public class RadioStateTask extends Task<RadioStateWrapper> {
    private static final String TAG = RadioStateTask.class.getSimpleName();

    private static final String RADIO_STATE_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* Radio state changed to (UP|DOWN)";

    private static final Pattern radioStateRecordsPattern = Pattern.compile(RADIO_STATE_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public RadioStateWrapper parseLine(String line) {
        Matcher matcher = radioStateRecordsPattern.matcher(line);
        if (matcher.find()) {
            RadioStateWrapper wrapper = new RadioStateWrapper();
            setTimestampToWrapper(wrapper, matcher);
            //Log.d(TAG, "parseLine found line " + matcher.group(1));
            if (matcher.group(3).equalsIgnoreCase("UP")) {
                wrapper.setRadioUp(true);
            } else if (matcher.group(3).equalsIgnoreCase("DOWN")) {
                wrapper.setRadioUp(false);
            }
            //Log.d(TAG, "Radio state changed to " + (wrapper.isRadioUp() ? "up." : "down."));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
