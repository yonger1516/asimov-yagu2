package com.seven.asimov.it.rest.resource.cms;

import com.seven.asimov.it.rest.model.Parameter;
import com.seven.asimov.it.rest.model.ParameterNode;

import javax.ws.rs.*;
import java.util.List;
import java.util.UUID;

@Consumes({ "application/json" })
@Produces({ "application/json" })
public interface ParameterNodeResource {

    /**
     * Create a new parameter node.
     * 
     * @param node parameter node to add
     * @return UUID UUID of the created node
     */

    @POST
	@Path("/")
	UUID createNode(ParameterNode node);
    
    /**
     * Creates a parameter node for a certain configuration instance.
     * 
     * @param instanceId
     * @param node
     * @return
     */
    @POST
    @Path("/usergroup/instance/{instanceid}")
    UUID createNode(@PathParam("instanceid") UUID instanceId, ParameterNode node);

    /**
     * Get the tree of parameter nodes.
     * 
     * @return the current parameter node tree
     */
	@GET
	@Path("/")
	ParameterNode getParameterNodeTree();
	
	@GET
	@Path("/{id}/tree")
	ParameterNode getParameterNodeSubTree(@PathParam("id") UUID nodeId);

	/**
	 * Get a single parameter node.
	 * 
	 * @return the parameter node corresponding to the UUID
	 */
	@GET
	@Path("/{id}")
    ParameterNode getParameterNode(@PathParam("id") UUID id);

	/**
	 * Update an existing parameter node.
	 * 
	 * @param id UUID of the parameter node to update
	 * @param node new values for the parameter node
	 */
	@PUT
	@Path("/{id}")
    void updateParameterNode(@PathParam("id") UUID id, ParameterNode node);

	/**
	 * Delete a parameter node.
	 * 
	 * @param id UUID of the node to delete
	 */
	@DELETE
	@Path("/{id}")
	void deleteParameterNode(@PathParam("id") UUID id);
	
	/**
	 * Delete a parameter node from a configuration instance.
	 * 
	 * @param instanceId
	 * @param id
	 */
	@DELETE
	@Path("/usergroup/instance/{instanceid}/node/{id}")
	void deleteParameterNode(@PathParam("instanceid") UUID instanceId, @PathParam("id") UUID id);

	/**
	 * Add a child node to a parent node.
	 * 
	 * @param parentId UUID of the parent node
	 * @param childId UUID of the child node
	 */
	@PUT
	@Path("/{parentId}/{childId}")
    void setChild(@PathParam("parentId") UUID parentId, @PathParam("childId") UUID childId);

	/**
	 * Child nodes that can be added directly under the given node.
     * 
	 * @param parentId UUID of the parent node under which nodes could be added
	 */
	@GET
	@Path("/{parentid}/templatenodes")
	List<ParameterNode> getChildren(@PathParam("parentid") UUID parentId);
	
	/**
	 * Child nodes that can be added directly under the given node for the given instance
     * 
     * @param instanceId UUID of the instance
	 * @param parentId UUID of the parent node under which nodes could be added
	 */
	@GET
	@Path("/usergroup/instance/{instanceid}/node/{parentid}/templatenodes")
	List<ParameterNode> getChildren(@PathParam("instanceid") UUID instanceId, @PathParam("parentid") UUID parentId);
	
	/**
	 * List nodes that can be added under the given parameter node
	 * (parameters that are allowed under the given node and haven't yet
	 * been defined).
	 * 
	 * @param node parameter node to list parameters under
	 */
	@POST
	@Path("{id}/optional")
	List<Parameter> getAvailableParameters(ParameterNode node);

	/**
	 * Optional params for a configuration instance.
	 * 
	 * @param instanceId
	 * @param node
	 * @return
	 */
	@POST
	@Path("/usergroup/instance/{instanceid}/optional")
	List<Parameter> getAvailableInstanceParameters(@PathParam("instanceid") UUID instanceId, ParameterNode node);

}