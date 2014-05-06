package com.seven.asimov.it.utils.sa;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.rest.RestClientFactory;
import com.seven.asimov.it.rest.handler.*;
import com.seven.asimov.it.rest.model.Change.TargetType;
import com.seven.asimov.it.rest.model.*;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.saTasks.SAUpdateReceivedTask;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/17/14
 * Time: 1:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class SaRestUtil {
    private static final Logger logger = LoggerFactory.getLogger(SaLocalUtil.class);

    private static final TargetType TYPE = TargetType.deviceconfig;
    static SAUpdateReceivedTask saUpdateReceivedTask = new SAUpdateReceivedTask();

    private static User user;

    private static RestClientFactory clientFactory;
    static ParameterResHandler parameterResHandler;
    static ParameterNodeResHandler parameterNodeResHandler;
    static ChangeResHandler changeResHandler;
    static UserResHandler userResHandler;

    public static void init() throws Exception {

        logger.trace("Init rest client");
        clientFactory = new RestClientFactory(TFConstantsIF.REST_SERVER_ADDRESS, Integer.toString(TFConstantsIF.REST_SERVER_PORT));

        logger.trace("Init rest service providers");
        parameterResHandler = new ParameterResHandler(clientFactory);
        parameterNodeResHandler = new ParameterNodeResHandler(clientFactory);
        changeResHandler = new ChangeResHandler(clientFactory);
        userResHandler = new UserResHandler(clientFactory);

    }

    public static void addParameter(Policy policy) throws Throwable {
        updateParameter(policy, DeliveryPriority.urgent);
    }

    public static void addParameter(Policy policy, DeliveryPriority priority) throws Throwable {
        updateParameter(policy, priority);
    }

    public static void updateParameter(Policy policy) throws Throwable {
        updateParameter(policy, DeliveryPriority.urgent);
    }


    public static void updateParameter(Policy policy, DeliveryPriority priority) throws Throwable {

        logger.debug("Update parameter:" + policy + " with delivery priority:" + priority);
        //get user info
        if (null == user) {
            setUserInfo();
        }

        Parameter target = findParameter(policy);
        if (null == target) {
            throw new Exception("");
        }
        logger.debug("Target parameter:" + target);

        ParameterBatch batch = new ParameterBatch();
        batch.setTargetIds(null);

        List<Parameter> parameters = new ArrayList<Parameter>();
        List<String> values = new ArrayList();
        values.add(policy.getValue());
        target.setValue(values);
        parameters.add(target);
        logger.debug("Target after updating:" + target);

        batch.setParameters(parameters);

        parameterResHandler.addUserParameter(user.getImei(), user.getMsisdn(), batch);

        ProvisioningDetails comment = new ProvisioningDetails();
        comment.setComment(String.format("Change parameter %s value to %s", policy.getName(), policy.getValue()));

        //check sa update data be received in logcat
        LogcatUtil logcat = new LogcatUtil(AsimovTestCase.getStaticContext(), saUpdateReceivedTask);

        try {
            logcat.start();
            pushSingleUnProvision(priority, comment);

            TestUtil.sleep(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            logcat.stop();
            if (saUpdateReceivedTask.getLogEntries().isEmpty()) {
                logger.info("SA update notification not received from server. Emulating SMS notification.");
                throw new Exception("SA update notification not received from server");
                //
                /*logcat = new LogcatUtil(AsimovTestCase.getStaticContext(), saUpdateReceivedTask);
                logcat.start();
                (new SmsUtil(AsimovTestCase.getStaticContext())).sendPolicyUpdate((byte) 1);
                TestUtil.sleep(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                logcat.stop();*/
            }

            logger.debug("Parameter update successful with logcat check");

        } finally {
            if (logcat.isRunning()) {
                logcat.stop();
            }

        }


    }

    public static Parameter findParameter(Policy policy) throws Throwable {

        logger.debug("Find parameter by " + policy);
        ParameterNode root = parameterNodeResHandler.getParameterNodeTree();
        UUID nodeId = parameterNodeResHandler.findNodeId(root, policy.getPath());
        if (null == nodeId) {
            throw new Exception("NodeId =null, can't find this parameter");
        }

        logger.debug("nodeId=" + nodeId);

        Parameter target = parameterResHandler.findParameter(nodeId, user.getId(), true, policy.getName());
        return target;
    }

    public static void cleanParameter(Policy policy) throws Throwable {


        LogcatUtil logcat = new LogcatUtil(AsimovTestCase.getStaticContext(), saUpdateReceivedTask);

        if (null == user) {
            setUserInfo();
        }

        Parameter targetParameter = findParameter(policy);
        logger.debug("Clean parameter:" + targetParameter);


        String targetPath = "/parameter/user?msisdn=" + user.getMsisdn();
        String url = MessageFormat.format(clientFactory.REST_BASE_URL, TFConstantsIF.mPmsServerIp, Integer.toString(TFConstantsIF.mPmsServerPort), targetPath);

        MyDelete delete = new MyDelete(url);
        try {
            StringEntity input = new StringEntity(String.format("{\"nodeId\":\"%s\",\"paramName\" : \"%s\"}", targetParameter.getNodeId().toString(), targetParameter.getName()));
            input.setContentType("application/json");
            delete.setEntity(input);


            logcat.start();
            HttpResponse response = clientFactory.httpClient.execute(delete);

            if (!response.getStatusLine().toString().contains("200")) {
                throw new Exception("Delete parameter failed with status:" + response.getStatusLine());
            }

            TestUtil.sleep(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            logcat.start();

            if (saUpdateReceivedTask.getLogEntries().isEmpty()) {
                logger.error("SA update notification not received from server. Emulating SMS notification.");
                throw new Exception("SA update notification not received from server");
            }

            logger.debug("Clean successful");

        } finally {
            if (logcat.isRunning()) {
                logcat.stop();
            }
        }

    }

    private static void pushSingleUnProvision(DeliveryPriority priority, ProvisioningDetails comment) {

        logger.debug(String.format("set delivery priority %s for user %s", priority, user.getId()));
        changeResHandler.setDeliveryPriority(TYPE, user.getId(), priority);

        logger.debug("Push single unprovisioned change for user:" + user.getId());
        changeResHandler.provisionSingleChange(comment, TYPE, user.getId());
    }


    private static void pushUnProvision(DeliveryPriority priority, ProvisioningDetails comment) {
        changeResHandler.setDeliveryPriority(TYPE, user.getId(), priority);
        logger.debug("Push unprovisioned changes");
        changeResHandler.provisionChanges(comment);
    }


    private static void setUserInfo() throws Exception {
        String z7tp = OCUtil.getDeviceZ7TpAddress();
        logger.debug("7tp address:" + z7tp);
        user = userResHandler.findUserBy7TP(z7tp);
        logger.debug("Find user:" + user + " according 7tp address " + z7tp);

    }

    public static void close() {
        logger.trace("close rest client connections");
        clientFactory.closeRestClient();
    }


}
