package com.seven.asimov.it.rest.resource.auth;

import com.seven.asimov.it.rest.model.Credentials;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

@Consumes({ "application/json" })
@Produces({ "application/json" })
public interface AuthLoginResource {

    /**
     * Login interface. Userid and Password should be provided in JSON format.
     * 
     * @param credentials {"username":"yourUsername","password" : "yourPassword"}
     */
    @POST
    void login(Credentials credentials);

}
