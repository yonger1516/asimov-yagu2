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
import static com.seven.asimov.it.utils.pcf.PcfHelper.Type;

public class Service {
    private static final String TAG = Service.class.getSimpleName();
    private String id;
    private String name;
    private List<Restriction> restrictions;
    private String packageName;
    private String category;
    private Version version;
    private Type type;
    private List<String> tags;
    private String description;
    private Long timestamp;
    private Status status;

    public static Service toServiceFromJson(String json) {
        try {
            JSONObject service = new JSONObject(json);

            String id = service.getString("id");
            String name = service.getString("name");
            JSONArray restrictions = service.getJSONArray("restrictions");
            ArrayList<Restriction> listOfRestrictions = new ArrayList<Restriction>();
            for (int i = 0; i < restrictions.length(); i++) {
                listOfRestrictions.add(Restriction.toRestrictionFromJson(restrictions.getString(i)));
            }
            String packageName = service.getString("packageName");
            String category = service.getString("category");
            String version = service.getString("version");
            String type = service.getString("type");
            JSONArray tags = service.getJSONArray("tags");
            ArrayList<String> listOfTags = new ArrayList<String>();
            for (int i = 0; i < tags.length(); i++) {
                listOfTags.add(tags.getString(i));
            }
            String description = service.getString("description");
            Long timestamp = service.getLong("timestamp");
            String status = service.getString("status");

            return new Service(id, name, listOfRestrictions, packageName, category, Version.getEnum(version), Type.getEnum(type), listOfTags, description, timestamp, Status.getEnum(status));
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create Service from json");
        //TODO should be custom exception
        return null;
    }

    public static List<Service> toListOfServices(String json) {
        List<Service> listOfServices = new ArrayList<Service>();
        try {
            JSONArray services = new JSONArray(json);
            for (int i = 0; i < services.length(); i++) {
                listOfServices.add(Service.toServiceFromJson(services.getString(i).toString()));
            }
            return listOfServices;
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create Services from json");
        //TODO should be custom exception
        return null;
    }

    public Service(String name) {
        this.name = name;
    }

    public Service(String name, String packageName, String category, Type type, Version version, String description) {
        this.name = name;
        this.packageName = packageName;
        this.category = category;
        this.type = type;
        this.version = version;
        this.description = description;
    }

    public Service(String id, String name, List<Restriction> restrictions, String packageName, String category, Version version, Type type, List<String> tags, String description, Long timestamp, Status status) {
        this.id = id;
        this.name = name;
        this.restrictions = restrictions;
        this.packageName = packageName;
        this.category = category;
        this.version = version;
        this.type = type;
        this.tags = tags;
        this.description = description;
        this.timestamp = timestamp;
        this.status = status;
    }

    public JSONObject toJson() {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("id", id);
            resultJson.put("name", name);
            if (restrictions != null) {
                JSONArray jsonRestrictions = new JSONArray();
                for (Restriction r : restrictions) {
                    jsonRestrictions.put(r.toJson());
                }
                resultJson.put("restrictions", jsonRestrictions);
            }
            resultJson.put("packageName", packageName);
            resultJson.put("category", category);
            resultJson.put("version", version);
            resultJson.put("type", type);
            if (tags != null) {
                JSONArray jTags = new JSONArray();
                for (String s : tags) {
                    jTags.put(s);
                }
                resultJson.put("tags", jTags);
            }
            resultJson.put("description", description);
            resultJson.put("timestamp", timestamp);
            resultJson.put("status", status);
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        return resultJson;
    }

    public GenericId toGenericId() {
        return new GenericId(id, name, status);
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

        Service other = (Service) obj;

        return new EqualsBuilder()
                .append(id, other.getId())
                .append(name, other.getName())
                .append(restrictions, other.getRestrictions())
                .append(packageName, other.getPackageName())
                .append(category, other.getCategory())
                .append(version, other.getVersion())
                .append(type, other.getType())
                .append(tags, other.getTags())
                .append(description, other.getDescription())
                .append(timestamp, other.getTimestamp())
                .append(status, other.getStatus()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(name)
                .append(restrictions)
                .append(packageName)
                .append(category)
                .append(version)
                .append(type)
                .append(tags)
                .append(description)
                .append(timestamp)
                .append(status).toHashCode();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Restriction> getRestrictions() {
        return restrictions;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getCategory() {
        return category;
    }

    public Version getVersion() {
        return version;
    }

    public Type getType() {
        return type;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Status getStatus() {
        return status;
    }
}
