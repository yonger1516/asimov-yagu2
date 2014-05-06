package com.seven.asimov.it.rest.handler;

import com.seven.asimov.it.rest.RestClientFactory;
import com.seven.asimov.it.rest.model.Parameter;
import com.seven.asimov.it.rest.model.ParameterBatch;
import com.seven.asimov.it.rest.resource.cms.ParameterResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/29/14
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class ParameterResHandler {

    private static final Logger logger= LoggerFactory.getLogger(ParameterResHandler.class);
    private static final String PATH="/parameter";

    ParameterResource resource;
    public ParameterResHandler(RestClientFactory factory) throws Exception{

        resource=factory.getClient(PATH,ParameterResource.class);
    }

    public Parameter findParameter(UUID nodeId,UUID userId,boolean includeGroup,String paramName){
        List<Parameter> parameters = resource.getUserParameters(nodeId,userId,includeGroup);

        for (Parameter p : parameters) {
            logger.debug(p.toString());
            if (paramName.equals(p.getName())) {
                logger.debug("find parameter:" + p.toString());
                return p;
            }
        }

        return null;
    }

    public void addUserParameter(String imei,String msisdn,ParameterBatch batch){
        resource.createUserParameters(imei,msisdn,batch);

    }
}
