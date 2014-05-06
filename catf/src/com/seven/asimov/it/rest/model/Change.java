package com.seven.asimov.it.rest.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class Change {


    public enum TargetType {
        policy, usergroup, service,application, membership, globalconfig, none, category, defaultconfig,deviceconfig, parameternode, configurationinstance
    }

    public Change(String description) {
        this();
        this.description = description;
    }


    private ChangeType changeType;

    private String description;

    private TargetType targetType;

    private UUID targetId;
    private Map<String, Object> extraProperties;

    private DeliveryPriority deliveryPriority;

    public Change() {
        this.extraProperties = new HashMap();
        this.description = "";
        this.setDeliveryPriority(DeliveryPriority.normal);
    }

    public DeliveryPriority getDeliveryPriority() {
        return deliveryPriority;
    }

    public void setDeliveryPriority(DeliveryPriority deliveryPriority) {
        this.deliveryPriority = deliveryPriority;
    }

    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties = extraProperties;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

}
