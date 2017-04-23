package com.aksh.titan.dto;

import java.util.List;

public class PropertyIndexVO {
	private String name;
	private boolean unique;
	private boolean composite;
	private List<String> propertyKeys;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isUnique() {
		return unique;
	}
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	public boolean isComposite() {
		return composite;
	}
	public void setComposite(boolean composite) {
		this.composite = composite;
	}
	public List<String> getPropertyKeys() {
		return propertyKeys;
	}
	public void setPropertyKeys(List<String> propertyKeys) {
		this.propertyKeys = propertyKeys;
	}
	
	
	

}
