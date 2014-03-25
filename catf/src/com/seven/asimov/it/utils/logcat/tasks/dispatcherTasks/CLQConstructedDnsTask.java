package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.tasks.CLQConstructedTask;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CLQConstructedDnsWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CLQConstructedDnsTask extends Task <CLQConstructedDnsWrapper> {
    private static final String TAG = CLQConstructedTask.class.getSimpleName();
    private static final String CLQ_REG_EXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*:[0-9]*:[0-9]*\\.[0-9]*).([A-Z]*).* OCEngineTaskDnsCLQ constructed, originator ([a-z]+)\\, DTRX\\[([0-9A-Z]{8})\\].* DST ([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)\\:([0-9]+)";
    private static final Pattern CLQ_PATTERN = Pattern.compile(CLQ_REG_EXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected CLQConstructedDnsWrapper parseLine(String line) {
        Matcher matcher = CLQ_PATTERN.matcher(line);
        if (matcher.find()) {
            CLQConstructedDnsWrapper wrapper = new CLQConstructedDnsWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setDispatcher(matcher.group(3));
            wrapper.setDtrx(matcher.group(4));
            wrapper.setDestinationIp(matcher.group(5));
            wrapper.setDestinationPort(Integer.parseInt(matcher.group(6)));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}