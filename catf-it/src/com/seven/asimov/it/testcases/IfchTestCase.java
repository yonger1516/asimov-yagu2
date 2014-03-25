package com.seven.asimov.it.testcases;

import android.content.Context;
import android.test.AssertionFailedError;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.*;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CsaTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CsdTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CsnTask;
import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CspTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.IfchTableTask;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.IfchTask;
import com.seven.asimov.it.utils.logcat.wrappers.*;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class IfchTestCase extends TcpDumpTestCase {

    private static final String TAG = IfchTestCase.class.getSimpleName();
    private static final Logger logger = LoggerFactory.getLogger(IfchTestCase.class.getSimpleName());
    protected static final int WAITING_SWITCHING_TIME = 15 * 1000;
    private static final int WAITING_TASKS_TIME = 3 * 1000;
    protected static String TEST_RESOURCE_OWNER = "asimov_it";
    //protected MobileNetworkUtil mobileNetworkUtil;

    protected LogcatUtil logcatUtil;

    protected final String STR_WIFI = "wifi";
    private List<Task> tasksList;

    protected String resource;
    protected String uri;
    protected HttpRequest request;
    protected int requestId;
    protected int sleepTime;

    protected IfchTask ifchTask;
    protected CsdTask csdTask;
    protected CsaTask csaTask;
    protected CspTask cspTask;
    protected CsnTask csnTask;
    protected IfchTableTask ifchTableTask;

    protected CSDWrapper csdWrapperTransaction1;
    protected CSNWrapper csnWrapperTransaction1;
    protected CSDWrapper csdWrapperTransaction2;
    protected CSAWrapper csaWrapperTransaction3;
    protected CSPWrapper cspWrapperTransaction3;

    protected long startTimeTransation1;
    protected long startTimeTransation2;
    protected long startTimeTransation3;
    protected long endTimeTransaction1;
    protected long endTimeTransaction2;
    protected long endTimeTransaction3;

    protected void clearTimeTransation() {
        startTimeTransation1 = 0;
        startTimeTransation2 = 0;
        startTimeTransation3 = 0;
        endTimeTransaction1 = 0;
        endTimeTransaction2 = 0;
        endTimeTransaction3 = 0;
    }

    protected void prepareTasks() {
        tasksList = new ArrayList<Task>();
        ifchTask = new IfchTask();
        csdTask = new CsdTask();
        csaTask = new CsaTask();
        cspTask = new CspTask();
        csnTask = new CsnTask();
        ifchTableTask = new IfchTableTask();

        tasksList.add(ifchTask);
        tasksList.add(csdTask);
        tasksList.add(csaTask);
        tasksList.add(cspTask);
        tasksList.add(csnTask);
        tasksList.add(ifchTableTask);

        for (Task task : tasksList) {
            task.setPrintEntries(true);
            //task.setChangeTimestampToGMT(false);
        }

        csdWrapperTransaction1 = null;
        csnWrapperTransaction1 = null;
        csdWrapperTransaction2 = null;
        csaWrapperTransaction3 = null;
        cspWrapperTransaction3 = null;
    }

    protected void collectTransationData() throws Exception {
        startTimeTransation1 = System.currentTimeMillis();
        checkMiss(request, requestId, sleepTime);
        endTimeTransaction1 = System.currentTimeMillis();

        startTimeTransation2 = System.currentTimeMillis();
        checkMiss(request, requestId, sleepTime);
        endTimeTransaction2 = System.currentTimeMillis();

        startTimeTransation3 = System.currentTimeMillis();
        checkMiss(request, requestId, sleepTime);
        endTimeTransaction3 = System.currentTimeMillis();
    }

    protected void checkWifiOn() {
        for (IfchWrapper ifchWrapper : ifchTask.getLogEntries()) {
            logger.trace(TAG, ifchWrapper.toString());
            if (!STR_WIFI.equalsIgnoreCase(ifchWrapper.getInterfaceType())) {
                throw new AssertionFailedError("Expected wifi interface but was : " + ifchWrapper.getInterfaceType());
            }
        }
    }

    protected void checkMobileOn() {
        for (IfchWrapper ifchWrapper : ifchTask.getLogEntries()) {
            logger.trace(TAG, ifchWrapper.toString());
            if (STR_WIFI.equalsIgnoreCase(ifchWrapper.getInterfaceType())) {
                throw new AssertionFailedError("Expected mobile interface but was : " + ifchWrapper.getInterfaceType());
            }
        }
    }

    protected void analyzeTransactions() {
        for (CSDWrapper csdWrapper : csdTask.getLogEntries()) {
            logger.trace(TAG, csdWrapper.toString());
            if (csdWrapper.getTimestamp() > startTimeTransation1 &&
                    csdWrapper.getTimestamp() < (endTimeTransaction1 + WAITING_TASKS_TIME)) {
                csdWrapperTransaction1 = csdWrapper;
            }
            if (csdWrapper.getTimestamp() > startTimeTransation2 &&
                    csdWrapper.getTimestamp() < (endTimeTransaction2 + WAITING_TASKS_TIME)) {
                csdWrapperTransaction2 = csdWrapper;
            }
        }
        for (CSAWrapper csaWrapper : csaTask.getLogEntries()) {
            logger.trace(TAG, csaWrapper.toString());
            if (csaWrapper.getTimestamp() > startTimeTransation3 &&
                    csaWrapper.getTimestamp() < (endTimeTransaction3 + WAITING_TASKS_TIME)) {
                csaWrapperTransaction3 = csaWrapper;
            }
        }
        for (CSPWrapper cspWrapper : cspTask.getLogEntries()) {
            logger.trace(TAG, cspWrapper.toString());
            if (cspWrapper.getTimestamp() > startTimeTransation3 &&
                    cspWrapper.getTimestamp() < (endTimeTransaction3 + WAITING_TASKS_TIME)) {
                cspWrapperTransaction3 = cspWrapper;
            }
        }
        for (CSNWrapper csnWrapper : csnTask.getLogEntries()) {
            logger.trace(TAG, csnWrapper.toString());
            if (csnWrapper.getTimestamp() > startTimeTransation1 &&
                    csnWrapper.getTimestamp() < (endTimeTransaction1 + WAITING_TASKS_TIME)) {
                csnWrapperTransaction1 = csnWrapper;
            }
        }

        if (csdWrapperTransaction1 == null) {
            throw new AssertionFailedError("Can't find csd verdict for transaction 1.");
        }
        if (csnWrapperTransaction1 == null) {
            throw new AssertionFailedError("Can't find executed csn task for transaction 1.");
        }
        if (csdWrapperTransaction2 == null) {
            throw new AssertionFailedError("Can't find csd verdict for transaction 2.");
        }
        if (csaWrapperTransaction3 == null) {
            throw new AssertionFailedError("Can't find csa verdict for transaction 3.");
        }
        if (cspWrapperTransaction3 == null) {
            throw new AssertionFailedError("Can't find executed csp task for transaction 3.");
        }
    }

    protected void prepareLogcatUtil(Context context, Task... tasks) {
        logcatUtil = new LogcatUtil(context, Arrays.asList(tasks));
    }

    protected void checkWifiInTableTask() {
        boolean isWifiDetected = false;
        boolean isNoneDetected = false;
        for (IfchTableWrapper ifchTableWrapper : ifchTableTask.getLogEntries()) {
            if (STR_WIFI.equalsIgnoreCase(ifchTableWrapper.getInterfaceType()))
                isWifiDetected = true;
            if ("none".equalsIgnoreCase(ifchTableWrapper.getInterfaceType())) {
                isNoneDetected = true;
            }
        }
        if (!isWifiDetected) {
            throw new AssertionFailedError("Can't find wifi interface in IFCH table.");
        }
        if (!isNoneDetected) {
            throw new AssertionFailedError("Can't find noneind wifi interface in IFCH table.");
        }
    }

    protected void checkMobileInTableTask() {
        boolean isMobileDetected = false;
        boolean isNoneDetected = false;
        for (IfchTableWrapper ifchTableWrapper : ifchTableTask.getLogEntries()) {
            if (ifchTableWrapper.getInterfaceType().contains("mobile"))
                isMobileDetected = true;
            if ("none".equalsIgnoreCase(ifchTableWrapper.getInterfaceType())) {
                isNoneDetected = true;
            }
        }
        if (!isMobileDetected) {
            throw new AssertionFailedError("Can't find mobile interface in IFCH table.");
        }
        if (!isNoneDetected) {
            throw new AssertionFailedError("Can't find none interface in IFCH table.");
        }
    }

    protected void sortAndCheckIfchTableTask() {
        List<IfchTableWrapper> sorted = new ArrayList<IfchTableWrapper>();
        ListIterator<IfchTableWrapper> listIterator = ifchTableTask.getLogEntries().listIterator();

        // adding last element from input list
        if (listIterator.hasNext()) {
            sorted.add(listIterator.next());
        }
        IfchTableWrapper previousElement = sorted.get(0);
        while (listIterator.hasNext()) {
            IfchTableWrapper currentElement = listIterator.next();
            if (currentElement.getTimestamp() - previousElement.getTimestamp() < 10) {
                sorted.add(currentElement);
            } else {
                break;
            }
            previousElement = currentElement;
        }

        if (sorted.size() != 20) {
            throw new AssertionFailedError("Count of elements in IFCH table not equals 20.");
        }
    }

    protected void killHttpDispatcher() throws Exception {
        ShellUtil.execSimple("su -c kill -9 $(cat /data/misc/openchannel/pids/ochttpd)");
    }

    protected void checkIfchTableTaskIsEmpty() {
        for (IfchTableWrapper ifchTableWrapper : ifchTableTask.getLogEntries()) {
            logger.debug(TAG, ifchTableWrapper.toString());
        }
        if (ifchTableTask.getLogEntries().size() > 0) {
            throw new AssertionFailedError("IFCH table should not appear but was.");
        }
    }

    protected void executeIFCHTest_CSN_CSP(String resource, boolean isMobileNetworkEnabled) throws Exception {
        MobileNetworkUtil mnh = MobileNetworkUtil.init(getContext());
        if (isMobileNetworkEnabled) {
            mnh.on3gOnly();
        } else {
            mnh.onWifiOnly();
        }
        TestUtil.sleep(40 * 1000);

        uri = createTestResourceUri(resource);
        request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();
        requestId = 0;
        sleepTime = 65 * 1000;

        prepareTasks();

        prepareLogcatUtil(getContext(), ifchTask, csdTask, csaTask, cspTask, csnTask);
        logcatUtil.start();

        clearTimeTransation();

        try {
            collectTransationData();
        } finally {
            //invalidateLongPoll(uri);
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logcatUtil.stop();
            logger.trace(TAG, "LastEntry parsed by logcat:" + logcatUtil.getLastEntry());
        }

        if (isMobileNetworkEnabled) {
            checkMobileOn();
        } else {
            checkWifiOn();
        }

        logger.trace(TAG, "StartTimeTransaction1 = " + startTimeTransation1);
        logger.trace(TAG, "EndTimeTransaction1 = " + endTimeTransaction1);
        logger.trace(TAG, "StartTimeTransaction2 = " + startTimeTransation2);
        logger.trace(TAG, "EndTimeTransaction2 = " + endTimeTransaction2);
        logger.trace(TAG, "StartTimeTransaction3 = " + startTimeTransation3);
        logger.trace(TAG, "EndTimeTransaction3 = " + endTimeTransaction3);

        analyzeTransactions();
    }

    protected void executeIFCHTest_ERR(String resource, boolean isMobileNetworkEnabled) throws Exception {
        MobileNetworkUtil mnh = MobileNetworkUtil.init(getContext());
        if (isMobileNetworkEnabled) {
            mnh.switchWifiOnOff(false);
        } else {
            mnh.switchWifiOnOff(true);
        }

        TestUtil.sleep(WAITING_SWITCHING_TIME);

        uri = createTestResourceUri(resource);
        request = createRequest().
                setMethod(HttpGet.METHOD_NAME).
                addHeaderField("X-OC-Sleep", "30").
                setUri(uri).
                getRequest();

        prepareTasks();
        prepareLogcatUtil(getContext(), ifchTask);
        logcatUtil.start();
        try {
            sendRequest2(request, false, false, 15 * 1000);
            TestUtil.sleep(2 * WAITING_SWITCHING_TIME);

        } finally {
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
        if (ifchTask.getLogEntries().size() <= 0) {
            throw new AssertionFailedError("Can't find ifch in logs for http error transaction.");
        }
    }

    protected void executeIFCHTest_airplane(String resource, boolean isMobileNetworkEnabled) throws Exception {
        AirplaneModeUtil airplaneModeUtil = new AirplaneModeUtil(getContext());
        MobileNetworkUtil mnh = MobileNetworkUtil.init(getContext());
        if (isMobileNetworkEnabled) {
            mnh.switchWifiOnOff(false);
        } else {
            mnh.switchWifiOnOff(true);
        }

        uri = createTestResourceUri(resource);
        request = createRequest().setMethod(HttpGet.METHOD_NAME).setUri(uri).getRequest();

        prepareTasks();
        prepareLogcatUtil(getContext(), ifchTableTask);
        logcatUtil.start();

        try {
            sendRequest2(request);

            airplaneModeUtil.setEnabled(true);
            TestUtil.sleep(60 * 1000);
            airplaneModeUtil.setEnabled(false);
            TestUtil.sleep(WAITING_SWITCHING_TIME);

            sendRequest2(request);

        } finally {
            airplaneModeUtil.setEnabled(false);
            logcatUtil.stop();
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }

        checkMobileInTableTask();
    }
}
