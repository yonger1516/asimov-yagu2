package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.DnsUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.wrappers.NetlogEntry;
import com.seven.asimov.it.utils.logcat.wrappers.OperationType;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetlogDnsTestCase extends AsimovTestCase {

    private static final Logger logger = LoggerFactory.getLogger(NetlogHttpTestCase.class.getSimpleName());

    protected LogcatUtil logcatUtil;
    protected TcpDumpUtil tcpDump;

    protected void setUp() throws Exception {
        //Log.v(TAG, "setUp()");
        super.setUp();
        Thread.currentThread().setName("0");
        netlogTask = new NetlogTask();
        logcatUtil = new LogcatUtil(getContext(), netlogTask);
        logcatUtil.start();
        tcpDump = TcpDumpUtil.getInstance(getContext());
        tcpDump.start();
    }

    @Override
    protected void tearDown() throws Exception {
        //Log.v(TAG, "tearDown()");
        tcpDump.stop();
        logcatUtil.stop();
        logcatUtil.logTasks();
        DnsUtil.resolveHost(AsimovTestCase.TEST_RESOURCE_HOST);
        super.tearDown();
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        executeChecks();
    }

    private void executeChecks() {
        try {
            tcpDump.stop();
            logcatUtil.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ExecuteCheck check : mChecksQueue) {
            check.executeCheck();
        }
        mChecksQueue.clear();
    }


    private class DnsCheck implements ExecuteCheck {
        private long timeStart;
        private long timeStop;
        private String host;
        private String errorCode;
        private boolean loportZeroControl;
        private boolean netportZeroControl;
        private boolean cacheInZeroControl;
        private boolean cacheOutZeroControl;

        private int loPort;
        private int netPort;

        public DnsCheck(long timeStart, long timeStop, String host, boolean loportZeroControl, boolean netportZeroControl,
                        boolean cacheInZeroControl, boolean cacheOutZeroControl, String errorCode) {
            this.timeStart = timeStart;
            this.timeStop = timeStop;
            this.host = host;
            this.loportZeroControl = loportZeroControl;
            this.netportZeroControl = netportZeroControl;
            this.cacheInZeroControl = cacheInZeroControl;
            this.cacheOutZeroControl = cacheOutZeroControl;
            this.errorCode = errorCode;
        }

        @Override
        public void executeCheck() {
            logger.debug("StartTime=" + this.timeStart + " TimeEnd=" + this.timeStop);

            NetlogEntry entry = null;
            for (NetlogEntry entryInner : netlogTask.getLogEntries()) {
                logger.debug("EntryAll= " + entryInner);
                if (entryInner.getHost().equals(this.host) &&
                        entryInner.getTimestamp() <= this.timeStop &&
                        entryInner.getTimestamp() >= this.timeStart) {
                    logger.debug("Entry= " + entryInner);
                    entry = entryInner;
                }
            }

            if (entry != null) {
                String errorCode = entry.getErrorCode();

                this.loPort = entry.getLoport();
                this.netPort = entry.getNetport();

                assertEquals("Incorrect ApplicationName value in netlog", "dns", entry.getApplicationName());
                assertEquals("Incorrect Operation type value in netlog", OperationType.proxy_dns, entry.getOpType());
                assertEquals("Incorrect LocalProtocolStack value in netlog", "-/-/dns/udp",
                        entry.getLocalProtocolStack().substring(0, entry.getLocalProtocolStack().length() - 1));

                logger.debug("Loport extracted from netlogs for dns sessions: " + this.loPort);
                logger.debug("Netport extracted from netlogs for dns sessions: " + this.netPort);

                if (loportZeroControl) {
                    assertTrue("Loport is not equals '0'. Loport: " + this.loPort, this.loPort == 0);
                } else {
                    assertTrue("Loport is equals '0', but it shouldn't.", this.loPort != 0);
                }
                if (netportZeroControl) {
                    assertTrue("Netport is not equals '0'. Netport: " + this.netPort, this.netPort == 0);
                } else {
                    assertTrue("Netport is equals '0', but it shouldn't.", this.netPort != 0);
                }

                if (cacheInZeroControl) {
                    assertTrue("CacheIn is not equals '0'. CacheIn: " + entry.getCache_in(), entry.getCache_in() == 0);
                } else {
                    assertTrue("CacheIn is equals '0', but it shouldn't.", entry.getCache_in() != 0);
                }
                if (cacheOutZeroControl) {
                    assertTrue("CacheOut is not equals '0'. CacheIn: " + entry.getCache_out(), entry.getCache_out() == 0);
                } else {
                    assertTrue("CacheOut is equals '0', but it shouldn't.", entry.getCache_out() != 0);
                }

                if (!(this.errorCode == null)) {
                    assertEquals("ErrorCode is not as expected. Expected: " + this.errorCode + ", but was: " + errorCode,
                            this.errorCode, errorCode);
                }
            } else {
                throw new AssertionFailedError("Can't find netlog in logcat between " + this.timeStart + " and " +
                        this.timeStop + " LoPort = " + loPort + " NetPort = " + netPort);
            }
        }
    }

    private interface ExecuteCheck {
        public void executeCheck();
    }

    protected NetlogTask netlogTask;
    private Queue<ExecuteCheck> mChecksQueue = new ConcurrentLinkedQueue<ExecuteCheck>();
    private final String enginePath = "@asimov@failovers@restart@engine";
    private final String controllerPath = "@asimov@failovers@restart@controller";
    private final String dispatchersPath = "@asimov@failovers@restart@dispatchers";
    private String paramName = "enabled";
    private String paramValue = "false";

    public void addDnsCheck(long timeStart, long timeStop, String host, boolean loportZeroControl, boolean netportZeroControl,
                            boolean cacheInZeroControl, boolean cacheOutZeroControl, String errorCode) {
        mChecksQueue.add(new DnsCheck(timeStart, timeStop, host, loportZeroControl, netportZeroControl,
                cacheInZeroControl, cacheOutZeroControl, errorCode));
    }

    protected void switchRestartFailover(boolean enabled) throws Exception {
        if (!enabled) {
            try {
                PMSUtil.addPolicies(new Policy[]{
                        new Policy(paramName, paramValue, enginePath, true),
                        new Policy(paramName, paramValue, controllerPath, true),
                        new Policy(paramName, paramValue, dispatchersPath, true)});
            } catch (Throwable e) {
                logger.error("Exception while switching Reset Failover: " + ExceptionUtils.getStackTrace(e));
            }
            TestUtil.sleep(TFConstantsIF.WAIT_FOR_POLICY_UPDATE * 2);
        } else {
            PMSUtil.cleanPaths(new String[]{enginePath, controllerPath, dispatchersPath});
        }
    }
}