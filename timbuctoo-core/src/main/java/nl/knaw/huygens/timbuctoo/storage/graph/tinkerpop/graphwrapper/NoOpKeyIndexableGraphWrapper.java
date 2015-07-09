package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.*;

import java.util.Set;

class NoOpKeyIndexableGraphWrapper implements KeyIndexableGraph {
  private Graph graph;

  public NoOpKeyIndexableGraphWrapper(Graph graph) {
    this.graph = graph;
  }

  @Override
  public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter... indexParameters) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Features getFeatures() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Vertex addVertex(Object id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Vertex getVertex(Object id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void removeVertex(Vertex vertex) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Iterable<Vertex> getVertices() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Iterable<Vertex> getVertices(String key, Object value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Edge getEdge(Object id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void removeEdge(Edge edge) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Iterable<Edge> getEdges() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Iterable<Edge> getEdges(String key, Object value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public GraphQuery query() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void shutdown() {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
