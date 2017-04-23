package com.aksh.janusgraph;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.aksh.janusgraph.db.JanusGraphDBInitializer;

@SpringBootApplication
public class JanusGraphCrud {
	private static final Logger logger = Logger.getLogger(JanusGraphCrud.class);

	public JanusGraphCrud() {
		logger.info("Constructor");
	}

	public static void main(String[] args) {

		/*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() {
		 * 
		 * try { GremlinServer.main(args); } catch (Exception e) { logger.error(
		 * "Error starting Gremlin server -",e); }
		 * 
		 * } }).start();
		 * 
		 * ConfigurableApplicationContext applicationContext=new
		 * GenericApplicationContext(); TitanDBConfigurator
		 * titanDBConfigurator=TitanDBConfigurator.getInstance();
		 * titanDBConfigurator.setDbConfigFile(new
		 * ClassPathResource("/dev/titan-berkeleydb-es.properties"));
		 * applicationContext.getBeanFactory().registerSingleton(
		 * "tianDBConfigurator", titanDBConfigurator ); new
		 * SpringApplicationBuilder().parent(applicationContext).build().run(
		 * EmailSurvLoadApplication.class, args);
		 */
		if(args.length<2){
			startGremlinNCrud(args);	
		}else{
			startGremlinServer(args);
		}
		

	}

	private static void startGremlinNCrud(String[] args) {
		startGremlinServer(args);
		SpringApplication.run(JanusGraphCrud.class, args);
	}

	private static void startGremlinServer(String[] args) {
		try {
			if (args.length != 0) {
				logger.info("Starting germlin server");
				GremlinServerModified custom = GremlinServerModified.start(args);
				Map<String, Graph> graphs = custom.getServerGremlinExecutor().getGraphManager().getGraphs();
				Graph graph = graphs.values().iterator().next();
				JanusGraphDBInitializer.setGraph(graph);
				logger.info("Done -Starting germlin server");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
