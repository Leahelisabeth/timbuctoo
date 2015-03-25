package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SystemRelationshipType.VERSION_OF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class NodeDuplicatorTest {

  private GraphDatabaseService dbMock;
  private NodeDuplicator instance;

  @Before
  public void setup() {
    dbMock = mock(GraphDatabaseService.class);
    instance = new NodeDuplicator(dbMock);
  }

  @Test
  public void saveDuplicateCopiesAllTheLabelsOfTheNode() {
    // setup
    Label label1 = DynamicLabel.label("label1");
    Label label2 = DynamicLabel.label("label2");
    Node nodeToDuplicate = aNode().withLabel(label1).withLabel(label2).build();
    Node duplicatedNode = aNode().createdBy(dbMock);

    // action
    instance.saveDuplicate(nodeToDuplicate);

    // verify
    verify(duplicatedNode).addLabel(label1);
    verify(duplicatedNode).addLabel(label2);
  }

  @Test
  public void saveDuplicateCopiesAllThePropertiesOfTheNode() {
    // setup
    String id = "id";
    int revision = 1;
    Node nodeToDuplicate = aNode().withId(id).withRevision(revision).build();
    Node duplicatedNode = aNode().createdBy(dbMock);

    // action
    instance.saveDuplicate(nodeToDuplicate);

    // verify
    verify(duplicatedNode).setProperty(ID_PROPERTY_NAME, id);
    verify(duplicatedNode).setProperty(REVISION_PROPERTY_NAME, revision);
  }

  @Test
  public void saveDuplicateCopiesAllTheRelationshipsOftheNode() {
    // setup
    Node endNodeRel1 = aNode().build();
    DynamicRelationshipType relType1 = DynamicRelationshipType.withName("outgoing");
    Relationship outgoingRelationship = aRelationship()//
        .withEndNode(endNodeRel1)//
        .withType(relType1)//
        .build();

    Node startNodeRel2 = aNode().build();
    DynamicRelationshipType relType2 = DynamicRelationshipType.withName("incomming");
    Relationship incommingRelationship = aRelationship()//
        .withStartNode(startNodeRel2)//
        .withType(relType2)//
        .build();

    Node nodeToDuplicate = aNode().withOutgoingRelationShip(outgoingRelationship)//
        .withIncommingRelationShip(incommingRelationship)//
        .build();

    Node duplicatedNode = aNode().createdBy(dbMock);

    // action
    instance.saveDuplicate(nodeToDuplicate);

    // verify
    verify(duplicatedNode).createRelationshipTo(endNodeRel1, relType1);
    verify(startNodeRel2).createRelationshipTo(duplicatedNode, relType2);
  }

  @Test
  public void saveDuplicateCreatesARelationBetweenTheDuplicateAndTheOriginal() {
    // setup
    Node nodeToDuplicate = aNode().build();
    Node duplicatedNode = aNode().createdBy(dbMock);

    // action
    instance.saveDuplicate(nodeToDuplicate);

    // verify
    verify(duplicatedNode).createRelationshipTo(nodeToDuplicate, VERSION_OF);
  }
}
