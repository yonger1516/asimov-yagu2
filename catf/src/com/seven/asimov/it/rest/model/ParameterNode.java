package com.seven.asimov.it.rest.model;

import org.jtransfo.MappedBy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParameterNode {

	private UUID id;

	private String type = "";

	private String name = "";

	private String doc = "";

	private String namespace = "";

	private boolean isCategory = true;
    private String displayName = "";

    private UUID parentId;

    private List<String> keyIndex = new ArrayList();


    @MappedBy(field = "children", typeConverterClass = ParameterNodeListConverter.class)
	private List<ParameterNode> children = new ArrayList();

	public List<String> getKeyIndex() {
		return keyIndex;
	}

	public void setKeyIndex(List<String> keyIndex) {
		this.keyIndex = keyIndex;
	}

	public String getDoc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
	}

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

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean getIsCategory() {
		return isCategory;
	}

	public void setIsCategory(boolean isCategory) {
		this.isCategory = isCategory;
	}

	public List<ParameterNode> getChildren() {
		return children;
	}

	public void setChildren(List<ParameterNode> children) {
		this.children = children;
	}

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    @Override
	public String toString() {
		return "ParameterNode [id=" + id + ", type=" + type + ", name=" + name
				+ ", doc=" + doc + ", namespace=" + namespace + ", isCategory="
				+ isCategory + ", displayName=" + displayName + ", parentId="
				+ parentId + ", keyIndex=" + keyIndex + ", children="
				+ children + "]";
	}

	// GEN-FIRST

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((children == null) ? 0 : children.hashCode());
		result = prime * result
				+ ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((doc == null) ? 0 : doc.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isCategory ? 1231 : 1237);
		result = prime * result
				+ ((keyIndex == null) ? 0 : keyIndex.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
		result = prime * result
				+ ((parentId == null) ? 0 : parentId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ParameterNode other = (ParameterNode) obj;
		if (children == null) {
			if (other.children != null) {
				return false;
			}
		} else if (!children.equals(other.children)) {
			return false;
		}
		if (displayName == null) {
			if (other.displayName != null) {
				return false;
			}
		} else if (!displayName.equals(other.displayName)) {
			return false;
		}
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
		if (isCategory != other.isCategory) {
			return false;
		}
		if (keyIndex == null) {
			if (other.keyIndex != null) {
				return false;
			}
		} else if (!keyIndex.equals(other.keyIndex)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (namespace == null) {
			if (other.namespace != null) {
				return false;
			}
		} else if (!namespace.equals(other.namespace)) {
			return false;
		}
		if (parentId == null) {
			if (other.parentId != null) {
				return false;
			}
		} else if (!parentId.equals(other.parentId)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	// GEN-LAST

}
