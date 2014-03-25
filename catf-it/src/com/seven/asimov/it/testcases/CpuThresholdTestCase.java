package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.conn.ConnUtils;
import com.seven.asimov.it.utils.logcat.LogcatChecks;
import com.seven.asimov.it.utils.logcat.tasks.CpuUsageTask;
import com.seven.asimov.it.utils.logcat.tasks.ServiceLogTask;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import com.seven.asimov.it.utils.nanohttpd.SimpleHttpServer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CpuThresholdTestCase extends TcpDumpTestCase {
    protected final String PATH_CPU_THRESHOLD = "@asimov@failovers@cpu";
    protected final String NAME_THRESHOLD_YELLOW = "threshold_yellow";
    protected final String NAME_THRESHOLD_RED = "threshold_red";
    protected final String NAME_THRESHOLD_TIME = "threshold_time";
    protected final String LOG_LEVEL_INFO = "INFO";
    protected final String LOG_LEVEL_WARNING = "WARNING";
    //CPU usage of Engine
    protected final String CPU_USAGE_OF_OCE = "oce";
    //CPU usage of Controller
    protected final String CPU_USAGE_OF_OCC = "occ";
    protected final String CPU_USAGE_OF_OPENCHANNEL = "openchannel";
    protected final String CPU_USAGE_OF_HTTPD = "ochttpd";
    protected final String CPU_USAGE_HIGHER_THAN_NORMAL = "is higher than normal";
    protected final String CPU_USAGE_CRITICAL_LEVEL = "has reached critical level";
    protected final String CPU_USAGE_BACK_TO_NORMAL = "is back to normal";
    protected final String CPU_YELLOW = "cpu_yellow";
    protected final String CPU_GREEN = "cpu_green";
    protected final String CPU_RED = "cpu_red";
    private final int PORT = 8080;
    protected LogEntryWrapper logEntry;
    protected SimpleHttpServer simpleHttpServer = new SimpleHttpServer(ConnUtils.getLocalIpAddress().getHostAddress(), PORT);

    protected Runnable getHttpReguestThread() {
        return new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    HttpRequest request = createRequest().setUri("http://" + ConnUtils.getLocalIpAddress().getHostAddress() + ":" + PORT)
                            .setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
                    try {
                        sendRequest(request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    protected void loadLocaleResource(int requestsNumber) {
        ExecutorService executorService = Executors.newFixedThreadPool(requestsNumber);
        for (int i = 0; i < requestsNumber; i++) {
            executorService.submit(getHttpReguestThread());
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5 * requestsNumber, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    protected void loadHttpResource(int requestsNumber) throws Exception {
        String uri = createTestResourceUri("test_CPU_threshold");
        PrepareResourceUtil.prepareResource(uri, false);
        HttpRequest request = createRequest().setUri(uri)
                .setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", 1024 * 5000 + ",c")
                .addHeaderField("Cache-Control", "max-age=1").getRequest();
        try {
            for (int i = 0; i < requestsNumber; i++) {
                sendRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {

        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    protected void checkCpuUsage(CpuUsageTask cpuUsageTask, ServiceLogTask serviceLogTask) throws Exception {
        logEntry = null;
        logEntry = LogcatChecks.checkLogEntryExist(cpuUsageTask, logEntry);
        LogcatChecks.checkLogEntryExist(serviceLogTask, logEntry);
    }
}
