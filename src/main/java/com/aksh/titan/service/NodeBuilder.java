package com.aksh.titan.service;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.aksh.titan.dao.PropertyKeyNames;
import com.aksh.titan.dto.Node;
import com.aksh.titan.dto.Relationship;

public class NodeBuilder {

	public Node buildNode(Vertex vertex){
		Node node = buildNodeOnly(vertex);
		vertex.edges(Direction.IN).forEachRemaining(edge->{
			Node fromNode=buildNodeOnly(edge.inVertex());
			Relationship relation=buildRelation(fromNode, null, edge);
			node.getInRelations().add(relation);
		});
		vertex.edges(Direction.OUT).forEachRemaining(edge ->{
			Node toNode=buildNodeOnly(edge.outVertex());
			Relationship relation=buildRelation(null, toNode, edge);
			node.getOutRelations().add(relation);
		});
		return node;
	}

	public Node buildNodeOnly(Vertex vertex) {
		Node node = new Node();
		node.setUri(vertex.value(PropertyKeyNames.URI));
		node.setId((Long) vertex.id());
		vertex.properties().forEachRemaining(vp -> {
			node.getAttributes().put(vp.key(), vp.value());
		});
		node.setEntityType(vertex.label());
		return node;
	}

	public Relationship buildRelation(Node fromNode, Node toNode, Edge edge) {

		Relationship relationship = new Relationship();
		relationship.setFromNode(fromNode);
		relationship.setToNode(toNode);
		relationship.setRelationshipName(edge.label());
		edge.keys().forEach(key -> {
			relationship.getProperties().put(key, edge.value(key));
		});
		return relationship;

	}

}
