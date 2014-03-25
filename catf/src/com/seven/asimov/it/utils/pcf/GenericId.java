package com.seven.asimov.it.utils.pcf;

import android.util.Log;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import static com.seven.asimov.it.utils.pcf.PcfHelper.Status;

public class GenericId {
    private static final String TAG = GenericId.class.getSimpleName();
    private String id;
    private String name;
    private Status status;

    public static GenericId toGenericIdFromJson(String json) {
        try {
            JSONObject genericId = new JSONObject(json);

            String id = genericId.getString("id");
            String name = genericId.getString("name");
            String status = genericId.getString("status");

            return new GenericId(id, name, Status.getEnum(status));
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create GenericId from json");
        //TODO should be custom exception
        return null;
    }

    public GenericId(String id, String name, Status status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public JSONObject toJson() {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("id", id);
            resultJson.put("name", name);
            resultJson.put("status", status);
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        return resultJson;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(name)
                .append(status).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (getClass() != obj.getClass()) return false;

        GenericId other = (GenericId) obj;

        return new EqualsBuilder()
                .append(id, other.getId())
                .append(name, other.getName())
                .append(status, other.getStatus()).isEquals();
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }
}
