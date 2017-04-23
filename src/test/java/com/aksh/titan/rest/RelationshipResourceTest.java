package com.aksh.titan.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.aksh.titan.dto.Node;
import com.aksh.titan.dto.Relationship;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RelationshipResourceTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void pingtest() {
		ResponseEntity<String> pingResponse = restTemplate.getForEntity("/relation/ping", String.class);
		assertEquals(HttpStatus.OK, pingResponse.getStatusCode());
	}

	@Test
	public void createRelationTest() {

		Node preeti = createAndValidateNode("Preet");
		Node akshaya = createAndValidateNode("akshaya");

		Relationship relation = createNValidateRelation(preeti, akshaya, "loves");

		getNValidateRelationshi(relation);
	}

	private Relationship createNValidateRelation(Node fromNode, Node toNode, String relationName) {
		Relationship relation = new Relationship();
		relation.setFromNode(fromNode);
		relation.setToNode(toNode);
		relation.setRelationshipName(relationName);
		relation.getProperties().put("since", "eternity");

		createNValidateRelation(relation);
		return relation;
	}

	private void createNValidateRelation(Relationship relation) {
		ResponseEntity<Relationship> response = restTemplate.postForEntity("/relation/create", relation,
				Relationship.class);
		Relationship fromDb = response.getBody();
		assertNotNull(fromDb);
		assertEquals(relation.getFromNode().getUri(), fromDb.getFromNode().getUri());
		assertEquals(relation.getToNode().getUri(), fromDb.getToNode().getUri());
		assertEquals(relation.getRelationshipName(), fromDb.getRelationshipName());
		relation.getProperties().entrySet().forEach(entry -> {
			assertEquals(relation.getProperties().get(entry.getKey()), fromDb.getProperties().get(entry.getKey()));
		});

	}

	private void updateNValidateRelation(Relationship relation) {
		ResponseEntity<Relationship> response = restTemplate.postForEntity("/relation/update", relation,
				Relationship.class);
		Relationship fromDb = response.getBody();
		assertNotNull(fromDb);
		assertEquals(relation.getFromNode().getUri(), fromDb.getFromNode().getUri());
		assertEquals(relation.getToNode().getUri(), fromDb.getToNode().getUri());
		assertEquals(relation.getRelationshipName(), fromDb.getRelationshipName());
		relation.getProperties().entrySet().forEach(entry -> {
			assertEquals(relation.getProperties().get(entry.getKey()), fromDb.getProperties().get(entry.getKey()));
		});

	}

	private Node createAndValidateNode(String name) {
		Node node = createNode(name);
		ResponseEntity<Node> response = restTemplate.postForEntity("/entity/create", node, Node.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Node nodeR = response.getBody();
		assertEquals(node.getUri(), nodeR.getUri());
		assertEquals(name, nodeR.getAttributes().get("name"));
		return node;
	}

	private Node createNode(String name) {
		Node node = new Node();
		node.setEntityType("email");
		node.setUri(System.currentTimeMillis() + "");
		node.getAttributes().put("name", name+System.currentTimeMillis());
		return node;
	}

	@Test
	public void updateRelationTest() {
		Node preeti = createAndValidateNode("Preet");
		Node akshaya = createAndValidateNode("akshaya");

		Relationship relation = createNValidateRelation(akshaya, preeti, "lovesMore");
		relation.getProperties().put("bonding", "strong");
		updateNValidateRelation(relation);
		getNValidateRelationshi(relation);
	}

	private void getNValidateRelationshi(Relationship relationship) {
		ResponseEntity<Relationship[]> r = restTemplate
				.getForEntity("/relation/read?" + QueryParamKeys.FROM_URI + "=" + relationship.getFromNode().getUri()
						+ "&" + QueryParamKeys.TO_URI + "=" + relationship.getToNode().getUri(), Relationship[].class);
		assertEquals(HttpStatus.OK, r.getStatusCode());
		assertNotNull(r.getBody());
		Relationship[] rFromDb = r.getBody();
		assertEquals(1,rFromDb.length);
		compareNValidateNodes(relationship, rFromDb[0]);
	}

	private void compareNValidateNodes(Relationship r1, Relationship r2) {
		assertEquals(r1.getFromNode().getUri(), r2.getFromNode().getUri());
		assertEquals(r1.getToNode().getUri(), r2.getToNode().getUri());
		assertEquals(r1.getRelationshipName(), r2.getRelationshipName());
		r1.getProperties().entrySet().forEach(entry -> {
			assertEquals(entry.getValue(), r2.getProperties().get(entry.getKey()));
		});
	}

}
