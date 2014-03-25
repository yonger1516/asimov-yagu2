package com.seven.asimov.it.tests.stability.performance;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.testcases.PerformanceTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.StreamUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sysmonitor.AppInfo;
import com.seven.asimov.it.utils.sysmonitor.SystemMonitorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PerformanceTests extends PerformanceTestCase {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTests.class.getSimpleName());

    //Test OC without load
    public void test_000_PerOC() throws Throwable {
        initMap();
        monitor.start();
        monitor.startedNewTest("test_000_PerOC");
        TestUtil.sleep(60 * 1000);
        monitor.endedTest();
        TestUtil.sleep(3 * 1000);
        list = SystemMonitorUtil.getTestResults();
        averageStats(list);
    }

    //Test OC with HTTP requests load (Polling)
    public void test_001_PerTC() throws Throwable {
        monitor.startedNewTest("test_001_PerTC");
        for (int i = 0; i <= resourceSize.length - 1; i++) {
            checkHTTP(resourceSize[i] * 1024);
        }
        monitor.endedTest();
        TestUtil.sleep(3 * 1000);

        List<AppInfo> appInfos = SystemMonitorUtil.getTestResults();
        executeChecks(appInfos, DEVICE_MEMORY);
    }

    //Test OC with HTTP requests load (RFC)
    public void test_002_PerTC() throws Throwable {
        String uri = "asimov_it_performance_cpu_mem_rfc";
        for (int i = 0; i < resourceSize.length; i++) {
            for (int k = 0; k < 2; k++) {
                checkRFC(resourceSize[i] * 1024, uri + "_" + i + "_" + k);
            }
        }

        List<AppInfo> appInfos = SystemMonitorUtil.getTestResults();
        executeChecks(appInfos, DEVICE_MEMORY);

    }

    //Test OC with BlackListed HTTP requests load (Polling)
    public void test_003_PerTC() throws Throwable {
        try {
            final Policy blacklist = new Policy("blacklist", AsimovTestCase.TEST_RESOURCE_HOST, "@asimov@http", true);
            PMSUtil.addPolicies(new Policy[]{blacklist});
            for (int i = 0; i <= resourceSize.length - 1; i++) {
                checkHTTP(resourceSize[i] * 1024);
            }

            List<AppInfo> appInfos = SystemMonitorUtil.getTestResults();
            System.out.println(appInfos.toString());
            executeChecks(appInfos, DEVICE_MEMORY);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
        }
    }

    //Test OC with HTTP requests load (Polling)
    public void test_004_PerTC() throws Throwable {
        for (int i = 0; i <= resourceSize.length - 1; i++) {
            checkPollHTTPS(resourceSize[i] * 1024);
        }
        List<AppInfo> appInfos = SystemMonitorUtil.getTestResults();
        executeChecks(appInfos, DEVICE_MEMORY);
    }


    //Test OC with HTTPS requests load (RFC)
    public void test_005_PerTC() throws Throwable {
        int[] A = {1, 256, 512, 1024};
        String uri = "asimov_it_cv_https_rfc";
        for (int i = 0; i < A.length; i++) {
            for (int k = 0; k < 2; k++) {
                checkhttpsRFC(A[i] * 1024, uri + "_" + i + "_" + k);
            }
        }
        List<AppInfo> appInfos = SystemMonitorUtil.getTestResults();
        executeChecks(appInfos, DEVICE_MEMORY);
    }


    //Test OC with BlackListed HTTPS requests load (Polling)
    public void test_006_PerTC() throws Throwable {
        try {
            final Policy policy = new Policy("domains", AsimovTestCase.TEST_RESOURCE_HOST, "@asimov@https@blacklist", true);
            PMSUtil.addPolicies(new Policy[]{policy});
            for (int i = 0; i <= resourceSize.length - 1; i++) {
                checkPollHTTPS(resourceSize[i] * 1024);
            }
            List<AppInfo> appInfos = SystemMonitorUtil.getTestResults();
            executeChecks(appInfos, DEVICE_MEMORY);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@https@blacklist"});
        }

    }
    //Test OC with stream in WhiteList

    public void test_007_PerTC() throws Throwable {
        String resource = "asimov_it_performance_cpu_mem_stream_WhiteList";
        String uri = createTestResourceUri(resource);
        StreamUtil.getStream(uri, STREAM_SIZE, 520, true);
    }


    //Test OC with stream in BlackList
    public void test_008_PerTC() throws Throwable {
        StreamUtil.getStreamTcp(STREAM_SIZE, 520, getContext());
    }

    //Test OC with stream\HTTP in WhiteList
    public void test_009_PerTC() throws Throwable {
        String resource = "asimov_it_performance_cpu_mem_stream_http_WhiteList";
        final String uri = createTestResourceUri(resource);
        TestCaseThread p1 = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                StreamUtil.getStream(uri, STREAM_SIZE, 5 * 60, true);
            }
        };

        TestCaseThread p2 = new TestCaseThread() {
            public void run() throws Throwable {
                for (int i = 0; i <= resourceSize.length - 1; i++) {
                    checkHTTP(resourceSize[i] * 1024);
                }
            }
        };
        try {

            executeThreads(timeout + 5 * 60 * 1000, p1, p2);
        } finally {
            logger.info("Ivalidating resource");
            PrepareResourceUtil.invalidateResourceSafely(uri);
            List<AppInfo> appInfos = SystemMonitorUtil.getTestResults();
            System.out.println(appInfos.toString());
            executeChecks(appInfos, DEVICE_MEMORY);
        }

    }

    //Test OC with stream\HTTPS in WhiteList
    public void test_010_PerTC() throws Throwable {
        String resource = "asimov_it_performance_cpu_mem_stream_https_WhiteList";
        final String uri = createTestResourceUri(resource);
        TestCaseThread p1 = new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                StreamUtil.getStream(uri, STREAM_SIZE, 5 * 60, true);
            }
        };

        TestCaseThread p2 = new TestCaseThread() {
            public void run() throws Throwable {
                for (int i = 0; i <= resourceSize.length - 1; i++) {
                    checkPollHTTPS(resourceSize[i] * 1024);
                }
            }
        };
        try {
            executeThreads(timeout + 5 * 60 * 1000, p1, p2);
        } finally {
            logger.info("Ivalidating resource");
            PrepareResourceUtil.invalidateResourceSafely(uri);
            List<AppInfo> appInfos = SystemMonitorUtil.getTestResults();
            executeChecks(appInfos, DEVICE_MEMORY);
        }
    }

    //Test OC with HTTP requests (White\Black Lists)
    public void test_011_PerTC() throws Throwable {
        try {
            final Policy blacklist = new Policy("blacklist", "hki-dev-testrunner4.7sys.eu", "@asimov@http", true);
            PMSUtil.addPolicies(new Policy[]{blacklist});
            for (int i = 0; i <= resourceSize.length - 1; i++) {
                System.out.println("Started size=" + resourceSize[i]);
                checkHTTP(resourceSize[i] * 1024);
                System.out.println("Finished size=" + resourceSize[i]);
            }
            List<AppInfo> appInfos = SystemMonitorUtil.getTestResults();
            executeChecks(appInfos, DEVICE_MEMORY);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@http"});
        }
    }

    //Test OC with HTTPS requests (White\Black Lists)
    public void test_012_PerTC() throws Throwable {
        try {
            final Policy policy = new Policy("enabled", "10.2.2.173", "@asimov@application@com.seven.asimov.it@ssl", false);
            PMSUtil.addPolicies(new Policy[]{policy});
            for (int i = 0; i <= resourceSize.length - 1; i++) {
                checkPollHTTPS(resourceSize[i] * 1024);
            }
            List<AppInfo> appInfos = SystemMonitorUtil.getTestResults();
            executeChecks(appInfos, DEVICE_MEMORY);
        } finally {
            PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@ssl"});
        }

    }

    //Test OC with HTTP\HTTPS requests

    public void test_013_PerTC() throws Throwable {
        for (int i = 0; i <= resourceSize.length - 1; i++) {
            checHttpAndHttps(resourceSize[i] * 1024);
        }
        List<AppInfo> appInfos = SystemMonitorUtil.getTestResults();
        executeChecks(appInfos, DEVICE_MEMORY);
    }


}