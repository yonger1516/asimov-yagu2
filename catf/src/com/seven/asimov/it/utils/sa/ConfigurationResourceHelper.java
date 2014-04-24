package com.seven.asimov.it.utils.sa;

import com.seven.asimov.it.model.cms.ConfigurationNode;
import com.seven.asimov.it.rest.RestClientFactory;
import com.seven.asimov.it.rest.cms.ConfigurationResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/23/14
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationResourceHelper {

    private static final Logger logger= LoggerFactory.getLogger(ConfigurationResourceHelper.class);
    private static ConfigurationResource resource;
    private static final String PATH="configuration";

    public ConfigurationResourceHelper(RestClientFactory clientFactory) throws Exception {
         resource=clientFactory.getClient(PATH,ConfigurationResource.class);
         logger.info("Initialize "+this.getClass().getName()+" done");


    }

    public ConfigurationNode exportConfiguration(){
        return resource.exportConfiguration();
    }

    public void importConfiguration(ConfigurationNode nodes){
        resource.importConfiguration(nodes);
    }

    public void replaceConfiguration(ConfigurationNode nodes){
        resource.replaceConfiguration(nodes);
    }

}
