package com.seven.asimov.it.testcases;

import android.os.Environment;
import com.seven.asimov.it.asserts.CATFAssert;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.DeadlockTask;

import java.io.File;

public class ThreadpoolDeadlockMitigationTestCase extends TcpDumpTestCase {

    private static final long WAIT_TIME = 5 * 60 * 1000;

    protected void checkThreadPoolDeadlockMitigationM(String testID, boolean taskTimeoutGuard, boolean processesCheck)
            throws Throwable {
        String url = createTestResourceUri("thread_pool_deadlock_mitigation_" + testID);
        DeadlockTask deadlockTask = new DeadlockTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), deadlockTask);
        try {
            PrepareResourceUtil.prepareResource(url, false);
            HttpRequest request = HttpRequest.Builder.create()
                    .setMethod(TFConstantsIF.GET_METHOD)
                    .setUri(url)
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .getRequest();
            logcatUtil.start();
            OCUtil.reinstallOCC(Environment.getExternalStorageDirectory().getPath() + File.separator + "asimov-signed-deadlock.apk");
            checkMiss(request, 1);
            logSleeping(35 * 1000);
            checkMiss(request, 2);
            logSleeping(35 * 1000);
            checkMiss(request, 3);
            logSleeping(35 * 1000);
            checkHit(request, 4);
            logSleeping(WAIT_TIME);
            if (taskTimeoutGuard) {
                CATFAssert.assertTrue("Deadlock was not detected", deadlockTask.getLogEntries().size() != 0);
            } else {
                CATFAssert.assertTrue("Deadlock was detected", deadlockTask.getLogEntries().size() == 0);
            }
            if (processesCheck) {
                CATFAssert.assertTrue("OC work not correct", OCUtil.isOpenChannelRunning());
            }
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(url);
            logcatUtil.stop();
        }
    }
}
