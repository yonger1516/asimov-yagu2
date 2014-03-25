package com.seven.asimov.it.base;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ThreadLocker {

    public static long TIMEOUT = 10 * 60 * 1000;
    private Semaphore mSemaphore = new Semaphore(1);
    private long mTimeOut = TIMEOUT;

    public ThreadLocker() throws Throwable {
        this(0);
    }

    public ThreadLocker(long timeOut) throws Throwable {
        if (timeOut > 0) {
            mTimeOut = timeOut;
        }
        mSemaphore.acquire();
    }

    /**
     * Blocks thread until some another thread calls unlock()
     *
     * @throws Throwable
     */
    public void waitForUnlock() throws Throwable {
        boolean ok = mSemaphore.tryAcquire(mTimeOut, TimeUnit.MILLISECONDS);
        if (!ok) throw new Exception("Timeout occured during waitForUnlock operation, timeout: " + mTimeOut);
    }

    public void unlock() {
        mSemaphore.release(10);
    }

    public static ThreadLocker[] getLockers(int size) throws Throwable {
        ThreadLocker[] lockers = new ThreadLocker[size];
        for (int i = 0; i < size; i++) {
            lockers[i] = new ThreadLocker();
        }
        return lockers;
    }


}