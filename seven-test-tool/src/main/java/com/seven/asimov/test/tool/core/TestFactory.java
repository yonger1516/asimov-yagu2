package com.seven.asimov.test.tool.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.seven.asimov.test.tool.activity.MainTab;
import com.seven.asimov.test.tool.activity.RootTab;
import com.seven.asimov.test.tool.activity.Tabs;
import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.core.testjobs.*;
import com.seven.asimov.test.tool.preferences.SharedPrefs;
import com.seven.asimov.test.tool.receivers.AlarmReceiver;
import com.seven.asimov.test.tool.serialization.TestItem;
import com.seven.asimov.test.tool.serialization.TestSuite;
import com.seven.asimov.test.tool.utils.Util;
import com.seven.asimov.test.tool.utils.Z7FileUtils;
import com.seven.asimov.test.tool.utils.Z7HttpUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * TestFactory.
 */

public class TestFactory extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(TestFactory.class.getSimpleName());

    public TestFactory(Context context) {
        this.mContext = context;
    }

    private static Long sCurrentTime;
    private static Long sUpdatedTime;
    private static int sCurrentMinutes;
    private static int sCurrentSeconds;
    private static String sTimerPreviousInterval;

    private static int sConnectionCounter = 0;

    public static void setConnectionCounter(int connectionCounter) {
        sConnectionCounter = connectionCounter;
    }

    public static int getConnectionCounter() {
        return sConnectionCounter;
    }

    public static void resetCounters() {
        sConnectionCounter = 0;
    }

    public static String getTimer() {
        String result;

        if (sCurrentTime == null) {
            sCurrentTime = System.currentTimeMillis();
        }
        sUpdatedTime = System.currentTimeMillis();
        double test = ((sUpdatedTime - sCurrentTime) / Constants.MILL_IN_SEC);
        DecimalFormat twoDForm = new DecimalFormat("#");
        sCurrentSeconds = Integer.valueOf(twoDForm.format(test));
        sCurrentMinutes = sCurrentSeconds / Constants.SEC_IN_MIN;
        if (sCurrentMinutes >= 1) {
            result = String.valueOf(sCurrentMinutes) + " min "
                    + String.valueOf(sCurrentSeconds - sCurrentMinutes * Constants.SEC_IN_MIN) + " sec";
        } else {
            result = String.valueOf(sCurrentSeconds) + " sec";
        }
        return result;
    }

    private static void refreshTimer() {
        String result;

        sCurrentTime = System.currentTimeMillis();
        if (sCurrentMinutes >= 1) {
            result = String.valueOf(sCurrentMinutes) + " min "
                    + String.valueOf(sCurrentSeconds - sCurrentMinutes * Constants.SEC_IN_MIN) + " sec";
        } else {
            result = String.valueOf(sCurrentSeconds) + " sec";
        }
        sTimerPreviousInterval = result;
    }

    public static String getPreviousTimer() {
        return sTimerPreviousInterval;
    }

    private static TestSuite sTestSuite;

    public static void setTestSuite(TestSuite testSuite) {
        // reset TestSuite runtime
        resetTests();
        resetIntervalPattern();
        // resetCounters();

        testSuite.parseVerificationPattern();

        TestFactory.sTestSuite = testSuite;
    }

    public static TestSuite getTestSuite() {
        return sTestSuite;
    }

    private static TestFactory sTestFactory;

    public static TestFactory getTestFactory() {
        return sTestFactory;
    }

    private static boolean sInitiated;

    public static boolean isInitiated() {
        return sInitiated;
    }

    public static void setInitiated(boolean initiated) {
        sInitiated = initiated;
    }

    public static void init(Context context) {
        while (true) {
            if (SharedPrefs.getLastTestSuiteFromSd() != null) {
                if (Z7FileUtils.loadTestSuiteFromSd(SharedPrefs.getLastTestSuiteFromSd())) {
                    break;
                }
            }
            sTestSuite = new TestSuite();
            sTestSuite.setName(sTestSuiteName);
            sTestSuite.setPattern(DEFAULT_PATTERN);
            TestItem test = Z7HttpUtil.buildDefaultTest();
            sTestSuite.getTestItems().add(test);
            break;
        }

        sTestFactory = new TestFactory(context);
        sTestFactory.start();
    }

    private static String sTestSuiteName = "YourTest";
    private static final String DEFAULT_PATTERN = "45";

    public static void setRequestHeaders(String requestHeaders) {
        sTestSuite.getTestItems().get(sCurrentTestId).setRequestHeaders(requestHeaders);
    }

    public static void setRequestContent(String requestContent) {
        sTestSuite.getTestItems().get(sCurrentTestId).setRequestContent(requestContent);
    }

    public static void setTestSuitePassedOk(boolean passedOk) {
        sTestSuite.setValidatedOk(passedOk);
    }

    public static boolean isTestSuitePassedOk() {
        return sTestSuite.isValidatedOk();
    }

    public static void setProxy(String proxy) {
        sTestSuite.getTestItems().get(sCurrentTestId).setProxy(proxy);
    }

    public static void setProxyPort(Integer proxyPort) {
        sTestSuite.getTestItems().get(sCurrentTestId).setProxyPort(proxyPort);
    }

    public static void setHttpMethod(String httpMethod) {
        sTestSuite
                .getTestItems()
                .get(sCurrentTestId)
                .setTestItem(
                        Z7HttpUtil.replaceHttpMethodFromHeaders(httpMethod.trim(),
                                sTestSuite.getTestItems().get(sCurrentTestId)));
    }

    public static String getHttpMethod() {
        return Z7HttpUtil.getHttpMethodFromHeaders(sTestSuite.getTestItems().get(sCurrentTestId).getRequestHeaders());
    }

    public static void setUri(String uri) {

        // http://en.wikipedia.org/wiki/URI_scheme#Generic_syntax
        // http://tools.ietf.org/html/rfc3986

        try {
            URI resultUri = new URI(uri);

            sTestSuite
                    .getTestItems()
                    .get(sCurrentTestId)
                    .setTestItem(
                            Z7HttpUtil.replaceUriFromHeaders(resultUri, sTestSuite.getTestItems().get(sCurrentTestId)));

        } catch (Exception e) {
            LOG.error("setUri: " + e.getMessage());
        }
    }

    public static String getUri() {
        // Remove port from uri string
        URI uri = Z7HttpUtil.getUriFromHeaders(sTestSuite.getTestItems().get(sCurrentTestId).getRequestHeaders());
        StringBuilder sb = new StringBuilder();
        sb.append(uri.getScheme()).append("://").append(uri.getAuthority()).append(uri.getRawPath())
                .append(((uri.getQuery() != null) ? "?" + uri.getRawQuery() : StringUtils.EMPTY))
                .append(((uri.getFragment() != null) ? "#" + uri.getRawFragment() : StringUtils.EMPTY));
        return sb.toString();
    }

    private static int sCurrentTestId;
    private static int sCurrentTestReiterationId;
    private static int sCurrentIntervalId;

    public static void setIntervalPattern(String pattern) {
        pattern = pattern.replace("[", StringUtils.EMPTY).replace("]", StringUtils.EMPTY);
        if (!pattern.equals(sTestSuite.getPattern())) {
            resetIntervalPattern();
            sTestSuite.setPattern(pattern);
        }
    }

    public static ArrayList<String> getIntervalPattern() {

        ArrayList<String> result = new ArrayList<String>();

        if (!StringUtils.isEmpty(sTestSuite.getPattern())) {
            result.addAll(Arrays.asList(sTestSuite.getPattern().replace(".", ",").split(",")));
        } else {
            result.add(DEFAULT_PATTERN);
        }
        return result;
    }

    public static int getIntervalPatternSize() {
        return getIntervalPattern().size();
    }

    public static void resetIntervalPattern() {
        sCurrentIntervalId = 0;
    }

    public static int getCurrentIntervalId() {
        return sCurrentIntervalId;
    }

    public static void moveToPreviousInterval() {
        if (sCurrentIntervalId - 1 > 0) {
            sCurrentIntervalId--;
        } else {
            sCurrentIntervalId = getIntervalPatternSize() - 1;
        }
    }

    public static void moveToNextInterval() {
        if (getIntervalPatternSize() == (sCurrentIntervalId + 1)) {
            if (getIntervalPatternSize() > 1) {
                sCurrentIntervalId = 1;
            } else {
                sCurrentIntervalId = 0;
            }
        } else {
            sCurrentIntervalId++;
        }
    }

    public static String getInitialDelayAndMoveToNext() {
        String result = null;
        if (getIntervalPatternSize() > 1 && sCurrentIntervalId == 0) {
            result = getIntervalPattern().get(sCurrentIntervalId);
            moveToNextInterval();
            return result;
        }
        return result;
    }

    public static String getCurrentPatternAndMoveToNext() {

        LOG.debug("Interval pattern: " + Arrays.toString(getIntervalPattern().toArray()));
        LOG.debug("Interval pattern size: " + getIntervalPatternSize());
        LOG.debug("Current interval id: " + sCurrentIntervalId);

        String currPattern = getIntervalPattern().get(sCurrentIntervalId);
        LOG.debug("Current interval: " + currPattern);

        if (currPattern != null) {

            moveToNextInterval();

            LOG.debug("Next interval id: " + (sCurrentIntervalId));
            LOG.debug("Next interval: " + getIntervalPattern().get(sCurrentIntervalId));
        }

        return currPattern;
    }

    public static int getCurrentTestId() {
        return sCurrentTestId;
    }

    public static Integer getCurrentTestReiterations() {
        TestItem test = sTestSuite.getTestItems().get(sCurrentTestId);
        if (test != null) {
            return test.getReiterations();
        }
        return null;
    }

    public static int getCurrentTestReiterationId() {
        return sCurrentTestReiterationId;
    }

    public static int getTestsCount() {
        return sTestSuite.getTestItems().size();
    }

    public static TestItem getCurrentTest() {
        return sTestSuite.getTestItems().get(sCurrentTestId);
    }

    public static void resetTests() {
        sCurrentTestId = 0;
        sCurrentTestReiterationId = 0;
    }

    public static TestItem getCurrentTestAndMoveToNext() {

        TestItem currTest = sTestSuite.getTestItems().get(sCurrentTestId);

        LOG.debug("Current test id: " + sCurrentTestId);
        LOG.debug("Current test reiterations: " + currTest.getReiterations());
        LOG.debug("Current test reiteration id: " + sCurrentTestReiterationId);

        moveToNextTest();

        TestItem nextTest = sTestSuite.getTestItems().get(sCurrentTestId);

        LOG.debug("Next test id: " + sCurrentTestId);
        LOG.debug("Next test reiterations: " + nextTest.getReiterations());
        LOG.debug("Next test reiteration id: " + sCurrentTestReiterationId);

        return currTest;
    }

    public static void moveToPreviousTest() {
        if ((sCurrentTestReiterationId - 1) >= 0) {
            sCurrentTestReiterationId--;
        } else {
            if (sCurrentTestId - 1 >= 0) {
                sCurrentTestId--;
                TestItem test = sTestSuite.getTestItems().get(sCurrentTestId);
                sCurrentTestReiterationId = test.getReiterations() - 1;
            } else {
                sCurrentTestId = getTestsCount() - 1;
                TestItem test = sTestSuite.getTestItems().get(sCurrentTestId);
                sCurrentTestReiterationId = test.getReiterations() - 1;
            }
        }
    }

    public static void moveToNextTest() {
        TestItem test = sTestSuite.getTestItems().get(sCurrentTestId);
        if (test.getReiterations() == (sCurrentTestReiterationId + 1) || (test.getReiterations() == 0)) {
            if (getTestsCount() == (sCurrentTestId + 1)) {
                sCurrentTestId = 0;
            } else {
                sCurrentTestId++;
            }
            sCurrentTestReiterationId = 0;
        } else {
            sCurrentTestReiterationId++;
        }
    }

    // //

    private static boolean sFreshTestSuiteLoadedFromServer;

    public static void setFreshTestSuiteLoadedFromServer(boolean freshTestSuiteLoadedFromServer) {
        if (freshTestSuiteLoadedFromServer) {
            sTestSuiteLoadedFromServer = true;
        }
        TestFactory.sFreshTestSuiteLoadedFromServer = freshTestSuiteLoadedFromServer;
    }

    public static boolean isFreshTestSuiteLoadedFromServer() {
        return sFreshTestSuiteLoadedFromServer;
    }

    private static boolean sTestSuiteLoadedFromServer;

    public static void setTestSuiteLoadedFromServer(boolean testSuiteLoadedFromServer) {
        TestFactory.sTestSuiteLoadedFromServer = testSuiteLoadedFromServer;
    }

    public static boolean isTestSuiteLoadedFromServer() {
        return sTestSuiteLoadedFromServer;
    }

    private static boolean sActive;

    public static void setActive(boolean active) {
        sActive = active;
    }

    public static boolean isActive() {
        return sActive;
    }

    private final Object mLock = new Object();

    public Object getLock() {
        return mLock;
    }

    private Context mContext;

    private static ArrayList<TestJob> sTestJobQueue = new ArrayList<TestJob>();

    public static void addTestJob(TestJob newTestJob) {

        sTestJobQueue.add(newTestJob);

        if (sTestFactory.getState() == State.NEW) {
            sTestFactory.start();
        } else if (sTestFactory.getState() == State.WAITING) {
            synchronized (TestFactory.getTestFactory().getLock()) {
                sTestFactory.getLock().notify();
            }
        }
    }

    public static void removeTestJobs(TestJobType testJobType) {

        try {

            TestJob tjToRemove = null;
            switch (testJobType) {
                default:
                    break;
                case BUTTON_PERIODIC_REQUESTS:
                    tjToRemove = new TestJob(TestJobType.UNSCHEDULE_BUTTON_PERIODIC_REQUESTS);
                    break;
                case BUTTON_LOOPED_REQUESTS:
                    tjToRemove = new TestJob(TestJobType.UNSCHEDULE_BUTTON_LOOPED_REQUESTS);
                    break;
            }

            TestFactory.addTestJob(tjToRemove);

        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    public static void removeTestJobs(String testSuiteName) {

        try {
            TestJob tjToRemove = new TestJob(TestJobType.UNSCHEDULE_AUTOMATED_TEST);
            tjToRemove.setTestSuiteName(testSuiteName);
            TestFactory.addTestJob(tjToRemove);

        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    @Override
    public void run() {
        try {
            LOG.debug(Util.getLogPrefix(this.getId()) + "TestFactory " + this.getName() + " started...");
            while (true) {
                synchronized (mLock) {
                    if (sTestJobQueue.size() == 0) {
                        LOG.debug(Util.getLogPrefix(this.getId()) + "Waiting new test jobs...");
                        mLock.wait();
                    }
                }
                if (sTestJobQueue.size() > 0) {
                    TestJob testJob = sTestJobQueue.get(0);
                    sTestJobQueue.remove(0);

                    String initialDelay;

                    TestJobType tyType = testJob.getType();

                    switch (tyType) {
                        case UNSCHEDULE_BUTTON_SINGLE_REQUEST:

                            unSchedulePendingTestJobs(TestJobType.BUTTON_SINGLE_REQUEST);

                            break;
                        case BUTTON_SINGLE_REQUEST:

                            // Handle redirect
                            if (testJob.getRequest() != null) {
                                sendTestJobToPipelineFactory(testJob);
                                break;
                            }

                            initialDelay = getInitialDelayAndMoveToNext();

                            if (initialDelay != null) {
                                scheduleTestJob(testJob, Integer.valueOf(initialDelay.trim()));
                                break;
                            }

                            sendTestJobToPipelineFactory(testJob);

                            break;
                        case UNSCHEDULE_BUTTON_PERIODIC_REQUESTS:

                            unSchedulePendingTestJobs(TestJobType.BUTTON_PERIODIC_REQUESTS);

                            break;
                        case BUTTON_PERIODIC_REQUESTS:
                            if (TestJobStates.getJobState(tyType) == TestJobState.NOT_RUNNING) {
                                break;
                            }

                            // Handle redirect
                            if (testJob.getRequest() != null) {
                                sendTestJobToPipelineFactory(testJob);
                                break;
                            }

                            initialDelay = getInitialDelayAndMoveToNext();

                            if (initialDelay != null) {
                                scheduleTestJob(testJob, Integer.valueOf(initialDelay.trim()));
                                break;
                            } else {
                                String scheduledDelay = getCurrentPatternAndMoveToNext();
                                if (scheduledDelay.equals("0")) {
                                    TestJob newTestJob = new TestJob(testJob.getType());
                                    sTestJobQueue.add(newTestJob);
                                } else {
                                    scheduleTestJob(testJob, Integer.valueOf(scheduledDelay));
                                }
                            }

                            sendTestJobToPipelineFactory(testJob);

                            break;
                        case UNSCHEDULE_BUTTON_LOOPED_REQUESTS:

                            unSchedulePendingTestJobs(TestJobType.BUTTON_LOOPED_REQUESTS);

                            break;
                        case BUTTON_LOOPED_REQUESTS:
                            if (TestJobStates.getJobState(tyType) == TestJobState.NOT_RUNNING) {
                                break;
                            }

                            // Handle redirect
                            if (testJob.getRequest() != null) {
                                sendTestJobToPipelineFactory(testJob);
                                break;
                            }

                            initialDelay = getInitialDelayAndMoveToNext();

                            if (initialDelay != null) {
                                scheduleTestJob(testJob, Integer.valueOf(initialDelay.trim()));
                                break;
                            }

                            sendTestJobToPipelineFactory(testJob);

                            break;
                        case UNSCHEDULE_AUTOMATED_TEST:

                            unSchedulePendingTestJobs(TestJobType.AUTOMATED_TEST);

                            break;
                        case AUTOMATED_TEST:
                            if (TestJobStates.getJobState(testJob.getTestSuiteName()) == TestJobState.NOT_RUNNING) {
                                break;
                            }

                            // Handle redirect
                            if (testJob.getRequest() != null) {
                                sendTestJobToPipelineFactory(testJob);
                                break;
                            }

                            initialDelay = getInitialDelayAndMoveToNext();

                            if (initialDelay != null) {
                                scheduleTestJob(testJob, Integer.valueOf(initialDelay.trim()));
                                break;
                            }

                            sendTestJobToPipelineFactory(testJob);

                            break;
                        default:
                            break;
                    }
                    displayTestFactoryInfo();
                }
            }
        } catch (Exception e) {
            LOG.error(Util.getLogPrefix(this.getId()), e);
        }
    }

    private void sendTestJobToPipelineFactory(TestJob testJob) throws Exception {

        if (testJob.getRequest() == null) {
            Request request = new Request();
            request.init(getCurrentTestAndMoveToNext());
            testJob.setRequest(request);
        }

        refreshTimer();

        PipelineFactory.sendRequestToPipeline(testJob, mContext);
    }

    private static ArrayList<PendingTestJob> sPendingTestJobs = new ArrayList<PendingTestJob>();

    public static ArrayList<PendingTestJob> getPendingTestJobs() {
        return sPendingTestJobs;
    }

    private static int sRequestCount;

    private void scheduleTestJob(TestJob testJob, int delay) {

        if (testJob.getType() == TestJobType.AUTOMATED_TEST) {
            LOG.debug("Scheduling automation testJob: " + testJob.getTestSuiteName() + ", delay: " + delay);
        } else {
            LOG.debug("Scheduling testJob: " + testJob.getType() + ", delay: " + delay);
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, delay);

        Intent intent = new Intent(AlarmReceiver.ACTION_ALARM);
        intent.putExtra("TestJobType", testJob.getType().getValue());
        intent.putExtra("TestSuiteName", testJob.getTestSuiteName());
        PendingIntent pI = PendingIntent.getBroadcast(mContext, sRequestCount, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pI);

        PendingTestJob newPendingTestJob;

        if (testJob.getType() == TestJobType.AUTOMATED_TEST) {
            newPendingTestJob = new PendingTestJob(pI, testJob.getTestSuiteName());
        } else {
            newPendingTestJob = new PendingTestJob(pI, testJob.getType());
        }
        sPendingTestJobs.add(newPendingTestJob);

        sRequestCount++;
    }

    private void unSchedulePendingTestJobs(TestJobType testJobType) {
        int n = 0;
        while (sPendingTestJobs.size() > 0) {
            PendingTestJob pendingTestJob = sPendingTestJobs.get(n);
            if (pendingTestJob.getTestJobType() == testJobType) {
                unSchedulePendingIntent(pendingTestJob.getPendingIntent());
                sPendingTestJobs.remove(n);
                n = 0;
            } else {
                n++;
            }
            if (n == sPendingTestJobs.size()) {
                break;
            }
        }
    }

    private void unSchedulePendingTestJobs(String testSuiteName) {
        int n = 0;
        while (sPendingTestJobs.size() > 0) {
            PendingTestJob pendingTestJob = sPendingTestJobs.get(n);
            if (pendingTestJob.getTestSuiteName().equals(testSuiteName)) {
                unSchedulePendingIntent(pendingTestJob.getPendingIntent());
                sPendingTestJobs.remove(n);
                n = 0;
            } else {
                n++;
            }
            if (n == sPendingTestJobs.size()) {
                break;
            }
        }
    }

    public static void removeLastPendingIntent(TestJobType testJobType) {
        int n = 0;
        while (sPendingTestJobs.size() > 0) {
            PendingTestJob pendingTestJob = sPendingTestJobs.get(n);
            if (pendingTestJob.getTestJobType() == testJobType) {
                sPendingTestJobs.remove(n);
            }
            n++;
            if (n == sPendingTestJobs.size()) {
                break;
            }
        }
    }

    public static void removeLastPendingIntent(String tesSuiteName) {
        int n = 0;
        while (sPendingTestJobs.size() > 0) {
            PendingTestJob pendingTestJob = sPendingTestJobs.get(n);
            if (pendingTestJob.getTestSuiteName().equals(tesSuiteName)) {
                sPendingTestJobs.remove(n);
            }
            n++;
            if (n == sPendingTestJobs.size()) {
                break;
            }
        }
    }

    private void unSchedulePendingIntent(PendingIntent pendingInent) {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingInent);
    }

    public static final String MESSAGE = "testFactoryMessage";

    private void displayTestFactoryInfo() {
        if (RootTab.getCurrentTab() == Tabs.MAIN_TAB) {
            Intent intent = new Intent(MainTab.ACTION_DISPLAY);
            intent.putExtra(MESSAGE, StringUtils.EMPTY);
            broadcastIntent(intent);
        }
    }

    private void broadcastIntent(Intent intent) {
        // Log.v(LOG, "displayIntent()");
        mContext.sendBroadcast(intent);
    }
}
