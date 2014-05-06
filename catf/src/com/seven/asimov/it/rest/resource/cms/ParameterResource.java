package com.seven.asimov.it.rest.resource.cms;

import com.seven.asimov.it.rest.model.*;

import javax.ws.rs.*;
import java.util.List;
import java.util.UUID;

@Consumes({ "application/json" })
@Produces({ "application/json" })
public interface ParameterResource {

	/**
	 * Importing a list of parameters.
	 * 
	 * Example format:
	 * <pre>
	 * {@code
	 * [
	{
		"name":"Cookie rules",
		"path":"asimov@normalization@cookie@*@prod4.rest-core.msg.yahoo.com@.*@cookie_rules",
		"metadata":{
			"type":"list",
			"description":"",
			"unit":"none",
			"displayedValues":"",
			"listValType":"string",
			"listValNum":"1",
			"integerMax":"",
			"integerMin":""
		},
		"value":[
			".*"
		],
		"targetType":"GLOBAL"
	},
	{
		"name":"Patterns",
		"path":"asimov@normalization@uri@*@prod3.rest-notify.msg.yahoo.com@.*@patterns",
		"metadata":{
			"type":"list",
			"description":"",
			"unit":"none",
			"displayedValues":"",
			"listValType":"string",
			"listValNum":"1",
			"integerMax":"",
			"integerMin":""
		},
		"value":[
			"idle=.*?(&|#|$)"
		],
		"targetType":"GLOBAL"
	}]
	 *}	
	 * </pre>
	 * @param params
	 */
	@POST
	@Path("/import")
	public void importDefaultParams(List<Parameter> params);
	
	/**
	 * Retrieve a list of <code>parameters</code>.
	 *
	 * @param nodeid UUID of the <code>parameter node</code> under which parameters are fetched.
	 * @return <a href="el_ns0_parameter.html">parameter</a>
	 */
	@GET
	@Path("/global")
	public List<ExtendedParameter> getGlobalParameters(@QueryParam("nodeid") UUID nodeid);
	
	@GET
	@Path("/global/raw")
	public List<Parameter> getGlobalRawParameters(@QueryParam("nodeid") UUID nodeid);

	/**
	 * Add or update a global parameter.
	 * 
	 * @param name parameter name
	 * @param nodeid ID of parameter node
	 * @param param new data for the parameter
	 */
	@PUT
	@Path("/global")
	public void setGlobalParameter(@QueryParam("name") String name, @QueryParam("nodeid") UUID nodeid, Parameter param);

	/**
	 * Delete a parameter.
	 * 
	 * @param nodeid UUID of the node of the parameter to delete
	 * @param name name of the node to delete
	 */
	@DELETE
	@Path("/global")
	void deleteGlobalParameter(@QueryParam("nodeid") UUID nodeid, @QueryParam("name") String name);

	/**
	 * Create a batch of parameters and attach them to the given usergroups.
	 * 
	 * @param paramBatch <a href="el_ns0_usergroupParameterBatch.html">a list of user group ids and a list of parameters</a>
	 */
	@POST
	@Path("/batch/usergroup")
	public void createUsergroupParameters(ParameterBatch paramBatch);
	
	/**
	 * Retrieves all usergroup Parameters under a given node id and instance id
	 * 
	 * @param instanceId The configuration instance id
	 * @param nodeid The node id
	 * @return List of <code>NodeParameters</code>
	 */
	@GET
	@Path("/usergroup/instance/{instanceid}")
	public List<NodeParameters> getInstanceUsergroupParameters(@PathParam("instanceid") UUID instanceId, @QueryParam("nodeid") UUID nodeid);

	
	/**
     * Creates a usergroup parameter for a given instance
     * 
     * @param instanceId
     * @param name
     * @param nodeid
     * @param param
     */
	@POST
	@Path("/usergroup/instance/{instanceid}")
	void createInstanceUsergroupParameter(@PathParam("instanceid") UUID instanceId, @QueryParam("name") String name, @QueryParam("nodeid") UUID nodeid, Parameter param);
	
	/**
	 * Updates a usergroup parameter for a given instance
	 * 
	 * @param instanceId
	 * @param name
	 * @param nodeid
	 * @param param
	 */
	@PUT
	@Path("/usergroup/instance/{instanceid}")
	void updateInstanceUsergroupParameter(@PathParam("instanceid") UUID instanceId, @QueryParam("name") String name, @QueryParam("nodeid") UUID nodeid, Parameter param);
	
	/**
     * Delete a usergroup parameter for a given instance.
     * 
     * @param instanceId UUID of the instance to which the parameter belongs
     * @param nodeId UUID of the node of the parameter to delete
     * @param parameterName name of the parameter to delete
     */
    @DELETE
    @Path("/usergroup/instance/{instanceid}")
    void deleteInstanceUsergroupParameter(@PathParam("instanceid") UUID instanceId, @QueryParam("nodeid") UUID nodeId, @QueryParam("name") String parameterName);
	
	/**
	 * If there is a usergroup parameter 
	 * defined at the path, the value of that parameter, otherwise the corresponding 
	 * global parameter if it exists. 
	 * 
	 * @param ugid user group id
	 * @param nodeid node ID under which parameter are retrieved
	 * @return usergroup parameter value at the given path, or if not available, global parameter value at given path
	 */
	@GET
	@Path("/usergroup/{ugid}")
	public List<Parameter> getUsergroupParameters(@PathParam("ugid") UUID ugid, @QueryParam("nodeid") UUID nodeid);
	
	/**
	 * Update a usergroup parameter. In effect create a new parameter and point this usergroup to it, so that
	 * other usergroups can still use the old version of the parameter.
	 * 
	 * @param nodeid node ID of the parameter
	 * @param name name of the parameter
	 * @param ugid user group id
	 * @param param new data for the parameter
	 */
	@PUT
	@Path("/usergroup/{ugid}")
	public void setUsergroupParameter(@QueryParam("nodeid") UUID nodeid, @QueryParam("name") String name,
                                      @PathParam("ugid") UUID ugid, Parameter param);

	/**
	 * Remove parameters from a usergroup. The previous instance of the parameter is not affected,
	 * so that other usergroups can keep using it.
	 * 
	 * @param paramIds <a href="el_ns0_parameterIdentifier.html">a list of names and node ids of the parameters to remove from the usergroup</a>
	 * @param ugid user group id
	 */
	@DELETE
	@Path("/usergroup/{ugid}")
	public void deleteUsergroupParameters(List<ParameterIdentifier> paramIds, @PathParam("ugid") UUID ugid);
	
	/**
	 * Create a batch of parameters and attach them to the given users.
	 * 
	 * @param paramBatch <a href="el_ns0_usergroupParameterBatch.html">a list of user ids and a list of parameters</a>
	 */
	@POST
	@Path("/batch/user")
	public void createUserParameters(
            @DefaultValue("") @QueryParam("imei") String imei,
            @DefaultValue("") @QueryParam("msisdn") String msisdn,
            ParameterBatch paramBatch);

	/**
	 * If there is a user parameter 
	 * defined at the path, the value of that parameter, otherwise the corresponding 
	 * global or default parameter if it exists. Usergroup level parameters are excluded because
	 * the local configuration wizard needs this kind of filtering. 
	 * 
	 * @param nodeid node id of the parameter
	 * @param uid user id
	 * @param includeUsergroups also include user group level parameters
	 * @return user parameters and global and default parameters under the given node
	 */
	@GET
	@Path("/user/{uid}")
	public List<Parameter> getUserParameters(@QueryParam("nodeid") UUID nodeid, @PathParam("uid") UUID uid, @QueryParam("includeusergroups") boolean includeUsergroups);
	
	/**
	 * Remove parameters from a user. The previous instance of the parameter is not affected,
	 * so that other users can keep using it.
	 * 
	 * @param paramIds <a href="el_ns0_parameterIdentifier.html"> list of the names and node ids of the parameters to remove from the usergroup </a>
	 * @param imei/msisdn
	 */
	@DELETE
	@Path("/user/")
	public void deleteUserParameters(List<ParameterIdentifier> paramIds, @DefaultValue("") @QueryParam("imei") String imei,
                                     @DefaultValue("") @QueryParam("msisdn") String msisdn);

}
