package com.aksh.titan.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix="schema")
@Component
public class Schema {
	
	private List<String> labels;
	private List<PropertyVO> properties;
	private List<EdgeVO> edges;
	private List<EdgeIndexVO> edgeIndexes;
	private List<PropertyIndexVO> propertyIndexes;
	
	public List<String> getLabels() {
		if(labels==null){
			labels=new ArrayList<>();
		}
		return labels;
	}
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
	public List<EdgeVO> getEdges() {
		if(edges==null){
			edges=new ArrayList<>();
		}
		return edges;
	}
	public void setEdges(List<EdgeVO> edges) {
		this.edges = edges;
	}
	public List<PropertyVO> getProperties() {
		if(properties==null){
			properties=new ArrayList<>();
		}
		return properties;
	}
	public void setProperties(List<PropertyVO> properties) {
		this.properties = properties;
	}
	public List<EdgeIndexVO> getEdgeIndexes() {
		if(edgeIndexes==null){
			edgeIndexes=new ArrayList<>();
		}
		return edgeIndexes;
	}
	public void setEdgeIndexes(List<EdgeIndexVO> edgeIndexes) {
		this.edgeIndexes = edgeIndexes;
	}
	public List<PropertyIndexVO> getPropertyIndexes() {
		if(propertyIndexes==null){
			propertyIndexes=new ArrayList<>();
		}
		return propertyIndexes;
	}
	public void setPropertyIndexes(List<PropertyIndexVO> propertyIndexes) {
		this.propertyIndexes = propertyIndexes;
	}
	
}
