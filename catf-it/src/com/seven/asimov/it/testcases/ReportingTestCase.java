package com.seven.asimov.it.testcases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TestCaseThread;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.conn.ConnUtils;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.CleanupReportProviderTask;
import com.seven.asimov.it.utils.logcat.tasks.LogLabelTask;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.*;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAddedTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAppliedTask;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.sms.SmsUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ReportingTestCase extends AsimovTestCase {

    private static final Logger logger = LoggerFactory.getLogger(ReportingTestCase.class.getSimpleName());
    private final String reportDataBaseFilename = "/data/data/com.seven.asimov/reporting.db";

    protected long br_timestamp;
    protected String[] lock_relay = new String[3];
    protected String[] unlock_relay = new String[3];
    private int httpActivityRetryCount = 0;
    protected String uri;
    protected Throwable ex;
    private int databaseCoefficient = 0;
    private static SQLiteDatabase dataBase;

    private List<Task> tasksList;
    //protected Map<String, String> properties = new HashMap<String, String>();
    protected LogEntryWrapper logEntry;
    protected ExecutorService executorService;
    private final int PORT = 8080;

    protected PolicyAddedTask policyAddedTask;
    protected PolicyAppliedTask policyAppliedTask;
    protected RadioStateTask radioStateTask;
    protected CrcsAccumulatedTask crcsAccumulatedTask;
    protected ReportTransferTaskNN reportTransferTaskNN;
    protected CrcsSendingTaskSuccessRemovedTask crcsSendingTaskSuccessRemovedTask;
    protected DumpTransactionTask dumpTransactionTask;
    protected ReportPackDumpExecutedAndSentTask reportPackDumpExecutedAndSentTask;
    protected PrepareToCleanupReportProviderTask prepareToCleanupReportProviderTask;
    protected CleanupReportProviderTask cleanupReportProviderTask;
    protected CrcsRemovedTask crcsRemovedTask;
    protected CreatedTaskSchedulerTaskTask createdTaskSchedulerTaskTask;
    protected CrcsTransferSuccessTask crcsTransferSuccessTask;
    protected ConnectionFailedRelayAvaliableTask connectionFailedRelayAvaliableTask;
    protected CRCSReportSendingRetryTask crcsReportSendingRetryTask;
    protected CrcsTaskInQueueTask crcsTaskInQueueTask;
    protected CrcsStorageFilled_75_PercentTask crcsStorageFilled_75_PercentTask;
    protected TryToForceCrcsReportSendingTask tryToForceCrcsReportSendingTask;
    protected CRCSReportSendingFailedTask crcsReportSendingFailedTask;
    protected CrcsStorageFilled_95_PercentTask crcsStorageFilled_95_PercentTask;
    protected LogLabelTask logLabelTask;
    protected ReportTransferParametrizedTask reportTransferParametrizedTask1;
    protected ReportTransferParametrizedTask reportTransferParametrizedTask2;

    protected LogcatUtil prepareLogcatUtil_new(Context context, Task... tasks) throws InterruptedException {
        Thread.sleep(5 * 1000);
        return new LogcatUtil(context, Arrays.asList(tasks));
    }

    protected void prepareTasks() {
        tasksList = new ArrayList<Task>();
        policyAddedTask = new PolicyAddedTask();
        tasksList.add(policyAddedTask);
        radioStateTask = new RadioStateTask();
        tasksList.add(radioStateTask);
        crcsAccumulatedTask = new CrcsAccumulatedTask();
        tasksList.add(crcsAccumulatedTask);
        reportTransferTaskNN = new ReportTransferTaskNN();
        tasksList.add(reportTransferTaskNN);
        crcsSendingTaskSuccessRemovedTask = new CrcsSendingTaskSuccessRemovedTask();
        tasksList.add(crcsSendingTaskSuccessRemovedTask);
        dumpTransactionTask = new DumpTransactionTask();
        tasksList.add(dumpTransactionTask);
        reportPackDumpExecutedAndSentTask = new ReportPackDumpExecutedAndSentTask();
        tasksList.add(reportPackDumpExecutedAndSentTask);
        prepareToCleanupReportProviderTask = new PrepareToCleanupReportProviderTask();
        tasksList.add(prepareToCleanupReportProviderTask);
        cleanupReportProviderTask = new CleanupReportProviderTask();
        tasksList.add(cleanupReportProviderTask);
        crcsRemovedTask = new CrcsRemovedTask();
        tasksList.add(crcsRemovedTask);
        createdTaskSchedulerTaskTask = new CreatedTaskSchedulerTaskTask();
        tasksList.add(createdTaskSchedulerTaskTask);
        crcsTransferSuccessTask = new CrcsTransferSuccessTask();
        tasksList.add(crcsTransferSuccessTask);
        connectionFailedRelayAvaliableTask = new ConnectionFailedRelayAvaliableTask();
        tasksList.add(connectionFailedRelayAvaliableTask);
        crcsReportSendingRetryTask = new CRCSReportSendingRetryTask();
        tasksList.add(crcsReportSendingRetryTask);
        crcsTaskInQueueTask = new CrcsTaskInQueueTask();
        tasksList.add(crcsTaskInQueueTask);
        crcsStorageFilled_75_PercentTask = new CrcsStorageFilled_75_PercentTask();
        tasksList.add(crcsStorageFilled_75_PercentTask);
        tryToForceCrcsReportSendingTask = new TryToForceCrcsReportSendingTask();
        tasksList.add(tryToForceCrcsReportSendingTask);
        crcsReportSendingFailedTask = new CRCSReportSendingFailedTask();
        tasksList.add(crcsReportSendingFailedTask);
        crcsStorageFilled_95_PercentTask = new CrcsStorageFilled_95_PercentTask();
        tasksList.add(crcsStorageFilled_95_PercentTask);
        tasksList.add(logLabelTask);
        tasksList.add(policyAppliedTask);
        /*
        conditionInitializedTask = new ConditionStateTask(IT_PACKAGE_NAME, "script_traffic", ConditionState.Disabled, ConditionState.Exited);
        tasksList.add(conditionInitializedTask);
        conditionEnteredTask = new ConditionStateTask(IT_PACKAGE_NAME, "script_traffic", ConditionState.Exited, ConditionState.Entered);
        tasksList.add(conditionInitializedTask);
        */
        setTasksTimestampToGMT(false);
    }

    protected void prepareTasksToTests_06_07() {
        radioStateTask = new RadioStateTask();
        crcsAccumulatedTask = new CrcsAccumulatedTask();
        reportTransferTaskNN = new ReportTransferTaskNN();
        crcsSendingTaskSuccessRemovedTask = new CrcsSendingTaskSuccessRemovedTask();
        dumpTransactionTask = new DumpTransactionTask();
        reportPackDumpExecutedAndSentTask = new ReportPackDumpExecutedAndSentTask();
        prepareToCleanupReportProviderTask = new PrepareToCleanupReportProviderTask();
        cleanupReportProviderTask = new CleanupReportProviderTask();
        crcsRemovedTask = new CrcsRemovedTask();
        createdTaskSchedulerTaskTask = new CreatedTaskSchedulerTaskTask();
        crcsTransferSuccessTask = new CrcsTransferSuccessTask();
        connectionFailedRelayAvaliableTask = new ConnectionFailedRelayAvaliableTask();
        crcsReportSendingRetryTask = new CRCSReportSendingRetryTask();
        crcsTaskInQueueTask = new CrcsTaskInQueueTask();
        crcsStorageFilled_75_PercentTask = new CrcsStorageFilled_75_PercentTask();
        tryToForceCrcsReportSendingTask = new TryToForceCrcsReportSendingTask();
        crcsReportSendingFailedTask = new CRCSReportSendingFailedTask();
        crcsStorageFilled_95_PercentTask = new CrcsStorageFilled_95_PercentTask();
    }

    protected TestCaseThread requestSendingThread = new TestCaseThread() {
        @Override
        public void run() throws Throwable {


            final String RESOURCE_URI = "crcs_reporting_06_07";
            final String uri = createTestResourceUri(RESOURCE_URI);
            final HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

            while (true) {
                sendRequest2(request);

                if (Thread.interrupted()) {
                    PrepareResourceUtil.invalidateResourceSafely(uri);

                    logger.info("Thread: " + Thread.currentThread().getId() + " is finished");
                    break;
                }
            }
        }
    };

    protected void setTasksTimestampToGMT(boolean changeTimestampToGMT) {
        for (Task task : tasksList) {
            if (task != null)
                task.setChangeTimestampToGMT(changeTimestampToGMT);
        }
    }

   /* protected void deleteAllProperties() {
        for (Map.Entry<String, String> en : properties.entrySet()) {
            if (en.getValue() != null) {
                logger.info("Before deleting property name:" + en.getKey() + " value:" + en.getValue());
                deleteProperty(en.getValue());
            }
        }
        properties = new HashMap<String, String>();
    }    */

    protected void deleteProperty(String id) {
        PMSUtil.deleteProperty(id);
        logger.info("Property " + id + " has been deleted.");
        TestUtil.sleep(60 * 1000);
        logger.info("deleteProperty end");
    }

    protected String addProperty(String name, String value, String path) throws Exception {
        String propId = null;
        SmsUtil smsUtil = new SmsUtil(getContext());
        logger.info("addProperty start");
        propId = PMSUtil.createPersonalScopeProperty(name, path, value, true);
        assertTrue("Property name:" + name + " hasn't been added!", (propId != null && !propId.equals("")));
        logger.info("Property name:" + name + " id:" + propId + " has been added.");
        //wait for policy to be applied
        TestUtil.sleep(60 * 1000);
        logger.info("addProperty end");
        smsUtil.sendPolicyUpdate((byte) 0);
        TestUtil.sleep(60 * 1000);
        return propId;
    }

    protected void sendRequests(HttpRequest request, int quantity) {
        for (int i = 0; i < quantity; i++) {
            sendRequest2(request);
            logSleeping(5 * DateUtil.SECONDS);
        }
    }

    protected void doHttpActivity(int quantityOfRequests) throws Exception {
        //Log.v(TAG, "doHttpActivity: " + quantityOfRequests);
        int timeout = 60 * 1000;
        String url;
        try {
            for (int i = 0; i < quantityOfRequests; i++) {
                int index = (int) (Math.random() * 50);
                uri = "http://amazon.com";             //TODO change to uri = StabilityTopResources.topHttpResources_tcp[index];
                if (uri.contains("http://")) {
                    url = uri.substring("http://".length());
                } else {
                    url = uri;
                }
                if (ConnUtils.getHostAddress(url) == null) {
                    throw new UnknownHostException("ConnUtils.getHostAddress for " + url + " is null!");
                }
                HttpRequest request = createRequest().setUri(uri).setMethod("GET").getRequest();
                logger.info("Sending request to " + uri);
                sendRequest(request, false, timeout);
            }
        } catch (UnknownHostException uhe) {
            logger.warn(ExceptionUtils.getStackTrace(uhe));
            logger.info("doHttpActivity: retryCount:" + httpActivityRetryCount);
            if (++httpActivityRetryCount > 5) {
                throw new UnknownHostException(uhe.getMessage());
            }
            doHttpActivity(quantityOfRequests);
        }
    }

    /**
     * Intervals are counted represent arithmetic progression
     * where
     * 1) The first term of the progression a0 = 30 seconds
     * 2) the sum of all the members of the progression is equal to 1800 seconds by default or specified branding option client.openchannel.mobile_networks_failover.attempt_interval
     * 3) The amount equal to 5 members of the progression by default or specified branding option client.openchannel.mobile_networks_failover.retries
     */
    public int[] getReportRetryIntervals() {
        int timeIntervals[] = new int[]{30 * 1000, 195 * 1000, 360 * 1000, 525 * 1000, 690 * 1000};
        int timeout = 1800 * 1000; //todo it is default value. must be got from branding
        //this get from branding
        if (TFConstantsIF.MOBILE_NETWORKS_FAILOVER_ATTEMPT_INTERVAL != null && TFConstantsIF.MOBILE_NETWORKS_FAILOVER_RETRIES != null) {
            int retries = TFConstantsIF.MOBILE_NETWORKS_FAILOVER_RETRIES;
            int firstInterval = TFConstantsIF.MOBILE_NETWORKS_FAILOVER_ATTEMPT_INTERVAL * 1000;
            double dif = (((2 * timeout) / retries) - 2 * firstInterval) / (retries - 1);
            logger.info("dif=" + dif);
            timeIntervals = new int[retries];
            timeIntervals[0] = firstInterval;
            for (int i = 1; i < retries; i++) {
                timeIntervals[i] = (int) (timeIntervals[i - 1] + dif);
            }
        }
        for (int ti : timeIntervals) {
            logger.info("timeInterval=" + ti);
        }
        return timeIntervals;
    }

    protected void naturalIncreaseOfDbToSize75() throws IOException {
        int iteration = 1;
        File reportDb = new File(getReportDataBaseFilename());


        //starting request sending threads
        for (int counter = 0; counter < 20; counter++) {
            executorService.submit(requestSendingThread);
        }

        while (reportDb.length() < 786432) {
            logger.info(String.format("Current iteration: %d, size of the database: %d", iteration++, reportDb.length()));
            TestUtil.sleep(20000);
        }

        finishingThreads();
    }

    protected boolean naturalIncreaseOfDbToSize95() throws IOException {
        int iteration = 1;
        long dbSize = 0;
        long newDbSize;
        boolean dbSizeWasReduced = false;
        File reportDb = new File(getReportDataBaseFilename());

        for (int counter = 0; counter < 20; counter++) {
            executorService.submit(requestSendingThread);
        }

        while (true) {
            newDbSize = reportDb.length();
            logger.info(String.format("Current request iteration: %d, size of the database: %d", iteration++, newDbSize));
            if (dbSize > newDbSize) {
                dbSizeWasReduced = true;
                break;
            } else {
                dbSize = newDbSize;
                if (dbSize > 1022361) break;
            }
            TestUtil.sleep(20000);
        }

        finishingThreads();

        return dbSizeWasReduced;
    }

    protected void finishingThreads() {
        int iteration = 0;
        executorService.shutdownNow();
        // Wait for threads will be interrupted
        TestUtil.sleep(30 * 1000);

        while (!executorService.isTerminated()) {
            TestUtil.sleep(5 * 1000);
            logger.info("ExecutorService hadn't terminated yet");
            if (iteration++ > 5) {
                executorService.shutdownNow();
                logger.info("Shutdown repeated");
                iteration = 0;
            }
        }
    }

    public boolean reduceDB() {
        long firstDbSize = getOcDBSize();
        long secondDbSize;

        logger.info("DB size: " + firstDbSize);
        copyDb();
        openCopiedDataBase();
        reduceDBcopy();
        logger.info("Copied DB size after reducing: " + getOcDbCopySize());
        copyDbBack();
        secondDbSize = getOcDBSize();
        logger.info("DB size after reducing: " + secondDbSize);

        return !(firstDbSize == secondDbSize);
    }

    public long getOcDBSize() {
        return getFileSize(reportDataBaseFilename);
    }

    public long getOcDbCopySize() {
        return getFileSize("/data/data/com.seven.asimov.it/databases/reporting" + Integer.toString(databaseCoefficient) + ".db");
    }

    public long getFileSize(String dbFilename) {
        //String dbFilename = "/data/data/com.seven.asimov/reporting.db";
        File dbFile = new File(dbFilename);
        return dbFile.length();
    }

    public void createDbDirectory() {
        String[] createDbDirectory = {"su", "-c", "mkdir " + "/data/data/com.seven.asimov.it/databases"};

        try {
            Runtime.getRuntime().exec(createDbDirectory).waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String[] chmod = {"su", "-c", "chmod 777 " + "/data/data/com.seven.asimov.it/databases"};
        try {
            Runtime.getRuntime().exec(chmod).waitFor();
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }
    }

    public void copyDb() {
        createDbDirectory();

        String outputFilename = "/data/data/com.seven.asimov.it/databases/reporting" + Integer.toString(++databaseCoefficient) + ".db";
        logger.info("copyDb: outputFilename=" + outputFilename);
        String[] corruptCE = {"su", "-c", "cat " + reportDataBaseFilename + " > " + outputFilename};
        try {
            Runtime.getRuntime().exec(corruptCE).waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void openCopiedDataBase() {
        String databaseFilename = "/data/data/com.seven.asimov.it/databases/reporting" + Integer.toString(databaseCoefficient) + ".db";
        logger.info(String.format("su -c chmod 777 %s", databaseFilename));
        String[] chmod = {"su", "-c", "chmod 777 " + databaseFilename};
        try {
            Runtime.getRuntime().exec(chmod).waitFor();
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }
        //Log.v(TAG, String.format(Shell.execSimple(String.format("su -c chmod 777 %s", databaseFilename))));
        dataBase = SQLiteDatabase.openDatabase(databaseFilename, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
    }

    public void reduceDBcopy() {
        dataBase.execSQL("DROP TABLE IF EXISTS TEMP_DATA;");

        TestUtil.sleep(5000);
    }

    public void copyDbBack() {
        String outputFilename = "/data/data/com.seven.asimov.it/databases/reporting" + Integer.toString(databaseCoefficient) + ".db";
        logger.info("copyDbBack: outputFilename=" + outputFilename);
        String[] corruptCE = {"su", "-c", "cat " + outputFilename + " > " + reportDataBaseFilename};
        try {
            Runtime.getRuntime().exec(corruptCE).waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void growUpDB(long dbSize) {
        if (getOcDBSize() < dbSize) {
            logger.info("getOC_DB_size:" + getOcDBSize());
            copyDb();

            openCopiedDataBase();

            growUpDBcopy(dbSize);

            logger.info("getOC_DBcopy_size:" + getOcDbCopySize());
            logger.info("getOC_DB_size:" + getOcDBSize());
            copyDbBack();
            logger.info("getOC_DB_size:" + getOcDBSize());
            logger.info("test_fill_DB finish1");
            TestUtil.sleep(5000);
            logger.info("test_fill_DB finish2");
        }
    }

    public void growUpDBcopy(long size) {
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS TEMP_DATA (id INTEGER PRIMARY KEY AUTOINCREMENT, aData TEXT);");
        int count = 0;
        int stringSize = 0;
        long sizeDiff = 0;
        while (getOcDbCopySize() < size) {
            sizeDiff = size - getOcDbCopySize();
            if (sizeDiff > 20000) stringSize = 20000;
            else if (sizeDiff > 5000) stringSize = 5000;
            else if (sizeDiff > 1000) stringSize = 1000;
            logger.info("stringSize=" + stringSize);
            dataBase.execSQL("INSERT INTO TEMP_DATA (aData) VALUES (\"" + RandomStringUtils.randomAlphabetic(stringSize) + "\");");
            TestUtil.sleep(500);
            logger.info(String.format("getOC_DBcopy_size%d: %d byte", count++, getOcDbCopySize()));
        }
        TestUtil.sleep(5000);
    }

    public String getReportDataBaseFilename() {
        return reportDataBaseFilename;
    }
}
