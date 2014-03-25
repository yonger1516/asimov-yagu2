package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;

import com.seven.asimov.it.utils.logcat.wrappers.RadioTimerWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadioTimerAddedByTimerTask extends Task<RadioTimerWrapper> {
    private static final String TAG = RadioTimerAddedByRadioTask.class.getSimpleName();
    private final String RADIO_TIMER_STATE_BY_TIMER = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* - Radio-timer condition triggered by timer: already_active=([a-z]*) \\(group=([a-z]*), script=([a-z, 0-9]*)\\)";
    private Pattern radioTimerStateByTimerPattern = Pattern.compile(RADIO_TIMER_STATE_BY_TIMER, Pattern.CASE_INSENSITIVE);

    @Override
    protected RadioTimerWrapper parseLine(String line) {
        Matcher matcher = radioTimerStateByTimerPattern.matcher(line);
        if (matcher.find()) {
            RadioTimerWrapper wrapper = new RadioTimerWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setAlreadyActive(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}