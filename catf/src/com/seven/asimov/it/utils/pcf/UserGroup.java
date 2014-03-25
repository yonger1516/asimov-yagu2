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

import static com.seven.asimov.it.utils.pcf.PcfHelper.Version;
import static com.seven.asimov.it.utils.pcf.PcfHelper.Status;

public class UserGroup {
    private static final String TAG = UserGroup.class.getSimpleName();
    private String id;
    private String name;
    private Version version;
    private Status status;
    private Long timestamp;
    private String description;

    public static UserGroup toUserGroupFromJson(String json) {
        try {
            JSONObject userGroup = new JSONObject(json);

            String id = userGroup.getString("id");
            String name = userGroup.getString("name");
            String version = userGroup.getString("version");
            String status = userGroup.getString("status");
            Long timestamp = userGroup.getLong("timestamp");
            String description = userGroup.getString("description");

            return new UserGroup(id, name, Version.getEnum(version), Status.getEnum(status), timestamp, description);
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create UserGroup from json");
        //TODO should be custom exception
        return null;
    }

    public static List<UserGroup> toListOfUserGroups(String json) {
        List<UserGroup> listOfUserGroups = new ArrayList<UserGroup>();
        try {
            JSONArray userGroups = new JSONArray(json);
            for (int i = 0; i < userGroups.length(); i++) {
                listOfUserGroups.add(UserGroup.toUserGroupFromJson(userGroups.getString(i).toString()));
            }
            return listOfUserGroups;
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create UserGroups from json");
        //TODO should be custom exception
        return null;
    }

    public UserGroup(String id, String name, Version version, Status status, Long timestamp, String description) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.status = status;
        this.timestamp = timestamp;
        this.description = description;
    }

    public JSONObject toJson() {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("id", id);
            resultJson.put("name", name);
            resultJson.put("version", version);
            resultJson.put("status", status);
            resultJson.put("timestamp", timestamp);
            resultJson.put("description", description);
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

        UserGroup other = (UserGroup) obj;

        return new EqualsBuilder()
                .append(id, other.getId())
                .append(name, other.getName())
                .append(version, other.getVersion())
                .append(status, other.getStatus())
                .append(timestamp, other.getTimestamp())
                .append(description, other.getDescription()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(name)
                .append(version)
                .append(status)
                .append(timestamp)
                .append(description).toHashCode();
    }

    public UserGroup(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Version getVersion() {
        return version;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
}
