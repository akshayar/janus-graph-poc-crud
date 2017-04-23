package com.aksh.titan.dto;

import com.thinkaurelius.titan.core.Cardinality;

public class PropertyVO {
	
	private String name;
	private String dataType;
	private Cardinality cardinality=Cardinality.SINGLE;
	public String getName() {
		return name;
	}
	public void setPropertyName(String name) {
		this.name = name;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public Cardinality getCardinality() {
		return cardinality;
	}
	public void setCardinality(Cardinality cardinality) {
		this.cardinality = cardinality;
	}
	public void setName(String name) {
		this.name = name;
	}

	
	
}
