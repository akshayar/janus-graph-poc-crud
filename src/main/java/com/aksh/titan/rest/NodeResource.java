package com.aksh.titan.rest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aksh.titan.dto.Node;
import com.aksh.titan.service.NodeService;

/**
 *
 */
@RestController
@RequestMapping("entity")
public class NodeResource {
	private static final Logger logger = Logger.getLogger(NodeResource.class);
	
	@Autowired
	private NodeService nodeService;

	@RequestMapping(method = RequestMethod.GET, path = "/ping")
	public String ping() {
		logger.info("Pinged");
		return "OK";
	}

	@RequestMapping(method=RequestMethod.POST,path="/create")
	public Node createEntity(@RequestBody Node node) {
		return nodeService.create(node);
	}
	
	@RequestMapping(method=RequestMethod.POST,path="/update")
	public Node updateEntity(@RequestBody Node node) {
		return nodeService.update(node);
	}
	@RequestMapping(method=RequestMethod.GET,path="/read")
	public Node getNode(@RequestParam(value=QueryParamKeys.URI) String uri){
		return nodeService.readByURI(uri);
	}
}
