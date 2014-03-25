package com.seven.asimov.test.tool.core.testjobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TestJobStates - place to store test job states.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public abstract class TestJobStates {

    private static final Map<TestJobType, TestJobState> TEST_JOB_STATES;
    private static final Map<String, TestJobState> AUTOMATED_TEST_JOB_STATES;

    private static ArrayList<Byte> sButtonSingleRequestStateQueue = new ArrayList<Byte>();

    static {
        TEST_JOB_STATES = new HashMap<TestJobType, TestJobState>();
        TEST_JOB_STATES.put(TestJobType.BUTTON_PERIODIC_REQUESTS, TestJobState.NOT_RUNNING);
        TEST_JOB_STATES.put(TestJobType.BUTTON_LOOPED_REQUESTS, TestJobState.NOT_RUNNING);
    }

    static {
        AUTOMATED_TEST_JOB_STATES = new HashMap<String, TestJobState>();
    }

    public static void setJobState(TestJobType testJobType, TestJobState state) {
        if (testJobType != null) {
            switch (testJobType) {
                case BUTTON_SINGLE_REQUEST:
                    if (state == TestJobState.IS_RUNNING) {
                        sButtonSingleRequestStateQueue.add(new Byte((byte) 0));
                    }
                    if (state == TestJobState.NOT_RUNNING) {
                        sButtonSingleRequestStateQueue.remove(0);
                    }
                default:
                    TEST_JOB_STATES.put(testJobType, state);
                    break;
            }
        }
    }

    public static TestJobState getJobState(TestJobType testJobType) {
        switch (testJobType) {
            case BUTTON_SINGLE_REQUEST:
                if (sButtonSingleRequestStateQueue.size() != 0) {
                    return TestJobState.IS_RUNNING;
                }
                if (sButtonSingleRequestStateQueue.size() == 0) {
                    return TestJobState.NOT_RUNNING;
                }
            default:
                return TEST_JOB_STATES.get(testJobType);
        }
    }

    public static TestJobState setJobState(String testName, TestJobState state) {

        return AUTOMATED_TEST_JOB_STATES.put(testName, state);
    }

    public static TestJobState getJobState(String testJobName) {

        TestJobState result = AUTOMATED_TEST_JOB_STATES.get(testJobName);

        if (result == null) {
            return TestJobState.NOT_RUNNING;
        }

        return result;
    }
}
