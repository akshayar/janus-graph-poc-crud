/**
 * 
 */
package com.aksh.titan.db;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;

import com.thinkaurelius.titan.core.TitanFactory;

/**
 * @author arawa3
 *
 */
@Configuration
public class TitanDBInitializer implements GraphDBInitializer{
	private static final Logger logger=Logger.getLogger("TitanDBConfigurator");
	
	public static Graph GRAPH_INSTANCE=null;
	
	
	@Value("${titan-db.config.location}")
	private Resource dbConfigFile;
	
	private Graph graph;
	
	@PostConstruct
	public void init() throws Exception {
		logger.warn(this+"Config File -"+dbConfigFile);
		printConfigProperties(dbConfigFile);
		if(GRAPH_INSTANCE!=null){
			graph=GRAPH_INSTANCE;
		}else{
			graph = TitanFactory.open(new PropertiesConfiguration(dbConfigFile.getURL()));			
		}
		
	}
	
	private void printConfigProperties(Resource configFilePath) throws Exception{
		try {
			Properties prop=new Properties();
					prop.load(configFilePath.getInputStream());
			for (Object key : prop.keySet()) {
				logger.warn(key+"="+prop.getProperty(key+""));
			}
		} catch (Exception e) {
			logger.error("Error while printing config ",e);
			throw e;
		}
	}
	
	
	@PreDestroy
	public void destroy() throws Exception{
		graph.close();
	}
	
	@Override
	@Bean(name="titanGraph",autowire=Autowire.BY_NAME)
	@Scope("singleton")
	public Graph getGraph() {
		return graph;
	}
	
	public static final void setGraph(Graph graph){
		GRAPH_INSTANCE=graph;
	}
}
