package com.aksh.titan.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.aksh.titan.dto.Node;
import com.aksh.titan.dto.Relationship;
import com.google.common.base.Preconditions;
@Component
public class VertexDaoImpl implements VertexDao {

	private static Logger logger = Logger.getLogger(VertexDaoImpl.class);

	@Autowired
	@Qualifier("titanGraph")
	private Graph graph;

	
	@Override
	public Vertex createVertex(Node node) {
		Vertex vertex = graph.addVertex(node.getEntityType());
		vertex.property(PropertyKeyNames.URI, node.getUri());
		addUpdateAttributes(node, vertex);
		return vertex;
	}

	@Override
	public Vertex getVertexByUri(String uri) {
		List<Vertex> vertices = graph.traversal().V().has(PropertyKeyNames.URI, uri).toList();
		Vertex vertex = null;
		if (!CollectionUtils.isEmpty(vertices)) {
			Preconditions.checkArgument(vertices.size() == 1, "Multiple nodes with uri:" + uri);
			vertex = vertices.get(0);
		}
		return vertex;
	}
	
	@Override
	public Vertex getVertexByUriNLabel(String uri, String label) {
		List<Vertex> vertices = graph.traversal().V().hasLabel(label).has(PropertyKeyNames.URI, uri).toList();
		Vertex vertex = null;
		if (!CollectionUtils.isEmpty(vertices)) {
			Preconditions.checkArgument(vertices.size() == 1, "Multiple nodes with uri:" + uri);
			vertex = vertices.get(0);
		}
		return vertex;
	}

	public List<Vertex> getVerticesFor(Map<String, Object> properties) {
		GraphTraversal<Vertex, Vertex> graphTraversal = graph.traversal().V();
		properties.keySet().forEach(key -> {
			graphTraversal.has(key, properties.get(key));
		});
		List<Vertex> vertices = graphTraversal.toList();
		return vertices;
	}

	public Vertex updateVertex(Node node) {
		Vertex vertex = getVertexByUri(node.getUri());
		if (vertex != null) {
			addUpdateAttributes(node, vertex);
		} else {
			logger.info("Node not found :" + node.getUri());
		}
		return vertex;
	}

	@Override
	public Edge createEdge(Vertex fromVertex, Vertex toVertex, Relationship relationShip) {
		Edge edge = fromVertex.addEdge(relationShip.getRelationshipName(), toVertex);
		relationShip.getProperties().entrySet().forEach(entry -> {
			edge.property(entry.getKey(), entry.getValue());
		});
		return edge;
	}

	@Override
	public Edge updateEdge(Vertex fromVertex, Vertex toVertex, Relationship relationShip) {
		Edge edge = getEdge(fromVertex, toVertex,relationShip.getRelationshipName());
		relationShip.getProperties().entrySet().forEach(entry -> {
			edge.property(entry.getKey(), entry.getValue());
		});
		return edge;
	}
	
	@Override
	public void deleteEdge(Vertex fromVertex, Vertex toVertex, Relationship relationShip) {
		fromVertex.edges(Direction.OUT, relationShip.getRelationshipName()).forEachRemaining(edge ->{
			if(edge.inVertex().equals(toVertex)){
				edge.remove();
			}
		});
	}
	
	@Override
	public List<Edge> getEdges(Vertex fromVertex, Vertex toVertex) {
		final List<Edge> edges=new ArrayList<>();
		fromVertex.edges(Direction.OUT).forEachRemaining(edge ->{
			if(edge.inVertex().equals(toVertex)){
				edges.add(edge);
			}
		});
		return edges;
	}
	
	@Override
	public Edge getEdge(Vertex fromVertex, Vertex toVertex,String label) {
		Edge resultEdge=null;
		final List<Edge> edges=new ArrayList<>();
		fromVertex.edges(Direction.OUT,label).forEachRemaining(edge ->{
			if(edge.inVertex().equals(toVertex)){
				edges.add(edge);
			}
		});
		Preconditions.checkArgument(edges.size() <= 1, "Multiple relation with same name between two nodes");
		return edges.stream().findFirst().orElse(null);
	}

	@Override
	public List<Edge> getEdges(Vertex fromVertex, String label) {
		final List<Edge> edges=new ArrayList<>();
		fromVertex.edges(Direction.OUT,label).forEachRemaining(edge ->{
			edges.add(edge);
		});
		return edges;
	}
	private void addUpdateAttributes(Node node, Vertex vertex) {
		node.getAttributes().entrySet().forEach(entry -> {
			vertex.property(entry.getKey(), entry.getValue());
		});
	}

}
