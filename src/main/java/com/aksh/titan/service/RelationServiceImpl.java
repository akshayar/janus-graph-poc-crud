package com.aksh.titan.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.aksh.titan.dao.VertexDao;
import com.aksh.titan.dto.Node;
import com.aksh.titan.dto.Relationship;

@Component
public class RelationServiceImpl implements RelationshipService {

	NodeBuilder nodeBuilder = new NodeBuilder();

	@Autowired
	private VertexDao vertexDao;

	@Autowired
	@Qualifier("titanGraph")
	private Graph graph;

	@Override
	public Relationship create(Relationship relationShip) {

		TransactionRunner<Relationship> runner = new TransactionRunner<>(graph);

		Relationship result = runner.executeNReturn(() -> {
			Vertex fromVertex = vertexDao.getVertexByUri(relationShip.getFromNode().getUri());
			Vertex toVertex = vertexDao.getVertexByUri(relationShip.getToNode().getUri());
			Edge edge = vertexDao.createEdge(fromVertex, toVertex, relationShip);
			return nodeBuilder.buildRelation(nodeBuilder.buildNodeOnly(fromVertex), nodeBuilder.buildNodeOnly(toVertex), edge);
		});
		return result;

	}

	@Override
	public Relationship update(Relationship relationShip) {
		TransactionRunner<Relationship> runner = new TransactionRunner<>(graph);
		Relationship result = runner.executeNReturn(() -> {
			Vertex fromVertex = vertexDao.getVertexByUri(relationShip.getFromNode().getUri());
			Vertex toVertex = vertexDao.getVertexByUri(relationShip.getToNode().getUri());
			Edge edge = vertexDao.updateEdge(fromVertex, toVertex, relationShip);
			return nodeBuilder.buildRelation(nodeBuilder.buildNodeOnly(fromVertex), nodeBuilder.buildNodeOnly(toVertex), edge);
		});
		return result;

	}

	@Override
	public void delete(Relationship relationShip) {
		TransactionRunner<Void> runner = new TransactionRunner<>(graph);
		runner.executeNReturn(() -> {
			Vertex fromVertex = vertexDao.getVertexByUri(relationShip.getFromNode().getUri());
			Vertex toVertex = vertexDao.getVertexByUri(relationShip.getToNode().getUri());
			vertexDao.deleteEdge(fromVertex, toVertex, relationShip);
			return null;
		});

	}

	@Override
	public List<Relationship> getRelationBetween(String fromUri, String toUri) {
		TransactionRunner<List<Relationship>> runner = new TransactionRunner<>(graph);
		List<Relationship> result = runner.executeNReturn(() -> {
			Vertex fromVertex = vertexDao.getVertexByUri(fromUri);
			Vertex toVertex = vertexDao.getVertexByUri(toUri);
			List<Edge> edges = vertexDao.getEdges(fromVertex, toVertex);
			Node fromNode = nodeBuilder.buildNodeOnly(fromVertex);
			Node toNode = nodeBuilder.buildNodeOnly(toVertex);
			List<Relationship> relations = new ArrayList<>();
			edges.forEach(edge -> {
				relations.add(nodeBuilder.buildRelation(fromNode, toNode, edge));
			});
			return relations;
		});
		return result;
	}

	@Override
	public Relationship getRelationBetween(String fromUri, String toUri, String label) {

		TransactionRunner<Relationship> runner = new TransactionRunner<>(graph);
		Relationship result = runner.executeNReturn(() -> {
			Vertex fromVertex = vertexDao.getVertexByUri(fromUri);
			Vertex toVertex = vertexDao.getVertexByUri(toUri);
			Edge edge = vertexDao.getEdge(fromVertex, toVertex, label);
			Node fromNode = nodeBuilder.buildNodeOnly(fromVertex);
			Node toNode = nodeBuilder.buildNodeOnly(toVertex);
			return nodeBuilder.buildRelation(fromNode, toNode, edge);
		});
		return result;
	}

	@Override
	public List<Relationship> getRelationFrom(String fromUri, String label) {
		TransactionRunner<List<Relationship>> runner = new TransactionRunner<>(graph);
		List<Relationship> result = runner.executeNReturn(() -> {
			Vertex fromVertex = vertexDao.getVertexByUri(fromUri);
			List<Edge> edges = vertexDao.getEdges(fromVertex, label);
			Node fromNode = nodeBuilder.buildNodeOnly(fromVertex);
			List<Relationship> relations = new ArrayList<>();
			edges.forEach(edge -> {
				Vertex toVertex = edge.outVertex();
				Node toNode = nodeBuilder.buildNodeOnly(toVertex);
				relations.add(nodeBuilder.buildRelation(fromNode, toNode, edge));
			});
			return relations;
		});
		return result;
	}
}
