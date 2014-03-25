package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;


import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FCKFoundWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FcFckTask extends Task<FCKFoundWrapper> {
    private static final String TAG = FcFckTask.class.getSimpleName();

    private String refCountRegexp = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Ignoring FC \\(CSM \\[[0-9A-Z]*\\] FCK \\[[0-9A-Z]*\\]\\) deletion: ref_count=([0-9]*)";
    private Pattern refCountPattern;

    public FcFckTask(int refCount) {
        refCountRegexp = String.format(refCountRegexp, refCount);
        refCountPattern = Pattern.compile(refCountRegexp, Pattern.CASE_INSENSITIVE);
        Log.v(TAG, "Ref Count Regexp = " + refCountRegexp);
    }

    @Override
    protected FCKFoundWrapper parseLine(String line) {
        Matcher matcher = refCountPattern.matcher(line);
        if (matcher.find()){
            FCKFoundWrapper fckFoundWrapper = new FCKFoundWrapper();
            setTimestampToWrapper(fckFoundWrapper, matcher);
            return fckFoundWrapper;
        }
        return null;
    }
}
