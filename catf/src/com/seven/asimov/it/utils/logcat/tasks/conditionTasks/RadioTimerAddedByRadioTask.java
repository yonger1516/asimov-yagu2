package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;

import com.seven.asimov.it.utils.logcat.wrappers.RadioTimerWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RadioTimerAddedByRadioTask extends Task<RadioTimerWrapper> {
    private static final String TAG = RadioTimerAddedByRadioTask.class.getSimpleName();
    private final String RADIO_TIMER_STATE_BY_RADIO = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* - Radio-timer condition ([a-z]*) by radio: is_timer_scheduled=([a-z]*) \\(group=([a-z]*), script=(0x\\d*\\w*)\\)";
    private Pattern radioTimerStateByRadioPattern = Pattern.compile(RADIO_TIMER_STATE_BY_RADIO, Pattern.CASE_INSENSITIVE);

    @Override
    protected RadioTimerWrapper parseLine(String line) {
        Matcher matcher = radioTimerStateByRadioPattern.matcher(line);

        if (matcher.find()) {
            RadioTimerWrapper wrapper = new RadioTimerWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setState(matcher.group(3));
            wrapper.setTimerScheduled(matcher.group(4));
            return wrapper;
        }
       return null;
    }
}
