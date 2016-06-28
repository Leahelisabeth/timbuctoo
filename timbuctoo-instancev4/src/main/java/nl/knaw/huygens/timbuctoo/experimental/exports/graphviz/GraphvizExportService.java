package nl.knaw.huygens.timbuctoo.experimental.exports.graphviz;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;

public class GraphvizExportService {

  private final GraphWrapper graph;
  private final Vre vre;

  public GraphvizExportService(GraphWrapper graph, Vres vres) {
    this.graph = graph;
    vre = vres.getVre("EuropeseMigratie");
  }

  public String generateGraphviz(Iterator<Vertex> vertices) {
    final GraphTraversalSource gt = graph.getGraph().traversal();
    Map<String, HashMap<String, String>> nodesPerType = new HashMap<>();
    Map<String, Set<Edge>> edgesPerType = new HashMap<>();
    Set<String> knownVertices = new HashSet<>();

    vertices.forEachRemaining(vertex -> {
      getCollection(vre, vertex).ifPresent(col -> {
        final String tim_id = "\"" + vertex.id() + "\"";
        knownVertices.add(tim_id);
        final String typeName = col.getCollectionName();
        final HashMap<String, String> nodes = nodesPerType.computeIfAbsent(typeName, lbl -> new HashMap<>());
        final String label = stream(gt.V(vertex.id())
           .union(col.getDisplayName().traversal())
           .filter(x -> x.get().isSuccess())
           .map(x -> x.get().get().asText()))
          .findFirst()
          .orElse("<unknown>");
        nodes.put(tim_id, label);

        vertex.edges(Direction.BOTH).forEachRemaining(edge -> {
          final Set<Edge> sets = edgesPerType.computeIfAbsent(edge.label(), lbl -> new HashSet<>());
          sets.add(
            new Edge("\"" + edge.inVertex().id() + "\"", "\"" + edge.outVertex().id() + "\"")
          );
        });
      });
    });

    StringBuilder result = new StringBuilder();
    result.append("digraph G {\n");
    nodesPerType.forEach((type, nodes) -> {
      result.append("  subgraph ").append(type).append(" {\n");
      nodes.forEach((id, label) -> {
        result.append("    ").append(id).append(" [label=\"").append(label).append("\"];\n");
      });
      result.append("  }\n");
    });
    edgesPerType.forEach((type, edges) -> {
      result.append("  subgraph ").append(type).append(" {\n");
      for (Edge edge: edges) {
        if (knownVertices.contains(edge.getLeft()) && knownVertices.contains(edge.getRight())) {
          result.append("    ").append(edge.getLeft()).append(" -> ").append(edge.getRight()).append(";\n");
        }
      }
      result.append("  }\n");
    });
    result.append("}\n");
    return result.toString();
  }

  private Optional<Collection> getCollection(Vre vre, Element sourceV) {
    String[] types = getEntityTypes(sourceV).orElseGet(() -> {
      return Try.success(new String[0]);
    }).getOrElse(() -> {
      return new String[0];
    });
    String ownType = vre.getOwnType(types);
    if (ownType == null) {
      return Optional.empty();
    }
    return Optional.of(vre.getCollectionForTypeName(ownType));
  }

  public class Edge {
    public final String left;
    private final String right;

    public Edge(String left, String right) {
      this.left = left;
      this.right = right;
    }

    public String getLeft() {
      return left;
    }

    public String getRight() {
      return right;
    }

    @Override
    public boolean equals(Object obj) {
      return obj != null &&
        obj instanceof Edge &&
        this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
      return (left + right).hashCode();
    }
  }

}
