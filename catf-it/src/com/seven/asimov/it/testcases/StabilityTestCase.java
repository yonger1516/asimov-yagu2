package com.seven.asimov.it.testcases;

import android.content.Context;
import android.content.res.AssetManager;
import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.constants.BaseConstantsIF;
import com.seven.asimov.it.base.interfaces.HttpUrlConnectionIF;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.sysmonitor.AppInfo;
import com.seven.asimov.it.utils.sysmonitor.AppInfoOC;
import com.seven.asimov.it.utils.sysmonitor.DispatcherInfo;
import com.seven.asimov.it.utils.sysmonitor.SystemMonitorUtil;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class StabilityTestCase extends TcpDumpTestCase {

    private static final Logger logger = LoggerFactory.getLogger(StabilityTestCase.class.getSimpleName());
    private static SystemMonitorUtil monitorUtil = SystemMonitorUtil.getInstance(getStaticContext(), "com.seven.asimov",
            "MemoryLeak", BaseConstantsIF.SD_CARD + "/OCIntegrationTestsResults");
    private ArrayList<String> tempListInfo = new ArrayList<String>();
    protected final long ONE_MIN = 60 * 1000;
    protected final long ONE_HOUER = 60 * ONE_MIN;
    protected final int NORMAL_SIZE = 25600;
    protected final int MAX_SIZE = 524288;
    private long sleepTime = 10 * 1000;
    protected final int[] SIZE = {1, 10, 100, 1000, 10000, /*100000*//*, 1000000*/};
    protected final int BACKGROUND_STREAM_SIZE = 20 * 1024;
    protected final String CONTROLLER_CRASH_MASS = "Controller crash ";

    protected List<Throwable> exceptions = new ArrayList<Throwable>();

    protected ArrayList<ProcessInfo> memoryHttp = new ArrayList<ProcessInfo>();
    protected ArrayList<ProcessInfo> memoryHttps = new ArrayList<ProcessInfo>();
    protected ArrayList<ProcessInfo> memoryOcc = new ArrayList<ProcessInfo>();
    protected ArrayList<ProcessInfo> memoryDns = new ArrayList<ProcessInfo>();
    protected ArrayList<ProcessInfo> memoryEngine = new ArrayList<ProcessInfo>();

    protected int memoryIteration = 1;

//    protected javax.net.ssl.SSLSocketFactory mSSLSocketFactory;

    protected ArrayList<String> processesParser() {
        Map<String, Integer> processes = OCUtil.getOcProcesses(false);
        ArrayList<String> result = new ArrayList<String>();
        if (processes.get("ocdnsd") != null) {
            result.add("ocdnsd=" + processes.get("ocdnsd"));
            result.add("ochttpd=" + processes.get("ochttpd"));
            result.add("ocshttpd=" + processes.get("ocshttpd"));
        } else {
            result.add("dns=" + processes.get("dns"));
            result.add("http=" + processes.get("http"));
            result.add("https=" + processes.get("https"));
        }
        result.add("occ=" + processes.get("occ"));
        result.add("com.seven.asimov=" + processes.get("com.seven.asimov"));
        return result;
    }

    protected HttpResponse sendHttpTopMiss(int requestId, HttpRequest request)
            throws MalformedURLException, IOException, URISyntaxException {
        logRequest(requestId, request.getUri());
        HttpResponse response = sendHttpRequest(request);
        assertTrue("StatusCode ", (response.getStatusCode() >= 100 && response.getStatusCode() < 400));

        return response;
    }

    protected HttpResponse sendHttpMiss(int requestId, HttpRequest request)
            throws MalformedURLException, IOException, URISyntaxException {
        HttpResponse response;
        boolean isSslModeOn = false;
        String uri = request.getUri();
        if (uri.startsWith("https://")) isSslModeOn = true; // we are going to send HTTPS request
        logRequest(requestId, uri);
        if (isSslModeOn) {
            response = sendHttpsRequest(request, this);
        } else {
            response = sendRequest2(request, false, false, 5 * 60 * 1000);
        }
        assertEquals("StatusCode ", HttpStatus.SC_OK, response.getStatusCode());
        return response;
    }

    protected HttpResponse sendHttpMiss(int requestId, HttpRequest request, Body bodyType)
            throws MalformedURLException, IOException, URISyntaxException {
        logRequest(requestId, request.getUri());
        HttpResponse response = sendHttpRequest(request, bodyType);
        assertEquals("StatusCode ", HttpStatus.SC_OK, response.getStatusCode());
        return response;
    }

    protected HttpResponse sendHttpRequest(HttpRequest request)
            throws MalformedURLException, IOException, URISyntaxException {
        return sendRequest(request, null, false, false, Body.BODY);
    }

    protected HttpResponse sendHttpRequest(HttpRequest request, Body bodyType)
            throws MalformedURLException, IOException, URISyntaxException {
        return sendRequest(request, null, false, false, bodyType);
    }

    //========================================HTTPS============================================

    protected HttpResponse sendHttpsTopMiss(int requestId, HttpRequest request) throws Throwable {
        logRequest(requestId, request.getUri());
        HttpResponse response = sendHttpsRequest(request, this);
        assertTrue("StatusCode ", (response.getStatusCode() >= 100 && response.getStatusCode() < 400));
//        addCheckFromNetworkHttps(requestId, response);
        return response;
    }

    protected HttpResponse sendHttpsMiss(int requestId, HttpRequest request) throws Throwable {
        logRequest(requestId, request.getUri());
        HttpResponse response = sendHttpsRequest(request, this);
        assertEquals("StatusCode ", HttpStatus.SC_OK, response.getStatusCode());
//        addCheckFromNetworkHttps(requestId, response);
        return response;
    }

    protected HttpResponse sendHttpsMiss(int requestId, HttpRequest request, Body bodyType) throws Throwable {
        logRequest(requestId, request.getUri());
        HttpResponse response = sendHttpsRequest(request, this, bodyType);
        assertEquals("StatusCode ", HttpStatus.SC_OK, response.getStatusCode());
//        addCheckFromNetworkHttps(requestId, response);
        return response;
    }

    protected HttpResponse sendHttpsRequest(HttpRequest request, HttpUrlConnectionIF decorator)
            throws MalformedURLException, IOException, URISyntaxException {
        return sendRequest(request, decorator, false, false, Body.BODY);
    }

    protected HttpResponse sendHttpsRequest(HttpRequest request, HttpUrlConnectionIF decorator, Body bodyType)
            throws MalformedURLException, IOException, URISyntaxException {
        return sendRequest(request, decorator, false, false, bodyType);
    }

    public HttpResponse sendHttpsRequest(byte[] request, String host) {
        return sendRequest(request, host, sslSocketFactory);
    }

    @Override
    public void decorate(HttpURLConnection conn) throws IOException {
        if (conn instanceof HttpsURLConnection) {
            logger.info("Https decorator applied");
            HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
            httpsConnection.setSSLSocketFactory(sslSocketFactory);
            httpsConnection.setHostnameVerifier(new AllowAllHostnameVerifier());
        }
    }

    protected void logSleeping(long time) {
        TestUtil.sleep(time);
    }

    public class ProcessInfo {
        private int iteration;
        private int memoryStart;
        private int memoryEnd;
        private double memoryStartEnd;
        private double cpuStart;
        private double cpuEnd;
        private double cpuStartEnd;

        public int getIteration() {
            return iteration;
        }

        public void setIteration(int iteration) {
            this.iteration = iteration;
        }

        public int getMemoryStart() {
            return memoryStart;
        }

        public void setMemoryStart(int memoryStart) {
            this.memoryStart = memoryStart;
        }

        public int getMemoryEnd() {
            return memoryEnd;
        }

        public void setMemoryEnd(int memoryEnd) {
            this.memoryEnd = memoryEnd;
        }

        public double getMemoryStartEnd() {
            return memoryStartEnd;
        }

        public void setMemoryStartEnd(double memoryStartEnd) {
            this.memoryStartEnd = memoryStartEnd;
        }

        public double getCpuEnd() {
            return cpuEnd;
        }

        public void setCpuEnd(double cpuEnd) {
            this.cpuEnd = cpuEnd;
        }

        public double getCpuStart() {
            return cpuStart;
        }

        public void setCpuStart(double cpuStart) {
            this.cpuStart = cpuStart;
        }

        public double getCpuStartEnd() {
            return cpuStartEnd;
        }

        public void setCpuStartEnd(double cpuStartEnd) {
            this.cpuStartEnd = cpuStartEnd;
        }

        @Override
        public String toString() {
            String temp;
            StringBuilder result = new StringBuilder();
            if (("" + iteration).length() < 2)
                result.append(iteration).append("       ");
            else if (("" + iteration).length() < 3)
                result.append(iteration).append("      ");
            else result.append(iteration).append("     ");
            if (("" + memoryStart).length() < 4)
                result.append(memoryStart).append("          ");
            else if (("" + memoryStart).length() < 5)
                result.append(memoryStart).append("         ");
            else result.append(memoryStart).append("        ");
            if (("" + memoryEnd).length() < 4)
                result.append(memoryEnd).append("        ");
            else if (("" + memoryEnd).length() < 5)
                result.append(memoryEnd).append("       ");
            else result.append(memoryEnd).append("      ");
            temp = String.format("%.2f", memoryStartEnd);
            if (temp.length() < 5)
                result.append(temp).append("               ");
            else if (temp.length() < 6)
                result.append(temp).append("              ");
            else if (temp.length() < 7)
                result.append(temp).append("             ");
            else result.append(temp).append("            ");
            if (("" + cpuStart).length() < 2)
                result.append(cpuStart).append("          ");
            else if (("" + cpuStart).length() < 3)
                result.append(cpuStart).append("         ");
            else result.append(cpuStart).append("        ");
            if (("" + cpuEnd).length() < 2)
                result.append(cpuEnd).append("        ");
            else if (("" + cpuEnd).length() < 3)
                result.append(cpuEnd).append("       ");
            else result.append(cpuEnd).append("      ");
            result.append(String.format("%.2f", cpuStartEnd));
            return result.toString();
        }
    }

    protected ArrayList<String> addMemoryInfo(int i, List<AppInfo> list, FileWriter httpWriter, FileWriter httpsWriter,
                                              FileWriter dnsWriter, FileWriter occWriter, FileWriter engineWriter) {
        ProcessInfo processInfoHttp = new ProcessInfo();
        ProcessInfo processInfoHttps = new ProcessInfo();
        ProcessInfo processInfoOcc = new ProcessInfo();
        ProcessInfo processInfoDns = new ProcessInfo();
        ProcessInfo processInfoEngine = new ProcessInfo();
        processInfoHttp.setIteration(i);
        processInfoHttps.setIteration(i);
        processInfoOcc.setIteration(i);
        processInfoDns.setIteration(i);
        processInfoEngine.setIteration(i);

        if (list.get(0) instanceof AppInfoOC) {
            for (DispatcherInfo dispatcherInfo : ((AppInfoOC) list.get(i)).getDispatcherList()) {
                if (dispatcherInfo.getName().equals("ochttpd")) {
                    processInfoHttp.setMemoryStart(dispatcherInfo.getMemoryUsage());
                    processInfoHttp.setCpuStart(dispatcherInfo.getCpuUsage());
                }
                if (dispatcherInfo.getName().equals("ocshttpd")) {
                    processInfoHttps.setMemoryStart(dispatcherInfo.getMemoryUsage());
                    processInfoHttps.setCpuStart(dispatcherInfo.getCpuUsage());
                }
                if (dispatcherInfo.getName().equals("ocdnsd")) {
                    processInfoDns.setMemoryStart(dispatcherInfo.getMemoryUsage());
                    processInfoDns.setCpuStart(dispatcherInfo.getCpuUsage());
                }
                processInfoOcc.setMemoryStart(dispatcherInfo.getMemoryUsage());
                processInfoOcc.setCpuStart(dispatcherInfo.getCpuUsage());
                processInfoEngine.setMemoryStart(dispatcherInfo.getMemoryUsage());
                processInfoEngine.setCpuStart(dispatcherInfo.getCpuUsage());
            }
        }
        if (list.get(list.size() - 1) instanceof AppInfoOC) {
            for (DispatcherInfo dispatcherInfo : ((AppInfoOC) list.get(i)).getDispatcherList()) {
                if (dispatcherInfo.getName().equals("ochttpd")) {
                    processInfoHttp.setMemoryEnd(dispatcherInfo.getMemoryUsage());
                    processInfoHttp.setCpuEnd(dispatcherInfo.getCpuUsage());
                }
                if (dispatcherInfo.getName().equals("ocshttpd")) {
                    processInfoHttps.setMemoryEnd(dispatcherInfo.getMemoryUsage());
                    processInfoHttps.setCpuEnd(dispatcherInfo.getCpuUsage());
                }
                if (dispatcherInfo.getName().equals("ocdnsd")) {
                    processInfoDns.setMemoryEnd(dispatcherInfo.getMemoryUsage());
                    processInfoDns.setCpuEnd(dispatcherInfo.getCpuUsage());
                }
                processInfoOcc.setMemoryEnd(dispatcherInfo.getMemoryUsage());
                processInfoOcc.setCpuStart(dispatcherInfo.getCpuUsage());
                processInfoEngine.setMemoryEnd(dispatcherInfo.getMemoryUsage());
                processInfoEngine.setCpuEnd(dispatcherInfo.getCpuUsage());
            }
        }

        processInfoHttp.setMemoryStartEnd((double) (processInfoHttp.getMemoryEnd() - processInfoHttp.getMemoryStart()) / processInfoHttp.getMemoryStart() * 100);
        processInfoHttps.setMemoryStartEnd((double) (processInfoHttps.getMemoryEnd() - processInfoHttps.getMemoryStart()) / processInfoHttps.getMemoryStart() * 100);
        processInfoOcc.setMemoryStartEnd((double) (processInfoOcc.getMemoryEnd() - processInfoOcc.getMemoryStart()) / processInfoOcc.getMemoryStart() * 100);
        processInfoDns.setMemoryStartEnd((double) (processInfoDns.getMemoryEnd() - processInfoDns.getMemoryStart()) / processInfoDns.getMemoryStart() * 100);
        processInfoEngine.setMemoryStartEnd((double) (processInfoEngine.getMemoryEnd() - processInfoEngine.getMemoryStart()) / processInfoEngine.getMemoryStart() * 100);

        processInfoHttp.setCpuStartEnd(processInfoHttp.getCpuEnd() - processInfoHttp.getCpuStart());
        processInfoHttps.setCpuStartEnd(processInfoHttps.getCpuEnd() - processInfoHttps.getCpuStart());
        processInfoDns.setCpuStartEnd(processInfoDns.getCpuEnd() - processInfoDns.getCpuStart());
        processInfoOcc.setCpuStartEnd(processInfoOcc.getCpuEnd() - processInfoOcc.getCpuStart());
        processInfoEngine.setCpuStartEnd(processInfoEngine.getCpuEnd() - processInfoEngine.getCpuStart());
        tempListInfo.add(processInfoHttp.toString());
        tempListInfo.add(processInfoHttps.toString());
        tempListInfo.add(processInfoDns.toString());
        tempListInfo.add(processInfoOcc.toString());
        tempListInfo.add(processInfoEngine.toString());
        try {
            httpWriter.write(processInfoHttp.toString() + "\r\n");
            httpsWriter.write(processInfoHttps.toString() + "\r\n");
            dnsWriter.write(processInfoDns.toString() + "\r\n");
            occWriter.write(processInfoOcc.toString() + "\r\n");
            engineWriter.write(processInfoEngine.toString() + "\r\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        memoryHttp.add(processInfoHttp);
        memoryHttps.add(processInfoHttps);
        memoryOcc.add(processInfoOcc);
        memoryDns.add(processInfoDns);
        memoryEngine.add(processInfoEngine);
        return tempListInfo;
    }

    protected void printMemory() {
        logger.info("Http:\niter    memoStart    memoEnd    % memoStart_end    cpuStart   cpuEnd   cpuStartEnd");
        for (ProcessInfo aMemoryHttp : memoryHttp) logger.info(aMemoryHttp.toString());
        logger.info("Https:\niter    memoStart    memoEnd    % memoStart_end    cpuStart   cpuEnd   cpuStartEnd");
        for (ProcessInfo aMemoryHttps : memoryHttps) logger.info(aMemoryHttps.toString());
        logger.info("DNS:\niter    memoStart    memoEnd    % memoStart_end    cpuStart   cpuEnd   cpuStartEnd");
        for (ProcessInfo aMemoryOcc : memoryOcc) logger.info(aMemoryOcc.toString());
        logger.info("OCC:\niter    memoStart    memoEnd    % memoStart_end    cpuStart   cpuEnd   cpuStartEnd");
        for (ProcessInfo aMemoryDns : memoryDns) logger.info(aMemoryDns.toString());
        logger.info("Engine:\niter    memoStart    memoEnd    % memoStart_end    cpuStart   cpuEnd   cpuStartEnd");
        for (ProcessInfo aMemoryEngine : memoryEngine) logger.info(aMemoryEngine.toString());
    }

    protected long[] getIterationCount() {
        long[] array = new long[2];
        long count = 0;
        long time = 0;
        int iterationPeriod = 5;
        Context context = this.getContext();
        AssetManager assetManager = null;
        if (context != null) {
            assetManager = context.getAssets();
        }
        InputStream in = null;
        try {
            if (assetManager != null) {
                in = assetManager.open("property.xml");
            }
            Properties prop = new Properties();
            prop.loadFromXML(in);
            time = Integer.parseInt(prop.getProperty("time"));
            switch ((int) time) {
                case 0:
                    count = memoryIteration;
                    time = 1;
                    break;
                default:
                    count = TimeUnit.HOURS.toMinutes(time) / iterationPeriod;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
            }
        }
        logger.error("time = " + time);
        logger.error("count = " + count);
        array[0] = count;
        array[1] = time;
        return array;
    }

    protected void assertOCCrash(String message, boolean expected, boolean actual) {
        message = "Thread " + getShortThreadName() + ": " + message;
        try {
            Assert.assertEquals(message, expected, actual);
        } catch (AssertionFailedError error) {
            exceptions.add(error);
            logger.error(message);
            throw error;
        }
    }

    protected void assertControllerCrash(String message, ArrayList<String> expected, ArrayList<String> actual) {
        message = "Thread " + getShortThreadName() + ": " + message;
        try {
            for (int i = 0; i < expected.size(); i++) {
                Assert.assertEquals(message + expected.get(i).substring(0, expected.get(i).indexOf("=")),
                        expected.get(i).substring(expected.get(i).indexOf("=") + 1), actual.get(i).substring(actual.get(i).indexOf("=") + 1));
            }
        } catch (AssertionFailedError error) {
            message = message + " True";
            exceptions.add(error);
            logger.error(message);
            throw error;
        }
    }

    protected void cleanTempList() {
        memoryHttp.clear();
        memoryHttps.clear();
        memoryOcc.clear();
        memoryDns.clear();
        memoryEngine.clear();
    }

    protected int stepSimulation(int stepID, int requestId, long startTime, int stepTime, String uri,
                                 boolean isSizeLoad) throws Throwable {
        return stepSimulation(stepID, requestId, startTime, stepTime, uri, isSizeLoad, null);
    }

    protected int stepSimulation(int stepID, int requestId, long startTime, int stepTime, String uri,
                                 boolean isSizeLoad, ArrayList<String> processes) throws Throwable {
        HttpRequest request;
        HttpResponse response;
        int id = requestId;
        int temp = stepID == 2 ? 10 : 1;
        while (true) {
            for (int i = 1; i <= temp; i++) {
                try {
                    request = createRequest().setUri(uri).setMethod("GET")
                            .addHeaderField("X-OC-ContentEncoding", "identity")
                            .addHeaderField("Random", TestUtil.generationRandomString())
                            .getRequest();
                    if (isSizeLoad)
                        request.addHeaderField(new HttpHeaderField("X-OC-ResponseContentSize", (stepID != 2 ? NORMAL_SIZE : MAX_SIZE) + ",c"));
                    response = sendHttpMiss(id++, request);
                    assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                    logSleeping(sleepTime / temp - response.getDuration());
                    if (processes != null) {
                        assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                    }
                    assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
                } catch (SocketTimeoutException ex) {
                    ex.printStackTrace();
                }
                if (System.currentTimeMillis() - startTime >= stepTime * ONE_MIN) {
                    break;
                }
            }
            if (System.currentTimeMillis() - startTime >= stepTime * ONE_MIN) {
                break;
            }
        }
        return id;
    }

    protected void memoryLeakSimulation(String name, FileWriter[] fileWriters, int[] stepTime, String uri,
                                        boolean isSizeLoad) throws Throwable {
        monitorUtil.setWriteToFile(false);
        monitorUtil.start();
        int id = 1;
        long startTime;
        long globalStartTime = System.currentTimeMillis();
        long[] iterationCount = getIterationCount();
//        logger.error("" + Arrays.asList(iterationCount).toString());
        List<AppInfo> list = null;
        PrepareResourceUtil.prepareResource(uri, false);
        for (int i = 0; i < iterationCount[0]; i++) {
            monitorUtil.startedNewTest("");
            startTime = System.currentTimeMillis();
            id = stepSimulation(1, id, startTime, stepTime[0], uri, isSizeLoad);
            startTime = System.currentTimeMillis();
            id = stepSimulation(2, id, startTime, stepTime[1], uri, isSizeLoad);
            startTime = System.currentTimeMillis();
            id = stepSimulation(3, id, startTime, stepTime[2], uri, isSizeLoad);
            logSleeping(2 * ONE_MIN);
            monitorUtil.endedTest();
            list = SystemMonitorUtil.getTestResults();
            logger.info("Step " + (i + 1));
            addMemoryInfo(i + 1, list, fileWriters[0], fileWriters[1], fileWriters[2], fileWriters[3], fileWriters[4]);
            printMemory();
//            top.clear();
            if (System.currentTimeMillis() - globalStartTime >= (iterationCount[1] * ONE_HOUER + 30 * ONE_MIN)) {
                break;
            }
        }
        monitorUtil.stop();
    }

    protected void stabilitySimulation(int[] stepTime, String uri, boolean isSizeLoad) throws Throwable {
        ArrayList<String> processes = processesParser();
        long startTime;
        int id = 1;
        PrepareResourceUtil.prepareResource(uri, false);
        startTime = System.currentTimeMillis();
        id = stepSimulation(1, id, startTime, stepTime[0], uri, isSizeLoad, processes);
        startTime = System.currentTimeMillis();
        id = stepSimulation(2, id, startTime, stepTime[1], uri, isSizeLoad, processes);
        startTime = System.currentTimeMillis();
        stepSimulation(3, id, startTime, stepTime[2], uri, isSizeLoad, processes);
    }

//    private void assertMemory(String log, String message, long expected, long actual) {
//        double minValue = expected * 0.975;
//        double maxValue = expected * 1.025;
//        message = message + minValue + " and " + maxValue + " but was " + actual;
//        try {
//            assertTrue(message, (minValue < actual && actual < maxValue));
//        } catch (AssertionFailedError error) {
//            exceptions.add(error);
//            logger.error(message);
//            throw error;
//        }
//    }

//    protected void checkMemory(String message, List<TopUtil.AppInfo> list) {
//        long startTime = list.get(0).getTime();
//        long stopTime = list.get(list.size() - 1).getTime();
//        int startMemoryHttp = 0;
//        int stopMemoryHttp = 0;
//        int startMemoryHttps = 0;
//        int stopMemoryHttps = 0;
//        for (TopUtil.AppInfo aList : list) {
//            if (aList.getTime() <= startTime) {
//                if (aList.getAppName().equals("ochttpd"))
//                    startMemoryHttp = aList.getAppRss();
//                if (aList.getAppName().equals("ocshttpd"))
//                    startMemoryHttps = aList.getAppRss();
//            } else if (aList.getTime() == stopTime) {
//                if (aList.getAppName().equals("ochttpd"))
//                    stopMemoryHttp = aList.getAppRss();
//                if (aList.getAppName().equals("ocshttpd"))
//                    stopMemoryHttps = aList.getAppRss();
//            }
//        }
//        assertMemory("Memory", "Http dispatcher" + message, startMemoryHttp, stopMemoryHttp);
//        assertMemory("Memory", "Https dispatcher" + message, startMemoryHttps, stopMemoryHttps);
//    }

    protected class App implements Callable<Boolean> {
        private final long timeOut = 12 * 60 * 1000;
        private int threadNumber = 0;
        private int testNumber = 0;
        private String uri;
        private long startTime;

        public App(int threadNumber, int testNumber, String uri) {
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
            final ArrayList<String> processes = processesParser();

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
                                    .addHeaderField("Random" + threadNumber, TestUtil.generationRandomString())
                                    .getRequest();
                            response = sendHttpMiss(requestId++, request);
                            assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                            assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
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
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                PrepareResourceUtil.invalidateResourceSafely(uri);
                return done;
            }

        }

        private Boolean maxHttpsLoad(int threadNumber) {
            boolean done = true;
            int time = 6 * 1000;
            int i = 5;
            boolean exit = false;
            final ArrayList<String> processes = processesParser();

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
                                    .addHeaderField("Random" + threadNumber, TestUtil.generationRandomString())
                                    .getRequest();
                            response = sendHttpsMiss(requestId++, request);
                            assertControllerCrash(CONTROLLER_CRASH_MASS, processes, processesParser());
                            assertOCCrash("OC running", true, OCUtil.isOpenChannelRunning());
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
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                PrepareResourceUtil.invalidateResourceSafely(uri);
                return done;
            }

        }

    }
}
