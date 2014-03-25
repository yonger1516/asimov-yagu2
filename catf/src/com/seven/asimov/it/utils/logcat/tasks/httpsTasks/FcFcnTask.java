package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FcFcnWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FcFcnTask extends Task<FcFcnWrapper> {
    private static final String FC_FCN_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*-.*FC.*verdict.*(FCN).*";
    private static Pattern fcnPattern = Pattern.compile(FC_FCN_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected FcFcnWrapper parseLine(String line) {
        Matcher matcher = fcnPattern.matcher(line);
        if (matcher.find()) {
            FcFcnWrapper wrapper = new FcFcnWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setVerdict(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
