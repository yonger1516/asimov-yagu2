package com.seven.asimov.it.utils.logcat.tasks.securityTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.SmsMessageWrapper;

public class SmsMessageTask extends Task<SmsMessageWrapper> {

    private String message;

    public SmsMessageTask(String message) {
        this.message = message;
    }

    @Override
    protected SmsMessageWrapper parseLine(String line) {
        if (line.contains(message)){
            SmsMessageWrapper wrapper = new SmsMessageWrapper();
            wrapper.setMessageBody(message);
            return wrapper;
        }
        return null;
    }
}
