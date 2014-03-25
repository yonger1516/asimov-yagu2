package com.seven.asimov.test.tool.tests.device.certification;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.test.tool.testcase.DeviceCertificationTestCase;
import org.apache.http.client.methods.HttpGet;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StabilityTests extends DeviceCertificationTestCase {

    public void test_001_StabilityMaxHttpLoad() throws Throwable {
        ArrayList<String> uriList = new ArrayList<String>();
        String resource = "test_asimov_stability_http_max_load_";
        String uri;
        int poolSize = 4;
        ExecutorService exec = Executors.newFixedThreadPool(poolSize);
        try {
            for (int i = 0; i < poolSize; i++) {
                uri = createTestResourceUri(resource + (i + 1));
                uriList.add(uri);
                exec.submit(new App(i + 1, 0, uri));
            }
            exec.shutdown();
            if (!exec.awaitTermination(13, TimeUnit.MINUTES)) {
                exec.shutdownNow();
            }
        } catch (Exception e) {
            exec.shutdownNow();
        } finally {
            if (!exec.awaitTermination(30, TimeUnit.SECONDS)) {
                exec.shutdownNow();
            }
            for (String anUriList : uriList) {
                PrepareResourceUtil.invalidateResourceSafely(anUriList);
            }
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_002_StabilityMaxHttpSizeLoad() throws Throwable {
        String resource = "test_asimov_stability_http_max_size_load";
        String uri = createTestResourceUri(resource);
        HttpRequest request;
        HttpResponse response;
        PrepareResourceUtil.prepareResource(uri, false);
        int time = 10 * 1000;
        try {
            for (int i = 0; i < SIZE.length; i++) {
                int temp = SIZE[i] * 1024;

                request = createRequest()
                        .setUri(uri)
                        .setMethod(HttpGet.METHOD_NAME)
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-ResponseContentSize", temp + ",c")
                        .addHeaderField("Random", generationRandomHeader()).getRequest();

                response = sendMiss(i + 1, request);

                logSleeping(time - response.getDuration());
            }
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }


    public void test_003_StabilityHttpPeakLoad() throws Throwable {
        int[] stepTime = {1, 2, 1};
        String resource = "asimov_stability_http_peak_load";
        String uri = createTestResourceUri(resource);
        try {
            stabilitySimulation("test_003_StabilityHttpPeakSizeLoad", stepTime, uri, false);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_004_StabilityHttpPeakSizeLoad() throws Throwable {
        int[] stepTime = {1, 2, 1};
        String resource = "asimov_stability_http_peak_size_load";
        String uri = createTestResourceUri(resource);
        try {
            stabilitySimulation("test_004_StabilityHttpPeakSizeLoad", stepTime, uri, true);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_005_StabilityMaxHttpsLoad() throws Throwable {
        setUp();
        ArrayList<String> uriList = new ArrayList<String>();
        String resource = "test_asimov_stability_https_max_load_";
        String uri;
        int poolSize = 4;
        ExecutorService exec = Executors.newFixedThreadPool(poolSize);
        try {
            for (int i = 0; i < poolSize; i++) {
                uri = createTestResourceUri(resource + (i + 1), true);
                uriList.add(uri);
                exec.submit(new App(i + 1, 1, uri));
            }
            exec.shutdown();
            if (!exec.awaitTermination(13, TimeUnit.MINUTES)) {
                exec.shutdownNow();
            }
        } catch (Exception e) {
            exec.shutdownNow();
        } finally {
            if (!exec.awaitTermination(30, TimeUnit.SECONDS)) {
                exec.shutdownNow();
            }
            for (String anUriList : uriList) {
                PrepareResourceUtil.invalidateResourceSafely(anUriList);
            }
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }

    }

    public void test_006_StabilityMaxHttpsSizeLoad() throws Throwable {
        setUp();
        String resource = "test_asimov_stability_https_max_size_load";
        String uri = createTestResourceUri(resource, true);
        HttpRequest request;
        HttpResponse response;
        PrepareResourceUtil.prepareResource(uri, false);
        final long sleepTime = 10 * 1000;
        try {
            for (int i = 0; i < SIZE.length; i++) {
                int temp = SIZE[i] * 1024;

                request = createRequest()
                        .setUri(uri)
                        .setMethod(HttpGet.METHOD_NAME)
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-ResponseContentSize", temp + ",c")
                        .addHeaderField("Random", generationRandomHeader()).getRequest();

                response = sendMiss(i + 1, request);

                logSleeping(sleepTime - response.getDuration());
            }
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_007_StabilityHttpsPeakLoad() throws Throwable {
        setUp();
        int[] stepTime = {1, 2, 1};
        String resource = "asimov_stability_http_peak_load";
        String uri = createTestResourceUri(resource, true);
        try {
            stabilitySimulation("test_007_NewStabilityHttpsPeakLoad", stepTime, uri, false);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_008_StabilityHttpsPeakSizeLoad() throws Throwable {
        setUp();
        int[] stepTime = {1, 2, 1};
        String resource = "asimov_stability_http_peak_load";
        String uri = createTestResourceUri(resource, true);
        try {
            stabilitySimulation("test_008_NewStabilityHttpsPeakLoad", stepTime, uri, true);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    class App implements Callable<Boolean> {
        private final long timeOut = 12 * 60 * 1000;
        private int threadNumber = 0;
        private int testNumber = 0;
        private String uri;
        private long startTime;

        App(int threadNumber, int testNumber, String uri) {
            this.threadNumber = threadNumber;
            this.testNumber = testNumber;
            this.uri = uri;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public Boolean call() throws Exception {
            switch (testNumber) {
                case 0:
                    return maxHttpLoad(threadNumber);
                case 1:
                    return maxHttpsLoad(threadNumber);
            }
            return false;
        }

        private Boolean maxHttpLoad(int threadNumber) {
            boolean done = true;
            int time = 10 * 1000;
            int i = 5;
            boolean exit = false;

            HttpRequest request;
            HttpResponse response;
            int requestId = 1;
            try {
                PrepareResourceUtil.prepareResource(uri, false);
                while (i <= 70) {
                    try {
                        for (int j = 0; j < i; j++) {
                            request = createRequest().setUri(uri).setMethod("GET")
                                    .addHeaderField("X-OC-ContentEncoding", "identity")
                                    .addHeaderField("Random" + threadNumber, generationRandomHeader())
                                    .getRequest();
                            response = sendMiss(requestId++, request);
                            logSleeping(time / i - response.getDuration());
                            if (System.currentTimeMillis() - startTime > timeOut)
                                break;
                        }
                    } catch (Exception e) {
                        if (i > 55) {
                            e.printStackTrace();
                            exit = true;
                        } else {
                            e.printStackTrace();
                            exit = true;
                            exceptions.add(e);
                            done = false;
                        }
                    }
                    if (exit)
                        break;
                    i += 5;
                    if (System.currentTimeMillis() - startTime > timeOut) {
                        PrepareResourceUtil.prepareResource(uri, true);
                        break;
                    }
                }
                return done;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                PrepareResourceUtil.invalidateResourceSafely(uri);
            }
        }

        private Boolean maxHttpsLoad(int threadNumber) {
            boolean done = true;
            int time = 6 * 1000;
            int i = 5;
            boolean exit = false;

            HttpRequest request;
            HttpResponse response;
            int requestId = 1;
            try {
                PrepareResourceUtil.prepareResource(uri, false);
                while (i <= 70) {
                    try {
                        for (int j = 0; j < i; j++) {
                            request = createRequest().setUri(uri).setMethod("GET")
                                    .addHeaderField("X-OC-ContentEncoding", "identity")
                                    .addHeaderField("Random" + threadNumber, generationRandomHeader())
                                    .getRequest();
                            response = sendMiss(requestId++, request);
                            logSleeping(time / i - response.getDuration());
                            if (System.currentTimeMillis() - startTime > timeOut)
                                break;
                        }
                    } catch (Exception e) {
                        if (i > 55) {
                            e.printStackTrace();
                            exit = true;
                        } else {
                            e.printStackTrace();
                            exit = true;
                            exceptions.add(e);
                            done = false;
                        }
                    }
                    if (exit)
                        break;
                    i += 5;
                    if (System.currentTimeMillis() - startTime > timeOut) {
                        PrepareResourceUtil.prepareResource(uri, true);
                        break;
                    }
                }
                return done;
            } catch (Throwable e) {
                e.printStackTrace();
                return false;
            } finally {
                PrepareResourceUtil.invalidateResourceSafely(uri);
            }
        }
    }
}