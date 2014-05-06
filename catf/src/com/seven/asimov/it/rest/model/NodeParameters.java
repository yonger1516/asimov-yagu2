package com.seven.asimov.it.rest.model;

import java.util.List;
import java.util.UUID;

/**
 * Representation of Parameters under a given node id
 * 
 * @author mketonen
 *
 */

public class NodeParameters {


	private UUID id;


	private List<Parameter> parameters;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
}
