package com.aksh.titan.dto;

import java.util.HashMap;
import java.util.Map;

public class Relationship {
	private String id;
	/**
	 * name, weight
	 */
	private String relationshipName;
	private Map<String, Object> properties;
	private Node fromNode;
	private Node toNode;
	private boolean tobeUsedForTagging;
	private boolean isSearchable=true;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getRelationshipName() {
		return relationshipName;
	}
	public void setRelationshipName(String relationshipName) {
		this.relationshipName = relationshipName;
	}
	public Map<String, Object> getProperties() {
		if(properties==null){
			properties=new HashMap<>();
		}
		return properties;
	}
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public boolean isTobeUsedForTagging() {
		return tobeUsedForTagging;
	}
	public void setTobeUsedForTagging(boolean tobeUsedForTagging) {
		this.tobeUsedForTagging = tobeUsedForTagging;
	}
	public Node getToNode() {
		return toNode;
	}
	public void setToNode(Node toNode) {
		this.toNode = toNode;
	}
	
	public void setFromNode(Node fromNode) {
		this.fromNode = fromNode;
	}
	public Node getFromNode() {
		return fromNode;
	}
	public boolean isSearchable() {
		return isSearchable;
	}
	public void setSearchable(boolean isSearchable) {
		this.isSearchable = isSearchable;
	}
	
	
	
	
}
