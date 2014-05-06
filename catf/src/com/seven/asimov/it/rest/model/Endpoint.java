package com.seven.asimov.it.rest.model;

import org.jtransfo.NotMapped;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class Endpoint {

    @NotMapped
    private static final long serialVersionUID = -89347589345789784L;
    @NotMapped
    private UUID sa_uuid;

    private Long id;

    private int nocId;

    private Date lastConnectionDate;
    private byte[] ekey;

    private byte[] publicKey;
    private List<Short> codecs;

    private Long extraCodecsId;

    private String ip;

    private Integer triggerPort;

    private String triggerPrefix;

    private Integer numOfTriggers = 0;

    private Date lastTriggerSendingTime;

    private Integer triggerDeliveryType = 0;

    private String imei;

    private String msisdn;

    private String os;

    private String vendor;

    private String model;

    private String imsi;

    private String osVersion;

    private String provisioningId;

    private String brandId;

    private String version;

    private String locale;

    private boolean msisdnValidated;

    private String deviceGroup;

    private String deviceUid;

    private String description;

    private boolean clumpingEnabled;

    private Date created;

    private Date modified;

    private String schemaVersion;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Endpoint)) return false;

        Endpoint endpoint = (Endpoint) o;

        if (clumpingEnabled != endpoint.clumpingEnabled) return false;
        if (msisdnValidated != endpoint.msisdnValidated) return false;
        if (nocId != endpoint.nocId) return false;
        if (!sa_uuid.equals(endpoint.equals(sa_uuid))) return false;
        if (brandId != null ? !brandId.equals(endpoint.brandId) : endpoint.brandId != null) return false;
        if (codecs != null ? !codecs.equals(endpoint.codecs) : endpoint.codecs != null) return false;
        if (created != null ? !created.equals(endpoint.created) : endpoint.created != null) return false;
        if (description != null ? !description.equals(endpoint.description) : endpoint.description != null)
            return false;
        if (deviceGroup != null ? !deviceGroup.equals(endpoint.deviceGroup) : endpoint.deviceGroup != null)
            return false;
        if (deviceUid != null ? !deviceUid.equals(endpoint.deviceUid) : endpoint.deviceUid != null) return false;
        if (!Arrays.equals(ekey, endpoint.ekey)) return false;
        if (extraCodecsId != null ? !extraCodecsId.equals(endpoint.extraCodecsId) : endpoint.extraCodecsId != null)
            return false;
        if (!id.equals(endpoint.id)) return false;
        if (imei != null ? !imei.equals(endpoint.imei) : endpoint.imei != null) return false;
        if (imsi != null ? !imsi.equals(endpoint.imsi) : endpoint.imsi != null) return false;
        if (ip != null ? !ip.equals(endpoint.ip) : endpoint.ip != null) return false;
        if (lastConnectionDate != null ? !lastConnectionDate.equals(endpoint.lastConnectionDate) : endpoint.lastConnectionDate != null)
            return false;
        if (lastTriggerSendingTime != null ? !lastTriggerSendingTime.equals(endpoint.lastTriggerSendingTime) : endpoint.lastTriggerSendingTime != null)
            return false;
        if (locale != null ? !locale.equals(endpoint.locale) : endpoint.locale != null) return false;
        if (model != null ? !model.equals(endpoint.model) : endpoint.model != null) return false;
        if (modified != null ? !modified.equals(endpoint.modified) : endpoint.modified != null) return false;
        if (msisdn != null ? !msisdn.equals(endpoint.msisdn) : endpoint.msisdn != null) return false;
        if (numOfTriggers != null ? !numOfTriggers.equals(endpoint.numOfTriggers) : endpoint.numOfTriggers != null)
            return false;
        if (os != null ? !os.equals(endpoint.os) : endpoint.os != null) return false;
        if (osVersion != null ? !osVersion.equals(endpoint.osVersion) : endpoint.osVersion != null) return false;
        if (provisioningId != null ? !provisioningId.equals(endpoint.provisioningId) : endpoint.provisioningId != null)
            return false;
        if (!Arrays.equals(publicKey, endpoint.publicKey)) return false;
        if (schemaVersion != null ? !schemaVersion.equals(endpoint.schemaVersion) : endpoint.schemaVersion != null)
            return false;
        if (triggerDeliveryType != null ? !triggerDeliveryType.equals(endpoint.triggerDeliveryType) : endpoint.triggerDeliveryType != null)
            return false;
        if (triggerPort != null ? !triggerPort.equals(endpoint.triggerPort) : endpoint.triggerPort != null)
            return false;
        if (triggerPrefix != null ? !triggerPrefix.equals(endpoint.triggerPrefix) : endpoint.triggerPrefix != null)
            return false;
        if (vendor != null ? !vendor.equals(endpoint.vendor) : endpoint.vendor != null) return false;
        if (version != null ? !version.equals(endpoint.version) : endpoint.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + nocId;
        result = 31 * result + sa_uuid.hashCode();
        result = 31 * result + (lastConnectionDate != null ? lastConnectionDate.hashCode() : 0);
        result = 31 * result + (ekey != null ? Arrays.hashCode(ekey) : 0);
        result = 31 * result + (publicKey != null ? Arrays.hashCode(publicKey) : 0);
        result = 31 * result + (codecs != null ? codecs.hashCode() : 0);
        result = 31 * result + (extraCodecsId != null ? extraCodecsId.hashCode() : 0);
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (triggerPort != null ? triggerPort.hashCode() : 0);
        result = 31 * result + (triggerPrefix != null ? triggerPrefix.hashCode() : 0);
        result = 31 * result + (numOfTriggers != null ? numOfTriggers.hashCode() : 0);
        result = 31 * result + (lastTriggerSendingTime != null ? lastTriggerSendingTime.hashCode() : 0);
        result = 31 * result + (triggerDeliveryType != null ? triggerDeliveryType.hashCode() : 0);
        result = 31 * result + (imei != null ? imei.hashCode() : 0);
        result = 31 * result + (msisdn != null ? msisdn.hashCode() : 0);
        result = 31 * result + (os != null ? os.hashCode() : 0);
        result = 31 * result + (vendor != null ? vendor.hashCode() : 0);
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (imsi != null ? imsi.hashCode() : 0);
        result = 31 * result + (osVersion != null ? osVersion.hashCode() : 0);
        result = 31 * result + (provisioningId != null ? provisioningId.hashCode() : 0);
        result = 31 * result + (brandId != null ? brandId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (msisdnValidated ? 1 : 0);
        result = 31 * result + (deviceGroup != null ? deviceGroup.hashCode() : 0);
        result = 31 * result + (deviceUid != null ? deviceUid.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (clumpingEnabled ? 1 : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (modified != null ? modified.hashCode() : 0);
        result = 31 * result + (schemaVersion != null ? schemaVersion.hashCode() : 0);
        return result;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNocId() {
        return nocId;
    }

    public void setNocId(int nocId) {
        this.nocId = nocId;
    }

    public UUID getSa_uuid() {
        return sa_uuid;
    }

    public void setSa_uuid(UUID sa_uuid) {
        this.sa_uuid = sa_uuid;
    }
    
    public Date getLastConnectionDate() {
        return lastConnectionDate;
    }

    public void setLastConnectionDate(Date lastConnectionDate) {
        this.lastConnectionDate = lastConnectionDate;
    }

    public byte[] getEkey() {
        return ekey;
    }

    public void setEkey(byte[] ekey) {
        this.ekey = ekey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public List<Short> getCodecs() {
        return codecs;
    }

    public void setCodecs(List<Short> codecs) {
        this.codecs = codecs;
    }

    public Long getExtraCodecsId() {
        return extraCodecsId;
    }

    public void setExtraCodecsId(Long extraCodecsId) {
        this.extraCodecsId = extraCodecsId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getTriggerPort() {
        return triggerPort;
    }

    public void setTriggerPort(Integer triggerPort) {
        this.triggerPort = triggerPort;
    }

    public String getTriggerPrefix() {
        return triggerPrefix;
    }

    public void setTriggerPrefix(String triggerPrefix) {
        this.triggerPrefix = triggerPrefix;
    }

    public Integer getNumOfTriggers() {
        return numOfTriggers;
    }

    public void setNumOfTriggers(Integer numOfTriggers) {
        this.numOfTriggers = numOfTriggers;
    }

    public Date getLastTriggerSendingTime() {
        return lastTriggerSendingTime;
    }

    public void setLastTriggerSendingTime(Date lastTriggerSendingTime) {
        this.lastTriggerSendingTime = lastTriggerSendingTime;
    }

    public Integer getTriggerDeliveryType() {
        return triggerDeliveryType;
    }

    public void setTriggerDeliveryType(Integer triggerDeliveryType) {
        this.triggerDeliveryType = triggerDeliveryType;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getProvisioningId() {
        return provisioningId;
    }

    public void setProvisioningId(String provisioningId) {
        this.provisioningId = provisioningId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isMsisdnValidated() {
        return msisdnValidated;
    }

    public void setMsisdnValidated(boolean msisdnValidated) {
        this.msisdnValidated = msisdnValidated;
    }

    public String getDeviceGroup() {
        return deviceGroup;
    }

    public void setDeviceGroup(String deviceGroup) {
        this.deviceGroup = deviceGroup;
    }

    public String getDeviceUid() {
        return deviceUid;
    }

    public void setDeviceUid(String deviceUid) {
        this.deviceUid = deviceUid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isClumpingEnabled() {
        return clumpingEnabled;
    }

    public void setClumpingEnabled(boolean clumpingEnabled) {
        this.clumpingEnabled = clumpingEnabled;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }


    @Override
    public String toString() {
        return "Endpoint{" +
                "id=" + id +
                ", nocId=" + nocId +
                ", lastConnectionDate=" + lastConnectionDate +
                ", ekey=" + Arrays.toString(ekey) +
                ", publicKey=" + Arrays.toString(publicKey) +
                ", codecs=" + codecs +
                ", extraCodecsId=" + extraCodecsId +
                ", ip='" + ip + '\'' +
                ", triggerPort=" + triggerPort +
                ", triggerPrefix='" + triggerPrefix + '\'' +
                ", numOfTriggers=" + numOfTriggers +
                ", lastTriggerSendingTime=" + lastTriggerSendingTime +
                ", triggerDeliveryType=" + triggerDeliveryType +
                ", imei='" + imei + '\'' +
                ", msisdn='" + msisdn + '\'' +
                ", os='" + os + '\'' +
                ", vendor='" + vendor + '\'' +
                ", model='" + model + '\'' +
                ", imsi='" + imsi + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", provisioningId='" + provisioningId + '\'' +
                ", brandId='" + brandId + '\'' +
                ", version='" + version + '\'' +
                ", locale='" + locale + '\'' +
                ", msisdnValidated=" + msisdnValidated +
                ", deviceGroup='" + deviceGroup + '\'' +
                ", deviceUid='" + deviceUid + '\'' +
                ", description='" + description + '\'' +
                ", clumpingEnabled=" + clumpingEnabled +
                ", created=" + created +
                ", modified=" + modified +
                ", schemaVersion='" + schemaVersion + '\'' +
                '}';
    }

}

