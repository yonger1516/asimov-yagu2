package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.OperationFailedMsg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperationFailedMsgTask extends Task<OperationFailedMsg> {

    private static final String OPERATION_FAILED_REGEXP =
            "(201[2-9].[0-1][0-9].[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*).*\\[ERROR\\].*Operation failed";
    private static final Pattern opeationFailedPattern = Pattern.compile(OPERATION_FAILED_REGEXP);

    @Override
    protected OperationFailedMsg parseLine(String line) {
        Matcher matcher = opeationFailedPattern.matcher(line);
        if (matcher.find()) {
            OperationFailedMsg operationFailedMsg = new OperationFailedMsg();
            setTimestampToWrapper(operationFailedMsg, matcher);
            return operationFailedMsg;
        }
        return null;
    }
}
