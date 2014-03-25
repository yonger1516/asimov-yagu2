package com.seven.asimov.it.base.androidtest;

import android.app.Instrumentation;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.AndroidTestRunner;
import android.test.InstrumentationTestRunner;
import android.util.Log;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.exceptionhandler.TFExceptionProcessor;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.runner.BaseTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * The <code>FixtureSharingTestRunner</code> class is an {@link InstrumentationTestRunner} that allows multiple Android
 * test cases to have a shared test fixture allowing creation of large integration tests.
 *
 * @author Maksim Solodovnikov (msolodovnikov@seven.com)
 */
public class FixtureSharingTestRunner extends InstrumentationTestRunner {
    private static final Logger logger = LoggerFactory.getLogger(FixtureSharingTestRunner.class.getSimpleName());
    private static final String LOG_TAG = "InstrumentationTestRunner";
    public static final String ARGUMENT_DELAY_MSEC = "delay_msec";
    protected static final String REPORT_KEY_START_TIME = "start_time";

    /**
     * Override this to set up a shared test fixture.
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        // Empty
    }

    /**
     * Override this to tear down a shared test fixture.
     *
     * @throws Exception
     */
    protected void tearDown() throws Exception {
        // Empty
    }

    @Override
    protected AndroidTestRunner getAndroidTestRunner() {
        return new AndroidTestRunnerParallel() {
            @Override
            public void setTest(Test test) {
                super.setTest(test);

                List<TestCase> testCases = getTestCases();

                testCases.add(0, new AndroidTestCase() {
                    {
                        setName("setupSharedTestFixture");
                    }

                    public void setupSharedTestFixture() throws Throwable {
                        FixtureSharingTestRunner.this.setUp();
                    }
                });

                testCases.add(new AndroidTestCase() {
                    {
                        setName("tearDownSharedTestFixture");
                    }

                    public void tearDownSharedTestFixture() throws Throwable {
                        FixtureSharingTestRunner.this.tearDown();
                    }
                });
            }

            @Override
            public void runTest() {
                super.clearTestListeners();
                super.addTestListener(new WatcherResultPrinter(super.getTestCases().size()));
                super.addTestListener(new TestPrinter("TestRunner", false));
                super.runTest();
            }
        };
    }

    private int mDelay;

    /* (non-Javadoc)
     * @see android.test.InstrumentationTestRunner#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        try {
            Object delay = arguments.get(ARGUMENT_DELAY_MSEC);  // Accept either string or int
            if (delay != null) mDelay = Integer.parseInt(delay.toString());
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Invalid delay_msec parameter", e);
        }
    }

    private class WatcherResultPrinter implements TestListener {
        private class TestResultStats {
            public int mTestResultCode;
            public Bundle mTestResult;
        }

        private final Bundle mResultTemplate;
        int mTestNum = 0;
        String mTestClass = null;
        boolean mIsTimedTest = false;
        boolean mIncludeDetailedStats = false;
        private Hashtable<Long, TestResultStats> mTestResults;

        public WatcherResultPrinter(int numTests) {
            mResultTemplate = new Bundle();
            mResultTemplate.putString(Instrumentation.REPORT_KEY_IDENTIFIER, REPORT_VALUE_ID);
            mResultTemplate.putInt(REPORT_KEY_NUM_TOTAL, numTests);
            mTestResults = new Hashtable<Long, TestResultStats>();
            System.out.println("test delay: " + mDelay);
        }

        /**
         * send a status for the start of a each test, so long tests can be seen
         * as "running"
         */
        public void startTest(Test test) {
            String testClass = test.getClass().getName();
            String testName = ((TestCase) test).getName();
            System.out.println("Test start for " + testName);
            TestResultStats stats = new TestResultStats();

            stats.mTestResult = new Bundle(mResultTemplate);
            stats.mTestResult.putString(REPORT_KEY_NAME_CLASS, testClass);
            stats.mTestResult.putString(REPORT_KEY_NAME_TEST, testName);
            stats.mTestResult.putInt(REPORT_KEY_NUM_CURRENT, ++mTestNum);
            stats.mTestResult.putLong(REPORT_KEY_START_TIME, System.currentTimeMillis());
            // pretty printing
            if (testClass != null && !testClass.equals(mTestClass)) {
                stats.mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                        String.format("\n%s:", testClass));
                mTestClass = testClass;
            } else {
                stats.mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, "");
            }

            // The delay_msec parameter is normally used to provide buffers of idle time
            // for power measurement purposes. To make sure there is a delay before and after
            // every test in a suite, we delay *after* every test (see endTest below) and also
            // delay *before* the first test. So, delay test1 delay test2 delay.

            try {
                if (mTestNum == 1) Thread.sleep(mDelay);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }

            sendStatus(REPORT_VALUE_RESULT_START, stats.mTestResult);
            synchronized (mTestResults) {
                stats.mTestResultCode = 0;
                mTestResults.put(Long.valueOf(Thread.currentThread().getId()), stats);
            }
        }

        /**
         * @see junit.framework.TestListener#addError(Test, Throwable)
         */
        public void addError(Test test, Throwable t) {
            TestResultStats stats = null;
            synchronized (mTestResults) {
                stats = mTestResults.get(Long.valueOf(Thread.currentThread().getId()));
            }
            stats.mTestResult.putString(REPORT_KEY_STACK, BaseTestRunner.getFilteredTrace(t));
            stats.mTestResultCode = REPORT_VALUE_RESULT_ERROR;
            // pretty printing
            stats.mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                    String.format("\nError in %s:\n%s",
                            ((TestCase) test).getName(), BaseTestRunner.getFilteredTrace(t)));
        }

        /**
         * @see junit.framework.TestListener#addFailure(Test, AssertionFailedError)
         */
        public void addFailure(Test test, AssertionFailedError t) {
            TestResultStats stats = null;
            synchronized (mTestResults) {
                stats = mTestResults.get(Long.valueOf(Thread.currentThread().getId()));
            }
            stats.mTestResult.putString(REPORT_KEY_STACK, BaseTestRunner.getFilteredTrace(t));
            stats.mTestResultCode = REPORT_VALUE_RESULT_FAILURE;
            // pretty printing
            stats.mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                    String.format("\nFailure in %s:\n%s",
                            ((TestCase) test).getName(), BaseTestRunner.getFilteredTrace(t)));
        }

        /**
         * @see junit.framework.TestListener#endTest(Test)
         */
        public void endTest(Test test) {
            String testName = ((TestCase) test).getName();
            System.out.println("Test end for " + testName);
            TestResultStats stats = null;
            synchronized (mTestResults) {
                stats = mTestResults.get(Long.valueOf(Thread.currentThread().getId()));
            }
            if (stats.mTestResultCode == 0) {
                stats.mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, ".");
            }
            sendStatus(stats.mTestResultCode, stats.mTestResult);

            try { // Sleep after every test, if specified
                Thread.sleep(mDelay);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        // TODO report the end of the cycle
    }

    /**
     * Prints the test progress to stdout. Android includes a default
     * implementation and calls these methods to print out test progress; you
     * probably will not need to create or extend this class or call its methods manually.
     * See the full {@link android.test} package description for information about
     * getting test results.
     * <p/>
     * {@hide} Not needed for 1.0 SDK.
     */
    private class TestPrinter implements TestListener {

        private String mTag;
        private boolean mOnlyFailures;
        private Set<String> mFailedTests = new HashSet<String>();


        public TestPrinter(String tag, boolean onlyFailures) {
            mTag = tag;
            mOnlyFailures = onlyFailures;
        }

        public void started(String className) {
            if (!mOnlyFailures) {
                logger.info("started: " + className);
            }
        }

        public void finished(String className) {
            if (!mOnlyFailures) {
                logger.info("finished: " + className);
            }
        }

        public void passed(String className) {
            if (!mOnlyFailures) {
                logger.info("passed: " + className);
            }
        }

        public void failed(String className, Throwable exception) {
            logger.info("failed: " + className);
            logger.info("----- begin exception -----");
            logger.info("", exception);
            logger.info("----- end exception -----");
        }

        private void failed(Test test, Throwable t) {
            mFailedTests.add(test.toString());
            failed(test.toString(), t);
            TFExceptionProcessor.processException(t);
        }

        public void addError(Test test, Throwable t) {
            failed(test, t);
        }

        public void addFailure(Test test, junit.framework.AssertionFailedError t) {
            failed(test, t);
        }

        public void endTest(Test test) {
            finished(test.toString());
            if (!mFailedTests.contains(test.toString())) {
                passed(test.toString());
            }
            mFailedTests.remove(test.toString());
        }

        public void startTest(Test test) {
            started(test.toString());
        }
    }

    protected void log(Throwable t) {
        t.printStackTrace();
    }

    protected void endSuite(Exception owner) throws Exception {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                TestUtil.sleep(5 * 1000);
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        };
        if (owner != null) {
            Thread t = new Thread(r);
            t.start();
            throw new Exception(owner.getMessage());
        }
    }
}
