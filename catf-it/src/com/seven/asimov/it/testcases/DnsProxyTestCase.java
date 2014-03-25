package com.seven.asimov.it.testcases;


import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.TimeInfoTransaction;
import com.seven.asimov.it.utils.DnsUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.tasks.sessionBasicTasks.DnsVerdictTask;
import com.seven.asimov.it.utils.logcat.tasks.sessionBasicTasks.ProcessingCLQTask;
import com.seven.asimov.it.utils.logcat.wrappers.DnsVerdictWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.NetlogEntry;
import com.seven.asimov.it.utils.logcat.wrappers.ProcessingCLQWrapper;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class DnsProxyTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(DnsProxyTestCase.class.getSimpleName());

    //DnsProxyTests
    protected void testDnsVerdicts(final String uri, final long[] intervals, final long[] shiftsBefore, final String[] expectedResults, final String host) throws InterruptedException, IOException, IllegalAccessException, InstantiationException {
        ProcessingCLQTask processingCLQTask = new ProcessingCLQTask(null, host, null, null);

        LogcatUtil logcatUtil = null;
        try {
            //move time to expire all dns cache entries
            DateUtil.moveTime(24 * DateUtil.HOURS);
            logSleeping(15 * DateUtil.SECONDS);
            for (int i = 0; i < intervals.length; i++) {
                if (shiftsBefore != null && shiftsBefore[i] != 0)
                    DateUtil.moveTime(shiftsBefore[i]);
                long start = System.currentTimeMillis();
                processingCLQTask.reset();
//                dnsVerdictTask.reset();
                DnsVerdictTask dnsVerdictTask = new DnsVerdictTask(null, expectedResults[i]);
                logcatUtil = new LogcatUtil(getContext(), processingCLQTask, dnsVerdictTask);
                logcatUtil.start();
                HttpRequest request = createRequest().setUri(uri).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
                logger.info(String.format("Expecting verdict is %s", expectedResults[i]));
                sendRequest2(request);
                long end = System.currentTimeMillis();
                logSleeping(intervals[i] - (end - start));
                logcatUtil.stop();
                List<ProcessingCLQWrapper> dnsCLQ = processingCLQTask.getLogEntries();
                for (ProcessingCLQWrapper clqWrapper : dnsCLQ) {
                    logger.info(String.format("Clq wrapper: %s", clqWrapper));
                    logger.info(String.format("Clg wrapper dtrx: %s", clqWrapper.getDTRX()));
                }
                logger.info("Total amount of dns sessions with corresponding host " + dnsCLQ.size());
                assertTrue("Should be one dns session for this period of time", dnsCLQ.size() == 1);
                List<DnsVerdictWrapper> dnsVerdictWrappers = dnsVerdictTask.getLogEntries();
                for (DnsVerdictWrapper wrapper : dnsVerdictWrappers) {
                    logger.info(String.format("DNS verdict wrapper: %s", wrapper));
                }
                DnsVerdictWrapper verdictWrapper = new DnsVerdictWrapper(dnsCLQ.get(0).getDTRX(), expectedResults[i]);
                logger.info(String.format("Expecting verdict: %s", verdictWrapper));
                assertTrue("Dns verdict is not correct", dnsVerdictWrappers.contains(verdictWrapper));
            }
        } finally {
            if (logcatUtil != null) logcatUtil.stop();
            DateUtil.syncTimeWithTestRunner();
        }
    }

    protected void checkDnsTimeOut(String host, String errorCode) throws Exception {
        tcpDump = TcpDumpUtil.getInstance(getContext());
        tcpDump.start();

        final NetlogTask netlogTask = new NetlogTask();
        final LogcatUtil logcatUtil = new LogcatUtil(getContext(), netlogTask);
        logcatUtil.start();
        TimeInfoTransaction timeInfoTransaction = null;
        try {
            timeInfoTransaction = new TimeInfoTransaction();
            timeInfoTransaction.setTimeStart(System.currentTimeMillis());
            DnsUtil.resolveHost("ya.ru", "1.2.3.4");
            logcatUtil.stop();
            List<NetlogEntry> entries = netlogTask.getLogEntries();
            for (NetlogEntry netlogEntry : entries) {
                if (netlogEntry.getHost() == host.substring(0, host.length() - 1))
                    assertTrue("We should have error code " + errorCode + " in the corresponding netlog", netlogEntry.getErrorCode().equals(errorCode));
            }
        } finally {
            timeInfoTransaction.setTimeEnd(System.currentTimeMillis());
            logcatUtil.stop();
            tcpDump.stop();
        }
    }


}
