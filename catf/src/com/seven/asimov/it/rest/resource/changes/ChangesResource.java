package com.seven.asimov.it.rest.resource.changes;

import com.seven.asimov.it.rest.model.Change;
import com.seven.asimov.it.rest.model.Change.TargetType;
import com.seven.asimov.it.rest.model.DeliveryPriority;
import com.seven.asimov.it.rest.model.HistoryItem;
import com.seven.asimov.it.rest.model.ProvisioningDetails;

import javax.ws.rs.*;
import java.util.List;
import java.util.UUID;


@Consumes({ "application/json" })
@Produces({ "application/json" })
public interface ChangesResource {

    /**
     * List unprovisioned changes.
     *
     * @return List of <a href="el_ns0_change.html">change</a>
     */
    @GET
    @Path("/unprovisioned")
    public List<Change> getUnprovisionedChanges();

    /**
     *  Provision all pending changes.
     *  
     * @param comment user-provided description of the changes for history
     */
    @POST
    @Path("/unprovisioned")
    void provisionChanges(ProvisioningDetails comment);


    @POST
    @Path("/unprovisioned/{targettype}/{targetid}")
    void provisionSingleChange(ProvisioningDetails comment, @PathParam("targettype") TargetType targetType, @PathParam("targetid") UUID id);

    /**
     * Show change history
     *
     * @param pattern
     *            Filter pattern (not yet supported)
     * @param limit
     *            Maximum number of results.
     * @return The change history. A list of <a href="el_ns0_historyItem.html">historyItems</a>
     */
    @GET
    @Path("/history")
    List<HistoryItem> getChangeHistory(@QueryParam("pattern") String pattern,
                                       @QueryParam("limit") int limit);

    /**
     * Cancels an individual <code>Change</code>.
     * 
     * @param change
     *            the change to cancel.
     */
    @POST
    @Path("/cancel")
    void cancel(Change change);

    /**
     * Set the delivery priority of an artifact.
     *
     *
     * @param targetType
     *            Target type {@link TargetType} :
     *            (policy/application/usergroup)
     * @param targetId
     *            UUID of the target
     * @param priority
     *            priority: {@link DeliveryPriority} : (normal, important,
     *            urgent)
     */
    @POST
    @Path("/{type}/{targetid}")
    void setDeliveryPriority(@PathParam("type") TargetType targetType,
                             @PathParam("targetid") UUID targetId,
                             @QueryParam("priority") DeliveryPriority priority);

}
