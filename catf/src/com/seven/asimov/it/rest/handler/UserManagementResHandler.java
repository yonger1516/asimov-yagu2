package com.seven.asimov.it.rest.handler;

import com.seven.asimov.it.rest.RestClientFactory;
import com.seven.asimov.it.rest.model.Parameter;
import com.seven.asimov.it.rest.resource.user.UserManagementResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/29/14
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserManagementResHandler {
    private static final Logger logger= LoggerFactory.getLogger(UserManagementResHandler.class);
    private static final String PATH="/user/management";

    UserManagementResource resource;
    public UserManagementResHandler(RestClientFactory factory) throws Exception {
          resource=factory.getClient(PATH, UserManagementResource.class);
    }

    public List<Parameter> getUserParamList(UUID endpointId){
        return resource.getUserOrUgOverwrittenParams(endpointId);
    }

}
