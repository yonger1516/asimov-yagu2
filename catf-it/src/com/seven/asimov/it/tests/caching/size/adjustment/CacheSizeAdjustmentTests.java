package com.seven.asimov.it.tests.caching.size.adjustment;

import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.testcases.CacheSizeTestCase;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CacheOccupiedSpaceTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CacheSizeTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.FreeSpaceTask;
import org.apache.http.client.methods.HttpGet;

import java.util.ArrayList;
import java.util.List;

    /*
    *Pre-requests:
    *1. Set timezone of device to GMT (+3:00)
    *2. Use device with free memory more then 600 MB
    *3. Install OC client with branding, that has next parameters :
    *    client.openchannel.cache.totalsize=52428800
    *    client.openchannel.cache.percenttotalsize=10
    *    client.openchannel.cache.spacecheckinterval=100
    *    (branding eng002_nozip_ga_qa_test_it_rooted_cache_size)
    **/

public class CacheSizeAdjustmentTests extends CacheSizeTestCase {
    private static final String TAG = CacheSizeAdjustmentTests.class.getSimpleName();
    private int requestId = 0;
     /*
     * <p> OC should set size of cache in accordance with formula min(totalsize, percenttotalsize * physical_memory_available_at_a_time)
     * each time when write something in cache.</p>
     * <p> As free space on device is more than 600 MB after receiving all three responses cache size should
     * be updated and take value of first argument (52428800).</p>
     */

    @LargeTest
    public void test_001_CacheSizeAdjustment() throws Throwable {
        final String RESOURCE_URI = "asimov_it_cv_test_001_CacheSizeAdjustment";
        int size = 524030;
        List<HttpRequest> requests = new ArrayList<HttpRequest>(3);
        String[] bodies = new String[]{"a", "b", "c"};
        final CacheSizeTask cacheSizeTask = new CacheSizeTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cacheSizeTask);

        for (int i = 0; i < 3; i++){
            requests.add(createRequest().setUri(createTestResourceUri(RESOURCE_URI + i))
                    .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cache-Control", "max-age=1000")
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .addHeaderField("X-OC-ResponseContentSize", size + "," + bodies[i]).getRequest());
        }

        assertTrue("Available memory should be more than 600 MB, but was " + getAvailableMemoryInMB(), getAvailableMemoryInMB() > 600);
        logcatUtil.start();
        long cacheSize = 0L;
        try {
        for (int i = 0; i < 3; i++) {
            checkMiss(requests.get(i), requestId++);
            logSleeping(5 * 1000);
            assertTrue("Cache size update should be reported in client log", !cacheSizeTask.getLogEntries().isEmpty());
            cacheSize = Long.parseLong(cacheSizeTask.getLogEntries().get(cacheSizeTask.getLogEntries().size() - 1).getCacheSize());
            assertTrue("Cache size should be 52428800, but was " + cacheSize , cacheSize == 52428800L);
            cacheSizeTask.reset();
        }
        logcatUtil.stop();
        } finally {
            logcatUtil.stop();
        }
    }

    /*
    * <p> OC should set size of cache in accordance with formula min(totalsize, percenttotalsize * physical_memory_available_at_a_time)
    * each time when write something in cache.</p>
    * <p> As free space on device is less than 600 MB after receiving all three responses cache size should
    * be updated and take value of second argument in accordance with formula.</p>
    */

    @LargeTest
    public void test_002_CacheSizeAdjustment() throws Throwable {
        final String RESOURCE_URI = "asimov_it_cv_test_002_CacheSizeAdjustment";
        int size = 524030;
        List<HttpRequest> requests = new ArrayList<HttpRequest>(3);
        String[] bodies = new String[]{"a", "b", "c"};
        final CacheSizeTask cacheSizeTask = new CacheSizeTask();
        final CacheOccupiedSpaceTask cacheOccupiedSpaceTask = new CacheOccupiedSpaceTask();
        final FreeSpaceTask freeSpaceTask = new FreeSpaceTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cacheSizeTask, cacheOccupiedSpaceTask, freeSpaceTask);

        for (int i = 0; i < 3; i++){
            requests.add(createRequest().setUri(createTestResourceUri(RESOURCE_URI + i))
                    .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cache-Control", "max-age=1000")
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .addHeaderField("X-OC-ResponseContentSize", size + "," + bodies[i]).getRequest());
        }

        logcatUtil.start();
        fillSDCardMemoryUpTo300MBfree();
        assertTrue("Available memory should be less than 600 MB, but was " + getAvailableMemoryInMB(), getAvailableMemoryInMB() < 600);

        long currentCacheSize = 0L;
        long currentCacheOccupiedSpace = 0L;
        long currentFreeSpace = 0L;

        try {
            for (int i = 0; i < 3; i++) {
                assertTrue("Free space update should be reported in client log", !freeSpaceTask.getLogEntries().isEmpty());
                currentFreeSpace = Long.parseLong(freeSpaceTask.getLogEntries().get(freeSpaceTask.getLogEntries().size() - 1).getFreeSpace());
                checkMiss(requests.get(i), requestId++);
                logSleeping(5*1000);

                assertTrue("Cache size update should be reported in client log", !cacheSizeTask.getLogEntries().isEmpty());
                assertTrue("Occupied space update should be reported in client log", !cacheOccupiedSpaceTask.getLogEntries().isEmpty());

                currentCacheSize = Long.parseLong(cacheSizeTask.getLogEntries().get(cacheSizeTask.getLogEntries().size() - 1).getCacheSize());
                currentCacheOccupiedSpace = Long.parseLong(cacheOccupiedSpaceTask.getLogEntries().get(cacheOccupiedSpaceTask.getLogEntries().size() - 2).getCacheOccupiedSpace());

                long calculatedCacheSize = Math.min(52428800, (long) ((currentFreeSpace + currentCacheOccupiedSpace) * 0.1));
                assertTrue("Cache size should be " + calculatedCacheSize + ", but was " + currentCacheSize, (currentCacheSize>=(calculatedCacheSize*0.99999))&&(currentCacheSize<=(calculatedCacheSize*1.00001)));
                cacheSizeTask.reset();
                cacheOccupiedSpaceTask.reset();
            }
            logcatUtil.stop();
        } finally {
            logcatUtil.stop();
            freeMemory();
        }
    }

     /*
     * <p> OC should set size of cache in accordance with formula min(totalsize, percenttotalsize * physical_memory_available_at_a_time)
     * each time when write something in cache.</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Free space on device is more than 600 MB.</li>
     * <li>1rd request should be HITed. After receiving of 1st response cache size should be updated and take the value of first argument (52428800) </li>
     * <li>2nd request should be HITed. After receiving of 2nd response cache size should be updated and take the value of first argument (52428800) </li>
     * <li>Free space on device is changed and less than 600 MB.</li>
     * <li>3rd request should be HITed. After receiving of 3rd response cache size should be updated and take the value of second argument in accordance with formula</li>
     * <li>4th request should be HITed. After receiving of 4th response cache size should be updated and take the value of second argument in accordance with formula</li>
     * </ol>
     */

    @LargeTest
    public void test_003_CacheSizeAdjustment() throws Throwable {
        final String RESOURCE_URI = "asimov_it_cv_test_003_CacheSizeAdjustment";
        int size = 524030;
        List<HttpRequest> requests = new ArrayList<HttpRequest>(4);
        String[] bodies = new String[]{"a", "b", "c", "d"};
        final CacheSizeTask cacheSizeTask = new CacheSizeTask();
        final CacheOccupiedSpaceTask cacheOccupiedSpaceTask = new CacheOccupiedSpaceTask();
        final FreeSpaceTask freeSpaceTask = new FreeSpaceTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cacheSizeTask, cacheOccupiedSpaceTask, freeSpaceTask);

        for (int i = 0; i < 4; i++){
            requests.add(createRequest().setUri(createTestResourceUri(RESOURCE_URI + i))
                    .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cache-Control", "max-age=1000")
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .addHeaderField("X-OC-ResponseContentSize", size + "," + bodies[i]).getRequest());
        }

        logSleeping(100*1000);
        assertTrue("Available memory should be more than 600 MB, but was " + getAvailableMemoryInMB(), getAvailableMemoryInMB() > 600);
        Log.i(TAG, "getAvailableMemoryInMB()=" + String.valueOf(getAvailableMemoryInMB()));

        long currentCacheSize = 0L;
        long currentCacheOccupiedSpace = 0L;
        long currentFreeSpace = 0L;
        logcatUtil.start();
        try {
            for (int i = 0; i < 2; i++) {
                checkMiss(requests.get(i), requestId++);
                logSleeping(5 * 1000);
                assertTrue("Cache size update should be reported in client log", !cacheSizeTask.getLogEntries().isEmpty());
                currentCacheSize = Long.parseLong(cacheSizeTask.getLogEntries().get(cacheSizeTask.getLogEntries().size() - 1).getCacheSize());
                assertTrue("Cache size should be 52428800, but was " + currentCacheSize, currentCacheSize == 52428800L);
                cacheSizeTask.reset();
            }

            fillSDCardMemoryUpTo300MBfree();
            logSleeping(100*1000);

            assertTrue("Available memory should be less 600 MB, but was " + getAvailableMemoryInMB(), getAvailableMemoryInMB() < 600);
            Log.i(TAG, "getAvailableMemoryInMB()=" + String.valueOf(getAvailableMemoryInMB()));

            for (int i = 2; i <4; i++) {
                assertTrue("Free space update should be reported in client log", !freeSpaceTask.getLogEntries().isEmpty());
                currentFreeSpace = Long.parseLong(freeSpaceTask.getLogEntries().get(freeSpaceTask.getLogEntries().size() - 1).getFreeSpace());
                checkMiss(requests.get(i), requestId++);
                logSleeping(5*1000);

                assertTrue("Cache size update should be reported in client log", !cacheSizeTask.getLogEntries().isEmpty());
                assertTrue("Occupied space update should be reported in client log", !cacheOccupiedSpaceTask.getLogEntries().isEmpty());

                currentCacheSize = Long.parseLong(cacheSizeTask.getLogEntries().get(cacheSizeTask.getLogEntries().size() - 1).getCacheSize());
                currentCacheOccupiedSpace = Long.parseLong(cacheOccupiedSpaceTask.getLogEntries().get(cacheOccupiedSpaceTask.getLogEntries().size() - 2).getCacheOccupiedSpace());

                long calculatedCacheSize = Math.min(52428800, (long) ((currentFreeSpace+currentCacheOccupiedSpace) * 0.1));
                assertTrue("Cache size should be " + calculatedCacheSize + ", but was " + currentCacheSize, (currentCacheSize>=(calculatedCacheSize*0.99999))&&(currentCacheSize<=(calculatedCacheSize*1.00001)));
                cacheSizeTask.reset();
                cacheOccupiedSpaceTask.reset();
            }
            logcatUtil.stop();
        } finally {
            logcatUtil.stop();
            freeMemory();
        }
    }

    /*
     * <p> OC should set size of cache in accordance with formula min(totalsize, percenttotalsize * physical_memory_available_at_a_time)
     * each time when write something in cache. After killing of OCE process, OCE restart, all CE should be loaded from database, and restored anew</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>Free space on device is more than 600 MB.</li>
     * <li>First three requests should be HITed. After receiving of the responses cache size should be updated and take the value of second argument </li>
     * <li>OCE process is killed </li>
     * <li>OCE restarted, all CE should be loaded from database, next three responses should be taken from cache.</li>
     * </ol>
     */

    @LargeTest
    public void test_004_CacheSizeAdjustment() throws Throwable {

        final String RESOURCE_URI = "asimov_it_cv_test_004_CacheSizeAdjustment";
        int size = 524030;
        List<HttpRequest> requests = new ArrayList<HttpRequest>(3);
        String[] bodies = new String[]{"a", "b", "c"};
        String[] killOcc = {"su", "-c", "kill -9 occ"};
        final CacheSizeTask cacheSizeTask = new CacheSizeTask();
        final CacheOccupiedSpaceTask cacheOccupiedSpaceTask = new CacheOccupiedSpaceTask();
        final FreeSpaceTask freeSpaceTask = new FreeSpaceTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cacheSizeTask, cacheOccupiedSpaceTask, freeSpaceTask);

        for (int i = 0; i < 3; i++){
            requests.add(createRequest().setUri(createTestResourceUri(RESOURCE_URI + i))
                    .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("Cache-Control", "max-age=1000")
                    .addHeaderField("X-OC-AddHeader_Date", "GMT")
                    .addHeaderField("X-OC-ResponseContentSize", size + "," + bodies[i]).getRequest());
        }

        logcatUtil.start();
        fillSDCardMemoryUpTo300MBfree();
        logSleeping(100 * 1000);

        assertTrue("Available memory should be less 600 MB, but was " + getAvailableMemoryInMB(), getAvailableMemoryInMB() < 600);

        long currentCacheSize = 0L;
        long currentCacheOccupiedSpace = 0L;

        forbidTraffic(true);
        try {
            for (int i = 0; i < 3; i++) {
                assertTrue("Free space update should be reported in client log", !freeSpaceTask.getLogEntries().isEmpty());
                long currentFreeSpace = Long.parseLong(freeSpaceTask.getLogEntries().get(freeSpaceTask.getLogEntries().size() - 1).getFreeSpace());
                checkMiss(requests.get(i), requestId++);
                logSleeping(5 * 1000);

                assertTrue("Cache size update should be reported in client log", !cacheSizeTask.getLogEntries().isEmpty());
                assertTrue("Occupied space update should be reported in client log", !cacheOccupiedSpaceTask.getLogEntries().isEmpty());

                currentCacheSize = Long.parseLong(cacheSizeTask.getLogEntries().get(cacheSizeTask.getLogEntries().size() - 1).getCacheSize());
                currentCacheOccupiedSpace = Long.parseLong(cacheOccupiedSpaceTask.getLogEntries().get(cacheOccupiedSpaceTask.getLogEntries().size() - 2).getCacheOccupiedSpace());

                long calculatedCacheSize = Math.min(52428800, (long) ((currentFreeSpace + currentCacheOccupiedSpace) * 0.1));
                Log.i(TAG, "Calculated cache size " + calculatedCacheSize + "; Real cache size " + currentCacheSize);
                assertTrue("Cache size should be " + calculatedCacheSize + ", but was " + currentCacheSize, (currentCacheSize>=(calculatedCacheSize*0.99999))&&(currentCacheSize<=(calculatedCacheSize*1.00001)));
                cacheSizeTask.reset();
                cacheOccupiedSpaceTask.reset();
            }

            Runtime.getRuntime().exec(killOcc).waitFor();
            logSleeping(60*1000);

            for (int i = 0; i < 3; i++) {
                checkHit(requests.get(i), requestId++);
                assertTrue("Cache size update should be reported in client log", cacheSizeTask.getLogEntries().isEmpty());
            }
            logcatUtil.stop();
        } finally {
            logcatUtil.stop();
            freeMemory();
            forbidTraffic(false);
        }
    }
}
