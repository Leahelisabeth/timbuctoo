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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;

public class Collection {
  private static final Logger LOG = LoggerFactory.getLogger(Collection.class);

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

  public Vertex persistToDatabase(GraphWrapper graphWrapper) {
    // Look for existing VRE vertex
    Graph graph = graphWrapper.getGraph();
    GraphTraversal<Vertex, Vertex> existing = graph.traversal().V().hasLabel("collection")
                                                   .has("collectionName", collectionName);


    Vertex collectionVertex;
    // Create new if does not exist
    if (existing.hasNext()) {
      collectionVertex = existing.next();
      LOG.info("Replacing existing vertex {}.", collectionVertex);
    } else {
      collectionVertex = graph.addVertex("collection");
      LOG.info("Creating new vertex");
    }

    // Set the hasArchetype edge for non-Admin collections
    if (!abstractType.equals(entityTypeName)) {
      GraphTraversal<Vertex, Vertex> archetype = graph.traversal().V().hasLabel("collection")
                                                      .has("entityTypeName", abstractType);

      if (!archetype.hasNext()) {
        LOG.error(databaseInvariant, "No archetype collection with entityTypeName {} present in the graph",
          abstractType);
      } else {
        collectionVertex.addEdge("hasArchetype", archetype.next());
      }
    } else {
      LOG.warn("Assuming collection {} is archetype because entityTypeName is equal to abstractType", collectionName);
    }

    collectionVertex.property("collectionName", collectionName);
    collectionVertex.property("entityTypeName", entityTypeName);

    // Drop any existing property configurations
    collectionVertex.vertices(Direction.OUT, "hasProperty", "hasDisplayName", "hasInitialProperty")
                    .forEachRemaining(Element::remove);


    // Add property configurations
    writeableProperties.forEach((clientPropertyName, property) -> {
      LOG.info("Adding property {} to collection {}", clientPropertyName, collectionName);
      collectionVertex.addEdge("hasProperty", property.persistToDatabase(graphWrapper, clientPropertyName));
    });

    // add hasInitialProperty edge for sortorder

    // set hasNextProperty edges to property vertices


    return collectionVertex;
  }
  //derivedRelations
}
