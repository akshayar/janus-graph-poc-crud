package com.aksh.titan.service;

import java.util.List;
import java.util.Map;

import com.aksh.titan.dto.Node;

public interface NodeService {
	
	Node create(Node node);

	Node update(Node node);

	Node readByURI(String uri);
	
	List<Node> query(Map<String, Object> properties);
	
	void delete(String uri);
	
	
}
