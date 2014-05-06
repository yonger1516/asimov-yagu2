package com.seven.asimov.it.rest.model;

import org.jtransfo.DomainClass;
import org.jtransfo.NotMapped;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@DomainClass("com.seven.oc.sa.db.model.ConfigurationInstance")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigurationInstance implements Serializable{

    @NotMapped
    private static final long serialVersionUID = -656345656345634564L;

    @XmlElement
    private UUID id;
    @XmlElement
    private UUID nodeId;
    @XmlElement
    private Set<UUID> targetIds = new HashSet();
    @XmlElement
    private ConfigurationType type;
    @XmlElement
    private ConfigurationTargetType targetType;
    @XmlElement
    private String templateName;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getNodeId() {
        return nodeId;
    }
    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }
    public Set<UUID> getTargetIds() {
        return targetIds;
    }
    public void setTargetIds(Set<UUID> targetIds) {
        this.targetIds = targetIds;
    }
    public ConfigurationType getType() {
        return type;
    }
    public void setType(ConfigurationType type) {
        this.type = type;
    }
    public ConfigurationTargetType getTargetType() {
        return targetType;
    }
    public void setTargetType(ConfigurationTargetType targetType) {
        this.targetType = targetType;
    }
    public String getTemplateName() {
        return templateName;
    }
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationInstance)) return false;

        ConfigurationInstance that = (ConfigurationInstance) o;

        if (!id.equals(that.id)) return false;
        if (nodeId != null ? !nodeId.equals(that.nodeId) : that.nodeId != null) return false;
        if (targetIds != null ? !targetIds.equals(that.targetIds) : that.targetIds != null) return false;
        if (targetType != that.targetType) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (nodeId != null ? nodeId.hashCode() : 0);
        result = 31 * result + (targetIds != null ? targetIds.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (targetType != null ? targetType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigurationInstance{" +
                "id=" + id +
                ", nodeId=" + nodeId +
                ", targetIds=" + targetIds +
                ", type=" + type +
                ", targetType=" + targetType +
                '}';
    }
}
