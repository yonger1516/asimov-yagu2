package com.seven.asimov.it.rest.handler;

import com.seven.asimov.it.rest.RestClientFactory;
import com.seven.asimov.it.rest.resource.cms.ConfigurationResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/29/14
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationResHandler {
    private static final Logger logger= LoggerFactory.getLogger(ConfigurationResHandler.class);
    private static final String PATH="/configuration";

    ConfigurationResource resource;
    public ConfigurationResHandler(RestClientFactory factory) throws Exception {
        resource=factory.getClient(PATH,ConfigurationResource.class);
    }
}
