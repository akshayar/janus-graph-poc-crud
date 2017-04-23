package com.aksh.titan.service;

import org.apache.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;

public class TransactionRunner<T> {
	private final boolean silent;
	private final Graph graph;
	private static final Logger logger=Logger.getLogger(TransactionRunner.class);

	TransactionRunner(Graph graph) {
		this(graph, true);
	}

	TransactionRunner(Graph graph, boolean silent) {
		this.graph = graph;
		this.silent = silent;
	}

	public T executeNReturn(Transactional<T> tx) {
		boolean opened=false;
		T result=null;
		try {
			opened=startTx();
			result=tx.run();
			commitTx(opened);
		} catch (RuntimeException e) {
			rollback(opened);
			e.printStackTrace();
			if (!silent)
				throw e;
			else
				logger.error(e);
				
		}finally{
			graph.tx().close();
		}
		return result;
	}

	
	
	private boolean startTx(){
		Transaction tx=graph.tx();
		boolean opened=false;
		if(!tx.isOpen()){
			tx.open();
			opened=true;
		}
		
		return opened;
	}
	
	private void commitTx(boolean opened){
		if(graph.tx().isOpen()&& opened){
			graph.tx().commit();	
			graph.tx().close();
		}
	}
	
	private void rollback(boolean opened){
		if(graph.tx().isOpen()&& opened){
			graph.tx().rollback();
			graph.tx().close();
		}
	}

	

}
