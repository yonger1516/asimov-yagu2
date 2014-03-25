package com.seven.asimov.it.tests.generic.application.handler.traffic;

import android.test.AssertionFailedError;
import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.TrafficConditionTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import com.seven.asimov.it.utils.logcat.wrappers.FTMdropMessageWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.seven.asimov.it.utils.logcat.LogcatChecks.checkLogEntryExist;
import static com.seven.asimov.it.utils.logcat.LogcatChecks.checkLogEntryNotExist;

public class TrafficConditionTests extends TrafficConditionTestCase {
    private static final Logger logger = LoggerFactory.getLogger(TrafficConditionTests.class.getSimpleName());
    private MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());

    //@Execute
    public void test_000_TrafficCondition() throws Throwable {
        mobileNetworkUtil.onWifiOnly();
        PMSUtil.deleteNamespace(TFConstantsIF.HTTPS_BLACKLIST_PATH + "@" + TFConstantsIF.IT_PACKAGE_NAME);
        PMSUtil.addPolicies(new Policy[]{new Policy("enabled", "false", "@asimov@application@com.seven.asimov.it@ssl", true)});
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address=91.198.174.234</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_001_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address=" + getHostIP(AsimovTestCase.TEST_RESOURCE_HOST), getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address=address!=91.198.174.222</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_002_TrafficCondition() throws Exception {
        //91.198.174.222 - wrong IP
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_003_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address=/</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is not activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_004_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address=/", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryNotExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: direction=up</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_005_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "direction=up", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: direction=down</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_006_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "direction=down", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: direction=</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_007_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "direction=", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: direction=no</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is not activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_008_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "direction=no", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryNotExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: port_range=400:500</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_009_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "port_range=400:500", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: port_range=400-500</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_010_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "port_range=400-500", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: port_range=443</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_011_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "port_range=443", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: port_range=</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_012_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "port_range=", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: port_range=/</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is not activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_013_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "port_range=/", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryNotExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: package=com\.seven\.asimov\.it</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_014_TrafficCondition() throws Exception {
        try {
            String packageRegexpName = TFConstantsIF.IT_PACKAGE_NAME.replace(".", "\\.");
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "package=" + packageRegexpName, getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: package=</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_015_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "package=", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: package=6666</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is not activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_016_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "package=6666", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryNotExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222 package=com\.seven\.asimov\.it</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policy is added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_017_TrafficCondition() throws Exception {
        try {
            String packageRegexpName = TFConstantsIF.IT_PACKAGE_NAME.replace(".", "\\.");
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222 package=" + packageRegexpName, getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            policies.add(conditionPolicy);
            logger.info(conditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryExist(conditionExitedEnteredTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: direction=up</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: address=91.198.174.234</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_018_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "direction=up", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "address=" + getHostIP(AsimovTestCase.TEST_RESOURCE_HOST), getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: direction=up</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: address!=91.198.174.222</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_019_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "direction=up", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: direction=up</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: address=</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_020_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "direction=up", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "address=", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: direction=up</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: address=/</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is disabled</li>
     * <li>the exit_condition is not activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_021_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "direction=up", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "address=/", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedDisabledTask, logEntry);
            checkLogEntryNotExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: direction=up</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    // @Execute
    @DeviceOnly
    @LargeTest
    public void test_022_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "direction=up", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: direction=down</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_023_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "direction=down", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: direction=</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_024_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "direction=", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: direction=no</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is disabled</li>
     * <li>the exit_condition is not activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_025_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "direction=no", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedDisabledTask, logEntry);
            checkLogEntryNotExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: port_range=400:500</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_026_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "port_range=400:500", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: port_range=400-500</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_027_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "port_range=400-500", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: port_range=</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_028_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "port_range=", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: port_range=443</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_029_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "port_range=443", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: port_range=/</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is disabled</li>
     * <li>the exit_condition is not activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_030_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "port_range=/", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedDisabledTask, logEntry);
            checkLogEntryNotExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: package=com\.seven\.asimov\.it</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_031_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            String packageRegexpName = TFConstantsIF.IT_PACKAGE_NAME.replace(".", "\\.");
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "package=" + packageRegexpName, getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: package=</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_032_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            String packageRegexpName = TFConstantsIF.IT_PACKAGE_NAME.replace(".", "\\.");
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "package=", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: package=/</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is disabled</li>
     * <li>the exit_condition is not activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_033_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "package=/", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            checkLogEntryNotExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: address!=91.198.174.223 package=com\.seven\.asimov\.it</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_034_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            String packageRegexpName = TFConstantsIF.IT_PACKAGE_NAME.replace(".", "\\.");
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.223 package=" + packageRegexpName, getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: address=91.198.174.234</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the exit_condition without condition is not initialized</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_035_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            exitConditionPolicy = new Policy(trafficPropertyName, "address=91.198.174.234", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(exitConditionPolicy);
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            checkLogEntryNotExist(conditionWaitingExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address=91.198.174.234</li>
     * <li>Add personal policy scripts@<script_name>@exit_conditions@traffic: address=91.198.174.234</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>the exit_condition is activated</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_036_TrafficCondition() throws Exception {
        try {
            scriptName = "script_traffic";
            conditionPolicy = new Policy(trafficPropertyName, "address=" + getHostIP(AsimovTestCase.TEST_RESOURCE_HOST), getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            exitConditionPolicy = new Policy(trafficPropertyName, "address=" + getHostIP(AsimovTestCase.TEST_RESOURCE_HOST), getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.EXIT_CONDITION), true);
            policies.add(conditionPolicy);
            policies.add(exitConditionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(exitConditionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            checkLogEntryExist(conditionEnteredExitedTask, logEntry);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    /**
     * <h3>Verify detection of traffic condition with address value</h3>
     * actions:
     * <ol>
     * <li>Add personal policy scripts@<script_name>@conditions@traffic: address!=91.198.174.222</li>
     * <li>Add personal policy scripts@<script_name>@actions@drop_sessions: port_range=400:500 package=</li>
     * <li>do 1 https request</li>
     * <p/>
     * </ol>
     * checks:
     * <ol>
     * <li>the policies are added</li>
     * <li>the condition is initialized</li>
     * <li>the condition is activated</li>
     * <li>FTM message should be sent to dispatchers with 1 drop field</li>
     * </ol>
     *
     * @throws Exception
     */
    //@Execute
    @DeviceOnly
    @LargeTest
    public void test_037_TrafficCondition() throws Exception {
        try {
            //91.198.174.222 - wrong IP
            scriptName = "script_traffic";
            String packageRegexpName = TFConstantsIF.IT_PACKAGE_NAME.replace(".", "\\.");
            conditionPolicy = new Policy(trafficPropertyName, "address!=91.198.174.222", getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.CONDITION), true);
            actionPolicy = new Policy("drop_sessions", "port_range=400:500 package=" + packageRegexpName, getPropertyPath(TFConstantsIF.IT_PACKAGE_NAME, scriptName, PropertyType.ACTION), true);
            policies.add(conditionPolicy);
            policies.add(actionPolicy);
            logger.info(conditionPolicy.toString());
            logger.info(actionPolicy.toString());
            prepareTrafficConditionTest();
            logEntry = null;
            logEntry = checkLogEntryExist(conditionPolicyAppliedTask, logEntry);
            logEntry = checkLogEntryExist(conditionWaitingExitedTask, logEntry);
            logEntry = checkLogEntryExist(conditionExitedEnteredTask, logEntry);
            FTMdropMessageWrapper fmtLogEntry = (FTMdropMessageWrapper) checkLogEntryExist(ftmDropMessageTask, logEntry);
            assertEquals("FTM message should be sent to dispatchers with 1 drop fields.", fmtLogEntry.getCount(), 1);
        } catch (Exception e) {
            logger.debug("Exception catched: " + ExceptionUtils.getStackTrace(e));
            ex = e;
        }
        if (ex != null) {
            throw new AssertionFailedError(ex.getMessage());
        }
    }

    //@Execute
    public void test_038_TrafficCondition() throws Exception {
        PMSUtil.cleanPaths(new String[]{"@asimov@application@com.seven.asimov.it@ssl"});
    }

    @Override
    protected void runTest() throws Throwable {
        boolean isPassed;
        int numberOfAttempts = 0;
        List<String> counts = new ArrayList<String>();
        do {
            isPassed = true;
            numberOfAttempts++;
            try {
                super.runTest();
            } catch (junit.framework.AssertionFailedError assertionFailedError) {
                logger.debug("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);
        assertTrue("The test was failed three times ", counts.size() != 3);
    }

}
