package com.seven.asimov.it.utils.pcf;

import android.util.Log;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.seven.asimov.it.utils.pcf.PcfHelper.Status;

public class Change {
    private static final String TAG = UserGroup.class.getSimpleName();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PcfHelper.Type getTargetType() {
        return targetType;
    }

    public void setTargetType(PcfHelper.Type targetType) {
        this.targetType = targetType;
    }

    public String getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(String extraProperties) {
        this.extraProperties = extraProperties;
    }

    public String getDeliveryPriority() {
        return deliveryPriority;
    }

    public void setDeliveryPriority(String deliveryPriority) {
        this.deliveryPriority = deliveryPriority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    private  String description;
    private PcfHelper.Type targetType;
    private  String extraProperties;
    private  String deliveryPriority;
    private String id;
    private PcfHelper.Status status;

    public Change(String targetId, Status status, String description, PcfHelper.Type targetType, String extraProperties, String deliveryPriority) {
        this.id=targetId;
        this.status=status;
        this.description=description;
        this.targetType=targetType;
        this.extraProperties=extraProperties;
        this.deliveryPriority =deliveryPriority;
    }


    public static Change toChangeFromJson(String json) {
        try {
            JSONObject change = new JSONObject(json);

            String changeType = change.getString("changeType");
            String description = change.getString("description");
            String targetType = change.getString("targetType");
            String targetId = change.getString("targetId");
            String extraProperties = change.getString("extraProperties");
            String deliveryPriority = change.getString("deliveryPriority");


            return new Change(targetId,PcfHelper.Status.getEnum(changeType),description,PcfHelper.Type.getEnum(targetType),extraProperties,deliveryPriority);
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create Change from json");
        //TODO should be custom exception
        return null;
    }

    public static List<Change> toListOfChanges(String json) {
        List<Change> listOfChanges = new ArrayList<Change>();
        try {
            JSONArray changes = new JSONArray(json);
            for (int i = 0; i < changes.length(); i++) {
                listOfChanges.add(Change.toChangeFromJson(changes.getString(i).toString()));
            }
            return listOfChanges;
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create Changes from json");
        //TODO should be custom exception
        return null;
    }


    public JSONObject toJson() {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("changeType", status.toString());
            resultJson.put("description", description);
            resultJson.put("targetType", targetType);
            resultJson.put("targetId", id);
            resultJson.put("extraProperties", extraProperties);
            resultJson.put("deliveryPriority", deliveryPriority);
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        return resultJson;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (getClass() != obj.getClass()) return false;

        Change other = (Change) obj;

        return new EqualsBuilder()
                .append(id, other.getStatus())
                .append(id, other.getId())
                .append(description, other.getDescription())
                .append(status, other.getDeliveryPriority())
                .append(extraProperties, other.getExtraProperties())
                .append(targetType, other.getTargetType()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(status)
                .append(id)
                .append(description)
                .append(deliveryPriority)
                .append(extraProperties)
                .append(targetType).toHashCode();
    }





}
