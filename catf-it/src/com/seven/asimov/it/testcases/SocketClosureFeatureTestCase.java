package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.preemptiveSocketClosureTasks.OutSocketTask;
import com.seven.asimov.it.utils.logcat.tasks.preemptiveSocketClosureTasks.PreemptiveNetlogTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.tcpdump.DbAdapter;
import com.seven.asimov.it.utils.tcpdump.TcpPacket;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketClosureFeatureTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(SocketClosureFeatureTestCase.class.getSimpleName());

    protected static final String DEFERRED_PREEMPTIVE_SOCKET_CLOSURE = "deferred_preemptive_socket_closure";
    protected static final String PREEMPTIVE_SOCKET_CLOSURE = "preemptive_socket_closure";
    protected static final String uri = createTestResourceUri("test_OutSocketClosure");
    protected static final String httpsUri = createTestResourceUri("test_OutSocketClosure", true);
    protected int MIN_PERIOD = 60 * 1000;
    protected String[] propertyId = new String[3];
    private long startTime;
    private long endTime;
    private long duration;

    public long getStartTime() {
        return startTime;
    }

    protected void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    protected void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    protected void checkSocketClosure(int detectionInterval, int cooldownInterval, boolean isRstSent) throws Throwable {
        checkSocketClosure(detectionInterval, cooldownInterval, isRstSent, false);
    }

    protected void checkSocketClosure(int detectionInterval, int cooldownInterval, boolean isRstSent,
                                      boolean isCooldawnStart) throws Throwable {
        checkSocketClosure(detectionInterval, cooldownInterval, isRstSent, isCooldawnStart, false);
    }

    protected void checkSocketClosure(int detectionInterval, int cooldownInterval, boolean isRstSent, boolean
            isCooldawnStart, boolean isCoolDownInActiv) throws Throwable {
        HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContent", "" + Math.random() * 1000).getRequest();
        checkSocketClosure(detectionInterval, cooldownInterval, isRstSent, isCooldawnStart, isCoolDownInActiv, request);
    }

    protected void checkSocketClosure(int detectionInterval, int cooldownInterval, boolean isRstSent, boolean
            isCooldawnStart, boolean isCoolDownInActiv, HttpRequest request) throws Throwable {
        checkSocketClosure(detectionInterval, cooldownInterval, isRstSent, isCooldawnStart, isCoolDownInActiv, request, false);
    }

    protected void checkSocketClosure(int detectionInterval, int cooldownInterval, boolean isRstSent,
                                      boolean isCooldawnStart, boolean isCoolDownInActiv, HttpRequest request,
                                      boolean useSSL) throws Throwable {
        checkSocketClosure(detectionInterval, cooldownInterval, isRstSent, isCooldawnStart, isCoolDownInActiv, request,
                useSSL, false);
    }

    protected void checkSocketClosure(int detectionInterval, int cooldownInterval, boolean isRstSent,
                                      boolean isCooldawnStart, boolean isCoolDownInActiv, HttpRequest request,
                                      boolean useSSL, boolean isFinSent) throws Throwable {
        checkSocketClosure(detectionInterval, cooldownInterval, isRstSent, isCooldawnStart, isCoolDownInActiv, request,
                useSSL, isFinSent, PREEMPTIVE_SOCKET_CLOSURE);
    }

    protected void checkSocketClosure(int detectionInterval, int cooldownInterval, boolean isRstSent,
                                      boolean isCooldawnStart, boolean isCoolDownInActiv,
                                      HttpRequest request, boolean useSSL, boolean isFinSent, String socketClosureType)
            throws Throwable {
        OutSocketTask closingAllOutConnectionTask = new OutSocketTask(OutSocketTask.CLOSING_ALL_OUT_CONNECTIONS_REGEXP);
        OutSocketTask detectionIntervalTask = new OutSocketTask(OutSocketTask.DETECTION_INTERVAL_REGEXP, detectionInterval);
        OutSocketTask coolDownIntervalTask = new OutSocketTask(OutSocketTask.COOLDOWN_INTERVAL_REGEXP, cooldownInterval);
        OutSocketTask coolDownIntervalInActionTask = new OutSocketTask(OutSocketTask.COOLDOWN_INTERVAL_IN_ACTION_REGEXP);
        PreemptiveNetlogTask preemptiveNetlogTask = new PreemptiveNetlogTask(socketClosureType);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), coolDownIntervalInActionTask,
                closingAllOutConnectionTask, detectionIntervalTask, preemptiveNetlogTask, coolDownIntervalTask);
        logcatUtil.start();
        try {
            TestUtil.sleep(2 * 1000);
            setStartTime(System.currentTimeMillis());
            if (!isFinSent) {
                if (useSSL) {
                    sendHttpsRequest(request, true);
                } else {
                    sendRequest(request, true);
                }
                TestUtil.sleep(2 * 1000);
            } else {
                sendRequest(request);
                TestUtil.sleep(2 * 1000);
                setEndTime(System.currentTimeMillis());
                boolean resultFinSend = isFinSent(startTime, endTime);
                assertEquals("[FIN, ACK] packet sent by OC: " + resultFinSend + " but expected "
                        + true, true, resultFinSend);
            }
            setEndTime(System.currentTimeMillis());
            setDuration((endTime - startTime) + 4 * 1000);
            logcatUtil.stop();
            logger.info("Start time = " + startTime + " end time = " + endTime + "duration = " + duration);
            checkResult(isRstSent, isFinSent, isCooldawnStart, isCoolDownInActiv, closingAllOutConnectionTask,
                    preemptiveNetlogTask, detectionInterval, detectionIntervalTask, coolDownIntervalTask, coolDownIntervalInActionTask, socketClosureType);
        } finally {
            logcatUtil.stop();
        }
    }

    protected void checkResult(boolean isRstSent, boolean isFinSent, boolean isCooldawnStart, boolean isCoolDownInActiv, OutSocketTask closingAllOutConnectionTask,
                               PreemptiveNetlogTask preemptiveNetlogTask, int detectionInterval, OutSocketTask detectionIntervalTask,
                               OutSocketTask coolDownIntervalTask, OutSocketTask coolDownIntervalInActionTask, String socketClosureType) throws Throwable {
        boolean resultRstSend = isRstSent(startTime, endTime);
       /* if (socketClosureType.equals(PREEMPTIVE_SOCKET_CLOSURE)) {
            assertEquals("[RST, ACK] packet sent by OC: " + resultRstSend + " but expected "
                    + isRstSent, isRstSent, resultRstSend);
        }
        if (!isCooldawnStart && !isCoolDownInActiv && !isFinSent && (isRstSent == socketClosureType.equals(PREEMPTIVE_SOCKET_CLOSURE))) {
            assertTrue("OUT socket was closed with \'Closing all OUT connections\'",
                    closingAllOutConnectionTask.getLogEntries().size() > 0);
            assertTrue("NetLog should include operate type: \'" + socketClosureType + " \'",
                    preemptiveNetlogTask.getLogEntries().size() > 0);
            assertTrue("Detection of next transaction was started with \'entered Detection Interval for " +
                    detectionInterval + "seconds\'",
                    detectionIntervalTask.getLogEntries().size() > 0);
        } else {
            assertFalse("OUT socket was closed with \'Closing all OUT connections\'",
                    closingAllOutConnectionTask.getLogEntries().size() > 0);
            if (!isFinSent) {
                assertEquals("Entered into CoolDown Interval: " + !coolDownIntervalTask.getLogEntries().isEmpty() +
                        " but expected " + isCooldawnStart, !coolDownIntervalTask.getLogEntries().isEmpty(), isCooldawnStart);
                assertEquals("CoolDown Interval in action: " + !coolDownIntervalInActionTask.getLogEntries().isEmpty() +
                        " but expected " + isCoolDownInActiv, !coolDownIntervalInActionTask.getLogEntries().isEmpty(), isCoolDownInActiv);
            }
        }*/
    }

    protected boolean isRstSent(long startTime, long endTime) {
        boolean rstSendResult = false;
        List<TcpPacket> tcpPackets = DbAdapter.getInstance(getContext()).getTcpPackets(startTime, endTime, false);
        for (TcpPacket tcpPacket : tcpPackets) {
            logger.info("is rst " + tcpPacket.isRst());
            if (tcpPacket.isRst() && (tcpPacket.getDestinationPort() >= 80 && tcpPacket.getDestinationPort() <= 443)) {
                rstSendResult = true;
            }
        }
        return rstSendResult;
    }

    protected boolean isFinSent(long startTime, long endTime) {
        boolean finSendResult = false;
        List<TcpPacket> tcpPackets = DbAdapter.getInstance(getContext()).getTcpPackets(startTime, endTime, false);
        for (TcpPacket tcpPacket : tcpPackets) {
            logger.info("is fin " + tcpPacket.isFin());
            if (tcpPacket.isFin()) {
                finSendResult = true;
            }
        }
        return finSendResult;
    }

    protected void checkTest(int detectionInterval, int cooldownInterval) throws Throwable {
        URI uriResource = new URI(uri);
        String request = "GET " + uriResource.getPath() + " HTTP/1.1\r\n" +
                "Host: " + uriResource.getHost() + "\r\n" +
                "X-OC-ContentEncoding: identity\r\n" +
                "X-OC-ResponseContent: " + Math.random() * 1000 + "\r\n" +
                "Connection: Keep-Alive\r\n\r\n";

        String request2 = "GET " + uriResource.getPath() + " HTTP/1.1\r\n" +
                "Host: " + uriResource.getHost() + "\r\n" +
                "X-OC-ContentEncoding: identity\r\n" +
                "X-OC-Sleep: 30\r\n" +
                "X-OC-ResponseContent: " + Math.random() * 1000 + "\r\n" +
                "Connection: Keep-Alive\r\n\r\n";

        Socket socket = new Socket(uriResource.getHost(), 80);
        socket.setKeepAlive(true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Writer writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        OutSocketTask closingAllOutConnectionTask = new OutSocketTask(OutSocketTask.CLOSING_ALL_OUT_CONNECTIONS_REGEXP);
        OutSocketTask detectionIntervalTask = new OutSocketTask(OutSocketTask.DETECTION_INTERVAL_REGEXP, detectionInterval);
        OutSocketTask coolDownIntervalTask = new OutSocketTask(OutSocketTask.COOLDOWN_INTERVAL_REGEXP, cooldownInterval);
        OutSocketTask coolDownIntervalInActionTask = new OutSocketTask(OutSocketTask.COOLDOWN_INTERVAL_IN_ACTION_REGEXP);
        PreemptiveNetlogTask preemptiveNetlogTask = new PreemptiveNetlogTask(PREEMPTIVE_SOCKET_CLOSURE);
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), coolDownIntervalInActionTask,
                closingAllOutConnectionTask, detectionIntervalTask, preemptiveNetlogTask, coolDownIntervalTask);
        logcatUtil.start();
        try {
            setStartTime(System.currentTimeMillis());
            TestUtil.sleep(2 * 1000);
            logger.info("Transaction #1");
            writer.write(request);
            writer.flush();
            TestUtil.sleep(35 * 1000);
            setEndTime(System.currentTimeMillis());
            setDuration(getEndTime() - getStartTime());
            TestUtil.sleep(6 * 1000 - getDuration());
            logcatUtil.stop();
            checkResult(true, false, false, false, closingAllOutConnectionTask,
                    preemptiveNetlogTask, detectionInterval, detectionIntervalTask, coolDownIntervalTask, coolDownIntervalInActionTask, PREEMPTIVE_SOCKET_CLOSURE);
            logger.info("Transaction #2");
            writer.write(request2);
            writer.flush();
            TestUtil.sleep(5 * 1000);
            setEndTime(System.currentTimeMillis());
            setDuration(getEndTime() - getStartTime());
            logcatUtil = new LogcatUtil(getContext(), coolDownIntervalInActionTask,
                    closingAllOutConnectionTask, detectionIntervalTask, preemptiveNetlogTask, coolDownIntervalTask);
            logcatUtil.start();
            TestUtil.sleep(5 * 1000 - getDuration());
            logger.info("Transaction #3");
            setStartTime(System.currentTimeMillis());
            TestUtil.sleep(2 * 1000);
            writer.write(request);
            writer.flush();
            TestUtil.sleep(5 * 1000);
            setEndTime(System.currentTimeMillis());
            setDuration(getEndTime() - getStartTime());
            TestUtil.sleep(30 * 1000);
            logcatUtil.stop();
            checkResult(false, false, true, true, closingAllOutConnectionTask,
                    preemptiveNetlogTask, detectionInterval, detectionIntervalTask, coolDownIntervalTask, coolDownIntervalInActionTask, PREEMPTIVE_SOCKET_CLOSURE);
            TestUtil.sleep(35 * 1000);
        } finally {
            logcatUtil.stop();
            writer.close();
            reader.close();
        }
    }

    private Thread getRadioKeep(final int seconds) {
        final String host = PMSUtil.getPmsServerIp();
        return new Thread() {
            @Override
            public void run() {
                int i = 0;
                while (i < seconds / 3) {
                    try {
                        try {
                            Runtime.getRuntime().exec("ping -s1 -c1 -W3 -n " + host).waitFor();
                            logger.info("Ping radioUP " + host);
                        } catch (InterruptedException interruptedExceprion) {
                            ExceptionUtils.getStackTrace(interruptedExceprion);
                        }
                        TestUtil.sleep(3 * 1000);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    i++;
                }
            }
        };
    }

    protected void changeRadioUP(int seconds) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(getRadioKeep(seconds));
        executorService.shutdown();
        if (!executorService.awaitTermination(seconds, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    }

    protected void banNetworkForAllApp(boolean ban) throws IOException, InterruptedException {
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        int csa;
        int csat;
        mobileNetworkUtil.onWifiOnly();
        csa = IpTablesUtil.getApplicationUid(getContext(), TFConstantsIF.OC_PACKAGE_NAME);
        csat = IpTablesUtil.getApplicationUid(getContext(), TFConstantsIF.IT_PACKAGE_NAME);
        IpTablesUtil.banNetworkForAllApplications(ban);
        IpTablesUtil.allowNetworkForApplication(ban, csa);
        IpTablesUtil.allowNetworkForApplication(ban, csat);
        mobileNetworkUtil.on3gOnly();
    }
}
