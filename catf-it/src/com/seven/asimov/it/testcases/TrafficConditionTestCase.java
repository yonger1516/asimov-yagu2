package com.seven.asimov.it.testcases;


import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.ConditionStateTask;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.FTMdropMessageTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAppliedTask;
import com.seven.asimov.it.utils.logcat.wrappers.ConditionState;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class TrafficConditionTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(TrafficConditionTestCase.class.getSimpleName());
    protected PolicyAppliedTask conditionPolicyAppliedTask;
    protected ConditionStateTask conditionWaitingExitedTask;
    protected ConditionStateTask conditionExitedEnteredTask;
    protected ConditionStateTask conditionEnteredExitedTask;
    protected ConditionStateTask conditionExitedDisabledTask;
    protected FTMdropMessageTask ftmDropMessageTask;
    protected Policy conditionPolicy;
    protected Policy exitConditionPolicy;
    protected Policy actionPolicy;
    protected String scriptName;
    protected String uri;
    protected final String trafficPropertyName = "traffic";
    protected Set<Policy> policies = new LinkedHashSet<Policy>();
    protected List<String> policyIDs = new ArrayList<String>();
    protected LogEntryWrapper logEntry;
    protected Throwable ex;
    protected Set<Task> tasks = new HashSet<Task>();
    protected LogcatUtil logcatUtil = null;
    protected LogcatUtil logcatUtilTasks = new LogcatUtil(getContext(), tasks);

    protected void prepareConditionTasks(String scriptName) {
        tasks = new HashSet<Task>();
        if (conditionPolicy != null) {
            conditionPolicyAppliedTask = new PolicyAppliedTask(conditionPolicy.getName(), conditionPolicy.getValue().replace("\\.", "\\\\."));
        } else if (exitConditionPolicy != null) {
            conditionPolicyAppliedTask = new PolicyAppliedTask(exitConditionPolicy.getName(), exitConditionPolicy.getValue().replace("\\.", "\\\\."));
        }
        tasks.add(conditionPolicyAppliedTask);
        conditionWaitingExitedTask = new ConditionStateTask(TFConstantsIF.IT_PACKAGE_NAME, scriptName, ConditionState.Waiting_for_configuration, ConditionState.Exited);
        tasks.add(conditionWaitingExitedTask);
        conditionExitedEnteredTask = new ConditionStateTask(TFConstantsIF.IT_PACKAGE_NAME, scriptName, ConditionState.Exited, ConditionState.Entered);
        tasks.add(conditionExitedEnteredTask);
        conditionEnteredExitedTask = new ConditionStateTask(TFConstantsIF.IT_PACKAGE_NAME, scriptName, ConditionState.Entered, ConditionState.Exited);
        tasks.add(conditionEnteredExitedTask);
        conditionExitedDisabledTask = new ConditionStateTask(TFConstantsIF.IT_PACKAGE_NAME, scriptName, ConditionState.Exited, ConditionState.Disabled);
        tasks.add(conditionExitedDisabledTask);
        ftmDropMessageTask = new FTMdropMessageTask();
        tasks.add(ftmDropMessageTask);

        setTasksTimestampToGMT(false);
    }

    protected void setTasksTimestampToGMT(boolean changeTimestampToGMT) {
        for (Task task : tasks) {
            task.setChangeTimestampToGMT(changeTimestampToGMT);
        }
    }

    protected String getPropertyPath(String appPackage, String scriptName, PropertyType type) throws Exception {
        String policyPath = "";
        switch (type) {
            case CONDITION:
                policyPath = String.format(TFConstantsIF.TRAFFIC_IN_CONDITION_PROPERTY_PATH_TEMPLATE, appPackage, scriptName);
                break;
            case EXIT_CONDITION:
                policyPath = String.format(TFConstantsIF.TRAFFIC_OUT_CONDITION_PROPERTY_PATH_TEMPLATE, appPackage, scriptName);
                break;
            case ACTION:
                policyPath = String.format(TFConstantsIF.TRAFFIC_IN_ACTION_PROPERTY_PATH_TEMPLATE, appPackage, scriptName);
                break;
            case EXIT_ACTION:
                policyPath = String.format(TFConstantsIF.TRAFFIC_OUT_ACTION_PROPERTY_PATH_TEMPLATE, appPackage, scriptName);
                break;
        }
        logger.info("conditionPath=" + policyPath);
        return policyPath;
    }

    protected void prepareTrafficConditionTest() throws Exception {
        try {
            prepareConditionTasks(scriptName);
            logcatUtil = new LogcatUtil(getContext(), tasks);
            logcatUtil.start();
            addPolicies(policies);
            doHttpsActivity();
            TestUtil.sleep(20 * 1000);
            logcatUtil.stop();
            logcatUtilTasks.logTasks();
        } finally {
            logcatUtil.stop();
            deleteAllProperties();
        }
    }

    protected enum PropertyType {
        CONDITION,
        EXIT_CONDITION,
        ACTION,
        EXIT_ACTION
    }

    protected void addPolicies(Set<Policy> policySet) throws Exception {
        String policyId;
        for (Policy policy : policySet) {
            policyId = PMSUtil.createPersonalScopeProperty(policy.getName(), policy.getPath(), policy.getValue(), true);
            assertTrue(policy.toString() + " hasn't been added!", (policyId != null && !policyId.equals("")));
            policyIDs.add(policyId);
            logger.info(policy.toString() + " id:" + policyId + " has been added.");
        }
        //wait for policy to be applied
        TestUtil.sleep(60 * 1000);
        Runtime.getRuntime().exec("ping -c 1 " + AsimovTestCase.TEST_RESOURCE_HOST).waitFor();
    }

    protected void deleteAllProperties() {
        for (String policyID : policyIDs) {
            logger.info("Before deleting property id:" + policyID);
            deleteProperty(policyID);
        }
        policies = new HashSet<Policy>();
        policyIDs = new ArrayList<String>();
    }

    private void deleteProperty(String id) {
        try {
            PMSUtil.deleteProperty(id);
        } catch (Throwable t) {
            logger.debug(ExceptionUtils.getStackTrace(t));
        }
    }

    protected void doHttpsActivity() throws Exception {
        String resourceURL = createTestResourceUri("traffic_conditions", true);
        try {
            PrepareResourceUtil.prepareResource(resourceURL, false);
            HttpRequest request = AsimovTestCase.createRequest().setUri(resourceURL).setMethod("GET").addHeaderField("Connection", "close")
                    .addHeaderField("Cache-Control", "no-cache, no-store")
                    .getRequest();
            try {
                sendHttpsRequest(request);
            } catch (Exception ex) {
                logger.debug("HTTPS request threw an exception");
                ex.printStackTrace();
            }
        } finally {
            PrepareResourceUtil.prepareResource(resourceURL, true);
        }
    }

    protected String getHostIP(String testResourceHost) {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(testResourceHost);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return inetAddress != null ? inetAddress.getHostAddress() : null;
    }
}
