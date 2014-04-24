package com.seven.asimov.it.utils.sa;

import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.model.cms.ConfigurationNode;
import com.seven.asimov.it.model.cms.ConfigurationParameter;
import com.seven.asimov.it.rest.RestClientFactory;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/17/14
 * Time: 1:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class SaRestUtil {
    private static final Logger logger = LoggerFactory.getLogger(SaLocalUtil.class);

    private static RestClientFactory clientFactory;

    //cms attributes
    private static ConfigurationNode originNodes;
    private static ConfigurationResourceHelper configurationResourceHelper;
    private static String CONFIGURATION_ROOT="optimization";

    static {
        try {
            if (null == clientFactory) {
                //construct rest api client
                clientFactory = new RestClientFactory(TFConstantsIF.mPmsServerIp, Integer.toString(TFConstantsIF.mPmsServerPort));
            }
            if (null == configurationResourceHelper) {
                configurationResourceHelper = new ConfigurationResourceHelper(clientFactory);
            }

        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        }
    }

    public void addPolicy(Policy policy) throws Exception {
        updatePolicy(policy);
    }


    public void updatePolicy(Policy policy)throws Exception{
        if (null==configurationResourceHelper){
            throw new Exception("ConfigurationResourceHelper not initialized yet");
        }
        configurationResourceHelper.importConfiguration(updateConfiguration(policy));
    }

    public void cleanPolicy(Policy policy)throws Exception{
        if (null==configurationResourceHelper){
            throw new Exception("ConfigurationResourceHelper not initialized yet");
        }
         configurationResourceHelper.importConfiguration(originNodes);
    }

    private ConfigurationNode updateConfiguration(Policy policy) throws Exception{
        //get current configuration from server
        getConfigurationNodes();
        ConfigurationNode newNodes=originNodes;

        String parameter=policy.getName();
        String value=policy.getValue();
        String path=policy.getPath();
        path=path.split("@")[2];


        List<ConfigurationNode> chNodes=newNodes.getChildren();  //get root list
        for (ConfigurationNode chNode:chNodes){

            //policy root , equal to optimization
            if (CONFIGURATION_ROOT.equals(chNode.getName())){
                List<ConfigurationNode> opNodes=chNode.getChildren();

                for (ConfigurationNode opNode : opNodes) {

                    //get child node , ex @asimov@http
                    if (path.equals(opNode.getName())) {

                        //parameters list
                        List<ConfigurationParameter> httpParameters = opNode.getParameters();
                        for (ConfigurationParameter httpParameter : httpParameters) {
                             if (parameter.equals(httpParameter.getName())){
                                 List<String> values=new ArrayList<String>();
                                 values.add(value);
                                 httpParameter.setValue(values);
                             }
                        }
                    }
                }

            }
        }

        return newNodes;


    }

    private void getConfigurationNodes() throws Exception {
        if (null != originNodes) {
            return;
        }

        if (null==configurationResourceHelper){
            throw new Exception("ConfigurationResourceHelper not initialized yet");
        }
        originNodes = configurationResourceHelper.exportConfiguration();

    }


}
