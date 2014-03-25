package com.seven.asimov.it.tests.stability.base;

import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.base.constants.StabilityConstantsIF;
import com.seven.asimov.it.testcases.StabilityTestCase;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.StreamUtil;
import com.seven.asimov.it.utils.TestUtil;
import org.apache.http.client.methods.HttpGet;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StabilityTests extends StabilityTestCase {

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
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
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
        final ArrayList<String> processes = processesParser();
        try {
            for (int i = 0; i < SIZE.length; i++) {
                int temp = SIZE[i] * 1024;

                request = createRequest()
                        .setUri(uri)
                        .setMethod(HttpGet.METHOD_NAME)
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-ResponseContentSize", temp + ",c")
                        .addHeaderField("Random", TestUtil.generationRandomString()).getRequest();

                response = sendHttpMiss(i + 1, request, Body.NOBODY);

                assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                logSleeping(time - response.getDuration());
            }
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_003_StabilityHttpPeriodicLoad_http() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_http_periodic_load_http";
        final String backgroundUri = createTestResourceUri(resource + "_background", true);
        final String uri = createTestResourceUri(resource);
        final long startTime = System.currentTimeMillis();
        final long sleepTime = 10 * 1000;
        long timeout = 370 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                stepSimulation(1, 1, System.currentTimeMillis(), 4, backgroundUri, false);
            }
        };

        TestCaseThread t2 = new TestCaseThread(sleepTime) {
            public void run() throws Throwable {
                int responseId = 1;
                int requestNumber = 2;
                HttpRequest request;
                HttpResponse response;
                while (true) {
                    for (int i = 1; i <= requestNumber; i++) {
                        request = createRequest().setUri(uri).setMethod("GET")
                                .addHeaderField("X-OC-ContentEncoding", "identity")
                                .addHeaderField("Random", TestUtil.generationRandomString())
                                .getRequest();
                        response = sendHttpMiss(responseId++, request);
                        assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                        assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                        logSleeping(sleepTime / requestNumber - response.getDuration());
                    }
                    logSleeping(sleepTime);
                    requestNumber += 2;
                    if (System.currentTimeMillis() - startTime >= 3 * ONE_MIN) {
                        break;
                    }

                }
            }
        };

        try {
            executeThreads(timeout, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            PrepareResourceUtil.invalidateResourceSafely(backgroundUri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_004_StabilityHttpPeriodicLoad_stream() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_http_periodic_load_stream";
        final String uri = createTestResourceUri(resource);
        final long startTime = System.currentTimeMillis();
        final long sleepTime = 10 * 1000;
        long timeout = 370 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                StreamUtil.getStreamTcp(BACKGROUND_STREAM_SIZE, 4 * 60, getContext());
            }
        };

        TestCaseThread t2 = new TestCaseThread(sleepTime) {
            public void run() throws Throwable {
                int responseId = 1;
                int requestNumber = 2;
                HttpRequest request;
                HttpResponse response;
                while (true) {
                    for (int i = 1; i <= requestNumber; i++) {
                        request = createRequest().setUri(uri).setMethod("GET")
                                .addHeaderField("X-OC-ContentEncoding", "identity")
                                .addHeaderField("Random", TestUtil.generationRandomString())
                                .getRequest();
                        response = sendHttpMiss(responseId++, request);
                        assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                        assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                        logSleeping(sleepTime / requestNumber - response.getDuration());
                    }
                    logSleeping(sleepTime);
                    requestNumber += 2;
                    if (System.currentTimeMillis() - startTime >= 3 * ONE_MIN) {
                        break;
                    }
                }
            }
        };

        try {
            executeThreads(timeout, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_005_StabilityHttpPeakLoad() throws Throwable {
        int[] stepTime = {1, 2, 1};
        String resource = "asimov_stability_http_peak_load";
        String uri = createTestResourceUri(resource);
        try {
            stabilitySimulation(stepTime, uri, false);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_006_StabilityHttpPeakSizeLoad() throws Throwable {
        int[] stepTime = {1, 2, 1};
        String resource = "asimov_stability_http_peak_size_load";
        String uri = createTestResourceUri(resource);
        try {
            stabilitySimulation(stepTime, uri, true);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_007_StabilityHttpPeakLoad_stream() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_http_periodic_load_stream";
        final String uri = createTestResourceUri(resource);
        final long startTime = System.currentTimeMillis();
        long timeout = 370 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                StreamUtil.getStreamTcp(BACKGROUND_STREAM_SIZE, 4 * 60, getContext());
            }
        };

        TestCaseThread t2 = new TestCaseThread(60 * 1000) {
            public void run() throws Throwable {
                int responseId = 1;
                int requestNumber = 30;
                long sleepTime = 10 * 1000;
                HttpRequest request;
                HttpResponse response;
                while (true) {
                    for (int i = 1; i <= requestNumber; i++) {
                        request = createRequest().setUri(uri).setMethod("GET")
                                .addHeaderField("X-OC-ContentEncoding", "identity")
                                .addHeaderField("Random", TestUtil.generationRandomString())
                                .getRequest();
                        response = sendHttpMiss(responseId++, request);
                        assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                        assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                        logSleeping(sleepTime / requestNumber - response.getDuration());
                    }
                    if (System.currentTimeMillis() - startTime >= 3 * ONE_MIN) {
                        break;
                    }

                }
            }
        };

        try {
            executeThreads(timeout, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_008_StabilityHttpPeakSizeLoad_stream() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_http_periodic_load_stream";
        final String uri = createTestResourceUri(resource);
        final long startTime = System.currentTimeMillis();
        long timeout = 370 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                StreamUtil.getStreamTcp(BACKGROUND_STREAM_SIZE, 4 * 60, getContext());
            }
        };

        TestCaseThread t2 = new TestCaseThread(ONE_MIN) {
            public void run() throws Throwable {
                int responseId = 1;
                int requestNumber = 5;
                long sleepTime = 10 * 1000;
                HttpRequest request;
                HttpResponse response;
                while (true) {
                    for (int i = 1; i <= requestNumber; i++) {
                        request = createRequest().setUri(uri).setMethod("GET")
                                .addHeaderField("X-OC-ContentEncoding", "identity")
                                .addHeaderField("X-OC-ResponseContentSize", NORMAL_SIZE + ",c")
                                .addHeaderField("Random", TestUtil.generationRandomString())
                                .getRequest();
                        response = sendHttpMiss(responseId++, request);
                        assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                        assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                        logSleeping(sleepTime / requestNumber - response.getDuration());
                    }
                    if (System.currentTimeMillis() - startTime >= 3 * ONE_MIN) {
                        break;
                    }

                }
            }
        };

        try {
            executeThreads(timeout, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    @Ignore
    @DeviceOnly
    public void test_009_StabilityTopHttpResources() throws Throwable {
        ArrayList<String> processes = processesParser();
        HttpRequest request;
        HttpResponse response;
        long sleepTime = 1000;
        int requestId = 1;
        try {
            for (int i = 0; i < StabilityConstantsIF.TOP_HTTP_RESOURCES.length; i++) {
                request = createRequest().setUri(StabilityConstantsIF.TOP_HTTP_RESOURCES[i]).setMethod("GET")
                        .getRequest();
                response = sendHttpTopMiss(requestId++, request);
                assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                logSleeping(sleepTime - response.getDuration());
            }
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } finally {
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_010_StabilityMaxHttpsLoad() throws Throwable {
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
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
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

    public void test_011_StabilityMaxHttpsSizeLoad() throws Throwable {
        String resource = "test_asimov_stability_https_max_size_load";
        String uri = createTestResourceUri(resource, true);
        HttpRequest request;
        HttpResponse response;
        PrepareResourceUtil.prepareResource(uri, false);
        final ArrayList<String> processes = processesParser();
        final long sleepTime = 10 * 1000;
        try {
            for (int i = 0; i < SIZE.length; i++) {
                int temp = SIZE[i] * 1024;

                request = createRequest()
                        .setUri(uri)
                        .setMethod(HttpGet.METHOD_NAME)
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-ResponseContentSize", temp + ",c")
                        .addHeaderField("Random", TestUtil.generationRandomString()).getRequest();

                response = sendHttpsMiss(i + 1, request, Body.NOBODY);

                assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());

                logSleeping(sleepTime - response.getDuration());
            }
            PrepareResourceUtil.prepareResource(uri, true);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_012_StabilityHttpsPeriodicLoad_https() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_https_periodic_load_";
        final String backgroundUri = createTestResourceUri(resource + "background", true);
        final String uri = createTestResourceUri(resource, true);
        final long startTime = System.currentTimeMillis();
        final long sleepTime = 10 * 1000;
        long timeout = 370 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                stepSimulation(1, 1, System.currentTimeMillis(), 4, backgroundUri, false, processes);
            }
        };

        TestCaseThread t2 = new TestCaseThread(10 * 1000) {
            public void run() throws Throwable {
                int responseId = 1;
                int requestNumber = 2;
                HttpRequest request;
                HttpResponse response;
                PrepareResourceUtil.prepareResource(uri, false);
                while (true) {
                    for (int i = 1; i <= requestNumber; i++) {
                        request = createRequest().setUri(uri).setMethod("GET")
                                .addHeaderField("X-OC-ContentEncoding", "identity")
                                .addHeaderField("Random", TestUtil.generationRandomString())
                                .getRequest();
                        response = sendHttpsMiss(responseId++, request);
                        assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                        assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                        logSleeping(sleepTime / requestNumber - response.getDuration());
                    }
                    logSleeping(sleepTime);
                    requestNumber += 2;
                    if (System.currentTimeMillis() - startTime >= 3 * ONE_MIN) {
                        PrepareResourceUtil.prepareResource(uri, true);
                        break;
                    }

                }
            }
        };

        try {
            executeThreads(timeout, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            PrepareResourceUtil.invalidateResourceSafely(backgroundUri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_013_StabilityHttpsPeriodicLoad_stream() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_https_periodic_load_stream";
        final String uri = createTestResourceUri(resource, true);
        final String backgroundUri = createTestResourceUri(resource + "_background", true);
        final long startTime = System.currentTimeMillis();
        final long sleepTime = 10 * 1000;
        long timeout = 370 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(backgroundUri, false);
                StreamUtil.getStream(backgroundUri, BACKGROUND_STREAM_SIZE, 240, true);
                PrepareResourceUtil.prepareResource(backgroundUri, true);
            }
        };

        TestCaseThread t2 = new TestCaseThread(sleepTime) {
            public void run() throws Throwable {
                int responseId = 1;
                int requestNumber = 2;
                HttpRequest request;
                HttpResponse response;
                PrepareResourceUtil.prepareResource(uri, false);
                while (true) {
                    for (int i = 1; i <= requestNumber; i++) {
                        request = createRequest().setUri(uri).setMethod("GET")
                                .addHeaderField("X-OC-ContentEncoding", "identity")
                                .addHeaderField("Random", TestUtil.generationRandomString())
                                .getRequest();
                        response = sendHttpsMiss(responseId++, request);
                        assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                        assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                        logSleeping(sleepTime / requestNumber - response.getDuration());
                    }
                    logSleeping(sleepTime);
                    requestNumber += 2;
                    if (System.currentTimeMillis() - startTime >= 3 * ONE_MIN) {
                        PrepareResourceUtil.prepareResource(uri, true);
                        break;
                    }

                }
            }
        };

        try {
            executeThreads(timeout, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_014_StabilityHttpsPeakLoad() throws Throwable {
        int[] stepTime = {1, 2, 1};
        String resource = "asimov_stability_http_peak_load";
        String uri = createTestResourceUri(resource, true);
        try {
            stabilitySimulation(stepTime, uri, false);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_015_StabilityHttpsPeakSizeLoad() throws Throwable {
        int[] stepTime = {1, 2, 1};
        String resource = "asimov_stability_http_peak_load";
        String uri = createTestResourceUri(resource, true);
        try {
            stabilitySimulation(stepTime, uri, true);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_016_StabilityHttpsPeakLoad_stream() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_https_periodic_load_stream";
        final String uri = createTestResourceUri(resource, true);
        final String backgroundUri = createTestResourceUri(resource + "_background", true);
        final long startTime = System.currentTimeMillis();
        long timeout = 370 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(backgroundUri, false);
                StreamUtil.getStream(backgroundUri, BACKGROUND_STREAM_SIZE, 4 * 60, true);
                PrepareResourceUtil.prepareResource(backgroundUri, true);
            }
        };

        TestCaseThread t2 = new TestCaseThread(ONE_MIN) {
            public void run() throws Throwable {
                int responseId = 1;
                int requestNumber = 30;
                long sleepTime = 10 * 1000;
                HttpRequest request;
                HttpResponse response;
                PrepareResourceUtil.prepareResource(uri, false);
                while (true) {
                    for (int i = 1; i <= requestNumber; i++) {
                        request = createRequest().setUri(uri).setMethod("GET")
                                .addHeaderField("X-OC-ContentEncoding", "identity")
                                .addHeaderField("Random", TestUtil.generationRandomString())
                                .getRequest();
                        response = sendHttpsMiss(responseId++, request);
                        assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                        assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                        logSleeping(sleepTime / requestNumber - response.getDuration());
                    }
                    if (System.currentTimeMillis() - startTime >= 3 * ONE_MIN) {
                        PrepareResourceUtil.prepareResource(uri, true);
                        break;
                    }
                }
            }
        };

        try {
            executeThreads(timeout, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            PrepareResourceUtil.invalidateResourceSafely(backgroundUri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_017_StabilityHttpsPeakSizeLoad_stream() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_https_periodic_load_stream";
        final String backgroundUri = createTestResourceUri(resource + "_background", true);
        final String uri = createTestResourceUri(resource, true);
        final long startTime = System.currentTimeMillis();
        long timeout = 370 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(backgroundUri, false);
                StreamUtil.getStream(backgroundUri, BACKGROUND_STREAM_SIZE, 4 * 60, true);
                PrepareResourceUtil.prepareResource(backgroundUri, true);
            }
        };

        TestCaseThread t2 = new TestCaseThread(ONE_MIN) {
            public void run() throws Throwable {
                int responseId = 1;
                int requestNumber = 5;
                long sleepTime = 10 * 1000;
                HttpRequest request;
                HttpResponse response;
                PrepareResourceUtil.prepareResource(uri, false);
                while (true) {
                    for (int i = 1; i <= requestNumber; i++) {
                        request = createRequest().setUri(uri).setMethod("GET")
                                .addHeaderField("X-OC-ContentEncoding", "identity")
                                .addHeaderField("X-OC-ResponseContentSize", NORMAL_SIZE + ",c")
                                .addHeaderField("Random", TestUtil.generationRandomString())
                                .getRequest();
                        response = sendHttpsMiss(responseId++, request);
                        assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                        assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                        logSleeping(sleepTime / requestNumber - response.getDuration());
                    }
                    if (System.currentTimeMillis() - startTime >= 3 * ONE_MIN) {
                        PrepareResourceUtil.prepareResource(uri, true);
                        break;
                    }

                }
            }
        };

        try {
            executeThreads(timeout, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            PrepareResourceUtil.invalidateResourceSafely(backgroundUri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    @Ignore
    @DeviceOnly
    public void test_018_StabilityTopHttpsResources() throws Throwable {
        ArrayList<String> processes = processesParser();
        HttpRequest request;
        HttpResponse response;
        long sleepTime = 1000;
        int requestId = 1;
        try {
            for (int i = 0; i < StabilityConstantsIF.TOP_HTTPS_RESOURCES.length; i++) {
                request = createRequest().setUri(StabilityConstantsIF.TOP_HTTPS_RESOURCES[i]).setMethod("GET")
                        .getRequest();
                response = sendHttpsTopMiss(requestId++, request);
                assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                logSleeping(sleepTime - response.getDuration());
            }
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } finally {
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_019_StabilityMaxStreamLoad() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_max_stream_load";
        final String backgroundUri = createTestResourceUri(resource + "_background");
        final String uri = createTestResourceUri(resource);
        int time = 630 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(backgroundUri, false);
                StreamUtil.getStream(backgroundUri, BACKGROUND_STREAM_SIZE, 520, true);
                PrepareResourceUtil.prepareResource(backgroundUri, true);
            }
        };

        TestCaseThread t2 = new TestCaseThread() {
            public void run() throws Throwable {
                int size = 20;
                int time = 10;
                PrepareResourceUtil.prepareResource(uri, false);
                for (int i = 0; i < 50; i++) {
                    StreamUtil.getStream(uri, size * 1024, time, true);
                    assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                    assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                    size += 10;
                }
                PrepareResourceUtil.prepareResource(uri, true);
            }
        };

        try {
            executeThreads(time, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            PrepareResourceUtil.invalidateResourceSafely(backgroundUri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }

    }

    public void test_020_StabilityStreamingPeakLoad_http() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_stream_peak_load_http";
        final String backgroundUri = createTestResourceUri(resource + "_background");
        final String uri = createTestResourceUri(resource, true);
        final long startTime = System.currentTimeMillis();
        long timeout = 370 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                stepSimulation(1, 1, System.currentTimeMillis(), 4, backgroundUri, false);
            }
        };

        TestCaseThread t2 = new TestCaseThread(ONE_MIN) {
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(uri, false);
                StreamUtil.getStream(uri, MAX_SIZE, 2 * 60, true);
                assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                PrepareResourceUtil.prepareResource(uri, true);
            }
        };

        try {
            executeThreads(timeout, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            PrepareResourceUtil.invalidateResourceSafely(backgroundUri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_021_StabilityStreamingPeakLoad_https() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_stream_peak_load_https";
        final String backgroundUri = createTestResourceUri(resource + "_background");
        final String uri = createTestResourceUri(resource);
        final long startTime = System.currentTimeMillis();
        long timeout = 370 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                stepSimulation(1, 1, System.currentTimeMillis(), 4, backgroundUri, false);
            }
        };

        TestCaseThread t2 = new TestCaseThread(ONE_MIN) {
            public void run() throws Throwable {
                PrepareResourceUtil.prepareResource(uri, false);
                StreamUtil.getStream(uri, MAX_SIZE, 2 * 60, true);
                assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                PrepareResourceUtil.prepareResource(uri, true);
            }
        };

        try {
            executeThreads(timeout, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            PrepareResourceUtil.invalidateResourceSafely(backgroundUri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

    public void test_022_StabilityStreamingPeakLoad_stream() throws Throwable {
        final ArrayList<String> processes = processesParser();
        String resource = "asimov_stability_stream_peak_load_stream";
        final String backgroundUri = createTestResourceUri(resource + "_background");
        final String uri = createTestResourceUri(resource);
        long timeout = 370 * 1000;

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                StreamUtil.getStream(backgroundUri, BACKGROUND_STREAM_SIZE, 4 * 60, true);
                assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
            }
        };

        TestCaseThread t2 = new TestCaseThread(ONE_MIN) {
            public void run() throws Throwable {
                StreamUtil.getStream(uri, MAX_SIZE, 2 * 60, true);
                assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
            }
        };

        try {
            executeThreads(timeout, t1, t2);
//            logSleeping(5 * ONE_MIN);
//            checkMemory(" expected memory value of between ", top.getAppsInfo());
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            PrepareResourceUtil.invalidateResourceSafely(backgroundUri);
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }


}