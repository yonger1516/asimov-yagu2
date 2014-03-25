package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.ThreadPoolTask;
import com.seven.asimov.it.utils.logcat.wrappers.InterfaceType;
import com.seven.asimov.it.utils.logcat.wrappers.ThreadPoolWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GWListTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(GWListTestCase.class.getSimpleName());

    private String prepareUri(String resource, boolean realResource, boolean https) throws Exception {
        String uri;
        if (realResource) {
            uri = resource;
        } else {
            uri = createTestResourceUri(resource, https);
            PrepareResourceUtil.prepareResource(uri, false);
        }
        return uri;
    }


    protected void checkPolling(String resource, boolean realResource, InterfaceType interfaceType,
                                boolean https, int timeBetweenRequest, int timeToWait, boolean polling)
            throws Exception {

        if (interfaceType == InterfaceType.WIFI)
            MobileNetworkUtil.init(getContext()).onWifiOnly();
        else
            MobileNetworkUtil.init(getContext()).on3gOnly();
        logSleeping(timeToWait);
        String uri = prepareUri(resource, realResource, https);

        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").getRequest();
        try {
            int requestId = 1;
            checkMiss(request, requestId++, null, timeBetweenRequest);
            checkMiss(request, requestId++, null, timeBetweenRequest);
            checkMiss(request, requestId++, null, timeBetweenRequest);
            if (polling) {
                checkHit(request, requestId);
            } else {
                checkMiss(request, requestId);
            }
        } finally {
            if (!realResource) PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }

    private int maxPendingTasks(List<ThreadPoolWrapper> threadPoolEntries) {
        int max = -1;
        for (ThreadPoolWrapper wrapper : threadPoolEntries) {
            logger.debug(wrapper.toString());
            if (wrapper.getPendingTasks() > max) max = wrapper.getPendingTasks();
        }
        return max;
    }

    protected void checkThreadPoolDeadlock(String uri, int amountOfThreads, int sleepToWait, int responseDuration) throws Throwable {

        final ThreadPoolTask threadPoolTask = new ThreadPoolTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), threadPoolTask);
        final HttpRequest request = createRequest().setUri(uri).setMethod("GET").getRequest();
        TestCaseThread[] threads = new TestCaseThread[amountOfThreads];
        logcatUtil.start();
        for (int i = 0; i < amountOfThreads; i++) {
            TestCaseThread thread = new TestCaseThread(5 * 1000) {
                @Override
                public void run() throws Throwable {
                    checkMiss(request, 1);
                }
            };
            threads[i] = thread;
        }
        ;
        executeThreads(threads);
        logSleeping(sleepToWait);
        logcatUtil.stop();
        logger.debug("First entry of the logcat", logcatUtil.getFirstEntry());
        logger.debug("Last entry of the logcat", logcatUtil.getLastEntry());
        List<ThreadPoolWrapper> firstThreadPoolEntries = threadPoolTask.getLogEntries();
        final int firstAmountOfPendingTasks = maxPendingTasks(firstThreadPoolEntries);
        logger.debug("Detected first max amount of pending task: " + firstAmountOfPendingTasks);
        assertTrue("Test should provide appropriate conditions for OC. There are no pending tasks!", firstAmountOfPendingTasks != -1);
        threadPoolTask.reset();
        logcatUtil = new LogcatUtil(getContext(), threadPoolTask);
        logcatUtil.start();
        logSleeping(5 * TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
        logcatUtil.stop();
        List<ThreadPoolWrapper> secondThreadPoolEntries = threadPoolTask.getLogEntries();
        if (secondThreadPoolEntries.size() != 0) {
            final int secondAmountOfPendingTasks = secondThreadPoolEntries.get(secondThreadPoolEntries.size() - 1).getPendingTasks();
            logger.debug("Detected second max amount of pending task: " + secondAmountOfPendingTasks);
            assertTrue("Amount of pending task should decrease with time!", firstAmountOfPendingTasks > secondAmountOfPendingTasks);
        }
        HttpResponse response = sendRequest2(request);
        assertTrue("Duration of the last response should be less than 10 seconds!", response.getDuration() < responseDuration);

    }

    protected String getHostForExcept() {
        String host = null;
        if (TEST_RESOURCE_HOST.equals("tln-dev-testrunner1.7sys.eu")) {
            host = "hki-dev-testrunner2.7sys.eu";
        } else {
            host = "tln-dev-testrunner1.7sys.eu";
        }
        return host;
    }
}
