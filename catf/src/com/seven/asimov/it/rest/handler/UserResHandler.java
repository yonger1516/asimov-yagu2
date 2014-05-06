package com.seven.asimov.it.rest.handler;

import com.seven.asimov.it.rest.RestClientFactory;
import com.seven.asimov.it.rest.model.User;
import com.seven.asimov.it.rest.resource.user.UserResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/29/14
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserResHandler {
    private static final Logger logger= LoggerFactory.getLogger(UserResHandler.class);
    private static final String PATH="/user";

    UserResource resource;
    public UserResHandler(RestClientFactory factory) throws Exception {
          resource=factory.getClient(PATH, UserResource.class);
    }

    public User findUserByMSISDN(String msisdn){
         return resource.findUser(msisdn,"","","");
    }

    public User findUserBy7TP(String z7tp){
        return resource.findUser("","",z7tp,"");
    }
}
