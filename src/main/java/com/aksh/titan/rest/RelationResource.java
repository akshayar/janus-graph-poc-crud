package com.aksh.titan.rest;


import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aksh.titan.dto.Relationship;
import com.aksh.titan.service.RelationshipService;

/**
 *
 */
@RestController
@RequestMapping("relation")
public class RelationResource {
	private static final Logger logger = Logger.getLogger(RelationResource.class);
	
	@Autowired
	private RelationshipService relationService;

	@RequestMapping(method = RequestMethod.GET, path = "/ping")
	public String ping() {
		logger.info("Pinged");
		return "OK";
	}

	@RequestMapping(method=RequestMethod.POST,path="/create")
	public Relationship createRelation(@RequestBody Relationship relationship) {
		return relationService.create(relationship);
	}
	
	@RequestMapping(method=RequestMethod.POST,path="/update")
	public Relationship updateEntity(@RequestBody Relationship relationship) {
		return relationService.update(relationship);
	}
	@RequestMapping(method=RequestMethod.GET,path="/read")
	public List<Relationship> getNode(@RequestParam(value=QueryParamKeys.FROM_URI) String fromUri, @RequestParam(value=QueryParamKeys.TO_URI) String toUri){
		return relationService.getRelationBetween(fromUri, toUri);
	}
}
