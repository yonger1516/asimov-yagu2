package com.seven.asimov.it.rest.resource.user;

import com.seven.asimov.it.rest.model.User;
import com.seven.asimov.it.rest.model.UserGroup;

import javax.ws.rs.*;
import java.util.List;
import java.util.UUID;

@Consumes({ "application/json" })
@Produces({ "application/json" })
public interface UserResource {

    /**
     * Show user that matches the provided query parameters. Note that even if multiple parameters are provided, only one of them will be
     * used for search.
     *
     * The precedence is as follows:
     * <ol>
     *     <li>7tp</li>
     *     <li>imsi</li>
     *     <li>msisdn</li>
     * </ol>
     *
     *
     * @param msisdn The users MSISDN
     * @param imsi The users IMSI
     * @param sevenAddress The users 7tp address.
     *
     * @return A user matching the search criteria.
     */
    @GET
    @Path("/")
    //@StatusCodes({@ResponseCode( code = 404, condition = "If no user is found with the given criteria."), @ResponseCode( code = 400, condition = "If no query parameters are provided.")})
    public User findUser(@DefaultValue("") @QueryParam("msisdn") String msisdn,
                         @DefaultValue("") @QueryParam("imsi") String imsi,
                         @DefaultValue("") @QueryParam("7tp") String sevenAddress,
                         @DefaultValue("") @QueryParam("imei") String imei);

    /**
     * Deletes a single user. The associated restrictions and user group memberships will be removed.
     *
     * @param id the ID of the user to delete
     */
    @DELETE
    @Path("/{userid}")
    void deleteUser(@PathParam("userid") UUID id);

    /**
     * Shows a single user.
     *
     * @param id the ID of the user to show
     *
     */
    @GET
    @Path("/{userid}")
   // @StatusCodes({@ResponseCode( code = 404, condition = "If no user is found.")})
    User getUser(@PathParam("userid") UUID id);

    /**
     * List the <code>Usergroup</code>s the user belongs to.
     *
     * @param id UUID of the user
     * @return A list of all the <code>Usergroup</code>s the specified user belongs to.
     */
    @GET
    @Path("/{userId}/groups")
    //@StatusCodes({@ResponseCode( code = 404, condition = "If the specified user does not exist.")})
    List<UserGroup> listGroupsForUser(@PathParam("userId") UUID id);

}
