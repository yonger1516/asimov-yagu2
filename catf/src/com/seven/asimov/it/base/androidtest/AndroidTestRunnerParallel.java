package com.seven.asimov.it.base.androidtest;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import android.app.Instrumentation;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.AndroidTestRunner;
import android.test.InstrumentationTestCase;
import android.test.TestSuiteProvider;

import com.seven.asimov.it.annotation.ParallelExecution;

public class AndroidTestRunnerParallel extends AndroidTestRunner {

    private TestResult mTestResult;
    private String mTestClassName;
    private List<TestCase> mTestCases;
    private Context mContext;
    private boolean mSkipExecution = false;

    private List<TestListener> mTestListeners = new ArrayList<TestListener>();
    private Instrumentation mInstrumentation;
    
    private static int MAX_THREADS = 3;

    @SuppressWarnings("unchecked")
    public void setTestClassName(String testClassName, String testMethodName) {
        super.setTestClassName(testClassName, testMethodName);
        mTestCases = super.getTestCases();
        mTestClassName = super.getTestClassName();
    }


    public void setTest(Test test) {
        super.setTest(test);
        mTestCases = super.getTestCases();
        mTestClassName = super.getTestClassName();
    }

    public void clearTestListeners() {
        super.clearTestListeners();
        mTestListeners.clear();
    }

    public void addTestListener(TestListener testListener) {
        super.addTestListener(testListener);
        if (testListener != null) {
            mTestListeners.add(testListener);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Test> loadTestClass(String testClassName) {
        try {
            return (Class<? extends Test>) mContext.getClassLoader().loadClass(testClassName);
        } catch (ClassNotFoundException e) {
            runFailed("Could not find test class. Class: " + testClassName);
        }
        return null;
    }

    private TestCase buildSingleTestMethod(Class testClass, String testMethodName) {
        try {
            TestCase testCase = (TestCase) testClass.newInstance();
            testCase.setName(testMethodName);
            return testCase;
        } catch (IllegalAccessException e) {
            runFailed("Could not access test class. Class: " + testClass.getName());
        } catch (InstantiationException e) {
            runFailed("Could not instantiate test class. Class: " + testClass.getName());
        }

        return null;
    }

    private boolean shouldRunSingleTestMethod(String testMethodName,
            Class<? extends Test> testClass) {
        return testMethodName != null && TestCase.class.isAssignableFrom(testClass);
    }

    private Test getTest(Class clazz) {
        if (TestSuiteProvider.class.isAssignableFrom(clazz)) {
            try {
                TestSuiteProvider testSuiteProvider =
                        (TestSuiteProvider) clazz.getConstructor().newInstance();
                return testSuiteProvider.getTestSuite();
            } catch (InstantiationException e) {
                runFailed("Could not instantiate test suite provider. Class: " + clazz.getName());
            } catch (IllegalAccessException e) {
                runFailed("Illegal access of test suite provider. Class: " + clazz.getName());
            } catch (InvocationTargetException e) {
                runFailed("Invocation exception test suite provider. Class: " + clazz.getName());
            } catch (NoSuchMethodException e) {
                runFailed("No such method on test suite provider. Class: " + clazz.getName());
            }
        }
        return getTest(clazz.getName());
    }

    protected TestResult createTestResult() {
        if (mSkipExecution) {
            return new NoExecTestResult();
        }
        return new TestResult();
    }

    void setSkipExecution(boolean skip) {
        mSkipExecution = skip;
    }

    public List<TestCase> getTestCases() {
        return mTestCases;
    }

    public String getTestClassName() {
        return mTestClassName;
    }

    public TestResult getTestResult() {
        return mTestResult;
    }

    public void runTest() {
        runTest(createTestResult());
    }

    public void runTest(TestResult testResult) {
        mTestResult = testResult;

        for (TestListener testListener : mTestListeners) {
            mTestResult.addListener(testListener);
        }
        
        String currentTestCase = "";
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        
        Context testContext = mInstrumentation == null ? mContext : mInstrumentation.getContext();
        for (TestCase testCase : mTestCases) {
            setContextIfAndroidTestCase(testCase, mContext, testContext);
            setInstrumentationIfInstrumentationTestCase(testCase, mInstrumentation);
            boolean newTestCase = !currentTestCase.equalsIgnoreCase(testCase.getClass().getName());
            currentTestCase = testCase.getClass().getName();
            boolean parallel = false;
            try {
                Class c = Class.forName(testCase.getClass().getName());
                Annotation[] anno = c.getMethod(testCase.getName(), null).getAnnotations();
                parallel = c.getMethod(testCase.getName(), null).isAnnotationPresent(ParallelExecution.class);
                if (!parallel) {
                    parallel = c.isAnnotationPresent(ParallelExecution.class);
                }
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            if (newTestCase || !parallel) {
                // wait for finishing all currently submitted tests
                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                    executor = Executors.newFixedThreadPool(MAX_THREADS);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            executor.submit(new TestRun(testCase, mTestResult));
         
            if (!parallel) {
                // wait for finishing all currently submitted tests
                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                    executor = Executors.newFixedThreadPool(MAX_THREADS);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
       
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setContext(Context context) {
        mContext = context;
        super.setContext(context);
    }

    private void setContextIfAndroidTestCase(Test test, Context context, Context testContext) {
        if (AndroidTestCase.class.isAssignableFrom(test.getClass())) {
            AndroidTestCase andrTest = (AndroidTestCase) test;
            andrTest.setContext(context);
            Class c;
            try {
                c = Class.forName(andrTest.getClass().getName());
                Method m = c.getMethod("setTestContext", Context.class);
                m.invoke(andrTest, testContext);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private void setInstrumentationIfInstrumentationTestCase(
            Test test, Instrumentation instrumentation) {
        if (InstrumentationTestCase.class.isAssignableFrom(test.getClass())) {
            ((InstrumentationTestCase) test).injectInstrumentation(instrumentation);
        }
    }

    public void setInstrumentation(Instrumentation instrumentation) {
        mInstrumentation = instrumentation;
        super.setInstrumentation(instrumentation);
    }

    /**
     * @deprecated Incorrect spelling,
     * use {@link #setInstrumentation(android.app.Instrumentation)} instead.
     */

    @Deprecated
    public void setInstrumentaiton(Instrumentation instrumentation) {
        setInstrumentation(instrumentation);
    }

    @Override
    protected Class loadSuiteClass(String suiteClassName) throws ClassNotFoundException {
        return mContext.getClassLoader().loadClass(suiteClassName);
    }

    public void testStarted(String testName) {
    }

    public void testEnded(String testName) {
    }

    public void testFailed(int status, Test test, Throwable t) {
    }

    protected void runFailed(String message) {
        throw new RuntimeException(message);
    }
    
    class NoExecTestResult extends TestResult {

        /**
         * Override parent to just inform listeners of test,
         * and skip test execution.
         */
        @Override
        protected void run(final TestCase test) {
            startTest(test);
            endTest(test);
        }

    }
    
    private final class TestRun implements Runnable {
        TestCase mTest;
        TestResult mResult;
        CountDownLatch mDoneSignal;
        
        public TestRun(TestCase test, TestResult result) {
            mTest = test;
            mResult = result;
        }
        public void run() {
            if (mTest != null && mResult != null) {
                mTest.run(mResult);
            }
        }
    }
}
