package com.seven.asimov.it.rest.model;

import java.util.Map;
import java.util.UUID;

public class User {

	private UUID id;

	private String endpointId;

	private String msisdn;

	private String imsi;
	private String imei;

	private String deviceOs;
	private String deviceVendor;

	private String deviceModel;

	private Map<String,UUID> params;

	public User() {

	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public Map<String,UUID> getParams() {
		return params;
	}

	public void setParams(Map<String,UUID> params) {
		this.params = params;
	}

	public String getDeviceOs() {
		return deviceOs;
	}

	public void setDeviceOs(String deviceOs) {
		this.deviceOs = deviceOs;
	}

	public String getDeviceVendor() {
		return deviceVendor;
	}

	public void setDeviceVendor(String deviceVendor) {
		this.deviceVendor = deviceVendor;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getEndpointId() {
		return endpointId;
	}

	public void setEndpointId(String endpointId) {
		this.endpointId = endpointId;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}


    @Override
    public String toString(){
        return "id:"+id+",endpointId:"+endpointId+",msisdn:"+msisdn+",imsi:"+imsi+",imei:"+imei+",deviceOs"+deviceOs+",deviceVendor:"+deviceVendor+",deviceModel:"+deviceModel;
    }
	// GEN-FIRST

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		User other = (User) obj;
		if (deviceModel == null) {
			if (other.deviceModel != null) {
				return false;
			}
		} else if (!deviceModel.equals(other.deviceModel)) {
			return false;
		}
		if (deviceOs == null) {
			if (other.deviceOs != null) {
				return false;
			}
		} else if (!deviceOs.equals(other.deviceOs)) {
			return false;
		}
		if (deviceVendor == null) {
			if (other.deviceVendor != null) {
				return false;
			}
		} else if (!deviceVendor.equals(other.deviceVendor)) {
			return false;
		}
		if (endpointId == null) {
			if (other.endpointId != null) {
				return false;
			}
		} else if (!endpointId.equals(other.endpointId)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (imei == null) {
			if (other.imei != null) {
				return false;
			}
		} else if (!imei.equals(other.imei)) {
			return false;
		}
		if (imsi == null) {
			if (other.imsi != null) {
				return false;
			}
		} else if (!imsi.equals(other.imsi)) {
			return false;
		}
		if (msisdn == null) {
			if (other.msisdn != null) {
				return false;
			}
		} else if (!msisdn.equals(other.msisdn)) {
			return false;
		}
		if (params == null) {
			if (other.params != null) {
				return false;
			}
		} else if (!params.equals(other.params)) {
			return false;
		}
		return true;
	}

	// GEN-LAST

	// GEN-FIRST

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((deviceModel == null) ? 0 : deviceModel.hashCode());
		result = prime * result
				+ ((deviceOs == null) ? 0 : deviceOs.hashCode());
		result = prime * result
				+ ((deviceVendor == null) ? 0 : deviceVendor.hashCode());
		result = prime * result
				+ ((endpointId == null) ? 0 : endpointId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((imei == null) ? 0 : imei.hashCode());
		result = prime * result + ((imsi == null) ? 0 : imsi.hashCode());
		result = prime * result + ((msisdn == null) ? 0 : msisdn.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		return result;
	}

	// GEN-LAST
}
