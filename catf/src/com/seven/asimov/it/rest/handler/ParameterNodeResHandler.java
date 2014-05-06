package com.seven.asimov.it.rest.handler;

import com.seven.asimov.it.rest.RestClientFactory;
import com.seven.asimov.it.rest.model.ParameterNode;
import com.seven.asimov.it.rest.resource.cms.ParameterNodeResource;
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
public class ParameterNodeResHandler {
    private static final Logger logger= LoggerFactory.getLogger(ParameterNodeResHandler.class);
    private static final String PATH="/parameternode";

    ParameterNodeResource resource;
    public ParameterNodeResHandler(RestClientFactory factory) throws Exception {
        resource=factory.getClient(PATH,ParameterNodeResource.class);
    }

    public ParameterNode getParameterNodeTree(){
        return resource.getParameterNodeTree();
    }


    public UUID findNodeId(ParameterNode root,String path){

        UUID nodeId=null;

        logger.trace("Node:"+root+", path:"+path);

        int len=path.indexOf("@");

        List<ParameterNode> chNodes=root.getChildren();
        String current=path.substring(0,len==-1?path.length():len);
        logger.trace("current path:"+current);
        for(ParameterNode node:chNodes){

            if (node.getName().equals(current)) {
                if (-1==len){
                    nodeId=node.getId();
                    break;
                }
                nodeId=findNodeId(node,path.substring(len+1,path.length()));
            }


        }
        return nodeId;

    }

}
