package com.seven.asimov.test.tool.utils.logcat.wrapper;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TestCaseEvents implements Serializable {

    private Map<String, TestStatus> testCases = new LinkedHashMap<String, TestStatus>();
    private String currentTestName;
    private TestStatus currentTestStatus;

    private Timestamp timeStampTestCasesStarted;
    private Timestamp timeStampTestCasesFinished;
    private long durationOfAllTestCasesMs;

    public long geturationOfAllTestCasesMs() {
        return durationOfAllTestCasesMs;
    }

    public Timestamp getTimeStampTestCasesStarted() {
        return timeStampTestCasesStarted;
    }

    public void setTimeStampTestCasesStarted(Timestamp timeStampTestCasesStart) {
        timeStampTestCasesStarted = timeStampTestCasesStart;
    }

    public Timestamp getTimeStampTestCasesFinished() {
        return timeStampTestCasesFinished;
    }

    public void setTimeStampTestCasesFinished(Timestamp timeStampTestCasesFinish) {
        timeStampTestCasesFinished = timeStampTestCasesFinish;
        durationOfAllTestCasesMs = timeStampTestCasesFinished.getTime() - timeStampTestCasesStarted.getTime();
    }

    public TestStatus getCurrentTestStatus() {
        return currentTestStatus;
    }

    public String getCurrentTestName() {
        return currentTestName;
    }

    public Map<String, TestStatus> getTestCases() {
        return testCases;
    }

    public void addNewTestCase(String testName, TestStatus testStatus) {
        testCases.put(testName, testStatus);
        currentTestName = testName;
        currentTestStatus = testCases.get(currentTestName);
    }

    public boolean isTestCaseAlreadyAdded(String testName) {
        return testCases.containsKey(testName);
    }

    public TestStatus getTestCase(String testName) {
        return testCases.get(testName);
    }

    public static class TestStatus {

        private String name;
        private TESTCASE_EVENT testCaseStatus;
        private long startTime;
        private long endTime;
        private long durationOfTestCaseMs;
        private Map<TESTCASE_ERROR, String> errors = new LinkedHashMap<TESTCASE_ERROR, String>();
        private String error;

        private boolean additionalInfo;

        public TestStatus() {
        }

        public TESTCASE_EVENT getTestCaseStatus() {
            return testCaseStatus;
        }

        public void setTestCaseStatus(TESTCASE_EVENT testCaseStatus) {
            this.testCaseStatus = testCaseStatus;
        }

        public Map<TESTCASE_ERROR, String> getErrors() {
            return errors;
        }

        public void addErrorMessage(TESTCASE_ERROR error, String errorMessage) {
            getErrors().put(error, errorMessage);
        }

        @Override
        public String toString() {
            String returnTestStatus = "TEST-CASE INFO \n" +
                    "duration in seconds = " + (getDurationOfTestCaseMs() / 1000) + "\n" +
                    //"STARTED at " + timeStampStart + "\n"
                    //+ this.testCaseStatus + " at " +timeStampFinish +
                    "\n" + "error messages during execurion of the test: \n";

            Set<Map.Entry<TESTCASE_ERROR, String>> set = getErrors().entrySet();
            for (Map.Entry<TESTCASE_ERROR, String> error : set) {
                returnTestStatus += error.getKey() + ": " + error.getValue() + "\n";
            }
            return returnTestStatus;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public long getDurationOfTestCaseMs() {
            return durationOfTestCaseMs;
        }

        public boolean getAdditionalInfo() {
            return this.additionalInfo;
        }

        public void setDurationOfTestCaseMs(long durationOfTestCaseMs) {
            this.durationOfTestCaseMs = durationOfTestCaseMs;
        }

        public void setAdditionalInfo(boolean status) {
            this.additionalInfo = status;
        }

        public void setErrors(Map<TESTCASE_ERROR, String> errors) {
            this.errors = errors;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    public static enum TESTCASE_EVENT {
        STARTED,
        PASSED,
        FAILED,
        SUSPENDED,
        IGNORED
    }

    public static enum TESTCASE_ERROR {
        ASSERTION_FAILED_ERROR,
        SSL_EXCEPTION,
        UNKNOWN_HOST_EXCEPTION
    }

    @Override
    public String toString() {
        String returnTestResults = "duration in seconds = " + (durationOfAllTestCasesMs / 1000) + " \n"
                + "started at " + timeStampTestCasesStarted
                + "\nfinished at " + timeStampTestCasesFinished + "\n\n";
        Set<Map.Entry<String, TestStatus>> set = testCases.entrySet();
        int i = 1;
        for (Map.Entry<String, TestStatus> testCase : set) {
            returnTestResults += "\n" + "Test case #" + (i++) + " " + testCase.getKey() + ": \n" + testCase.getValue().toString() + "\n\n";
        }
        return returnTestResults;
    }
}
