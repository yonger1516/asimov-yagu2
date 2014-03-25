package com.seven.asimov.it.utils.logcat.stopconditions;


import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;

import java.util.List;

public interface LogcatStopCondition {
    public boolean onNewWrapper(List<Task> tasks, LogEntryWrapper wrapper);
}
