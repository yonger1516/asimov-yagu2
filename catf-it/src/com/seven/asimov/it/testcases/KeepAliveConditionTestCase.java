package com.seven.asimov.it.testcases;


import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.conn.ConnUtils;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.*;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.tasks.streamTasks.NaqForTrxTask;
import com.seven.asimov.it.utils.logcat.tasks.streamTasks.NarForHtrxTask;
import com.seven.asimov.it.utils.logcat.wrappers.KeepaliveWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.NaqForTrxWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.NarForHtrxWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.NetlogEntry;
import com.seven.asimov.it.utils.nanohttpd.SimpleHttpServer;
import com.seven.asimov.it.utils.pms.PMSUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KeepAliveConditionTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(KeepAliveConditionTestCase.class.getSimpleName());

    private static final int RADIO_KEEPER_DELAY_MS = 5 * 1000;
    protected static final long HOUR = 60 * 60 * 1000;
    protected static final long MIN_PERIOD = 30 * 1000;
    protected static final long DAY = 24 * 60 * 60 * 1000;
    protected static final boolean OPTIMIZATION = true;
    protected static final boolean FIRST_START = true;
    protected static final long MINUTE = 60 * 1000;

    protected List<Property> managedProperties = new ArrayList<Property>();
    protected final List<String> properties = new ArrayList<String>();

    private HttpRequest request;

    protected void checkKaPresent(KAStreamHistoryTask kaStreamHistoryTask, NetlogTask netlogTask, String[] expectedKaState) {
        assertFalse("There were no KAs in the log.", kaStreamHistoryTask.getLogEntries().isEmpty());

        List<KeepaliveWrapper> applicationKas = getApplicationKa(kaStreamHistoryTask, netlogTask);
        assertTrue("No KA belonging to the application were detected", !applicationKas.isEmpty());
        int i = 0;
        for (KeepaliveWrapper keepaliveWrapper : applicationKas) {
            assertEquals(expectedKaState[i++], keepaliveWrapper.getKAstate());
        }
    }


    /**
     * Check if there are packets in the stream event history that belong to com.seven.asimov.it application
     * and contain the specified expected KA state (either "KA" or "NOT KA")
     *
     * @param kaStreamHistoryTask KA stream history task
     * @param netlogTask          Netlog task
     * @param expectedKaState     KA state (either "KA" or "NOT KA")
     */
    protected void checkKaPresent(KAStreamHistoryTask kaStreamHistoryTask, NetlogTask netlogTask, String expectedKaState) {
        assertFalse("There were no KAs in the log.", kaStreamHistoryTask.getLogEntries().isEmpty());

        List<KeepaliveWrapper> applicationKas = getApplicationKa(kaStreamHistoryTask, netlogTask);
        assertTrue("No KA belonging to the application were detected", !applicationKas.isEmpty());
        for (KeepaliveWrapper keepaliveWrapper : applicationKas) {
            assertEquals(expectedKaState, keepaliveWrapper.getKAstate());
        }
    }

    /**
     * @return All entries in stream history that belong to the application.
     */
    protected List<KeepaliveWrapper> getApplicationKa(KAStreamHistoryTask kaStreamHistoryTask, NetlogTask netlogTask) {
        List<KeepaliveWrapper> keepaliveWrapperList = new ArrayList<KeepaliveWrapper>();

        for (NetlogEntry netlogEntry : netlogTask.getLogEntries()) {
            if (netlogEntry.getApplicationName().equals("com.seven.asimov.it")) {
                for (KeepaliveWrapper keepaliveWrapper : kaStreamHistoryTask.getLogEntries()) {
                    if (keepaliveWrapper.getBfc() == netlogEntry.getClient_in() && keepaliveWrapper.getBfc() != 0) {
                        keepaliveWrapperList.add(keepaliveWrapper);
                    }
                }
            }
        }

        return keepaliveWrapperList;
    }

    /**
     * Check if NAQ and NAR belonging to the application separated by the specified time interval.
     */
    protected void checkNoNaqAfterNar(KAStreamHistoryTask kaStreamHistoryTask, NetlogTask netlogTask,
                                      NaqForTrxTask naqForTrxTask, NarForHtrxTask narForHtrxTask, int idleTime) {

        KeepaliveWrapper messageWithNaq = null;
        List<KeepaliveWrapper> applicationKas = getApplicationKa(kaStreamHistoryTask, netlogTask);
        for (KeepaliveWrapper keepaliveWrapper : applicationKas) {
            if (keepaliveWrapper.getMsg().equals("NAQ")) {
                messageWithNaq = keepaliveWrapper;
                break;
            }
        }

        assertTrue("No KA NAQ messages", messageWithNaq != null);

        String csmId = messageWithNaq.getCsmId();
        NaqForTrxWrapper naqMessage = null;
        for (NaqForTrxWrapper naqTask : naqForTrxTask.getLogEntries()) {
            if (naqTask.gethTrx().equals(csmId)) {
                naqMessage = naqTask;
                break;
            }
        }
        assertTrue("No NAQ messages", naqMessage != null);

        NarForHtrxWrapper narMessage = null;

        for (NarForHtrxWrapper narTask : narForHtrxTask.getLogEntries()) {
            if (narTask.getAppUid().equals(naqMessage.getAppUid())) {
                narMessage = narTask;
                break;
            }
        }
        assertTrue("No NAR messages", narMessage != null);


        assertTrue("Interval between NAR and NAQ should be >= " +
                idleTime / 1000 + " seconds", (naqMessage.getTimestamp() - narMessage.getTimestamp()) >= idleTime);
    }

    /**
     * Delete the OC database and kill engine and TCP dispatcher in order to cleanup
     * recent stream history.
     */
    protected void performStreamHistoryCleanup() {
        ShellUtil.killAll(TFConstantsIF.OC_PROCESS_NAME);
        ShellUtil.killAll("occ");
        TestUtil.sleep(30 * 1000);
    }

    protected void sendKeepAlive(int payloadSize, int csmMinLifeTime, int number, int keepOpenFor)
            throws IOException {
        final int PORT = 8080;
        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(ConnUtils.getLocalIpAddress().getHostAddress(), PORT);
        simpleHttpServer.start();
        TestUtil.sleep(5 * 1000);
        Socket socket = null;
        OutputStream outputStream = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ConnUtils.getLocalIpAddress().getHostAddress(), PORT));
            logSleeping(csmMinLifeTime * 1000);

            byte[] b = new byte[payloadSize];
            new Random().nextBytes(b);

            logger.info("Sending Keep-alive nr. " + number + ", payload: " + payloadSize + " bytes.");
            outputStream = socket.getOutputStream();
            outputStream.write(b);
            outputStream.flush();
            logSleeping(keepOpenFor * 1000);

        } finally {
            simpleHttpServer.stop();
            TestUtil.sleep(2 * 1000);
            if (socket != null) socket.close();
            if (outputStream != null) outputStream.close();
        }
    }

    /**
     * Send keep-alive packet (a random byte sequence of the specified length)
     *
     * @param payloadSize    The size of random byte sequence to send
     * @param csmMinLifeTime The time between socket connection creation and sending keep-alives
     * @param number         Sequence number of the keep-alive request
     * @throws IOException
     */
    protected void sendKeepAlive(int payloadSize, int csmMinLifeTime, int number)
            throws IOException {
        sendKeepAlive(payloadSize, csmMinLifeTime, number, 0);
    }

    protected void keepaliveConditionTest(List<Property> policies, boolean optimization, boolean firstStart, Radio radioState, Screen screenState, long SLEEP, int count, boolean largeResponse) throws Throwable {

        KAStreamHistoryTask kaStreamHistoryTask = new KAStreamHistoryTask();
        CalculatingFilterIDTask calculatingFilterIDTask = new CalculatingFilterIDTask();
        KAactivationTask kAactivationTask = new KAactivationTask();
        FTMmessageTask ftMmessageTask = new FTMmessageTask();
        KAUpdateProfileTask kaUpdateProfileTask = new KAUpdateProfileTask();
        TimerConditionDisabledExitedStateTask timerConditionDisabledExitedStateTask = new TimerConditionDisabledExitedStateTask();

        LogcatUtil logcatUtil = new LogcatUtil(getContext(),
                kaStreamHistoryTask,
                calculatingFilterIDTask,
                kAactivationTask,
                ftMmessageTask,
                timerConditionDisabledExitedStateTask);

        if (radioState == Radio.RADIO_UP) {
            TestCaseThread radioUpKeeperThread = createRadioUpKeeperThread();
            executeThreads(MINUTE * 15, radioUpKeeperThread, executeKACheck(policies, logcatUtil, firstStart, optimization, SLEEP, count, largeResponse,
                    screenState, kaUpdateProfileTask,
                    kaStreamHistoryTask, calculatingFilterIDTask, kAactivationTask, ftMmessageTask, timerConditionDisabledExitedStateTask,
                    radioUpKeeperThread));
        } else {
            executeKACheck(policies, logcatUtil, firstStart, optimization, SLEEP, count, largeResponse, screenState, kaUpdateProfileTask,
                    kaStreamHistoryTask, calculatingFilterIDTask, kAactivationTask, ftMmessageTask, timerConditionDisabledExitedStateTask, null);
        }
    }

    private TestCaseThread createRadioUpKeeperThread() {
        return new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                while (!isInterruptedSoftly()) {
                    pingHost(PMSUtil.getPmsServerIp());
                    TestUtil.sleep(RADIO_KEEPER_DELAY_MS);
                }
            }
        };
    }

    private TestCaseThread executeKACheck(final List<Property> policies, final LogcatUtil logcatUtil,
                                          final boolean firstStart, final boolean optimization,
                                          final long SLEEP_TIME, final int count, final boolean largeResponse,
                                          final Screen screenState,
                                          final KAUpdateProfileTask kaUpdateProfileTask,
                                          final KAStreamHistoryTask kaStreamHistoryTask,
                                          final CalculatingFilterIDTask calculatingFilterIDTask,
                                          final KAactivationTask kAactivationTask,
                                          final FTMmessageTask ftMmessageTask,
                                          final TimerConditionDisabledExitedStateTask timerConditionDisabledExitedStateTask,
                                          final TestCaseThread radioUpKeepThread) {

        return new TestCaseThread() {
            @Override
            public void run() throws Throwable {
                long delay = MINUTE * 3;
                if (optimization) {
                    delay = MINUTE * 10;
                }
                ScreenUtils.ScreenSpyResult spy = ScreenUtils.switchScreenAndSpy(getContext(), true);
                logcatUtil.start();
                Thread.sleep(MINUTE);
                try {
                    if (policies != null) {
                        for (Property p : policies) {
                            sendPolicy(p, MINUTE * 2, ConditionType.Keepalive);
                        }
                    }
                    tryToConnect(count, largeResponse);
                    Thread.sleep(delay);

                    spy = ScreenUtils.switchScreenAndSpy(getContext(), screenState == Screen.SCREEN_ON);
                    if (SLEEP_TIME > HOUR) {
                        TimeSetterUtil.setSystemTime(System.currentTimeMillis() + DateUtil.CURRENT_DEVICE_TZ_OFFSET + SLEEP_TIME);
                    }
                    if (firstStart) {
                        assertTrue("There should be calculating filter ID task", calculatingFilterIDTask.getLogEntries().size() > 0);
                        assertTrue("There should be sent FTM message to dispatchers ", ftMmessageTask.getLogEntries().size() > 0);
                    }

                    if (!optimization) {
                        assertTrue("The state didn't switch Disabled - Exited", timerConditionDisabledExitedStateTask.getLogEntries().size() > 0);
                        assertTrue("Keepalive state condition wasn't activated", kAactivationTask.getLogEntries().size() != 0);
                    } else {
                        assertTrue("The App profile should be updated", kaUpdateProfileTask.getLogEntries().size() > 0);
                        assertTrue("There are no records in KA stream history", kaStreamHistoryTask.getLogEntries().size() > 0);
                    }

                } finally {
                    Thread.sleep(MINUTE);
                    clearProperties();
                    logcatUtil.stop();
                    ScreenUtils.finishScreenSpy(getContext(), spy);
                    if (radioUpKeepThread != null) radioUpKeepThread.interruptSoftly();
                    Thread.sleep(MINUTE * 3);
                }
            }
        };
    }

    protected void tryToConnect(int number, boolean largeRequest) throws Exception {
        String[] characters = {"This is the house that Jack built.",
                "This is the malt",
                "That lay in the house that Jack built.",
                "This is the rat",
                "That ate the malt",
                "That lay in the house that Jack built",
                "This is the cat",
                "That killed the rat",
                "That ate the malt",
                "That lay in the house that Jack built"};
        Random rng = new Random();

        String pathEnd = "test_asimov";
        String uri = createTestResourceUri(pathEnd);

        try {
            PrepareResourceUtil.prepareResource(uri, false);
            for (int i = 1; i < number + 1; i++) {
                String header = characters[rng.nextInt(10)];

                if (largeRequest) {
                    request = createRequest().setUri(createTestResourceUri(uri))
                            .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                            .addHeaderField("X-OC-AddHeader_Date", "GMT")
                            .addHeaderField("X-OC-ResponseContentSize", "50000").getRequest();
                } else {
                    request = createRequest()
                            .setMethod("GET")
                            .setUri(uri)
                            .addHeaderField("X-OC-ContentEncoding", "identity")
                            .addHeaderField("SomeHeader", header)
                            .getRequest();
                }

                sendMiss(i, request);
            }
        } catch (SocketTimeoutException socketTimeoutException) {
            logger.debug("Connection failed due to " + ExceptionUtils.getStackTrace(socketTimeoutException));
        } catch (Exception e) {
            logger.debug("Response duration");
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    private void sendPolicy(Property p, long SLEEP_TIME, ConditionType conditionType) throws Throwable {
        logger.info("send policy");
        properties.add(PMSUtil.createPersonalScopeProperty(p.getName(), p.getPath(), p.getValue(), true, true));
        Thread.sleep(SLEEP_TIME);
    }

    private void pingHost(String host) throws IOException {
        Runtime.getRuntime().exec("ping -c 1 " + host);
    }

    protected void clearProperties() {
        for (String property : properties) {
            deleteProperty(property);
        }
        properties.clear();
        TestUtil.sleep(10 * 1000);
    }

    private void deleteProperty(String id) {
        try {
            PMSUtil.deleteProperty(id);
        } catch (Throwable t) {
            logger.debug(ExceptionUtils.getStackTrace(t));
        }
    }

    protected class Property {

        private String name;
        private String value;
        private String path;
        private boolean push;
        private boolean applied;
        private int networkType;
        private State[] state;
        private long delay;
        private int policyToDetete;
        private boolean before;

        public Property(String name, String path, String value) {
            this.name = name;
            this.value = value;
            this.path = path;
        }

        public Property(String name, String path, String value, boolean push) {
            this.name = name;
            this.value = value;
            this.path = path;
            this.push = push;
        }

        public Property(String name, String path, String value, boolean applied, int networkType) {
            this.name = name;
            this.value = value;
            this.path = path;
            this.applied = applied;
            this.networkType = networkType;
        }

        public Property(String name, String path, String value, long delay, int policyToDetete, boolean before, State... states) {
            this.name = name;
            this.value = value;
            this.path = path;
            this.state = states;
            this.delay = delay;
            this.policyToDetete = policyToDetete;
            this.before = before;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

        public String getPath() {
            return this.path;
        }

        public boolean getApplied() {
            return this.applied;
        }

        public State[] getState() {
            return this.state;
        }

        public long getDelay() {
            return this.delay;
        }

        public boolean getWhen() {
            return this.before;
        }
    }

    protected enum TimerState {
        DISABLED_EXITED,
        EXITED_ENTERED,
        ENTERED_EXITED,
        EXITED_DISABLED
    }

    protected enum Radio {
        RADIO_UP,
        RADIO_DOWN
    }

    protected enum Screen {
        SCREEN_ON,
        SCREEN_OFF
    }

    private enum ConditionType {
        Networktype,
        Timer,
        Keepalive,
        Traffic,
        notype
    }

    protected class State {
        private TimerState timerState;
        private boolean present;

        public State(TimerState timerState, boolean present) {
            this.timerState = timerState;
            this.present = present;
        }

        public TimerState getTimerState() {
            return this.timerState;
        }

        public boolean isPresent() {
            return present;
        }
    }
}
