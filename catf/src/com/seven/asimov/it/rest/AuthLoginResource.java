package com.seven.asimov.it.rest;

import com.seven.asimov.it.model.cms.Credentials;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "login")
@XmlAccessorType(XmlAccessType.FIELD)
public interface AuthLoginResource {

    /**
     * Login interface. Userid and Password should be provided in JSON format.
     * 
     * @param credentials {"username":"yourUsername","password" : "yourPassword"}
     */
    @POST
    @Produces({ "application/json", "application/xml" })
    @Consumes({ "application/json", "application/xml" })
    void login(Credentials credentials);

}
