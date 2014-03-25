package com.seven.asimov.it.utils.logcat.tasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.wrappers.SpanHistoryWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpanHistoryTask extends Task<SpanHistoryWrapper> {

    private static final String SPAN_HISTORY_HEADER_REGEXP = "(201[3-9]/[0-1][0-9]/[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9]+).([A-Z]*)" +
            ".*pattern_detector.cpp[:\\d\\]\\(\\)\\-# ]+\\|[ ]*HTRX ID[ ]*\\|[ ]*Request[ ]*\\|[ ]*Response[ ]*\\|[ ]*End[ ]*\\|" +
            "[ ]*RI[ ]*\\|[ ]*D[ ]*\\|[ ]*TO[ ]*\\|[ ]*CLQ[ ]*\\|[ ]*CSQ[ ]*\\|[ ]*Response hash[ ]*\\|[ ]*Err code";
    private static final String SPAN_HISTORY_ENTRY_REGEXP = "(201[3-9]/[0-1][0-9]/[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9]+).([A-Z]*)" +
            ".*pattern_detector.cpp[:\\d\\]\\(\\)\\- ]+\\|[ ]*[\\d\\w]+[ ]*\\|[ ]*[\\d]+[ ]*\\|[ ]*[\\d]+[ ]*\\|[ ]*[\\d]+[ ]*\\|[ ]*([\\d]+)[ ]" +
            "*\\|[ ]*[\\d]+[ ]*\\|[ ]*[\\d]+[ ]*\\|[ ]*[\\w]+[ ]*\\|[ ]*[\\w]/[\\w]+[ ]*\\|[ ]*([\\w\\d]+)[ ]*\\|[ ]*[\\d]+";

    private static final Pattern spanHistoryHeaderPattern = Pattern.compile(SPAN_HISTORY_HEADER_REGEXP);
    private static final Pattern spanHistoryEntryPattern = Pattern.compile(SPAN_HISTORY_ENTRY_REGEXP);

    private SpanHistoryWrapper spanHistory;

    private int expectedHistorySize;

    public SpanHistoryTask(int expectedHistorySize) {
        this.expectedHistorySize = expectedHistorySize;
    }

    @Override
    protected SpanHistoryWrapper parseLine(String line) {
        Matcher headerMatcher = spanHistoryHeaderPattern.matcher(line);
        if (headerMatcher.find()) {
            spanHistory = new SpanHistoryWrapper();
            setTimestampToWrapper(spanHistory, headerMatcher);
        }
        Matcher entryMatcher = spanHistoryEntryPattern.matcher(line);
        if (entryMatcher.find()) {
            SpanHistoryWrapper.SpanHistoryRecord record = new SpanHistoryWrapper.SpanHistoryRecord(entryMatcher.group(2));
            spanHistory.addRecord(record);
            Log.e("SPAN_HISTORY", record.toString());
            if (spanHistory.getSize() == expectedHistorySize) {
                return spanHistory;
            }
        }
        return null;
    }
}
