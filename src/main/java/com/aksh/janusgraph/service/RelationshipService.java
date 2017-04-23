package com.aksh.janusgraph.service;

import java.util.List;

import com.aksh.janusgraph.dto.Relationship;

public interface RelationshipService {
	
	Relationship create(Relationship baseRelationship);

	Iterable<Relationship> getRelationFrom(String fromId,String label);

	List<Relationship> getRelationBetween(String fromId,String toId);
	
	Relationship getRelationBetween(String fromId,String toId, String label);
	
	void delete(Relationship node);

	Relationship update(Relationship relationShip);
	
}
