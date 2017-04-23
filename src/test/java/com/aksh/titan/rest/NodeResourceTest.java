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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class NodeResourceTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void pingtest() {
		ResponseEntity<String> pingResponse=restTemplate.getForEntity("/entity/ping", String.class);
		assertEquals(HttpStatus.OK,pingResponse.getStatusCode());
	}
	
	@Test
	public void createNodeTest() {
		Node node = createNode();
		
		ResponseEntity<Node> response=restTemplate.postForEntity("/entity/create",node ,Node.class);
		
		assertEquals(HttpStatus.OK,response.getStatusCode());
		Node nodeR=response.getBody();
		compareNValidateNodes(node, nodeR);
		
		
		getNValidateNode(node);
	}

	private void getNValidateNode(Node node) {
		ResponseEntity<Node> r=restTemplate.getForEntity("/entity/read?uri="+node.getUri(),Node.class);
		assertEquals(HttpStatus.OK,r.getStatusCode());
		assertNotNull(r.getBody());
		Node nodeFromDb=r.getBody();
		compareNValidateNodes(node, nodeFromDb);
	}
	
	private void compareNValidateNodes(Node node1, Node node2){
		assertEquals(node1.getUri(), node2.getUri());
		assertEquals(node1.getEntityType(), node2.getEntityType());
		node1.getAttributes().entrySet().forEach(entry->{
			assertEquals(entry.getValue(), node2.getAttributes().get(entry.getKey()));
		});	
	}

	private Node createNode() {
		Node node=new Node();
		node.setEntityType("email");
		node.setUri(System.currentTimeMillis()+"");
		node.getAttributes().put("name", "Akshya"+System.currentTimeMillis());
		return node;
	}
	
	@Test
	public void updateNodeTest() {
		Node node = createNode();
		
		
		ResponseEntity<Node> response=restTemplate.postForEntity("/entity/create",node ,Node.class);
		assertEquals(HttpStatus.OK,response.getStatusCode());
		Node nodeR=response.getBody();
		compareNValidateNodes(node, nodeR);
		
		node.getAttributes().put("age", "20");
		node.getAttributes().put("name", "Akshaya1");
		response=restTemplate.postForEntity("/entity/update",node ,Node.class);
		assertEquals(HttpStatus.OK,response.getStatusCode());
		nodeR=response.getBody();
		compareNValidateNodes(node, nodeR);
		
		getNValidateNode(node);
		
	}
	
	

}
