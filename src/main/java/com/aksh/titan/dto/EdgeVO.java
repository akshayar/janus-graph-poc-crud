package com.aksh.titan.dto;

import com.thinkaurelius.titan.core.Multiplicity;

public class EdgeVO {
	
	private String label;
	private Multiplicity multiplicity;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Multiplicity getMultiplicity() {
		return multiplicity;
	}
	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity = multiplicity;
	}
	

}
