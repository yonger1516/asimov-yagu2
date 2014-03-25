package com.seven.asimov.test.tool.core.testjobs;

import android.app.PendingIntent;

/**
 * PendingTestJob class.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public class PendingTestJob {

    private TestJobType mTestJobType;
    private String mTestSuiteName;
    private PendingIntent mPendingIntent;

    public PendingTestJob(PendingIntent pendingIntent, TestJobType testJobType) {
        mPendingIntent = pendingIntent;
        mTestJobType = testJobType;
    }

    public PendingTestJob(PendingIntent pendingIntent, String testSuiteName) {
        mPendingIntent = pendingIntent;
        mTestSuiteName = testSuiteName;
    }

    public PendingIntent getPendingIntent() {
        return mPendingIntent;
    }

    public TestJobType getTestJobType() {
        return mTestJobType;
    }

    public String getTestSuiteName() {
        return mTestSuiteName;
    }
}
