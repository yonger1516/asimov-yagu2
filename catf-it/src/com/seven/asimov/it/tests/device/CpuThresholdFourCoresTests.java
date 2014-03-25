package com.seven.asimov.it.tests.device;

import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.testcases.CpuThresholdTestCase;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.CpuUsageTask;
import com.seven.asimov.it.utils.logcat.tasks.ServiceLogTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * For devices with four cores
 */

public class CpuThresholdFourCoresTests extends CpuThresholdTestCase {
    private static final Logger logger = LoggerFactory.getLogger(CpuThresholdFourCoresTests.class.getSimpleName());

    /**
     * <h3>OC should report when CPU usage of Engine exceeds a yellow threshold on device with 4 cores</h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_yellow = 8
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of Engine should exceed 2% and should remain at a higher level more than 1000 ms.
     * 4. Logcat message with usage percentage should be written once per crossing, it should has an INFO level.
     * 5. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,oce,cpu_yellow,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_004_CPU_threshold() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_OCE, CPU_USAGE_HIGHER_THAN_NORMAL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_OCE, CPU_YELLOW);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "85", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadLocaleResource(20);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(25);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(30);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(35);
            Thread.sleep(60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    /**
     * <h3>OC should report when CPU usage of Engine exceeds a red threshold on device with 4 cores</h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_red = 12
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.com and https://isocket.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of Engine should exceed 3% and should remain at a higher level more than 1000 ms.
     * 4. Logcat message with usage percentage should be written once per crossing, it should has an WARNING level.
     * 5. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,oce,cpu_red,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_005_CPU_threshold() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_WARNING, CPU_USAGE_OF_OCE, CPU_USAGE_CRITICAL_LEVEL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_OCE, CPU_RED);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "12", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadLocaleResource(100);
            Thread.sleep(60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    /**
     * <h3>OC should report when CPU usage of Engine drops back below yellow threshold on device with 4 cores </h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_yellow = 8
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of Engine should exceed 2% and should remain at a higher level more then 1000 ms
     * 4. CPU usage should drop back below yellow threshold and remain at a lower level more than 1000 ms.
     * 5. INFO logcat message should be recorded to indicate that situation is back to normal.
     * 6. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,oce,cpu_green,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_006_CPU_threshold() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_OCE, CPU_USAGE_HIGHER_THAN_NORMAL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_OCE, CPU_YELLOW);
        CpuUsageTask cpuUsageTaskGreen = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_OCE, CPU_USAGE_BACK_TO_NORMAL);
        ServiceLogTask serviceLogTaskGreen = new ServiceLogTask(CPU_USAGE_OF_OCE, CPU_GREEN);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask,
                cpuUsageTaskGreen, serviceLogTaskGreen);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "85", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadLocaleResource(20);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(25);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(30);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(35);
            Thread.sleep(60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
            checkCpuUsage(cpuUsageTaskGreen, serviceLogTaskGreen);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    /**
     * <h3>OC should report when CPU usage of Controller exceeds a yellow threshold on device with 4 cores</h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_yellow = 8
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of Controller should exceed 2% and should remain at a higher level more than 1000 ms.
     * 4. Logcat message with usage percentage should be written once per crossing, it should has an INFO level.
     * 5. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,occ,cpu_yellow,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_010_CPU_threshold() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_OCC, CPU_USAGE_HIGHER_THAN_NORMAL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_OCC, CPU_YELLOW);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "85", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadLocaleResource(20);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(25);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(30);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(35);
            Thread.sleep(60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    /**
     * <h3>OC should report when CPU usage of Controller exceeds a red threshold on device with 4 cores</h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_red = 12
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.com and https://isocket.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of Controller should exceed 3% and should remain at a higher level more than 1000 ms.
     * 4. Logcat message with usage percentage should be written once per crossing, it should has an WARNING level.
     * 5. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,occ,cpu_red,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_011_CPU_threshold() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_WARNING, CPU_USAGE_OF_OCC, CPU_USAGE_CRITICAL_LEVEL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_OCC, CPU_RED);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "12", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadLocaleResource(100);
            Thread.sleep(60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    /**
     * <h3>OC should report when CPU usage of Controller drops back below yellow threshold on device with 4 cores</h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_yellow = 8
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of Controller should exceed 2% and should remain at a higher level more then 1000 ms
     * 4. CPU usage should drop back below yellow threshold and remain at a lower level more than 1000 ms.
     * 5. INFO logcat message should be recorded to indicate that situation is back to normal.
     * 6. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,occ,cpu_green,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_012_CPU_threshold() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_OCC, CPU_USAGE_HIGHER_THAN_NORMAL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_OCC, CPU_YELLOW);
        CpuUsageTask cpuUsageTaskGreen = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_OCC, CPU_USAGE_BACK_TO_NORMAL);
        ServiceLogTask serviceLogTaskGreen = new ServiceLogTask(CPU_USAGE_OF_OCC, CPU_GREEN);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask,
                cpuUsageTaskGreen, serviceLogTaskGreen);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "85", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadLocaleResource(20);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(25);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(30);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(35);
            Thread.sleep(60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
            checkCpuUsage(cpuUsageTaskGreen, serviceLogTaskGreen);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    /**
     * <h3>OC should report when CPU usage of dispatcher exceeds a yellow threshold on device with 4 cores</h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_yellow = 8
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of dispatcher should exceed 2% and should remain at a higher level more than 1000 ms.
     * 4. Logcat message with usage percentage should be written once per crossing, it should has an INFO level.
     * 5. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,httpd,cpu_yellow,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_016_CPU_threshold() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_HTTPD, CPU_USAGE_HIGHER_THAN_NORMAL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_HTTPD, CPU_YELLOW);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "85", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadHttpResource(1);
            TestUtil.sleep(5 * 1000);
            loadHttpResource(2);
            TestUtil.sleep(5 * 1000);
            loadHttpResource(3);
            TestUtil.sleep(5 * 1000);
            Thread.sleep(60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    /**
     * <h3>OC should report when CPU usage of dispatcher exceeds a red threshold on device with 4 cores</h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_red = 12
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.com and http://www.cnn.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of dispatcher should exceed 3% and should remain at a higher level more than 1000 ms.
     * 4. Logcat message with usage percentage should be written once per crossing, it should has an WARNING level.
     * 5. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,httpd,cpu_red,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_017_CPU_threshold() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_WARNING, CPU_USAGE_OF_HTTPD, CPU_USAGE_CRITICAL_LEVEL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_HTTPD, CPU_RED);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "12", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadHttpResource(1);
            TestUtil.sleep(5 * 1000);
            loadHttpResource(2);
            TestUtil.sleep(5 * 1000);
            loadHttpResource(3);
            TestUtil.sleep(5 * 1000);
            Thread.sleep(60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    /**
     * <h3>OC should report when CPU usage of dispatcher drops back below yellow threshold on device with 4 cores</h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_yellow = 8
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of dispatcher should exceed 2% and should remain at a higher level more then 1000 ms
     * 4. CPU usage should drop back below yellow threshold and remain at a lower level more than 1000 ms.
     * 5. INFO logcat message should be recorded to indicate that situation is back to normal.
     * 6. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,httpd,cpu_green,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_018_CPU_threshold() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_HTTPD, CPU_USAGE_HIGHER_THAN_NORMAL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_HTTPD, CPU_YELLOW);
        CpuUsageTask cpuUsageTaskGreen = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_HTTPD, CPU_USAGE_BACK_TO_NORMAL);
        ServiceLogTask serviceLogTaskGreen = new ServiceLogTask(CPU_USAGE_OF_HTTPD, CPU_GREEN);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask,
                cpuUsageTaskGreen, serviceLogTaskGreen);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "85", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadHttpResource(1);
            TestUtil.sleep(5 * 1000);
            loadHttpResource(2);
            TestUtil.sleep(5 * 1000);
            loadHttpResource(3);
            TestUtil.sleep(5 * 1000);
            Thread.sleep(2 * 60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
            checkCpuUsage(cpuUsageTaskGreen, serviceLogTaskGreen);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    /**
     * <h3>OC should report when when summarized CPU usage of all OC modules exceeds a yellow threshold on device with 4 cores</h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_yellow = 8
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of all OC modules should exceed 2% and should remain at a higher level more than 1000 ms.
     * 4. Logcat message with usage percentage should be written once per crossing, it should has an INFO level.
     * 5. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,openchannel,cpu_yellow,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_022_CPU_threshold() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_OPENCHANNEL, CPU_USAGE_HIGHER_THAN_NORMAL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_OPENCHANNEL, CPU_YELLOW);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "85", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadLocaleResource(20);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(25);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(30);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(35);
            Thread.sleep(60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    /**
     * <h3>OC should report when summarized CPU usage of all OC modules exceeds a red threshold on device with 4 cores</h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_red = 12
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.comand http://www.cnn.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of all OC modules should exceed 3% and should remain at a higher level more than 1000 ms.
     * 4. Logcat message with usage percentage should be written once per crossing, it should has an WARNING level.
     * 5. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,openchannel,cpu_red,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_023_CPU_threshold() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_WARNING, CPU_USAGE_OF_OPENCHANNEL, CPU_USAGE_CRITICAL_LEVEL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_OPENCHANNEL, CPU_RED);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "12", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadLocaleResource(100);
            Thread.sleep(60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    /**
     * <h3>OC should report when summarized CPU usage of all OC modules drops back below yellow threshold on device with 4 cores</h3>
     * <p>Pre-requests
     * 1. Set such policies on PMS:
     * asimov@failovers@cpu@threshold_yellow = 8
     * asimov@failovers@cpu@threshold_time = 1000
     * </p>
     * <p>Steps
     * 1. Install OC
     * 2. Observe logcat
     * 3. Load http://china.com
     * 4. Observe logcat
     * </p>
     * <p>Result
     * 1. Policy should be received and applied.
     * 2. OC should monitor oce CPU usage every 500 ms
     * 3. CPU usage of all OC modules should exceed 2% and should remain at a higher level more then 1000 ms
     * 4. CPU usage should drop back below yellow threshold and remain at a lower level more than 1000 ms.
     * 5. INFO logcat message should be recorded to indicate that situation is back to normal.
     * 6. ServiceLog entry of the following flavor should be recorded:
     * ...,service,event,openchannel,cpu_green,...
     * </p>
     *
     * @throws Throwable
     */

    @DeviceOnly
    public void test_CPU_threshold_024() throws Throwable {
        CpuUsageTask cpuUsageTask = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_OPENCHANNEL, CPU_USAGE_HIGHER_THAN_NORMAL);
        ServiceLogTask serviceLogTask = new ServiceLogTask(CPU_USAGE_OF_OPENCHANNEL, CPU_YELLOW);
        CpuUsageTask cpuUsageTaskGreen = new CpuUsageTask(LOG_LEVEL_INFO, CPU_USAGE_OF_OPENCHANNEL, CPU_USAGE_BACK_TO_NORMAL);
        ServiceLogTask serviceLogTaskGreen = new ServiceLogTask(CPU_USAGE_OF_OPENCHANNEL, CPU_GREEN);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), cpuUsageTask, serviceLogTask,
                cpuUsageTaskGreen, serviceLogTaskGreen);
        logcatUtil.start();
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_YELLOW, "8", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_RED, "85", PATH_CPU_THRESHOLD, true)});
        PMSUtil.addPolicies(new Policy[]{new Policy(NAME_THRESHOLD_TIME, "1000", PATH_CPU_THRESHOLD, true)});
        Thread.sleep(2 * 60 * 1000);
        try {
            logger.info("Load resource");
            simpleHttpServer.start();
            loadLocaleResource(20);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(25);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(30);
            TestUtil.sleep(10 * 1000);
            loadLocaleResource(35);
            Thread.sleep(2 * 60 * 1000);
            simpleHttpServer.stop();
            logger.info("LogcatUtil stop");
            logcatUtil.stop();
            checkCpuUsage(cpuUsageTask, serviceLogTask);
            checkCpuUsage(cpuUsageTaskGreen, serviceLogTaskGreen);
        } finally {
            simpleHttpServer.stop();
            logcatUtil.stop();
            PMSUtil.cleanPaths(new String[]{PATH_CPU_THRESHOLD});
        }
    }

    @Override
    protected void runTest() throws Throwable {
        boolean isPassed;
        int numberOfAttempts = 0;
        List<String> counts = new ArrayList<String>();
        do {
            isPassed = true;
            numberOfAttempts++;
            try {
                super.runTest();
            } catch (AssertionFailedError assertionFailedError) {
                logger.debug("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);
        assertTrue("The test was failed " + 3 + " times ", counts.size() != 3);
    }
}
