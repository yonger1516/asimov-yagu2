package com.seven.asimov.test.tool.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.seven.asimov.test.tool.core.TestFactory;
import com.seven.asimov.test.tool.core.testjobs.TestJob;
import com.seven.asimov.test.tool.core.testjobs.TestJobState;
import com.seven.asimov.test.tool.core.testjobs.TestJobStates;
import com.seven.asimov.test.tool.core.testjobs.TestJobType;

/**
 * AlarmReceiver BroadcastReceiver.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_ALARM = "com.seven.asimov.test.tool.intent.action.ALARM";

    private static final String LOG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            final Bundle bundle = intent.getExtras();
            TestJobType tjType = TestJobType.get(bundle.getInt("TestJobType"));
            Log.d(LOG, "TestJob: " + tjType);

            String tsName = bundle.getString("TestSuiteName");

            TestJob newTestJob = null;

            if (tjType == TestJobType.AUTOMATED_TEST) {
                TestFactory.removeLastPendingIntent(tsName);
            } else {
                TestFactory.removeLastPendingIntent(tjType);
            }

            switch (tjType) {
                case BUTTON_SINGLE_REQUEST:

                    newTestJob = new TestJob(tjType);
                    TestFactory.addTestJob(newTestJob);

                    break;
                case BUTTON_PERIODIC_REQUESTS:
                    if (TestJobStates.getJobState(tjType) == TestJobState.IS_RUNNING) {

                        newTestJob = new TestJob(tjType);
                        TestFactory.addTestJob(newTestJob);

                    }
                    break;
                case BUTTON_LOOPED_REQUESTS:
                    if (TestJobStates.getJobState(tjType) == TestJobState.IS_RUNNING) {

                        newTestJob = new TestJob(tjType);
                        TestFactory.addTestJob(newTestJob);

                    }
                case AUTOMATED_TEST:
                    if (TestJobStates.getJobState(tsName) == TestJobState.IS_RUNNING) {

                        newTestJob = new TestJob(tjType);
                        TestFactory.addTestJob(newTestJob);

                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            Log.e(LOG, ex.getMessage());
        }
    }
}
