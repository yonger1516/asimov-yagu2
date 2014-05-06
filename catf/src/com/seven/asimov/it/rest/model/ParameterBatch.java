package com.seven.asimov.it.rest.model;

import java.util.List;
import java.util.UUID;


public class ParameterBatch {


	private List<UUID> targetIds;

	private List<Parameter> params;

	public List<UUID> getTargetIds() {
		return targetIds;
	}

	public void setTargetIds(List<UUID> ugids) {
		this.targetIds = ugids;
	}

	public List<Parameter> getParams() {
		return params;
	}

	public void setParameters(List<Parameter> params) {
		this.params = params;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result
				+ ((targetIds == null) ? 0 : targetIds.hashCode());
		return result;
	}

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
		ParameterBatch other = (ParameterBatch) obj;
		if (params == null) {
			if (other.params != null) {
				return false;
			}
		} else if (!params.equals(other.params)) {
			return false;
		}
		if (targetIds == null) {
			if (other.targetIds != null) {
				return false;
			}
		} else if (!targetIds.equals(other.targetIds)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ParameterBatch [targetIds=" + targetIds + ", parameters="
				+ params + "]";
	}

}
