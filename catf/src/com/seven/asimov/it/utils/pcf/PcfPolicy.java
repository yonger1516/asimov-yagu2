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

import static com.seven.asimov.it.utils.pcf.PcfHelper.*;

public class PcfPolicy {
    private static final String TAG = PcfPolicy.class.getSimpleName();
    private Boolean removed;
    private String id;
    private String name;
    private Version version;
    private List<String> tags;
    private String category;
    private Trigger trigger;
    private Long timestamp;
    private List<String> whitelistedPackages;
    private Status status;
    private InterfaceType networkInterface;
    private Type type;
    private Boolean active;

    public static PcfPolicy toPcfPolicyFromJson(String json) {
        try {
            JSONObject pcfPolicy = new JSONObject(json);

            Boolean removed = pcfPolicy.getBoolean("removed");
            String id = pcfPolicy.getString("id");
            String name = pcfPolicy.getString("name");
            String version = pcfPolicy.getString("version");
            JSONArray tags = pcfPolicy.getJSONArray("tags");
            ArrayList<String> listOfTags = new ArrayList<String>();
            for (int i = 0; i < tags.length(); i++) {
                listOfTags.add(tags.getString(i));
            }
            String category = pcfPolicy.getString("category");
            JSONObject trigger = pcfPolicy.getJSONObject("trigger");
            Trigger triggerToSet = Trigger.toTriggerFromJson(trigger.toString());
            Long timestamp = pcfPolicy.getLong("timestamp");
            JSONArray whitelistedPackages = pcfPolicy.getJSONArray("whitelistedPackages");
            ArrayList<String> listOfPackages = new ArrayList<String>();
            for (int i = 0; i < whitelistedPackages.length(); i++) {
                listOfPackages.add(whitelistedPackages.getString(i));
            }
            String status = pcfPolicy.getString("status");
            String networkInterface = pcfPolicy.getString("networkInterface");
            String type = pcfPolicy.getString("type");
            Boolean active = pcfPolicy.getBoolean("active");

            return new PcfPolicy(removed, id, name, Version.getEnum(version), listOfTags, category, triggerToSet, timestamp, listOfPackages, Status.getEnum(status), InterfaceType.getEnum(networkInterface), Type.getEnum(type), active);
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create PcfPolicy from json");
        //TODO should be custom exception
        return null;
    }

    public static List<PcfPolicy> toListOfPcfPolicies(String json) {
        List<PcfPolicy> listOfPcfPolicies = new ArrayList<PcfPolicy>();
        try {
            JSONArray PcfPolicies = new JSONArray(json);
            for (int i = 0; i < PcfPolicies.length(); i++) {
                listOfPcfPolicies.add(PcfPolicy.toPcfPolicyFromJson(PcfPolicies.getString(i).toString()));
            }
            return listOfPcfPolicies;
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create PcfPolicies from json");
        //TODO should be custom exception
        return null;
    }

    public PcfPolicy(String name) {
        this.name = name;
    }

    public PcfPolicy(String name, Version version, String category, Trigger trigger, List<String> whitelistedPackages, Status status, InterfaceType networkInterface, Type type, Boolean active) {
        this.name = name;
        this.version = version;
        this.category = category;
        this.trigger = trigger;
        this.whitelistedPackages = whitelistedPackages;
        this.status = status;
        this.networkInterface = networkInterface;
        this.type = type;
        this.active = active;
    }

    public PcfPolicy(Boolean removed, String id, String name, Version version, List<String> tags, String category, Trigger trigger, Long timestamp, List<String> whitelistedPackages, Status status, InterfaceType networkInterface, Type type, Boolean active) {
        this.removed = removed;
        this.id = id;
        this.name = name;
        this.version = version;
        this.tags = tags;
        this.category = category;
        this.trigger = trigger;
        this.timestamp = timestamp;
        this.whitelistedPackages = whitelistedPackages;
        this.status = status;
        this.networkInterface = networkInterface;
        this.type = type;
        this.active = active;
    }

    public JSONObject toJson() {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("removed", removed);
            resultJson.put("id", id);
            resultJson.put("name", name);
            resultJson.put("version", version);
            if (tags != null) {
                JSONArray jTags = new JSONArray();
                for (String s : tags) {
                    jTags.put(s);
                }
                resultJson.put("tags", jTags);
            }
            resultJson.put("category", category);
            if (trigger != null) resultJson.put("trigger", trigger.toJson());
            resultJson.put("timestamp", timestamp);
            if (whitelistedPackages != null) {
                JSONArray jPackages = new JSONArray();
                for (String s : whitelistedPackages) {
                    jPackages.put(s);
                }
                resultJson.put("whitelistedPackages", jPackages);
            }
            resultJson.put("status", status);
            resultJson.put("networkInterface", networkInterface);
            resultJson.put("type", type);
            resultJson.put("active", active);
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

        PcfPolicy other = (PcfPolicy) obj;

        return new EqualsBuilder()
                .append(removed, other.isRemoved())
                .append(id, other.getId())
                .append(name, other.getName())
                .append(tags, other.getTags())
                .append(category, other.getCategory())
                .append(trigger, other.getTrigger())
                .append(timestamp, other.getTimestamp())
                .append(whitelistedPackages, other.getWhitelistedPackages())
                .append(status, other.getStatus())
                .append(networkInterface, other.getNetworkInterface())
                .append(type, other.getType())
                .append(active, other.isActive()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(removed)
                .append(id)
                .append(tags)
                .append(category)
                .append(trigger)
                .append(timestamp)
                .append(whitelistedPackages)
                .append(status)
                .append(networkInterface)
                .append(type)
                .append(active).toHashCode();
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean isRemoved() {
        return removed;
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

    public List<String> getTags() {
        return tags;
    }

    public String getCategory() {
        return category;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public List<String> getWhitelistedPackages() {
        return whitelistedPackages;
    }

    public Status getStatus() {
        return status;
    }

    public InterfaceType getNetworkInterface() {
        return networkInterface;
    }

    public Type getType() {
        return type;
    }

    public Boolean isActive() {
        return active;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public void setNetworkInterface(InterfaceType networkInterface) {
        this.networkInterface = networkInterface;
    }
}
