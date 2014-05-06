package com.seven.asimov.it.rest.model;

import org.jtransfo.DomainClass;
import org.jtransfo.NotMapped;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * UserGroup logical groping for users, this makes it easier to target for
 * example policies to multiple users (a single group).
 * 
 * @author vermes
 */
@DomainClass("com.seven.oc.sa.db.model.UserGroup")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserGroup implements Serializable, Generalizable {

	/**
     * 
     */
	@NotMapped
	private static final long serialVersionUID = 3694484030535154714L;
	/**
	 * Id of the group, when creating a group the value here is ignored and a
	 * new unique id is assigned to the group
	 */
	@XmlElement
	private UUID id;
	/** Name of the group, this is used to identify the group in the UI. */
	@XmlElement
	private String name;
	@XmlElement
	private Version version;
	@XmlElement
	private Status status;
	@XmlElement
	private long timestamp;
	@XmlElement
	private String description;
	private Map<String, UUID> params = new HashMap<String, UUID>();
	@NotMapped
	public static final String ALL_USERS_UUID = "2bb71910-dd78-11e2-80f9-c97097820bda";

	public UserGroup() {
		this.name = "";
		this.description = "";
		this.version = Version.SAVED;
		this.status = Status.UPTODATE;
	}

	public Map<String, UUID> getParams() {
		return params;
	}

	public void setParams(Map<String, UUID> params) {
		this.params = params;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	@Override
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// GEN-FIRST

	@Override
	public String toString() {
		return "UserGroup [id=" + id + ", name=" + name + ", version="
				+ version + ", status=" + status + ", timestamp=" + timestamp
				+ ", description=" + description + ", params=" + params + "]";
	}

	// GEN-LAST

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
		UserGroup other = (UserGroup) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (params == null) {
			if (other.params != null) {
				return false;
			}
		} else if (!params.equals(other.params)) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		if (timestamp != other.timestamp) {
			return false;
		}
		if (version != other.version) {
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
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	// GEN-LAST
}
