package com.aksh.titan.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;


public class Node implements Cloneable{
	
	private String uri;
	private long id;
	/**
	 * To be added as labels
	 */
	private String entityType;
	private Map<String, Object> attributes;
	private List<Relationship> outRelations;
	private List<Relationship> inRelations;
	private boolean isSoftDeleted=true;
	private boolean isSearchable=true;

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public boolean isSoftDeleted() {
		return isSoftDeleted;
	}
	public void setSoftDeleted(boolean isSoftDeleted) {
		this.isSoftDeleted = isSoftDeleted;
	}
	public boolean isSearchable() {
		return isSearchable;
	}
	public void setSearchable(boolean isSearchable) {
		this.isSearchable = isSearchable;
	}
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public Map<String, Object> getAttributes() {
		if(attributes==null){
			attributes=new HashMap<>();
		}
		return attributes;
	}
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public List<Relationship> getOutRelations() {
		if(outRelations==null){
			outRelations=new ArrayList<>();
		}
		return outRelations;
	}
	public void setOutRelations(List<Relationship> outRelations) {
		this.outRelations = outRelations;
	}
	public List<Relationship> getInRelations() {
		if(inRelations==null){
			inRelations=new ArrayList<>();
		}
		return inRelations;
	}
	public void setInRelations(List<Relationship> inRelations) {
		this.inRelations = inRelations;
	}
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
