package com.aksh.janusgraph.db;

import org.apache.tinkerpop.gremlin.structure.Graph;

public interface GraphDBInitializer {
	
	void init() throws Exception;

	Graph getGraph();
	
}
