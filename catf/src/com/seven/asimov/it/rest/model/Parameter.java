package com.seven.asimov.it.rest.model;

import java.util.*;

public class Parameter {

	private UUID id;

	private String name = "";

	private String type = "";
	private String doc = "";

	private UUID nodeId;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

	public UUID getNodeId() {
		return nodeId;
	}

	public void setNodeId(UUID nodeId) {
		this.nodeId = nodeId;
	}


	private Map<String, String> metadata = new HashMap<String, String>();

	private List<String> value = new ArrayList();

	private ConfigurationTargetType targetType = ConfigurationTargetType.GLOBAL;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	public ConfigurationTargetType getTargetType() {
		return targetType;
	}

	public void setTargetType(ConfigurationTargetType targetType) {
		this.targetType = targetType;
	}

	@Override
	public String toString() {
		return "Parameter [id=" + id + ", name=" + name + ", type=" + type
				+ ", doc=" + doc + ", nodeId=" + nodeId + ", metadata="
				+ metadata + ", value=" + value + ", targetType=" + targetType
				+ "]";
	}

	// GEN-FIRST

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((doc == null) ? 0 : doc.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
		result = prime * result
				+ ((targetType == null) ? 0 : targetType.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
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
		Parameter other = (Parameter) obj;
		if (doc == null) {
			if (other.doc != null) {
				return false;
			}
		} else if (!doc.equals(other.doc)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (metadata == null) {
			if (other.metadata != null) {
				return false;
			}
		} else if (!metadata.equals(other.metadata)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (nodeId == null) {
			if (other.nodeId != null) {
				return false;
			}
		} else if (!nodeId.equals(other.nodeId)) {
			return false;
		}
		if (targetType != other.targetType) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	// GEN-LAST

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

}
