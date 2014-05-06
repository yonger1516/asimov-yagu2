package com.seven.asimov.it.rest.handler;

import com.seven.asimov.it.rest.RestClientFactory;
import com.seven.asimov.it.rest.model.Change;
import com.seven.asimov.it.rest.model.Change.TargetType;
import com.seven.asimov.it.rest.model.DeliveryPriority;
import com.seven.asimov.it.rest.model.ProvisioningDetails;
import com.seven.asimov.it.rest.resource.changes.ChangesResource;
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
public class ChangeResHandler {
    private static final Logger logger= LoggerFactory.getLogger(ChangeResHandler.class);
    private static final String PATH="/changes";

    ChangesResource resource;
    public ChangeResHandler(RestClientFactory factory) throws Exception {
        resource=factory.getClient(PATH,ChangesResource.class);
    }

    public List<Change> getUnProvisionChanges(){
          return resource.getUnprovisionedChanges();
    }

    public void setDeliveryPriority(TargetType type,UUID userId,DeliveryPriority priority){
        resource.setDeliveryPriority(type,userId,priority);
    }

    public void provisionSingleChange(ProvisioningDetails details,TargetType type,UUID userId){

        resource.provisionSingleChange(details,type,userId);
    }

    public void provisionChanges(ProvisioningDetails details){
        resource.provisionChanges(details);
    }
}
