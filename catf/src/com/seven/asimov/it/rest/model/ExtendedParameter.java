package com.seven.asimov.it.rest.model;

import org.jtransfo.NotMapped;

public class ExtendedParameter extends Parameter  {

	/**
	 * 
	 */
	@NotMapped
	private static final long serialVersionUID = 1428376712553978166L;
	// These attributes are for the UI to figure out whether the global
	// parameter has been overridden

	@NotMapped
	private boolean hasDeviceOverride = false;

	@NotMapped
	private boolean hasUsergroupOverride = false;

	@NotMapped
	private boolean hasDefault = false;

	public boolean hasDeviceOverride() {
		return hasDeviceOverride;
	}

	public void setHasDeviceOverride(boolean hasDeviceOverride) {
		this.hasDeviceOverride = hasDeviceOverride;
	}

	public boolean hasUsergroupOverride() {
		return hasUsergroupOverride;
	}

	public void setHasUsergroupOverride(boolean hasUsergroupOverride) {
		this.hasUsergroupOverride = hasUsergroupOverride;
	}

	public boolean hasDefault() {
		return hasDefault;
	}

	public void setHasDefault(boolean hasDefaultOverride) {
		this.hasDefault = hasDefaultOverride;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (hasDefault ? 1231 : 1237);
		result = prime * result + (hasDeviceOverride ? 1231 : 1237);
		result = prime * result + (hasUsergroupOverride ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ExtendedParameter other = (ExtendedParameter) obj;
		if (hasDefault != other.hasDefault) {
			return false;
		}
		if (hasDeviceOverride != other.hasDeviceOverride) {
			return false;
		}
		if (hasUsergroupOverride != other.hasUsergroupOverride) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ExtendedParameter [hasDeviceOverride=" + hasDeviceOverride
				+ ", hasUsergroupOverride=" + hasUsergroupOverride
				+ ", hasDefault=" + hasDefault + ", getId()=" + getId()
				+ ", getName()=" + getName() + ", getValue()=" + getValue()
				+ ", getTargetType()=" + getTargetType() + ", toString()="
				+ super.toString() + ", getMetadata()=" + getMetadata()
				+ ", getClass()=" + getClass() + "]";
	}

}
