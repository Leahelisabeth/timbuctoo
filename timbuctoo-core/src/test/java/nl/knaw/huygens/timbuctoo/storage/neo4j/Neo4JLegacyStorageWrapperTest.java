package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubADomainEntityBuilder.aDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubARelationBuilder.aRelation;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.TestSystemEntityWrapperBuilder.aSystemEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

public class Neo4JLegacyStorageWrapperTest {

  private static final String RELATION_PROPERTY_NAME = SubARelation.SOURCE_ID;
  private static final String SYSTEM_ENTITY_PROPERTY = TestSystemEntityWrapper.ANOTATED_PROPERTY_NAME;
  private static final String PROPERTY_VALUE = "TEST";
  private static final String DOMAIN_ENTITY_PROPERTY_NAME = SubADomainEntity.VALUEA2_NAME;
  protected static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  protected static final String PRIMITIVE_DOMAIN_ENTITY_NAME = TypeNames.getInternalName(PRIMITIVE_DOMAIN_ENTITY_TYPE);
  protected static final Label PRIMITIVE_DOMAIN_ENTITY_LABEL = DynamicLabel.label(PRIMITIVE_DOMAIN_ENTITY_NAME);

  protected static final int FIRST_REVISION = 1;
  protected static final int SECOND_REVISION = 2;
  protected static final int THIRD_REVISION = 3;
  protected static final String ID = "id";
  protected static final Change CHANGE = new Change();
  protected static final String PID = "pid";
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;

  protected GraphDatabaseService dbMock;
  protected PropertyContainerConverterFactory propertyContainerConverterFactoryMock;
  protected Neo4JLegacyStorageWrapper instance;
  protected Transaction transactionMock;
  protected IdGenerator idGeneratorMock;
  protected NodeDuplicator nodeDuplicatorMock;
  protected RelationshipDuplicator relationshipDuplicatorMock;
  protected Neo4JStorage neo4JStorageMock;

  @Before
  public void setUp() throws Exception {
    setupDBTransaction();
    setupEntityConverterFactory();

    neo4JStorageMock = mock(Neo4JStorage.class);
    relationshipDuplicatorMock = mock(RelationshipDuplicator.class);
    nodeDuplicatorMock = mock(NodeDuplicator.class);
    idGeneratorMock = mock(IdGenerator.class);

    instance = new Neo4JLegacyStorageWrapper(neo4JStorageMock);
  }

  private void setupDBTransaction() {
    transactionMock = mock(Transaction.class);
    dbMock = mock(GraphDatabaseService.class);
    when(dbMock.beginTx()).thenReturn(transactionMock);
  }

  private void setupEntityConverterFactory() throws Exception {
    propertyContainerConverterFactoryMock = mock(PropertyContainerConverterFactory.class);
  }

  protected <T extends Entity> NodeConverter<T> propertyContainerConverterFactoryHasANodeConverterTypeFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    NodeConverter<T> nodeConverter = mock(NodeConverter.class);
    when(propertyContainerConverterFactoryMock.createForType(argThat(equalTo(type)))).thenReturn(nodeConverter);
    return nodeConverter;
  }

  protected void verifyNodeAndItsRelationAreDelete(Node node, Relationship relMock1, Relationship relMock2, InOrder inOrder) {
    inOrder.verify(node).getRelationships();
    inOrder.verify(relMock1).delete();
    inOrder.verify(relMock2).delete();
    inOrder.verify(node).delete();
  }

  protected void idGeneratorMockCreatesIDFor(Class<? extends Entity> type, String id) {
    when(idGeneratorMock.nextIdFor(type)).thenReturn(id);
  }

  @Test
  public void addDomainEntityDelegatesToNeo4JStorageAddDomainEntity() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();
    when(neo4JStorageMock.addDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE)).thenReturn(ID);

    // action
    String id = instance.addDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);

    // verify
    assertThat(id, is(equalTo(ID)));
    verify(neo4JStorageMock).addDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityThrowsAnExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();
    when(neo4JStorageMock.addDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE)).thenThrow(new StorageException());

    // action
    instance.addDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);
  }

  @Test
  public void getEntityForDomainEntityDelegatesToNeo4JStorageGetEntity() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();
    when(neo4JStorageMock.getEntity(DOMAIN_ENTITY_TYPE, ID)).thenReturn(entity);

    // action
    SubADomainEntity actualEntity = instance.getEntity(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void getEntityForDomainEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(neo4JStorageMock.getEntity(DOMAIN_ENTITY_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getEntity(DOMAIN_ENTITY_TYPE, ID);
  }

  @Test
  public void updateDomainEntityDelegatesToNeo4JStorage() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();

    // action
    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);

    // verify
    verify(neo4JStorageMock).updateDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);
  }

  @Test(expected = StorageException.class)
  public void updateDomainEntityThrowAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();

    doThrow(StorageException.class).when(neo4JStorageMock).updateDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);

    // action
    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);

  }

  @Test
  public void deleteDomainEntityIsDelegatedToNeo4JStorage() throws Exception {
    // action
    instance.deleteDomainEntity(DOMAIN_ENTITY_TYPE, ID, CHANGE);

    // verify
    verify(neo4JStorageMock).deleteDomainEntity(DOMAIN_ENTITY_TYPE, ID, CHANGE);
  }

  @Test(expected = StorageException.class)
  public void deleteDomainEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    doThrow(StorageException.class).when(neo4JStorageMock).deleteDomainEntity(DOMAIN_ENTITY_TYPE, ID, CHANGE);

    // action
    instance.deleteDomainEntity(DOMAIN_ENTITY_TYPE, ID, CHANGE);
  }

  @Test
  public void setPIDDelegatesToNeo4JStorageSetDomainEntityPID() throws Exception {
    // action
    instance.setPID(DOMAIN_ENTITY_TYPE, ID, PID);

    // verify
    verify(neo4JStorageMock).setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
  }

  @Test(expected = StorageException.class)
  public void setPIDThrowsAStorageExceptionIfTheDelegateDoes() throws Exception {
    // setup
    doThrow(StorageException.class).when(neo4JStorageMock).setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);

    // action
    instance.setPID(DOMAIN_ENTITY_TYPE, ID, PID);
  }

  @Test
  public void findItemByPropertyForDomainEntityDelegatesToNeo4JStorageFindEntityByProperty() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();
    when(neo4JStorageMock.findEntityByProperty(DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_PROPERTY_NAME, PROPERTY_VALUE))//
        .thenReturn(entity);

    // action
    SubADomainEntity actualEntity = instance.findItemByProperty(DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_PROPERTY_NAME, PROPERTY_VALUE);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void findItemByPropertyForDomainEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(neo4JStorageMock.findEntityByProperty(DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_PROPERTY_NAME, PROPERTY_VALUE))//
        .thenThrow(new StorageException());

    // action
    instance.findItemByProperty(DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_PROPERTY_NAME, PROPERTY_VALUE);
  }

  @Test
  public void countDomainEntityDelegatesToNeo4JStorage() {
    // setup
    long count = 2l;
    when(neo4JStorageMock.countEntities(DOMAIN_ENTITY_TYPE)).thenReturn(count);

    // action
    long actualCount = instance.count(DOMAIN_ENTITY_TYPE);

    // verify
    assertThat(actualCount, is(equalTo(count)));
  }

  @Test
  public void addDomainEntityForRelationDelegatesToNeo4JStorageAddRelation() throws Exception {
    // setup
    SubARelation relation = aRelation().build();
    when(neo4JStorageMock.addRelation(RELATION_TYPE, relation, CHANGE)).thenReturn(ID);

    // action
    String id = instance.addDomainEntity(RELATION_TYPE, relation, CHANGE);

    // verify
    assertThat(id, is(equalTo(ID)));
    verify(neo4JStorageMock).addRelation(RELATION_TYPE, relation, CHANGE);
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    SubARelation relation = aRelation().build();
    when(neo4JStorageMock.addRelation(RELATION_TYPE, relation, CHANGE)).thenThrow(new StorageException());

    // action
    instance.addDomainEntity(RELATION_TYPE, relation, CHANGE);
  }

  @Test
  public void getEntityForRelationDelegatesToNeo4JStorageGetRelation() throws Exception {
    // setup
    SubARelation relation = aRelation().build();
    when(neo4JStorageMock.getRelation(RELATION_TYPE, ID)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));
  }

  @Test
  public void updateDomainEntityForRelationDelegatesToNeo4JStorageAddRelation() throws Exception {
    // setup
    SubARelation entity = aRelation().build();

    // action
    instance.updateDomainEntity(RELATION_TYPE, entity, CHANGE);

    // verify
    verify(neo4JStorageMock).updateRelation(RELATION_TYPE, entity, CHANGE);
  }

  @Test(expected = StorageException.class)
  public void updateDomainEntityForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    SubARelation entity = aRelation().build();

    doThrow(StorageException.class).when(neo4JStorageMock).updateRelation(RELATION_TYPE, entity, CHANGE);

    // action
    instance.updateDomainEntity(RELATION_TYPE, entity, CHANGE);
  }

  @Test
  public void setPIDForRelationDelegatesToNeo4JStorageSetRelationPID() throws Exception {
    // action
    instance.setPID(RELATION_TYPE, ID, PID);

    // verify
    verify(neo4JStorageMock).setRelationPID(RELATION_TYPE, ID, PID);
  }

  @Test(expected = StorageException.class)
  public void setPIDForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    doThrow(StorageException.class).when(neo4JStorageMock).setRelationPID(RELATION_TYPE, ID, PID);

    // action
    instance.setPID(RELATION_TYPE, ID, PID);
  }

  @Test
  public void getRevisionForRelationDelegatesTheCallToNeo4JStorageGetRelationRevision() throws Exception {
    // setup
    when(neo4JStorageMock.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION)).thenReturn(aRelation().build());

    // action
    SubARelation relation = instance.getRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(notNullValue()));

    verify(neo4JStorageMock).getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);
  }

  @Test(expected = StorageException.class)
  public void getRevisionThrowsAStorageExceptionWhenNeo4JStorageGetRelationRevisionDoes() throws Exception {
    // setup
    when(neo4JStorageMock.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION)).thenThrow(new StorageException());

    // action
    instance.getRevision(RELATION_TYPE, ID, FIRST_REVISION);
  }

  @Test
  public void findItemByPropertyForRelationDelegatesToNeo4JStorageFindRelationByProperty() throws Exception {
    // setup
    SubARelation entity = aRelation().build();
    when(neo4JStorageMock.findRelationByProperty(RELATION_TYPE, RELATION_PROPERTY_NAME, PROPERTY_VALUE))//
        .thenReturn(entity);

    // action
    SubARelation actualEntity = instance.findItemByProperty(RELATION_TYPE, RELATION_PROPERTY_NAME, PROPERTY_VALUE);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void findItemByPropertyForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(neo4JStorageMock.findRelationByProperty(RELATION_TYPE, RELATION_PROPERTY_NAME, PROPERTY_VALUE))//
        .thenThrow(new StorageException());

    // action
    instance.findItemByProperty(RELATION_TYPE, RELATION_PROPERTY_NAME, PROPERTY_VALUE);
  }

  @Test
  public void countRelationDelegatesToNeo4JStorage() {
    // setup
    long count = 3l;
    when(neo4JStorageMock.countRelations(RELATION_TYPE)).thenReturn(count);

    // action
    long actualCount = instance.count(RELATION_TYPE);

    // verify
    assertThat(actualCount, is(equalTo(count)));
  }

  @Test
  public void addSystemEntityDelegatesToNeo4JStorage() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    when(neo4JStorageMock.addSystemEntity(SYSTEM_ENTITY_TYPE, entity)).thenReturn(ID);

    // action
    String actualId = instance.addSystemEntity(SYSTEM_ENTITY_TYPE, entity);

    // verify
    assertThat(actualId, is(equalTo(ID)));
    verify(neo4JStorageMock).addSystemEntity(SYSTEM_ENTITY_TYPE, entity);
  }

  @Test(expected = StorageException.class)
  public void addSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    TestSystemEntityWrapper entity = aSystemEntity().build();
    when(neo4JStorageMock.addSystemEntity(SYSTEM_ENTITY_TYPE, entity)).thenThrow(new StorageException());

    // action
    instance.addSystemEntity(SYSTEM_ENTITY_TYPE, entity);
  }

  @Test
  public void getEntityForSystemEntityDelegatesToNeo4JStorageGetEntity() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    when(neo4JStorageMock.getEntity(SYSTEM_ENTITY_TYPE, ID)).thenReturn(entity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void getEntityForSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(neo4JStorageMock.getEntity(SYSTEM_ENTITY_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
  }

  @Test
  public void getSystemEntitiesDelegatesToNeo4JStorage() throws StorageException {
    // setup
    @SuppressWarnings("unchecked")
    StorageIterator<TestSystemEntityWrapper> storageIteratorMock = mock(StorageIterator.class);
    when(neo4JStorageMock.getSystemEntities(SYSTEM_ENTITY_TYPE)).thenReturn(storageIteratorMock);

    // action
    StorageIterator<TestSystemEntityWrapper> actualSystemEntities = instance.getSystemEntities(SYSTEM_ENTITY_TYPE);

    // verify
    assertThat(actualSystemEntities, is(sameInstance(storageIteratorMock)));

  }

  @Test
  public void updateSystemEntityDelegatesToNeo4JStorage() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();

    // action
    instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, entity);

    // verify
    verify(neo4JStorageMock).updateSystemEntity(SYSTEM_ENTITY_TYPE, entity);
  }

  @Test(expected = StorageException.class)
  public void updateSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    doThrow(StorageException.class).when(neo4JStorageMock).updateSystemEntity(SYSTEM_ENTITY_TYPE, entity);

    // action
    instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, entity);
  }

  @Test
  public void deleteSystemEntityDelegatesToNeo4JStorage() throws Exception {
    // action
    instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    verify(neo4JStorageMock).deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);
  }

  @Test(expected = StorageException.class)
  public void deleteSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(neo4JStorageMock.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID)).thenThrow(new StorageException());
    // action
    instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);
  }

  @Test
  public void findItemByPropertyForSystemEntityDelegatesToNeo4JStorageFindEntityByProperty() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    when(neo4JStorageMock.findEntityByProperty(SYSTEM_ENTITY_TYPE, SYSTEM_ENTITY_PROPERTY, PROPERTY_VALUE))//
        .thenReturn(entity);

    // action
    TestSystemEntityWrapper actualEntity = instance.findItemByProperty(SYSTEM_ENTITY_TYPE, SYSTEM_ENTITY_PROPERTY, PROPERTY_VALUE);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void findItemByPropertyForSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(neo4JStorageMock.findEntityByProperty(SYSTEM_ENTITY_TYPE, SYSTEM_ENTITY_PROPERTY, PROPERTY_VALUE))//
        .thenThrow(new StorageException());

    // action
    instance.findItemByProperty(SYSTEM_ENTITY_TYPE, SYSTEM_ENTITY_PROPERTY, PROPERTY_VALUE);
  }

  @Test
  public void countSystemEntityDelegatesToNeo4JStorage() {
    // setup
    long count = 2l;
    when(neo4JStorageMock.countEntities(SYSTEM_ENTITY_TYPE)).thenReturn(count);

    // action
    long actualCount = instance.count(SYSTEM_ENTITY_TYPE);

    // verify
    assertThat(actualCount, is(equalTo(count)));
  }

  @Test
  public void closeDelegatesToTheNeo4JStorage() {
    // action
    instance.close();

    // verify
    verify(neo4JStorageMock).close();
  }

  @Test
  public void isAvailableReturnsTheValueTheNeo4JStorageReturns() {
    boolean available = true;
    // setup
    when(neo4JStorageMock.isAvailable()).thenReturn(available);

    // action
    boolean actualAvailable = instance.isAvailable();

    // verify
    assertThat(actualAvailable, is(equalTo(available)));

    verify(neo4JStorageMock).isAvailable();
  }

}