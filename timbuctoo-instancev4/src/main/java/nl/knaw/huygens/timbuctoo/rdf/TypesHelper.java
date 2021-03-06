package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.database.tinkerpop.changelistener.AddLabelChangeListener;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Set;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;

public class TypesHelper {
  private final AddLabelChangeListener labelChangeListener;

  public TypesHelper() {
    this(new AddLabelChangeListener());
  }

  TypesHelper(AddLabelChangeListener labelChangeListener) {
    this.labelChangeListener = labelChangeListener;
  }

  public void addTypeInformation(Vertex vertex, Set<Collection> collections, Collection newCollection) {
    Stream<String> typesStream =
      collections.stream().map(collection -> collection.getDescription().getEntityTypeName());

    vertex.property("types", jsnA(typesStream.map(JsonBuilder::jsn)).toString());
    labelChangeListener.handleRdfLabelAdd(vertex, newCollection.getDescription().getEntityTypeName());
  }

  public void removeTypeInformation(Vertex vertex, Set<Collection> collections, Collection removedCollection) {
    Stream<String> typesStream =
      collections.stream().map(collection -> collection.getDescription().getEntityTypeName());

    vertex.property("types", jsnA(typesStream.map(JsonBuilder::jsn)).toString());
    labelChangeListener.handleRdfLabelRemove(vertex, removedCollection.getDescription().getEntityTypeName());
  }
}
