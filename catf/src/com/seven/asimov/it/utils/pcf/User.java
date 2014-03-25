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

public class User {
    private static final String TAG = User.class.getSimpleName();
    private String id;
    private String endpointId;
    private String msisdn;
    private String imsi;


    public static User toUserFromJson(String json) {
        try {
            JSONObject user = new JSONObject(json);

            String id = user.getString("id");
            String endpointId = user.getString("endpointId");
            String msisdn = user.getString("msisdn");
            String imsi = user.getString("imsi");
            return new User(id, endpointId, msisdn, imsi);
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create User from json");
        //TODO should be custom exception
        return null;
    }

    public static List<User> toListOfUsers(String json) {
        List<User> listOfUsers = new ArrayList<User>();
        try {
            JSONArray users = new JSONArray(json);
            for (int i = 0; i < users.length(); i++) {
                listOfUsers.add(User.toUserFromJson(users.getString(i)));
            }
            return listOfUsers;
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create Users from json");
        //TODO should be custom exception
        return null;
    }


    public User(String id, String endpointId, String msisdn, String imsi) {
        this.id = id;
        this.endpointId = endpointId;
        this.msisdn = msisdn;
        this.imsi = imsi;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public JSONObject toJson() {

        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("id", id);
            resultJson.put("endpointId", endpointId);
            resultJson.put("msisdn", msisdn);
            resultJson.put("imsi", imsi);
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        return resultJson;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return new EqualsBuilder()
                .append(id, user.getId())
                .append(endpointId, user.getEndpointId())
                .append(imsi, user.getImsi())
                .append(msisdn, user.getImsi()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(endpointId)
                .append(msisdn)
                .append(imsi).toHashCode();
    }

    public String getId() {
        return id;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public String getImsi() {
        return imsi;
    }
}
