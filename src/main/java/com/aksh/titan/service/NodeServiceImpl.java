package com.aksh.titan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.aksh.titan.dao.VertexDao;
import com.aksh.titan.dto.Node;
import com.google.common.base.Preconditions;

@Component
public class NodeServiceImpl implements NodeService {

	private static Logger logger = Logger.getLogger(NodeServiceImpl.class);
	NodeBuilder nodeBuilder = new NodeBuilder();
	@Autowired
	VertexDao vertexDao;

	@Autowired
	@Qualifier("titanGraph")
	private Graph graph;

	@Override
	public Node create(Node node) {
		Preconditions.checkArgument(!StringUtils.isEmpty(node.getUri()));
		TransactionRunner<Node> runner = new TransactionRunner<>(graph);

		Node result = runner.executeNReturn(() -> {
			logger.info("Creating node : " + node);
			Vertex vertex=vertexDao.getVertexByUri(node.getUri());
			if(vertex==null){
				vertex = vertexDao.createVertex(node);
			}
			Node resultNode = buildNode(vertex);
			logger.info("Node created:" + resultNode);
			return resultNode;
		});
		return result;

	}

	@Override
	public Node update(Node node) {
		Preconditions.checkArgument(!StringUtils.isEmpty(node.getUri()));
		TransactionRunner<Node> runner = new TransactionRunner<>(graph);

		Node result = runner.executeNReturn(() -> {
			Node resultNode = null;
			logger.info("Updating node :" + node);
			Vertex vertex = vertexDao.updateVertex(node);
			if (vertex != null) {
				resultNode = buildNode(vertex);
				logger.info("Node updated :" + resultNode);
			}
			return resultNode;
		});
		return result;
	}

	@Override
	public void delete(String uri) {
		TransactionRunner<Void> runner = new TransactionRunner<>(graph);
		runner.executeNReturn(() -> {
			Vertex vertex = vertexDao.getVertexByUri(uri);
			if (vertex != null) {
				vertex.remove();
			} else {
				logger.info("Node not found :" + uri);
			}
			return null;
		});

	}

	@Override
	public Node readByURI(String graphEntityId) {
		TransactionRunner<Node> runner = new TransactionRunner<>(graph);
		Node result = runner.executeNReturn(() -> {
			Vertex vertex = vertexDao.getVertexByUri(graphEntityId);
			Node temp=null;
			if(vertex!=null){
				temp=buildNode(vertex);
			}
			return temp;
		});

		return result;
	}

	@Override
	public List<Node> query(Map<String, Object> properties) {
		TransactionRunner<List<Node>> runner = new TransactionRunner<>(graph);
		List<Node> result = new ArrayList<>();
		runner.executeNReturn(() -> {
			List<Vertex> vertices = vertexDao.getVerticesFor(properties);
			return vertices.stream().map(this::buildNode).collect(Collectors.toList());
		});

		return result;
	}

	private Node buildNode(Vertex vertex) {
		return nodeBuilder.buildNode(vertex);
	}
}
