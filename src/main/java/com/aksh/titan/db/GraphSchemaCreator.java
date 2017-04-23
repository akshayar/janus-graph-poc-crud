package com.aksh.titan.db;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aksh.titan.dto.EdgeIndexVO;
import com.aksh.titan.dto.EdgeVO;
import com.aksh.titan.dto.PropertyIndexVO;
import com.aksh.titan.dto.PropertyVO;
import com.aksh.titan.dto.Schema;
import com.thinkaurelius.titan.core.Cardinality;
import com.thinkaurelius.titan.core.EdgeLabel;
import com.thinkaurelius.titan.core.Multiplicity;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.VertexLabel;
import com.thinkaurelius.titan.core.schema.PropertyKeyMaker;
import com.thinkaurelius.titan.core.schema.RelationTypeIndex;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;

@Component
public class GraphSchemaCreator {
	public static final String INDEX_BACK_END_NAME = "search";
	@Autowired
	private TitanDBInitializer titanDBConfigurator;
	TitanGraph graphDb;
	TitanManagement mgmt;
	@Autowired
	private Schema schema;
	private static final Logger logger = Logger.getLogger(GraphSchemaCreator.class);

	@PostConstruct
	public void init() {
		logger.info("Init");
		graphDb = (TitanGraph) titanDBConfigurator.getGraph();
//		graphDb.tx().onReadWrite(Transaction.READ_WRITE_BEHAVIOR.MANUAL);
		this.mgmt = graphDb.openManagement();
		create(schema);
		this.mgmt.commit();
	}

	private void create(Schema schema) {
		schema.getLabels().forEach(label -> createVertexLabel(label));
		schema.getProperties().forEach(property -> createProperty(property));
		schema.getEdges().forEach(edge -> createEdge(edge));
		schema.getEdgeIndexes().forEach(edgeIndex -> createEdgeIndex(edgeIndex));
		schema.getPropertyIndexes().forEach(index -> createPropertyIndex(index));
	}

	private Object createPropertyIndex(PropertyIndexVO index) {
		TitanGraphIndex resultIndex = null;
		try {
			TitanManagement.IndexBuilder nameIndexBuilder = mgmt.buildIndex(index.getName(), Vertex.class);
			if (index.isUnique()) {
				nameIndexBuilder = nameIndexBuilder.unique();
			}
			for (String key : index.getPropertyKeys()) {
				PropertyKey pKey = getPropertyKey(key);
				nameIndexBuilder = nameIndexBuilder.addKey(pKey);

			}
			if (index.isComposite()) {
				resultIndex = nameIndexBuilder.buildCompositeIndex();
			} else {
				resultIndex = nameIndexBuilder.buildMixedIndex(INDEX_BACK_END_NAME);
			}
			logger.info("Index created with name :" + resultIndex.name() + ",backingIndex:"
					+ resultIndex.getBackingIndex() + ",indexed-element :" + resultIndex.getIndexedElement() + ",unique:"
					+ resultIndex.isUnique() + ",keys:" + Arrays.asList(resultIndex.getFieldKeys()));
			logger.info("Index :"+resultIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultIndex;
	}

	private PropertyKey getPropertyKey(String key) {
		PropertyKey pKey = mgmt.getOrCreatePropertyKey(key);
		return pKey;
	}

	private Object createEdgeIndex(EdgeIndexVO edgeIndex) {
		RelationTypeIndex relationTypeIndex = null;
		EdgeLabel edgeLable = null;
		PropertyKey pKey = null;
		try {
			edgeLable = mgmt.getEdgeLabel(edgeIndex.getEdgeLabel());
			pKey = mgmt.getPropertyKey(edgeIndex.getPropertyKey());
			Direction dir = edgeIndex.getDirection() != null ? edgeIndex.getDirection() : Direction.OUT;
			relationTypeIndex = mgmt.buildEdgeIndex(edgeLable, edgeIndex.getName(), dir, pKey);
			logger.info("Edge Index  with name:" + relationTypeIndex.name() + ",sort-keys:"
					+ Arrays.asList(relationTypeIndex.getSortKey()) + ",sort-order:" + relationTypeIndex.getSortOrder()
					+ ",direction:" + relationTypeIndex.getDirection() + ",type:" + relationTypeIndex.getType());
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage());

			// retrieve already create index
			relationTypeIndex = mgmt.getRelationIndex(edgeLable, edgeIndex.getName());
			if (relationTypeIndex != null) {
				logger.info("Edge Index with name:" + edgeIndex.getName() + " already created ");
				logger.info("Edge Index with name:" + relationTypeIndex.name() + "retrieved, sort-keys "
						+ Arrays.asList(relationTypeIndex.getSortKey()) + ", type" + relationTypeIndex.getType());
			}
		}
		return relationTypeIndex;
	}

	private void createEdge(EdgeVO edge) {
		createEdgeLabel(edge.getLabel(), edge.getMultiplicity());
	}

	private Object createProperty(PropertyVO property) {
		PropertyKey propertyKey = mgmt.getPropertyKey(property.getName());
		if (propertyKey == null) {
			PropertyKeyMaker propertyKeyMaker = mgmt.makePropertyKey(property.getName())
					.dataType(com.aksh.titan.util.TitanDataType.getDataTypeClass(property.getDataType()));

			switch (property.getCardinality()) {
			case SINGLE:
				propertyKeyMaker = propertyKeyMaker.cardinality(Cardinality.SINGLE);
				break;
			case LIST:
				propertyKeyMaker = propertyKeyMaker.cardinality(Cardinality.LIST);
				break;
			case SET:
				propertyKeyMaker = propertyKeyMaker.cardinality(Cardinality.SET);
				break;
			default:
				propertyKeyMaker = propertyKeyMaker.cardinality(Cardinality.SINGLE);
				break;
			}

			propertyKey = propertyKeyMaker.make();
			logger.info("Property Key created for field <<" + propertyKey.name() + " >> Data Type : <<"
					+ propertyKey.dataType() + ">> Id : <<" + propertyKey.id() + ">> Cardinality : <<"
					+ propertyKey.cardinality() + ">> Label : <<" + propertyKey.label());

		}
		return propertyKey;
	}

	public void createVertexLabel(String label) {
		VertexLabel vertexLabel = mgmt.getVertexLabel(label);
		if (vertexLabel == null) {
			vertexLabel = mgmt.makeVertexLabel(label).make();
			logger.info("Vertex label created - " + vertexLabel.name());
		}

	}

	public void createEdgeLabel(String label, Multiplicity multiplicity) {
		EdgeLabel edgeLabel = mgmt.getEdgeLabel(label);
		if (edgeLabel == null) {
			if (multiplicity != null) {
				edgeLabel = mgmt.makeEdgeLabel(label).multiplicity(multiplicity).make();
			} else {
				edgeLabel = mgmt.makeEdgeLabel(label).make();
			}
			logger.info("Edge label created name:" + edgeLabel.name() + ",label:" + edgeLabel.label() + ",multiplicity:"
					+ edgeLabel.multiplicity() + ",description:" + edgeLabel.toString());
		}

	}

	public Iterable<TitanGraphIndex> getIndexes() {
		return graphDb.openManagement().getGraphIndexes(Vertex.class);
	}

	public PropertyKey createPropertyKey(String propertyKeyName, Class<?> propertyType) {

		PropertyKey propertyKey = getPropertyKey(propertyKeyName);
		if (propertyKey == null) {
			propertyKey = mgmt.makePropertyKey(propertyKeyName).dataType(String.class).make();
			logger.info("Property Key:" + propertyKey + "-" + propertyKey.label());
		}
		return propertyKey;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	/*
	 * private void createUniqueCompositeIndexForVertex(String schemaTypeName,
	 * String propertyKeyName, Class<?> propertyType) {
	 * logger.info("Creating Index -" + schemaTypeName + ":" + propertyKeyName +
	 * ":" + propertyType); TitanSchemaType titanSchemaType =
	 * mgmt.getVertexLabel(schemaTypeName); PropertyKey propertyKey =
	 * createPropertyKey(propertyKeyName, propertyType); String indexName =
	 * titanSchemaType.name() + "_" + propertyKey; TitanGraphIndex graphIndex =
	 * mgmt.getGraphIndex(indexName); if (graphIndex == null) { graphIndex =
	 * mgmt.buildIndex(indexName, Vertex.class).addKey(propertyKey) .unique()
	 * indexOnly(titanSchemaType) .buildCompositeIndex();
	 * logger.info("Index created -" + graphIndex.name() + ":" +
	 * graphIndex.getBackingIndex() + ":" + graphIndex.getClass() + ":" +
	 * graphIndex.getIndexedElement() + ":" +
	 * StringUtils.join(graphIndex.getFieldKeys())); }
	 * 
	 * }
	 * 
	 * private void createCompositeIndexForVertexProperty(String schemaTypeName,
	 * String propertyKeyName, Class<?> propertyType) {
	 * logger.info("Creating Index -" + schemaTypeName + ":" + propertyKeyName +
	 * ":" + propertyType); PropertyKey propertyKey =
	 * createPropertyKey(propertyKeyName, propertyType); String indexName =
	 * schemaTypeName + "_" + propertyKey; TitanGraphIndex graphIndex =
	 * mgmt.getGraphIndex(indexName); if (graphIndex == null) { graphIndex =
	 * mgmt.buildIndex(indexName,
	 * Vertex.class).addKey(propertyKey).buildCompositeIndex();
	 * logger.info("Index created -" + graphIndex.name() + ":" +
	 * graphIndex.getBackingIndex() + ":" + graphIndex.getClass() + ":" +
	 * graphIndex.getIndexedElement() + ":" +
	 * StringUtils.join(graphIndex.getFieldKeys())); }
	 * 
	 * }
	 * 
	 * private void createCompositeIndexForEdgeProperty(String schemaTypeName,
	 * String propertyKeyName, Class<?> propertyType) {
	 * logger.info("Creating Index -" + schemaTypeName + ":" + propertyKeyName +
	 * ":" + propertyType); PropertyKey propertyKey =
	 * createPropertyKey(propertyKeyName, propertyType); String indexName =
	 * schemaTypeName + "_" + propertyKey; TitanGraphIndex graphIndex =
	 * mgmt.getGraphIndex(indexName); if (graphIndex == null) { graphIndex =
	 * mgmt.buildIndex(indexName,
	 * Edge.class).addKey(propertyKey).buildCompositeIndex();
	 * logger.info("Index created -" + graphIndex.name() + ":" +
	 * graphIndex.getBackingIndex() + ":" + graphIndex.getClass() + ":" +
	 * graphIndex.getIndexedElement() + ":" +
	 * StringUtils.join(graphIndex.getFieldKeys())); }
	 * 
	 * }
	 * 
	 * private void createMixedIndexForVertexProperty(String schemaTypeName,
	 * String propertyKeyName, Class<?> propertyType) {
	 * logger.info("Creating Mixed Index -" + schemaTypeName + ":" +
	 * propertyKeyName + ":" + propertyType); PropertyKey propertyKey =
	 * createPropertyKey(propertyKeyName, propertyType); String indexName =
	 * schemaTypeName + "_" + propertyKey; TitanGraphIndex graphIndex =
	 * mgmt.getGraphIndex(indexName); if (graphIndex == null) { graphIndex =
	 * mgmt.buildIndex(indexName, Vertex.class) .addKey(propertyKey,
	 * Parameter.of("mapped-name", propertyKey.name()))
	 * .buildMixedIndex(INDEX_BACK_END_NAME); logger.info("Index created -" +
	 * graphIndex.name() + ":" + graphIndex.getBackingIndex() + ":" +
	 * graphIndex.getClass() + ":" + graphIndex.getIndexedElement() + ":" +
	 * StringUtils.join(graphIndex.getFieldKeys())); }
	 * 
	 * }
	 */
}
