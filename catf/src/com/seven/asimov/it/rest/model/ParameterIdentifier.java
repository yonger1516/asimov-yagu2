package com.seven.asimov.it.rest.model;

import java.util.UUID;



public class ParameterIdentifier  {


	private UUID nodeId;

	private String paramName = "";

	public ParameterIdentifier() {}
	
	public ParameterIdentifier(UUID nodeId, String paramName) {
		this.nodeId = nodeId;
		this.paramName = paramName;
	}
	
    public UUID getNodeId() {
        return nodeId;
    }

    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
        result = prime * result
                + ((paramName == null) ? 0 : paramName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParameterIdentifier other = (ParameterIdentifier) obj;
        if (nodeId == null) {
            if (other.nodeId != null)
                return false;
        } else if (!nodeId.equals(other.nodeId))
            return false;
        if (paramName == null) {
            if (other.paramName != null)
                return false;
        } else if (!paramName.equals(other.paramName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ParameterIdentifier [nodeId=" + nodeId + ", paramName="
                + paramName + "]";
    }

}
