package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

public class Neo4jIndexHandler implements IndexHandler {
  private final TinkerpopGraphManager tinkerpopGraphManager;

  public Neo4jIndexHandler(TinkerpopGraphManager tinkerpopGraphManager) {
    this.tinkerpopGraphManager = tinkerpopGraphManager;
  }

  @Override
  public boolean hasIndexFor(Collection collection) {
    return indexManager().existsForNodes(getIndexName(collection));
  }

  @Override
  public GraphTraversal<Vertex, Vertex> getVerticesByDisplayName(Collection collection, String query) {
    return traversalFromIndex(collection, query);
  }

  @Override
  public GraphTraversal<Vertex, Vertex> getKeywordVertices(Collection collection, String query, String keywordType) {
    return traversalFromIndex(collection, query).has("keyword_type", keywordType);
  }

  private GraphTraversal<Vertex, Vertex> traversalFromIndex(Collection collection, String query) {

    Index<Node> index = indexManager().forNodes(getIndexName(collection));
    IndexHits<Node> hits = index.query("displayName", query);
    List<Long> ids = StreamSupport.stream(hits.spliterator(), false).map(h -> h.getId()).collect(toList());

    return tinkerpopGraphManager.getGraph().traversal().V(ids);
  }

  private IndexManager indexManager() {
    tinkerpopGraphManager.getGraphDatabase().beginTx();
    return tinkerpopGraphManager.getGraphDatabase().index();
  }

  private String getIndexName(Collection collection) {
    return collection.getCollectionName();
  }
}
