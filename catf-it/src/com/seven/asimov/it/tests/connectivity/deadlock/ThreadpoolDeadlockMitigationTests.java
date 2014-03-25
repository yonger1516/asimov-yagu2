package com.seven.asimov.it.tests.connectivity.deadlock;

import com.seven.asimov.it.testcases.ThreadpoolDeadlockMitigationTestCase;

/**
 * <h1>ThreadpoolDeadlockMitigation Suite</h1>
 * <p>The tests are designed to check ThreadpoolDeadlockMitigation</p>
 * <p>{@link com.seven.asimov.it.tests.connectivity.deadlock.ThreadpoolDeadlockMitigationTests#test_001_stubGuard() Test1}
 * {@link com.seven.asimov.it.tests.connectivity.deadlock.ThreadpoolDeadlockMitigationTests#test_002_taskTimeoutGuard() Test2}
 * {@link com.seven.asimov.it.tests.connectivity.deadlock.ThreadpoolDeadlockMitigationTests#test_003_taskTimeoutGuardWithPolicy() Test3}
 * {@link com.seven.asimov.it.tests.connectivity.deadlock.ThreadpoolDeadlockMitigationTests#test_004_taskTimeoutGuardWithProcessesCheck() Test4}</p>
 * <h2>Pre-requisites:</h2>
 * <p>Apk for testing must be placed on SD card with name asimov-signed-deadlock.apk</p>
 *
 * <h2>For run test used next command</h2>
 * <p>adb shell am instrument -w -e class com.seven.asimov.it.tests.connectivity.deadlock.ThreadpoolDeadlockMitigationTests#&#8249test_name&#8250 com.seven.asimov.it/com.seven.asimov.it.IntegrationTestRunnerGa</p>
 */
public class ThreadpoolDeadlockMitigationTests extends ThreadpoolDeadlockMitigationTestCase {

    /**
     * <h2>Pre-requisites:</h2>
     * <p>Set policies:</p>
     * <ol>
     * <li>asimov@threadpool@deadlock_guard@stub_guard</li>
     * </ol>
     * @throws Throwable
     */
    public void test_001_stubGuard() throws Throwable {
        checkThreadPoolDeadlockMitigationM("001", false, false);
    }

    /**
     * <h2>Pre-requisites:</h2>
     * <p>Set policies:</p>
     * <ol>
     * <li>asimov@threadpool@deadlock_guard@task_timeout_guard</li>
     * </ol>
     * @throws Throwable
     */
    public void test_002_taskTimeoutGuard() throws Throwable {
        checkThreadPoolDeadlockMitigationM("002", true, false);
    }

    /**
     * <h2>Pre-requisites:</h2>
     * <p>Set policies:</p>
     * <ol>
     * <li>asimov@threadpool@deadlock_guard@task_timeout_guard@max_execution_time=15</li>
     * </ol>
     * @throws Throwable
     */
    public void test_003_taskTimeoutGuardWithPolicy() throws Throwable {
        checkThreadPoolDeadlockMitigationM("003", true, false);
    }

    /**
     * <h2>Pre-requisites:</h2>
     * <p>Set policies:</p>
     * <ol>
     * <li>asimov@threadpool@deadlock_guard@task_timeout_guard@max_execution_time=15</li>
     * </ol>
     * @throws Throwable
     */
    public void test_004_taskTimeoutGuardWithProcessesCheck() throws Throwable {
        checkThreadPoolDeadlockMitigationM("003", true, true);
    }
}
