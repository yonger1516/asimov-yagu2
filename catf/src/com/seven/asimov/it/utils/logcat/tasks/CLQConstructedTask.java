package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.CLQConstructedWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CLQConstructedTask extends Task<CLQConstructedWrapper> {

    private static final String TAG = CLQConstructedTask.class.getSimpleName();
    private static final String CLQ_REG_EXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*:[0-9]*:[0-9]*\\.[0-9]*).([A-Z]*).* Constructed HTTP CLQ from ([A-Z]+) HTRX \\[([0-9A-Z]{8})\\] CSM \\[([0-9A-Z]{8})\\].* DST ([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)\\:([0-9]+).* loport ([0-9]+)";
    private static final Pattern CLQ_PATTERN = Pattern.compile(CLQ_REG_EXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected CLQConstructedWrapper parseLine(String line) {
        Matcher matcher = CLQ_PATTERN.matcher(line);
        if (matcher.find()) {
            CLQConstructedWrapper wrapper = new CLQConstructedWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setDispatcher(matcher.group(3));
            wrapper.setHtrx(matcher.group(4));
            wrapper.setCsm(matcher.group(5));
            wrapper.setDestinationIp(matcher.group(6));
            wrapper.setDestinationPort(Integer.parseInt(matcher.group(7)));
            wrapper.setLoPort(Integer.parseInt(matcher.group(8)));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}