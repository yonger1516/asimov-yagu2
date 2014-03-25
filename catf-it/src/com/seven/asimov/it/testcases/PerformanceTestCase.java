package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.base.constants.BaseConstantsIF;
import com.seven.asimov.it.base.interfaces.HttpUrlConnectionIF;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.sysmonitor.AppInfo;
import com.seven.asimov.it.utils.sysmonitor.AppInfoOC;
import com.seven.asimov.it.utils.sysmonitor.DispatcherInfo;
import com.seven.asimov.it.utils.sysmonitor.SystemMonitorUtil;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class PerformanceTestCase extends TcpDumpTestCase {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTestCase.class.getSimpleName());

    protected static SystemMonitorUtil monitor = SystemMonitorUtil.getInstance(getStaticContext(), "com.seven.asimov,",
            "PerformanceReport", BaseConstantsIF.SD_CARD + "/OCIntegrationTestsResults");

    private Map<String, Double> avgValue;
    private Map<String, Double> tempValue;

    protected static List<AppInfo> list;

    private static final double INC_COEFF = 1.15;

    protected SSLSocketFactory mSSLSocketFactory;

    private String RESOURCE_URI1 = "asimov_it_performance_cpu_mem1";
    private String RESOURCE_URI2 = "asimov_it_performance_cpu_mem2";
    private String RESOURCE_URI3 = "asimov_it_performance_cpu_mem3";

    protected final int STREAM_SIZE = 10 * 1024;
    protected final int sleepPeriod = 15 * 1000; //3 requsts per sec (sleep period)
    protected final int sleepPeriodRfc = sleepPeriod / 3;
    protected int[] resourceSize = {1, 256, 512, 1024};
    protected int timeout = 15 * 60 * 1000;
    protected final static int DEVICE_MEMORY = 512000;//MEM size in device

    protected void initMap() {
        avgValue = new HashMap<String, Double>();
        tempValue = new HashMap<String, Double>();
        List<String> ocProcesses = OCUtil.getRunningProcesses();
        for (String process : ocProcesses) {
            avgValue.put(process + "_cpu", 0D);
            avgValue.put(process + "_mem", 0D);
            tempValue.put(process + "_cpu", 0D);
            tempValue.put(process + "_mem", 0D);
        }
    }

    protected void averageStats(List<AppInfo> appInfos) {
        for (AppInfo a : appInfos) {
            if (a instanceof AppInfoOC) {
                for (DispatcherInfo dispatcherInfo : ((AppInfoOC) a).getDispatcherList()) {
                    avgValue.put(dispatcherInfo.getName() + "_cpu", avgValue.get(dispatcherInfo.getName() + "_cpu") + dispatcherInfo.getCpuUsage());
                    avgValue.put(dispatcherInfo.getName() + "_mem", avgValue.get(dispatcherInfo.getName() + "_mem") + dispatcherInfo.getMemoryUsage());
                }
                avgValue.put("occ_cpu", avgValue.get("occ_cpu") + ((AppInfoOC) a).getCpuController());
                avgValue.put("occ_mem", avgValue.get("occ_mem") + ((AppInfoOC) a).getMemController());
                avgValue.put("com.seven.asimov_cpu", avgValue.get("com.seven.asimov_cpu") + ((AppInfoOC) a).getCpuEngine());
                avgValue.put("com.seven.asimov_mem", avgValue.get("com.seven.asimov_mem") + ((AppInfoOC) a).getMemEngine());
            }
        }
        List<String> ocProcesses = OCUtil.getRunningProcesses();
        for (String process : ocProcesses) {
            avgValue.put(process + "_cpu", avgValue.get(process + "_cpu") / appInfos.size() * INC_COEFF);
            avgValue.put(process + "_mem", avgValue.get(process + "_mem") / appInfos.size() * INC_COEFF);
        }
    }

    protected void executeChecks(List<AppInfo> appInfos, int r) throws IOException {
        for (AppInfo a : appInfos) {
            if (a instanceof AppInfoOC) {
                for (DispatcherInfo dispatcherInfo : ((AppInfoOC) a).getDispatcherList()) {
                    tempValue.put(dispatcherInfo.getName() + "_cpu", tempValue.get(dispatcherInfo.getName() + "_cpu") + dispatcherInfo.getCpuUsage());
                    tempValue.put(dispatcherInfo.getName() + "_mem", tempValue.get(dispatcherInfo.getName() + "_mem") + dispatcherInfo.getMemoryUsage());
                }
                tempValue.put("occ_cpu", tempValue.get("occ_cpu") + ((AppInfoOC) a).getCpuController());
                tempValue.put("occ_mem", tempValue.get("occ_mem") + ((AppInfoOC) a).getMemController());
                tempValue.put("com.seven.asimov_cpu", tempValue.get("com.seven.asimov_cpu") + ((AppInfoOC) a).getCpuEngine());
                tempValue.put("com.seven.asimov_mem", tempValue.get("com.seven.asimov_mem") + ((AppInfoOC) a).getMemEngine());
            }
        }
        List<String> ocProcesses = OCUtil.getRunningProcesses();
        for (String process : ocProcesses) {
            tempValue.put(process + "_cpu", tempValue.get(process + "_cpu") / appInfos.size());
            tempValue.put(process + "_mem", tempValue.get(process + "_mem") / appInfos.size());
        }

        for (String process : ocProcesses) {
            assertTrue("AssertCPU is incorrect for proccess : " + process, avgValue.get(process + "_cpu") - tempValue.get(process + "_cpu") < 15);
            assertTrue("AssertRss is incorrect", avgValue.get(process + "_cpu") / r - tempValue.get(process + "_cpu") / r < 0.15);
        }

    }

    protected void checkRFC(int sizeResponseBody, String uri) throws Exception {

        HttpRequest request = createRequest().setUri(createTestResourceUri(uri))
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                .addHeaderField("X-OC-AddHeader_Date", "GMT")
                .addHeaderField("X-OC-ResponseContentSize", +sizeResponseBody + ",w").getRequest();

        sendRequest2(request);
        TestUtil.sleep(sleepPeriodRfc);

    }

    protected void checkHttps(HttpRequest request, int sleepPeriod, HttpUrlConnectionIF outer) throws Exception {
        checkHttps(request, sleepPeriod, 4, outer);
    }

    protected void checkHttps(HttpRequest request, int sleepPeriod, int countRequests, HttpUrlConnectionIF outer) throws Exception {
        // 1.1 this request shall be returned from network
        for (int i = 0; i < countRequests; i++) {
            HttpResponse response = sendHttpsRequest(request, outer);
            TestUtil.sleep(sleepPeriod - response.getDuration());
        }
    }

    protected void checkHttp(HttpRequest request, int sleepPeriod) throws Exception {
        checkHttp(request, sleepPeriod, 4);
    }

    protected void checkHttp(HttpRequest request, int sleepPeriod, int countRequests) throws Exception {
        // 1.1 this request shall be returned from network
        for (int i = 0; i < countRequests; i++) {
            HttpResponse response = sendRequest2(request);
            TestUtil.sleep(sleepPeriod - response.getDuration());
        }
    }

    //========================================HTTPS============================================

    protected HttpResponse sendHttpsRequest(HttpRequest request, HttpUrlConnectionIF decorator)
            throws MalformedURLException, IOException, URISyntaxException {
        return sendRequest(request, decorator, false, false, Body.BODY);
    }

    public HttpResponse sendHttpsRequest(byte[] request, String host) {
        return sendRequest(request, host, mSSLSocketFactory);
    }

    @Override
    public void decorate(HttpURLConnection conn) throws IOException {
        if (conn instanceof HttpsURLConnection) {
            logger.trace("Https decorator applied");
            HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
            httpsConnection.setSSLSocketFactory(mSSLSocketFactory);
            httpsConnection.setHostnameVerifier(new AllowAllHostnameVerifier());
        }
    }

    protected void checHttpAndHttps(int sizeResponseBody) throws Throwable {

        String uri1 = createTestResourceUri(RESOURCE_URI1 + "_" + sizeResponseBody);
        final HttpRequest request1 = createRequest()
                .setUri(createTestResourceUri(uri1, true))
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        String uri2 = createTestResourceUri(RESOURCE_URI2 + "_" + sizeResponseBody);
        final HttpRequest request2 = createRequest()
                .setUri(createTestResourceUri(uri2, true))
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        String uri3 = createTestResourceUri(RESOURCE_URI3 + "_" + sizeResponseBody);
        final HttpRequest request3 = createRequest()
                .setUri(createTestResourceUri(uri3, true))
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        final HttpUrlConnectionIF outer = this;
        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                checkHttp(request1, sleepPeriod);
            }
        };

        TestCaseThread t2 = new TestCaseThread(10 * 1000) {
            public void run() throws Throwable {
                checkHttps(request2, sleepPeriod, outer);
            }
        };

        TestCaseThread t3 = new TestCaseThread(20 * 1000) {
            public void run() throws Throwable {
                checkHttps(request3, sleepPeriod, outer);
            }
        };

        String id = null;
        try {
            executeThreads(timeout, t1, t2, t3);

        } finally {
            logger.info("Ivalidating resource");
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
            PrepareResourceUtil.invalidateResourceSafely(uri3);
        }
    }

    protected void checkPollHTTPS(int sizeResponseBody) throws Throwable {
        String uri1 = createTestResourceUri(RESOURCE_URI1 + "_" + sizeResponseBody);
        final HttpRequest request1 = createRequest()
                .setUri(createTestResourceUri(uri1, true))
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        String uri2 = createTestResourceUri(RESOURCE_URI2 + "_" + sizeResponseBody);
        final HttpRequest request2 = createRequest()
                .setUri(createTestResourceUri(uri2, true))
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        String uri3 = createTestResourceUri(RESOURCE_URI3 + "_" + sizeResponseBody);
        final HttpRequest request3 = createRequest()
                .setUri(createTestResourceUri(uri3, true))
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        final HttpUrlConnectionIF outer = this;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                checkHttps(request1, sleepPeriod, outer);
            }
        };

        TestCaseThread t2 = new TestCaseThread(10 * 1000) {
            public void run() throws Throwable {
                checkHttps(request2, sleepPeriod, outer);
            }
        };

        TestCaseThread t3 = new TestCaseThread(20 * 1000) {
            public void run() throws Throwable {
                checkHttps(request3, sleepPeriod, outer);
            }
        };

        try {
            executeThreads(timeout, t1, t2, t3);
        } finally {
            // invalidate resource to stop server polling
            logger.info("Ivalidating resource");
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
            PrepareResourceUtil.invalidateResourceSafely(uri3);
        }
    }

    protected void checkHTTP(int sizeResponseBody) throws Throwable {
        String uri1 = createTestResourceUri(RESOURCE_URI1 + "_" + sizeResponseBody);
        final HttpRequest request1 = createRequest()
                .setUri(uri1)
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        String uri2 = createTestResourceUri(RESOURCE_URI2 + "_" + sizeResponseBody);
        final HttpRequest request2 = createRequest()
                .setUri(uri2)
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        String uri3 = createTestResourceUri(RESOURCE_URI3 + "_" + sizeResponseBody);
        final HttpRequest request3 = createRequest()
                .setUri(uri3)
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                checkHttp(request1, sleepPeriod);
            }
        };

        TestCaseThread t2 = new TestCaseThread(10 * 1000) {
            public void run() throws Throwable {
                checkHttp(request2, sleepPeriod);
            }
        };

        TestCaseThread t3 = new TestCaseThread(20 * 1000) {
            public void run() throws Throwable {
                checkHttp(request3, sleepPeriod);
            }
        };

        try {
            executeThreads(timeout, t1, t2, t3);
        } finally {
            // invalidate resource to stop server polling
            logger.info("Ivalidating resource");
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
            PrepareResourceUtil.invalidateResourceSafely(uri3);
        }
    }

    protected void checkhttpsRFC(int sizeResponseBody, String uri) throws Throwable {

        HttpRequest request = createRequest().setUri(createTestResourceUri(uri, true))
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                .addHeaderField("X-OC-AddHeader_Date", "GMT")
                .addHeaderField("X-OC-ResponseContentSize", +sizeResponseBody + ",w").getRequest();

        sendHttpsRequest(request, this);
        TestUtil.sleep(sleepPeriodRfc);
    }
}
