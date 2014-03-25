package com.seven.asimov.test.tool.tests.device.certification;

import android.util.Log;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.StreamUtil;
import com.seven.asimov.test.tool.testcase.DeviceCertificationTestCase;
import org.apache.http.client.methods.HttpGet;

public class PerformanceTests extends DeviceCertificationTestCase {
    String TAG = PerformanceTests.class.getSimpleName();

    public void test_001_PerTC_001() throws Throwable {
        for (int i = 0; i <= resourceSize.length - 1; i++) {
            execute(resourceSize[i] * 1024, false);
        }
    }

    public void test_002_PerTC_002() throws Throwable {
        String resource = "asimov_it_performance_cpu_mem_rfc";
        HttpRequest request;
        for (int i = 0; i < resourceSize.length; i++) {
            for (int k = 0; k < 2; k++) {
                request = createRequest().setUri(createTestResourceUri(resource + "_" + i + "_" + k))
                        .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                        .addHeaderField("X-OC-AddHeader_Date", "GMT")
                        .addHeaderField("X-OC-ResponseContentSize", +resourceSize[i] * 1024 + ",w").getRequest();
                sendRequests(request, SLEEP_PERIOD_RFS, 2);
            }
        }
    }

    public void test_003_PerTC_003() throws Throwable {
        setUp();
        for (int i = 0; i <= resourceSize.length - 1; i++) {
            execute(resourceSize[i] * 1024, true);
        }
    }


    public void test_004_PerTC_004() throws Throwable {
        setUp();
        String resource = "asimov_it_cv_https_rfc";
        HttpRequest request;
        for (int i = 0; i < resourceSize.length; i++) {
            for (int k = 0; k < 2; k++) {
                request = createRequest().setUri(createTestResourceUri(resource + "_" + i + "_" + k, true))
                        .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                        .addHeaderField("X-OC-AddHeader_Date", "GMT")
                        .addHeaderField("X-OC-ResponseContentSize", +resourceSize[i] * 1024 + ",w").getRequest();
                sendRequests(request, SLEEP_PERIOD_RFS, 2);
            }
        }
    }

    public void test_005_PerTC_005() throws Throwable {
        String resource = "asimov_it_performance_cpu_mem_stream_WhiteList";
        String uri = createTestResourceUri(resource);
        StreamUtil.getStream(uri, STREAM_SIZE, 520, true);
    }

    public void test_006_PerTC_006() throws Throwable {
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
                    execute(resourceSize[i] * 1024, false);
                }
            }
        };
        try {

            executeThreads(TIME_OUT + 5 * 60 * 1000, p1, p2);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    public void test_007_PerTC_007() throws Throwable {
        setUp();
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
                    execute(resourceSize[i] * 1024, true);
                }
            }
        };
        try {
            executeThreads(TIME_OUT + 5 * 60 * 1000, p1, p2);
        } finally {
            Log.i(TAG, "Test Life: Ivalidating resource");
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    public void test_008_PerTC_008() throws Throwable {
        setUp();
        for (int i = 0; i <= resourceSize.length - 1; i++) {
            execute(resourceSize[i] * 1024, true);
        }
    }
}
