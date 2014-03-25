package com.seven.asimov.it.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Class for separate thread.
 */
public abstract class TestCaseThread implements Callable<Throwable> {

    private static final Logger logger = LoggerFactory.getLogger(TestCaseThread.class.getSimpleName());

    /**
     * Delay in milliseconds. Thread will start after this delay.
     */
    private long mThreadStartDelay = 0;

    private boolean mInterrupted = false;

    /**
     * Method to be called in a thread.
     *
     * @throws Throwable
     */
    public abstract void run() throws Throwable;

    public TestCaseThread() {
        logger.info("Thread " + toString() + " has been created.");
    }

    public TestCaseThread(long delay) {
        this.mThreadStartDelay = delay;
        logger.info("Thread " + toString() + " with delay " +delay+ " has been created.");
    }

    public TestCaseThread(double delay) {
        this.mThreadStartDelay = (long) delay;
        logger.info("Thread " + toString() + " with delay " +delay+ " has been created.");
    }

    public final Throwable call() {
        logger.info("Thread " + toString() + " has been called.");
        Throwable t = null;
        try {
            this.run();
        } catch (InterruptedException e) {
        } catch (Throwable e) {
            e.printStackTrace();
            t = e;
        }
        return t;
    }

    public long getDelay() {
        return mThreadStartDelay;
    }

    /**
     * Method for managed interrupting. Method only sets interrupted-flag to true.
     * You can check this flag calling isInterruptedSoftly().
     */
    public synchronized void interruptSoftly() {
        mInterrupted = true;
        logger.info("Thread " + toString() + " has been scheduled for soft interrupt.");
    }

    public synchronized boolean isInterruptedSoftly() {
        return mInterrupted;
    }

}
