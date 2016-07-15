package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Sets;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class EntityTest {
  @Test
  public void addPropertyAddsThePropertyToTheEntityForEachCollectionTheEntityIsConnectedTo() {
    Vertex vertex = mock(Vertex.class);
    Collection collection1 = mock(Collection.class);
    Collection collection2 = mock(Collection.class);
    Entity entity = new Entity(vertex, Sets.newHashSet(collection1, collection2));
    String propName = "propName";
    String value = "value";

    entity.addProperty(propName, value);

    verify(collection1).addProperty(vertex, propName, value);
    verify(collection2).addProperty(vertex, propName, value);
  }

  @Ignore
  @Test
  @SuppressWarnings("unchecked")
  public void addToCollectionAddsTheEntityToTheCollection() {
    Vertex vertex = mock(Vertex.class);
    Collection newCollection = mock(Collection.class);
    Collection otherCollection = mock(Collection.class);
    Set<Collection> collections = Sets.newHashSet(otherCollection);
    TypesHelper typesHelper = mock(TypesHelper.class);
    Entity instance = new Entity(vertex, collections, typesHelper);

    instance.addToCollection(newCollection);

    verify(newCollection).add(argThat(is(vertex)), any());
    assertThat(collections, containsInAnyOrder(newCollection, otherCollection));
    ArgumentCaptor<Set> collectionsCaptor = ArgumentCaptor.forClass(Set.class);
    verify(typesHelper).updateTypeInformation(argThat(is(vertex)), (Set<Collection>) collectionsCaptor.capture());
    assertThat((Set<Collection>) collectionsCaptor.getValue(), containsInAnyOrder(otherCollection, newCollection));
  }

  @Ignore
  @Test
  public void addToCollectionAddsTheCurrentPropertiesOfTheEntityToTheNewCollection() {
    fail("Yet to be implemented");
  }

  @Ignore
  @Test
  public void addToCollectionAddsTheEntityToTheArchetypeOfTheCollection() {
    fail("Yet to be implemented");
  }

  @Test
  public void removeFromCollectionRemovesTheVertexOfTheEntityFromTheCollection() {
    Vertex vertex = mock(Vertex.class);
    Collection collectionToRemoveFrom = mock(Collection.class);
    Collection otherCollection = mock(Collection.class);
    Set<Collection> collections = Sets.newHashSet(collectionToRemoveFrom, otherCollection);
    Entity entity = new Entity(vertex, collections);

    entity.removeFromCollection(collectionToRemoveFrom);

    verify(collectionToRemoveFrom).remove(vertex);
    assertThat(collections, contains(otherCollection));
  }
}
