package nl.knaw.huygens.timbuctoo.storage.graph;

import static nl.knaw.huygens.timbuctoo.storage.graph.DomainEntityMatcher.likeDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.graph.SubADomainEntityBuilder.aDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.graph.SubARelationBuilder.aRelation;
import static nl.knaw.huygens.timbuctoo.storage.graph.TestSystemEntityWrapperBuilder.aSystemEntity;
import static nl.knaw.huygens.timbuctoo.storage.graph.TestSystemEntityWrapperMatcher.likeTestSystemEntityWrapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

import com.google.common.collect.Lists;

public class GraphLegacyStorageWrapperTest {

  private static final Class<Relation> PRIMITIVE_RELATION_TYPE = Relation.class;
  private static final String RELATION_PROPERTY_NAME = SubARelation.SOURCE_ID;
  private static final String SYSTEM_ENTITY_PROPERTY = TestSystemEntityWrapper.ANOTATED_PROPERTY_NAME;
  private static final String PROPERTY_VALUE = "TEST";
  private static final String DOMAIN_ENTITY_PROPERTY_NAME = SubADomainEntity.VALUEA2_NAME;
  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;

  private static final int FIRST_REVISION = 1;
  private static final int SECOND_REVISION = 2;
  private static final String ID = "id";
  private static final Change CHANGE = new Change();
  private static final String PID = "pid";
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;

  private GraphLegacyStorageWrapper instance;
  private IdGenerator idGeneratorMock;
  private GraphStorage graphStorageMock;

  @Before
  public void setUp() throws Exception {
    graphStorageMock = mock(GraphStorage.class);
    idGeneratorMock = mock(IdGenerator.class);

    instance = new GraphLegacyStorageWrapper(graphStorageMock, idGeneratorMock);
  }

  private void idGeneratorMockCreatesIDFor(Class<? extends Entity> type, String id) {
    when(idGeneratorMock.nextIdFor(type)).thenReturn(id);
  }

  @Test
  public void addDomainEntityManagesTheLifeCycleAndDelegatesToGraphStorageAddDomainEntity() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().withAPid().build();
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    // action
    String id = instance.addDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);

    // verify
    assertThat(id, is(equalTo(ID)));

    verify(graphStorageMock).addDomainEntity(//
        argThat(is(equalTo(DOMAIN_ENTITY_TYPE))), //
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE)//
            .withoutAPID()//
            .withId(ID) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)), //
        argThat(is(CHANGE)));
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityThrowsAnExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();
    doThrow(StorageException.class).when(graphStorageMock).addDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);

    // action
    instance.addDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);
  }

  @Test
  public void getEntityForDomainEntityDelegatesToGraphStorageGetEntity() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();
    when(graphStorageMock.getEntity(DOMAIN_ENTITY_TYPE, ID)).thenReturn(entity);

    // action
    SubADomainEntity actualEntity = instance.getEntity(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void getEntityForDomainEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(graphStorageMock.getEntity(DOMAIN_ENTITY_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getEntity(DOMAIN_ENTITY_TYPE, ID);
  }

  @Test
  public void getEntityOrDefaultVariationDelegatesToGraphStorageGetEntityIfTheVariantExists() throws Exception {
    variantExists();
    SubADomainEntity entity = aDomainEntity().build();
    when(graphStorageMock.getEntity(DOMAIN_ENTITY_TYPE, ID)).thenReturn(entity);

    // action
    SubADomainEntity foundEntity = instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(foundEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void getEntityOrDefaultVariationThrowsAStorageExceptionifGraphStorageGetEntityDoes() throws Exception {
    // setup
    variantExists();
    when(graphStorageMock.getEntity(DOMAIN_ENTITY_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, ID);

  }

  private void variantExists() {
    when(graphStorageMock.entityExists(DOMAIN_ENTITY_TYPE, ID)).thenReturn(true);
  }

  @Test
  public void getEntityOrDefaultVariationDelegatesToGraphStorageGetDefaultVariationIfTheVariantDoesNotExist() throws Exception {
    // setup
    variantDoesNotExist();
    SubADomainEntity entity = aDomainEntity().build();
    when(graphStorageMock.getDefaultVariation(DOMAIN_ENTITY_TYPE, ID)).thenReturn(entity);

    // action
    SubADomainEntity foundEntity = instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(foundEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void getEntityOrDefaultVariationThrowsAStorageExceptionifGraphStorageGetDefaultVariationDoes() throws Exception {
    // setup
    variantDoesNotExist();
    when(graphStorageMock.getDefaultVariation(DOMAIN_ENTITY_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, ID);
  }

  @Test
  public void getDomainEntitiesDelegatesToGraphStorageGetEntities() throws StorageException {
    // setup
    @SuppressWarnings("unchecked")
    StorageIterator<SubADomainEntity> storageIteratorMock = mock(StorageIterator.class);
    when(graphStorageMock.getEntities(DOMAIN_ENTITY_TYPE)).thenReturn(storageIteratorMock);

    // action
    StorageIterator<SubADomainEntity> actualSystemEntities = instance.getDomainEntities(DOMAIN_ENTITY_TYPE);

    // verify
    assertThat(actualSystemEntities, is(sameInstance(storageIteratorMock)));
  }

  @Test(expected = StorageException.class)
  public void getDomainEntitiesThrowsAnExceptionWhenTheDelegateDoes() throws StorageException {
    // setup
    when(graphStorageMock.getEntities(DOMAIN_ENTITY_TYPE)).thenThrow(new StorageException());

    // action
    instance.getDomainEntities(DOMAIN_ENTITY_TYPE);

  }

  @Test
  public void getAllVariationsDelegatesToGraphStorage() throws Exception {
    // setup
    List<BaseDomainEntity> variations = Lists.newArrayList();
    when(graphStorageMock.getAllVariations(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID)).thenReturn(variations);

    // action
    List<BaseDomainEntity> actualVariations = instance.getAllVariations(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualVariations, is(sameInstance(variations)));
  }

  @Test(expected = StorageException.class)
  public void getAllVariationsThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(graphStorageMock.getAllVariations(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getAllVariations(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID);

  }

  @Test
  public void getAllVariationsForRelationDelegatesToGraphStorage() throws Exception {
    // setup
    List<Relation> variations = Lists.newArrayList();
    when(graphStorageMock.getAllVariationsOfRelation(PRIMITIVE_RELATION_TYPE, ID)).thenReturn(variations);

    // action
    List<Relation> actualVariations = instance.getAllVariations(PRIMITIVE_RELATION_TYPE, ID);

    // verify
    assertThat(actualVariations, is(sameInstance(variations)));
  }

  @Test(expected = StorageException.class)
  public void getAllVariationsForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(graphStorageMock.getAllVariationsOfRelation(PRIMITIVE_RELATION_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getAllVariations(PRIMITIVE_RELATION_TYPE, ID);
  }

  @Test
  public void updateDomainEntityDelegatesToGraphStorage() throws Exception {
    // setup
    Change oldModified = new Change();
    SubADomainEntity entity = aDomainEntity() //
        .withId(ID) //
        .withAPid() //
        .withModified(oldModified)//
        .withRev(FIRST_REVISION) //
        .build();
    entityAndVariantExist();

    // action
    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);

    // verify
    InOrder inOrder = inOrder(graphStorageMock);
    inOrder.verify(graphStorageMock).removePropertyFromEntity(DOMAIN_ENTITY_TYPE, ID, DomainEntity.PID);
    inOrder.verify(graphStorageMock).updateEntity( //
        argThat(is(equalTo(DOMAIN_ENTITY_TYPE))), //
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE) //
            .withId(ID) //
            .withoutAPID() //
            .withRevision(SECOND_REVISION) //
            .withAModifiedValueNotEqualTo(oldModified)));
  }

  private void entityAndVariantExist() {
    when(graphStorageMock.entityExists(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID)).thenReturn(true);
    variantExists();
  }

  @Test(expected = StorageException.class)
  public void updateDomainEntityThrowAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().withId(ID).build();
    entityAndVariantExist();

    doThrow(StorageException.class).when(graphStorageMock).updateEntity(DOMAIN_ENTITY_TYPE, entity);

    // action
    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);

  }

  @Test
  public void updateDomainEntityDelegatesToGraphStoragesAddNewVariantWhenTheVariantDoesNotExist() throws Exception {
    // setup
    Change oldModified = new Change();
    SubADomainEntity entity = aDomainEntity() //
        .withId(ID) //
        .withAPid() //
        .withModified(oldModified)//
        .withRev(FIRST_REVISION) //
        .build();
    entityAndVariantExist();
    variantDoesNotExist();

    // action
    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);

    // verify
    InOrder inOrder = inOrder(graphStorageMock);
    inOrder.verify(graphStorageMock).removePropertyFromEntity(DOMAIN_ENTITY_TYPE, ID, DomainEntity.PID);
    verify(graphStorageMock).addVariant(//
        argThat(is(equalTo(DOMAIN_ENTITY_TYPE))), //
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE) //
            .withId(ID) //
            .withoutAPID() //
            .withRevision(SECOND_REVISION) //
            .withAModifiedValueNotEqualTo(oldModified)));
  }

  private void variantDoesNotExist() {
    when(graphStorageMock.entityExists(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID)).thenReturn(true);
    when(graphStorageMock.entityExists(DOMAIN_ENTITY_TYPE, ID)).thenReturn(false);
  }

  @Test(expected = StorageException.class)
  public void updateDomainEntityThrowsAStorageExceptionWhenGraphStoragesAddNewVariantWhenTheVariantDoesNotExist() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().withId(ID).build();
    variantDoesNotExist();

    doThrow(StorageException.class).when(graphStorageMock).addVariant(DOMAIN_ENTITY_TYPE, entity);

    // action
    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);
  }

  @Test(expected = UpdateException.class)
  public void updateDomainEntityThrowsAnUpdateExceptionWhenTheEntityDoesNotExist() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().withId(ID).build();
    entityDoesNotExist();

    // action
    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);
  }

  private void entityDoesNotExist() {
    when(graphStorageMock.entityExists(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID)).thenReturn(false);
  }

  @Test
  public void deleteDomainEntityIsDelegatedToGraphStorage() throws Exception {
    // action
    instance.deleteDomainEntity(DOMAIN_ENTITY_TYPE, ID, CHANGE);

    // verify
    verify(graphStorageMock).deleteDomainEntity(DOMAIN_ENTITY_TYPE, ID);
  }

  @Test(expected = StorageException.class)
  public void deleteDomainEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    doThrow(StorageException.class).when(graphStorageMock).deleteDomainEntity(DOMAIN_ENTITY_TYPE, ID);

    // action
    instance.deleteDomainEntity(DOMAIN_ENTITY_TYPE, ID, CHANGE);
  }

  @Test
  public void deleteNonPersistentCallsDeleteDomainEntityOnGraphStorageMockForEveryIdInTheListWhenADomainEntityNeedsToBeDeleted() throws Exception {
    // setup
    String id1 = "id1";
    String id2 = "id2";
    List<String> ids = Lists.newArrayList(id1, id2);

    // action
    instance.deleteNonPersistent(DOMAIN_ENTITY_TYPE, ids);

    // verify
    verifyEntityDeleted(id1);
    verifyEntityDeleted(id2);
  }

  private void verifyEntityDeleted(String id) throws StorageException {
    verify(graphStorageMock).deleteDomainEntity(//
        argThat(equalTo(PRIMITIVE_DOMAIN_ENTITY_TYPE)), //
        argThat(equalTo(id)));
  }

  @Test(expected = StorageException.class)
  public void deleteNonPersistentThrowsAStorageExceptionWhenADomainEntityCannotBeDeleted() throws Exception {
    // setup
    String id1 = "id1";
    String id2 = "id2";
    List<String> ids = Lists.newArrayList(id1, id2);

    doThrow(StorageException.class).when(graphStorageMock).deleteDomainEntity( //
        argThat(equalTo(PRIMITIVE_DOMAIN_ENTITY_TYPE)), //
        argThat(equalTo(id1)));

    // action
    instance.deleteNonPersistent(DOMAIN_ENTITY_TYPE, ids);
  }

  @Test
  public void deleteNonPersistentDoesNothingIfTheTypeIsARelationBecauseRelationsAreDeletedWithTheDomainEntity() throws Exception {
    // setup
    String id1 = "id1";
    String id2 = "id2";
    List<String> ids = Lists.newArrayList(id1, id2);

    // action
    instance.deleteNonPersistent(RELATION_TYPE, ids);

    // verify
    verifyZeroInteractions(graphStorageMock);
  }

  @Test
  public void deleteVariationDelegatesToGraphStorage() throws Exception {
    // setup
    Change oldModified = new Change();
    SubADomainEntity entity = aDomainEntity() //
        .withId(ID) //
        .withAPid() //
        .withModified(oldModified)//
        .withRev(FIRST_REVISION) //
        .build();
    when(graphStorageMock.getEntity(DOMAIN_ENTITY_TYPE, ID)).thenReturn(entity);

    // action
    instance.deleteVariation(DOMAIN_ENTITY_TYPE, ID, CHANGE);

    // verify
    verify(graphStorageMock).deleteVariant(argThat(//
        likeDomainEntity(DOMAIN_ENTITY_TYPE) //
            .withId(ID) //
            .withoutAPID() //
            .withRevision(SECOND_REVISION) //
            .withAModifiedValueNotEqualTo(oldModified)));
  }

  @Test(expected = StorageException.class)
  public void deleteVariationThrowsAStorageExceptionWhenGetEntityDoes() throws Exception {
    // setup
    when(graphStorageMock.getEntity(DOMAIN_ENTITY_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.deleteVariation(DOMAIN_ENTITY_TYPE, ID, CHANGE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteVariationThrowsAnIllegalArgumentExceptionWhenTheTypeIsAPrimitive() throws Exception {
    // action
    instance.deleteVariation(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, CHANGE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteVariationThrowsAnIllegalArgumentExceptionWhenGraphStorageDoes() throws Exception {
    deleteVariationThrowsAnExceptionWhenTheGraphStorageDoes(IllegalArgumentException.class);
  }

  @Test(expected = NoSuchEntityException.class)
  public void deleteVariationThrowsANoSuchEntityExceptionWhenGraphStorageDoes() throws Exception {
    deleteVariationThrowsAnExceptionWhenTheGraphStorageDoes(NoSuchEntityException.class);
  }

  @Test(expected = NoSuchEntityException.class)
  public void deleteVariationThrowsANoSuchEntityExceptionWhenTheEntityCannotBeFound() throws Exception {
    // setup
    when(graphStorageMock.getEntity(DOMAIN_ENTITY_TYPE, ID)).thenReturn(null);

    // action
    instance.deleteVariation(DOMAIN_ENTITY_TYPE, ID, CHANGE);
  }

  @Test(expected = StorageException.class)
  public void deleteVariationThrowsAStorageExceptionWhenGraphStorageDoes() throws Exception {
    deleteVariationThrowsAnExceptionWhenTheGraphStorageDoes(StorageException.class);
  }

  private void deleteVariationThrowsAnExceptionWhenTheGraphStorageDoes(Class<? extends Exception> exceptionToThrow) throws StorageException, NoSuchEntityException {
    // setup
    SubADomainEntity entity = aDomainEntity().build();
    when(graphStorageMock.getEntity(DOMAIN_ENTITY_TYPE, ID)).thenReturn(entity);
    doThrow(exceptionToThrow).when(graphStorageMock).deleteVariant(argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE)));

    instance.deleteVariation(DOMAIN_ENTITY_TYPE, ID, CHANGE);
  }

  @Test
  public void setPIDDelegatesToGraphStorageSetDomainEntityPID() throws Exception {
    // action
    instance.setPID(DOMAIN_ENTITY_TYPE, ID, PID);

    // verify
    verify(graphStorageMock).setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
  }

  @Test(expected = StorageException.class)
  public void setPIDThrowsAStorageExceptionIfTheDelegateDoes() throws Exception {
    // setup
    doThrow(StorageException.class).when(graphStorageMock).setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);

    // action
    instance.setPID(DOMAIN_ENTITY_TYPE, ID, PID);
  }

  @Test
  public void getAllIdsWithoutPIDForDomainEntityDelegatesToGraphStorageGetIdsOfNonPersistentDomainEntities() throws Exception {
    // setup
    List<String> ids = Lists.newArrayList();
    when(graphStorageMock.getIdsOfNonPersistentDomainEntities(DOMAIN_ENTITY_TYPE)).thenReturn(ids);

    // action
    List<String> foundIds = instance.getAllIdsWithoutPIDOfType(DOMAIN_ENTITY_TYPE);

    // verify
    assertThat(foundIds, is(sameInstance(ids)));
  }

  @Test
  public void findItemByPropertyForDomainEntityDelegatesToGraphStorageFindEntityByProperty() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();
    when(graphStorageMock.findEntityByProperty(DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_PROPERTY_NAME, PROPERTY_VALUE))//
        .thenReturn(entity);

    // action
    SubADomainEntity actualEntity = instance.findItemByProperty(DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_PROPERTY_NAME, PROPERTY_VALUE);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void findItemByPropertyForDomainEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(graphStorageMock.findEntityByProperty(DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_PROPERTY_NAME, PROPERTY_VALUE))//
        .thenThrow(new StorageException());

    // action
    instance.findItemByProperty(DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_PROPERTY_NAME, PROPERTY_VALUE);
  }

  @Test
  public void countDomainEntityDelegatesToGraphStorage() {
    // setup
    long count = 2l;
    when(graphStorageMock.countEntities(DOMAIN_ENTITY_TYPE)).thenReturn(count);

    // action
    long actualCount = instance.count(DOMAIN_ENTITY_TYPE);

    // verify
    assertThat(actualCount, is(equalTo(count)));
  }

  @Test
  public void entityExistsForDomainEntityDelegatesToGraphStorage() throws Exception {
    // setup
    boolean entityExists = true;
    when(graphStorageMock.entityExists(DOMAIN_ENTITY_TYPE, ID)).thenReturn(entityExists);

    // action
    boolean actualEntityExists = instance.entityExists(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntityExists, is(entityExists));
  }

  @Test
  public void addDomainEntityForRelationManagesTheLifeCylceDelegatesToGraphStorageAddRelation() throws Exception {
    // setup
    SubARelation relation = aRelation().withAPID().build();

    idGeneratorMockCreatesIDFor(RELATION_TYPE, ID);

    // action
    String id = instance.addDomainEntity(RELATION_TYPE, relation, CHANGE);

    // verify
    assertThat(id, is(equalTo(ID)));
    verify(graphStorageMock).addRelation(//
        argThat(is(equalTo(RELATION_TYPE))), //
        argThat(likeDomainEntity(RELATION_TYPE)//
            .withId(ID) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)//
            .withoutAPID()), //
        argThat(is(CHANGE)));
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    SubARelation relation = aRelation().build();
    doThrow(StorageException.class).when(graphStorageMock).addRelation(RELATION_TYPE, relation, CHANGE);

    // action
    instance.addDomainEntity(RELATION_TYPE, relation, CHANGE);
  }

  @Test
  public void getEntityForRelationDelegatesToGraphStorageGetRelation() throws Exception {
    // setup
    SubARelation relation = aRelation().build();
    when(graphStorageMock.getRelation(RELATION_TYPE, ID)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));
  }

  @Test
  public void updateDomainEntityForRelationDelegatesToGraphStorageAddRelation() throws Exception {
    // setup
    Change oldModified = new Change();
    SubARelation entity = aRelation() //
        .withId(ID) //
        .withAPID() //
        .withModified(oldModified) //
        .withRevision(FIRST_REVISION) //
        .build();

    // action
    instance.updateDomainEntity(RELATION_TYPE, entity, CHANGE);

    // verify
    InOrder inOrder = inOrder(graphStorageMock);
    inOrder.verify(graphStorageMock).removePropertyFromRelation(RELATION_TYPE, ID, DomainEntity.PID);
    inOrder.verify(graphStorageMock).updateRelation( //
        argThat(is(equalTo(RELATION_TYPE))), //
        argThat(likeDomainEntity(RELATION_TYPE) //
            .withId(ID) //
            .withoutAPID() //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)), //
        argThat(is(CHANGE)));
  }

  @Test(expected = StorageException.class)
  public void updateDomainEntityForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    SubARelation entity = aRelation().build();

    doThrow(StorageException.class).when(graphStorageMock).updateRelation(RELATION_TYPE, entity, CHANGE);

    // action
    instance.updateDomainEntity(RELATION_TYPE, entity, CHANGE);
  }

  @Test
  public void setPIDForRelationDelegatesToGraphStorageSetRelationPID() throws Exception {
    // action
    instance.setPID(RELATION_TYPE, ID, PID);

    // verify
    verify(graphStorageMock).setRelationPID(RELATION_TYPE, ID, PID);
  }

  @Test(expected = StorageException.class)
  public void setPIDForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    doThrow(StorageException.class).when(graphStorageMock).setRelationPID(RELATION_TYPE, ID, PID);

    // action
    instance.setPID(RELATION_TYPE, ID, PID);
  }

  @Test
  public void getRevisionForRelationDelegatesTheCallToGraphStorageGetRelationRevision() throws Exception {
    // setup
    when(graphStorageMock.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION)).thenReturn(aRelation().build());

    // action
    SubARelation relation = instance.getRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(notNullValue()));

    verify(graphStorageMock).getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);
  }

  @Test(expected = StorageException.class)
  public void getRevisionThrowsAStorageExceptionWhenGraphStorageGetRelationRevisionDoes() throws Exception {
    // setup
    when(graphStorageMock.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION)).thenThrow(new StorageException());

    // action
    instance.getRevision(RELATION_TYPE, ID, FIRST_REVISION);
  }

  @Test
  public void getRelationsByEntityIdDelegatesToGraphStorage() throws Exception {
    // setup
    @SuppressWarnings("unchecked")
    StorageIterator<SubARelation> storageIteratorMock = mock(StorageIterator.class);
    when(graphStorageMock.getRelationsByEntityId(RELATION_TYPE, ID)).thenReturn(storageIteratorMock);

    // action
    StorageIterator<SubARelation> actualStorageIterator = instance.getRelationsByEntityId(RELATION_TYPE, ID);

    // verify
    assertThat(actualStorageIterator, is(sameInstance(storageIteratorMock)));
  }

  @Test(expected = StorageException.class)
  public void getRelationsByEntityIdThrowsAnExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(graphStorageMock.getRelationsByEntityId(RELATION_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getRelationsByEntityId(RELATION_TYPE, ID);
  }

  @Test
  public void getRelationIdsReturnsAListWithRelationsIdsThatBelongToTheEntities() throws Exception {
    // setup
    String relId1 = "relId1";
    String relId2 = "relId2";
    findsForIdRelationsWithId(ID, relId1, relId2);

    String relId3 = "relId3";
    String entityId2 = "entityId2";
    findsForIdRelationsWithId(entityId2, relId3, relId2);

    // action
    List<String> foundIds = instance.getRelationIds(Lists.newArrayList(ID, entityId2));

    // verify
    assertThat(foundIds, hasSize(3));
    assertThat(foundIds, containsInAnyOrder(relId1, relId2, relId3));
  }

  @Test(expected = StorageException.class)
  public void getRelationsIdsThrowsAStorageExceptionWhenTheRetrievalCausesAnExceptionToBeThrown() throws Exception {
    // setup
    when(graphStorageMock.getRelationsByEntityId(PRIMITIVE_RELATION_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getRelationIds(Lists.newArrayList(ID));
  }

  private void findsForIdRelationsWithId(String entityId, String relId1, String relId2) throws StorageException {
    @SuppressWarnings("unchecked")
    StorageIterator<Relation> relationIterator = mock(StorageIterator.class);
    SubARelation relation1 = aRelation().withId(relId1).build();
    SubARelation relation2 = aRelation().withId(relId2).build();
    when(relationIterator.hasNext()).thenReturn(true, true, false);
    when(relationIterator.next()).thenReturn(relation1, relation2);
    when(graphStorageMock.getRelationsByEntityId(PRIMITIVE_RELATION_TYPE, entityId)).thenReturn(relationIterator);
  }

  @Test
  public void getAllIdsWithoutPIDForRelationDelegatesToGraphStorageGetIdsOfNonPersistentRelations() throws Exception {
    // setup
    List<String> ids = Lists.newArrayList();
    when(graphStorageMock.getIdsOfNonPersistentRelations(RELATION_TYPE)).thenReturn(ids);

    // action
    List<String> foundIds = instance.getAllIdsWithoutPIDOfType(RELATION_TYPE);

    // verify
    assertThat(foundIds, is(sameInstance(ids)));
  }

  @Test
  public void findItemByPropertyForRelationDelegatesToGraphStorageFindRelationByProperty() throws Exception {
    // setup
    SubARelation entity = aRelation().build();
    when(graphStorageMock.findRelationByProperty(RELATION_TYPE, RELATION_PROPERTY_NAME, PROPERTY_VALUE))//
        .thenReturn(entity);

    // action
    SubARelation actualEntity = instance.findItemByProperty(RELATION_TYPE, RELATION_PROPERTY_NAME, PROPERTY_VALUE);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void findItemByPropertyForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(graphStorageMock.findRelationByProperty(RELATION_TYPE, RELATION_PROPERTY_NAME, PROPERTY_VALUE))//
        .thenThrow(new StorageException());

    // action
    instance.findItemByProperty(RELATION_TYPE, RELATION_PROPERTY_NAME, PROPERTY_VALUE);
  }

  @Test
  public void countRelationDelegatesToGraphStorage() {
    // setup
    long count = 3l;
    when(graphStorageMock.countRelations(RELATION_TYPE)).thenReturn(count);

    // action
    long actualCount = instance.count(RELATION_TYPE);

    // verify
    assertThat(actualCount, is(equalTo(count)));
  }

  @Test
  public void entityExistsForRelationDelegatesToGraphStorageRelationExists() throws Exception {
    // setup
    boolean relationExists = true;
    when(graphStorageMock.relationExists(RELATION_TYPE, ID)).thenReturn(relationExists);

    // action
    boolean actualEntityExists = instance.entityExists(RELATION_TYPE, ID);

    // verify
    assertThat(actualEntityExists, is(relationExists));
  }

  @Test
  public void addSystemEntityManagesLifeCyleDelegatesToGraphStorage() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();

    idGeneratorMockCreatesIDFor(SYSTEM_ENTITY_TYPE, ID);

    // action
    String actualId = instance.addSystemEntity(SYSTEM_ENTITY_TYPE, entity);

    // verify
    assertThat(actualId, is(equalTo(ID)));
    verify(graphStorageMock).addSystemEntity(//
        argThat(is(equalTo(SYSTEM_ENTITY_TYPE))), //
        argThat(likeTestSystemEntityWrapper() //
            .withId(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
  }

  @Test
  public void findRelationDelegatesToGraphStorage() throws Exception {
    String sourceId = "sourceId";
    String targetId = "targetId";
    String relationTypeId = "relationTypeId";
    SubARelation relation = aRelation().build();

    when(graphStorageMock.findRelation(RELATION_TYPE, sourceId, targetId, relationTypeId))//
        .thenReturn(relation);

    // action
    SubARelation foundRelation = instance.findRelation(RELATION_TYPE, sourceId, targetId, relationTypeId);

    // verify
    assertThat(foundRelation, is(sameInstance(relation)));
  }

  @Test(expected = StorageException.class)
  public void findRelationThrowsAnExceptionIfTheDelegateDoes() throws Exception {
    String sourceId = "sourceId";
    String targetId = "targetId";
    String relationTypeId = "relationTypeId";

    when(graphStorageMock.findRelation(RELATION_TYPE, sourceId, targetId, relationTypeId)) //
        .thenThrow(new StorageException());

    // action
    instance.findRelation(RELATION_TYPE, sourceId, targetId, relationTypeId);

  }

  @Test
  public void findRelationsDelegatesToGraphStorage() throws Exception {
    String sourceId = "sourceId";
    String targetId = "targetId";
    String relationTypeId = "relationTypeId";

    @SuppressWarnings("unchecked")
    StorageIterator<SubARelation> relations = mock(StorageIterator.class);

    when(graphStorageMock.findRelations(RELATION_TYPE, sourceId, targetId, relationTypeId))//
        .thenReturn(relations);

    // action
    StorageIterator<SubARelation> actualRelations = instance.findRelations(RELATION_TYPE, sourceId, targetId, relationTypeId);

    // verify
    assertThat(actualRelations, is(sameInstance(relations)));
  }

  @Test(expected = StorageException.class)
  public void addSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    TestSystemEntityWrapper entity = aSystemEntity().build();
    doThrow(StorageException.class).when(graphStorageMock).addSystemEntity(SYSTEM_ENTITY_TYPE, entity);

    // action
    instance.addSystemEntity(SYSTEM_ENTITY_TYPE, entity);
  }

  @Test
  public void getEntityForSystemEntityDelegatesToGraphStorageGetEntity() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    when(graphStorageMock.getEntity(SYSTEM_ENTITY_TYPE, ID)).thenReturn(entity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void getEntityForSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(graphStorageMock.getEntity(SYSTEM_ENTITY_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
  }

  @Test
  public void getSystemEntitiesDelegatesToGraphStorageGetEntities() throws StorageException {
    // setup
    @SuppressWarnings("unchecked")
    StorageIterator<TestSystemEntityWrapper> storageIteratorMock = mock(StorageIterator.class);
    when(graphStorageMock.getEntities(SYSTEM_ENTITY_TYPE)).thenReturn(storageIteratorMock);

    // action
    StorageIterator<TestSystemEntityWrapper> actualSystemEntities = instance.getSystemEntities(SYSTEM_ENTITY_TYPE);

    // verify
    assertThat(actualSystemEntities, is(sameInstance(storageIteratorMock)));
  }

  @Test(expected = StorageException.class)
  public void getSystemEntitiesThrowsAnExceptionWhenTheDelegateDoes() throws StorageException {
    // setup
    when(graphStorageMock.getEntities(SYSTEM_ENTITY_TYPE)).thenThrow(new StorageException());

    // action
    instance.getSystemEntities(SYSTEM_ENTITY_TYPE);

  }

  @Test
  public void updateSystemEntityDelegatesToGraphStorage() throws Exception {
    // setup
    Change oldModified = new Change();
    TestSystemEntityWrapper entity = aSystemEntity() //
        .withId(ID) //
        .withModified(oldModified) //
        .withRev(FIRST_REVISION) //
        .build();

    // action
    instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, entity);

    // verify
    verify(graphStorageMock).updateEntity( //
        argThat(is(equalTo(SYSTEM_ENTITY_TYPE))), //
        argThat(likeTestSystemEntityWrapper() //
            .withId(ID) //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)));
  }

  @Test(expected = StorageException.class)
  public void updateSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    doThrow(StorageException.class).when(graphStorageMock).updateEntity(SYSTEM_ENTITY_TYPE, entity);

    // action
    instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, entity);
  }

  @Test
  public void deleteSystemEntityDelegatesToGraphStorage() throws Exception {
    // action
    instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    verify(graphStorageMock).deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);
  }

  @Test(expected = StorageException.class)
  public void deleteSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(graphStorageMock.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID)).thenThrow(new StorageException());
    // action
    instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);
  }

  @Test
  public void findItemByPropertyForSystemEntityDelegatesToGraphStorageFindEntityByProperty() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    when(graphStorageMock.findEntityByProperty(SYSTEM_ENTITY_TYPE, SYSTEM_ENTITY_PROPERTY, PROPERTY_VALUE))//
        .thenReturn(entity);

    // action
    TestSystemEntityWrapper actualEntity = instance.findItemByProperty(SYSTEM_ENTITY_TYPE, SYSTEM_ENTITY_PROPERTY, PROPERTY_VALUE);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void findItemByPropertyForSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(graphStorageMock.findEntityByProperty(SYSTEM_ENTITY_TYPE, SYSTEM_ENTITY_PROPERTY, PROPERTY_VALUE))//
        .thenThrow(new StorageException());

    // action
    instance.findItemByProperty(SYSTEM_ENTITY_TYPE, SYSTEM_ENTITY_PROPERTY, PROPERTY_VALUE);
  }

  @Test
  public void countSystemEntityDelegatesToGraphStorage() {
    // setup
    long count = 2l;
    when(graphStorageMock.countEntities(SYSTEM_ENTITY_TYPE)).thenReturn(count);

    // action
    long actualCount = instance.count(SYSTEM_ENTITY_TYPE);

    // verify
    assertThat(actualCount, is(equalTo(count)));
  }

  @Test
  public void entityExistsForSystemEntityDelegatesToGraphStorage() throws Exception {
    // setup
    boolean entityExists = true;
    when(graphStorageMock.entityExists(SYSTEM_ENTITY_TYPE, ID)).thenReturn(entityExists);

    // action
    boolean actualEntityExists = instance.entityExists(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntityExists, is(entityExists));
  }

  @Test
  public void closeDelegatesToTheGraphStorage() {
    // action
    instance.close();

    // verify
    verify(graphStorageMock).close();
  }

  @Test
  public void isAvailableReturnsTheValueTheGraphStorageReturns() {
    boolean available = true;
    // setup
    when(graphStorageMock.isAvailable()).thenReturn(available);

    // action
    boolean actualAvailable = instance.isAvailable();

    // verify
    assertThat(actualAvailable, is(equalTo(available)));

    verify(graphStorageMock).isAvailable();
  }

}