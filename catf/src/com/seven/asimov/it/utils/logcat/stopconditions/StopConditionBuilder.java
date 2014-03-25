package com.seven.asimov.it.utils.logcat.stopconditions;


import com.seven.asimov.it.utils.logcat.tasks.Task;

public class StopConditionBuilder {
    StopOnGECondition condition = null;

    private StopConditionBuilder(StopOnGECondition condition) {
        this.condition = condition;
    }

    public static StopConditionBuilder GECondition() {   //Greater or Equal condition
        return new StopConditionBuilder(new StopOnGECondition());
    }

    public StopConditionBuilder addTask(Task task, int numWrappersExpected) {
        condition.addTaskToCheck(task, numWrappersExpected);
        return this;
    }

    public LogcatStopCondition getCondition() {
        return condition;
    }
}
