package nl.knaw.huygens.timbuctoo.model.vre;

import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;

public class Collection {
  private static final Logger LOG = LoggerFactory.getLogger(Collection.class);

  public static final String DATABASE_LABEL = "collection";
  public static final String COLLECTION_ENTITIES_LABEL = "collectionEntities";

  public static final String COLLECTION_NAME_PROPERTY_NAME = "collectionName";
  public static final String ENTITY_TYPE_NAME_PROPERTY_NAME = "entityTypeName";

  public static final String HAS_ENTITY_NODE_RELATION_NAME = "hasEntityNode";
  public static final String HAS_PROPERTY_RELATION_NAME = "hasProperty";
  public static final String HAS_DISPLAY_NAME_RELATION_NAME = "hasDisplayName";
  public static final String HAS_ENTITY_RELATION_NAME = "hasEntity";
  public static final String HAS_INITIAL_PROPERTY_RELATION_NAME = "hasInitialProperty";

  private final String entityTypeName;
  private final String collectionName;
  private final Vre vre;
  private final String abstractType;
  private final ReadableProperty displayName;
  private final LinkedHashMap<String, ReadableProperty> properties;
  private final LinkedHashMap<String, LocalProperty> writeableProperties;
  private final Map<String, Supplier<GraphTraversal<Object, Vertex>>> derivedRelations;
  private final boolean isRelationCollection;

  Collection(@NotNull String entityTypeName, @NotNull String abstractType,
             @NotNull ReadableProperty displayName, @NotNull LinkedHashMap<String, ReadableProperty> properties,
             @NotNull String collectionName, @NotNull Vre vre,
             @NotNull Map<String, Supplier<GraphTraversal<Object, Vertex>>> derivedRelations,
             boolean isRelationCollection) {
    this.entityTypeName = entityTypeName;
    this.abstractType = abstractType;
    this.displayName = displayName;
    this.properties = properties;
    this.collectionName = collectionName;
    this.vre = vre;
    this.derivedRelations = derivedRelations;
    this.isRelationCollection = isRelationCollection;
    writeableProperties = properties.entrySet().stream()
      .filter(e -> e.getValue() instanceof LocalProperty)
      .collect(toMap(
        Map.Entry::getKey,
        e -> (LocalProperty) e.getValue(),
        (v1, v2) -> { throw new IllegalStateException("Duplicate key"); },
        LinkedHashMap::new
      ));
  }

  public String getEntityTypeName() {
    return entityTypeName;
  }

  public String getAbstractType() {
    return abstractType;
  }

  public ReadableProperty getDisplayName() {
    return displayName;
  }

  public Map<String, LocalProperty> getWriteableProperties() {
    return writeableProperties;
  }

  public Map<String, ReadableProperty> getReadableProperties() {
    return properties;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public Vre getVre() {
    return vre;
  }

  public Map<String, Supplier<GraphTraversal<Object, Vertex>>> getDerivedRelations() {
    return derivedRelations;
  }

  public boolean isRelationCollection() {
    return isRelationCollection;
  }

  public Vertex save(GraphWrapper graphWrapper) {

    Graph graph = graphWrapper.getGraph();

    Vertex collectionVertex = findOrCreateCollectionVertex(graph);

    collectionVertex.property(COLLECTION_NAME_PROPERTY_NAME, collectionName);
    collectionVertex.property(ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName);

    saveArchetypeRelation(graph, collectionVertex);

    savePropertyConfigurations(graphWrapper, collectionVertex);

    // Create a container node to hold the entities in this collection.
    Iterator<Vertex> entityNodeIt = collectionVertex.vertices(Direction.OUT, HAS_ENTITY_NODE_RELATION_NAME);
    if (!entityNodeIt.hasNext()) {
      collectionVertex.addEdge(HAS_ENTITY_NODE_RELATION_NAME, graph.addVertex(COLLECTION_ENTITIES_LABEL));
    }

    return collectionVertex;
  }



  private void savePropertyConfigurations(GraphWrapper graphWrapper, Vertex collectionVertex) {

    dropExistingPropertyConfigurations(collectionVertex);

    // Add property configurations
    List<Vertex> propertyVertices = new ArrayList<>();
    writeableProperties.forEach((clientPropertyName, property) -> {
      LOG.info("Adding property {} to collection {}", clientPropertyName, collectionName);
      final Vertex propertyVertex = property.save(graphWrapper, clientPropertyName);
      collectionVertex.addEdge(HAS_PROPERTY_RELATION_NAME, propertyVertex);
      propertyVertices.add(propertyVertex);
    });

    savePropertyConfigurationSortorder(collectionVertex, propertyVertices);

    if (displayName != null) {
      Vertex displayNameVertex = displayName.save(graphWrapper, ReadableProperty.DISPLAY_NAME_PROPERY_NAME);
      collectionVertex.addEdge(HAS_DISPLAY_NAME_RELATION_NAME, displayNameVertex);
    }
  }

  private void savePropertyConfigurationSortorder(Vertex collectionVertex, List<Vertex> propertyVertices) {
    // add hasInitialProperty for sortorder
    if (propertyVertices.size() > 0) {
      collectionVertex.addEdge(HAS_INITIAL_PROPERTY_RELATION_NAME, propertyVertices.get(0));

      // hasNextProperty for sortorder
      Iterator<Vertex> propertyIterator = propertyVertices.iterator();
      Vertex previous = propertyIterator.next();
      Vertex next = propertyIterator.hasNext() ? propertyIterator.next() : null;

      while (next != null) {
        previous.addEdge(ReadableProperty.HAS_NEXT_PROPERTY_RELATION_NAME, next);
        previous = next;
        next = propertyIterator.hasNext() ? propertyIterator.next() : null;
      }
    }
  }

  private void dropExistingPropertyConfigurations(Vertex collectionVertex) {
    collectionVertex
      .vertices(Direction.OUT, HAS_PROPERTY_RELATION_NAME, HAS_DISPLAY_NAME_RELATION_NAME,
        HAS_INITIAL_PROPERTY_RELATION_NAME)
      .forEachRemaining(Element::remove);
  }

  private void saveArchetypeRelation(Graph graph, Vertex collectionVertex) {
    if (!abstractType.equals(entityTypeName)) {
      GraphTraversal<Vertex, Vertex> archetype = graph.traversal().V().hasLabel(DATABASE_LABEL)
                                                      .has(ENTITY_TYPE_NAME_PROPERTY_NAME, abstractType);

      if (!archetype.hasNext()) {
        LOG.error(databaseInvariant, "No archetype collection with entityTypeName {} present in the graph",
          abstractType);
      } else {
        collectionVertex.addEdge("hasArchetype", archetype.next());
      }
    } else {
      LOG.warn("Assuming collection {} is archetype because entityTypeName is equal to abstractType", collectionName);
    }
  }

  private Vertex findOrCreateCollectionVertex(Graph graph) {
    Vertex collectionVertex;
    GraphTraversal<Vertex, Vertex> existing = graph.traversal().V().hasLabel(DATABASE_LABEL)
                                                   .has(COLLECTION_NAME_PROPERTY_NAME, collectionName);

    // Create new if does not exist
    if (existing.hasNext()) {
      collectionVertex = existing.next();
      LOG.info("Replacing existing vertex {}.", collectionVertex);
    } else {
      collectionVertex = graph.addVertex(DATABASE_LABEL);
      LOG.info("Creating new vertex");
    }
    return collectionVertex;
  }
}
