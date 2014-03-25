package com.seven.asimov.it.utils.logcat.stopconditions;


import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopOnGECondition implements LogcatStopCondition {
    private static final Logger logger = LoggerFactory.getLogger(StopOnGECondition.class.getSimpleName());
    private Map<Task, Integer> tasksToCheck = new HashMap<Task, Integer>();

    public StopOnGECondition() {
    }

    public StopOnGECondition(Task task, int numWrappersExpected) {
        addTaskToCheck(task, numWrappersExpected);
    }

    public void addTaskToCheck(Task task, int numWrappersExpected) {  //Find at least numWrappersExpected  wrappers.
        if (task != null)
            tasksToCheck.put(task, numWrappersExpected);
    }

    @Override
    public boolean onNewWrapper(List<Task> tasks, LogEntryWrapper wrapper) {
        for (Task task : tasksToCheck.keySet()) {
            Integer expectedCount = tasksToCheck.get(task);
            if (expectedCount > task.getLogEntries().size()) {
                return true;
            }
        }
        logger.info("All stop conditions met");
        return false;
    }
}
