package com.seven.asimov.it.rest.resource.user;

import com.seven.asimov.it.rest.model.ConfigurationInstance;
import com.seven.asimov.it.rest.model.Endpoint;
import com.seven.asimov.it.rest.model.Parameter;

import javax.ws.rs.*;
import java.util.List;
import java.util.UUID;

@Consumes({ "application/json" })
@Produces({ "application/json" })
public interface UserManagementResource {

    /**
     * Show user that matches the provided query parameters. Note that even if multiple parameters are provided, only one of them will be
     * used for search.
     * <p/>
     * The precedence is as follows:
     * <ol>
     * <li>7tp</li>
     * <li>imsi</li>
     * <li>msisdn</li>
     * </ol>
     *
     * @param msisdn       The users MSISDN
     * @param imsi         The users IMSI
     * @param sevenAddress The users 7tp address.
     * @return A user matching the search criteria.
     */
    @GET
    @Path("/")
    //@StatusCodes({@ResponseCode(code = 404, condition = "If no user is found with the given criteria."), @ResponseCode(code = 400, condition = "If no query parameters are provided.")})
    Endpoint findEndpoint(@DefaultValue("") @QueryParam("msisdn") String msisdn,
                          @DefaultValue("") @QueryParam("imsi") String imsi,
                          @DefaultValue("") @QueryParam("7tp") String sevenAddress,
                          @DefaultValue("") @QueryParam("imei") String imei);

    /**
     * Toggle user to enabled/disabled
     *
     * @param id UUID of the user
     */
    @PUT
    @Path("/{userid}/toggle/enable")
    //@StatusCodes({@ResponseCode(code = 404, condition = "If the specified user does not exist.")})
    Parameter toggleUserEnabled(@PathParam("userid") UUID id);

    /**
     * Toggle transparent parameter on/off for user
     *
     * @param id UUID of the user
     */
    @PUT
    @Path("/{userid}/toggle/transparent")
    //@StatusCodes({@ResponseCode(code = 404, condition = "If the specified user does not exist.")})
    Parameter toggleUserTransparent(@PathParam("userid") UUID id);

    /**
     * Toggle firewall on/off for user
     *
     * @param id UUID of the user
     */
    @PUT
    @Path("/{userid}/toggle/firewall")
    //@StatusCodes({@ResponseCode(code = 404, condition = "If the specified user does not exist.")})
    Parameter toggleUserFirewall(@PathParam("userid") UUID id);

    /**
     * Getting OCF strategy template instances for a given user
     *
     * @param id UUID of the user
     * @return
     */
    @GET
    @Path("/{userid}/strategies/ocf")
    //@StatusCodes({@ResponseCode(code = 404, condition = "If the specified user does not exist.")})
    List<ConfigurationInstance> getOCFStrategyTemplates(@PathParam("userid") UUID id);

    /**
     * Getting the CMS parameters overwritten for the specific user (on user group or user level).
     *
     * @param id UUID of the user
     * @return
     */
    @GET
    @Path("{userid}/params/overwritten")
    //@StatusCodes({@ResponseCode(code = 404, condition = "If the specified user does not exist.")})
    List<Parameter> getUserOrUgOverwrittenParams(@PathParam("userid") UUID id);
}
