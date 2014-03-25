package com.seven.asimov.it.utils.logcat.tasks;


import com.seven.asimov.it.utils.logcat.wrappers.HtrxNotOptimizingWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtrxNotOptimizingTask extends Task<HtrxNotOptimizingWrapper> {
    private static final String TAG = HtrxNotOptimizingTask.class.getSimpleName();
    private static final String TRUE_NUM="1";
    private static final String HTRXNO_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).+?(\\S+) HTRX \\[(\\S+)\\]: not optimizing \\(optimize: (\\S+), transparent: (\\S+)\\)";
    private static Pattern htrxNOPattern = Pattern.compile(HTRXNO_REGEXP, Pattern.CASE_INSENSITIVE);


    protected HtrxNotOptimizingWrapper parseLine(String line) {
        Matcher matcher = htrxNOPattern.matcher(line);
        if (matcher.find()) {
            HtrxNotOptimizingWrapper wrapper = new HtrxNotOptimizingWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setDispatcher(matcher.group(3));
            wrapper.setHtrxID(matcher.group(4));
            wrapper.setOptimize(matcher.group(5).equals(TRUE_NUM));
            wrapper.setTransparent(matcher.group(6).equals(TRUE_NUM));
            return wrapper;
        }
        return null;
    }}
