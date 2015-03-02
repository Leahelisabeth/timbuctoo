package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectValueConverterTest implements FieldConverterTest {
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final String FIELD_NAME = "objectValue";
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private Node nodeMock;
  private TestSystemEntityWrapper containingEntity;
  private Field field;
  private String propertyName;
  private ObjectValueFieldConverter instance;

  @Before
  public void setUp() throws Exception {
    nodeMock = mock(Node.class);
    containingEntity = new TestSystemEntityWrapper();
    field = TYPE.getDeclaredField(FIELD_NAME);
    propertyName = FIELD_TYPE.propertyName(TYPE, FIELD_NAME);

    instance = new ObjectValueFieldConverter();
    setupInstance(instance);
  }

  private void setupInstance(ObjectValueFieldConverter objectValueFieldWrapper) {
    objectValueFieldWrapper.setContainingType(TYPE);
    objectValueFieldWrapper.setField(field);
    objectValueFieldWrapper.setFieldType(FIELD_TYPE);
    objectValueFieldWrapper.setName(FIELD_NAME);
  }

  @Override
  @Test
  public void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    // setup
    Change change = new Change(87l, "userId", "vreId");
    String serializedValue = serializeValue(change);

    containingEntity.setObjectValue(change);

    // action
    instance.setNodeProperty(nodeMock, containingEntity);

    // verify
    verify(nodeMock).setProperty(propertyName, serializedValue);
  }

  private String serializeValue(Change change) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String serializedValue = objectMapper.writeValueAsString(change);
    return serializedValue;
  }

  @Override
  @Test
  public void addValueToNodeDoesNotSetIfTheValueIsNull() throws Exception {
    // setup
    containingEntity.setObjectValue(null);

    // action
    instance.setNodeProperty(nodeMock, containingEntity);

    // verify
    verify(nodeMock, never()).setProperty(anyString(), any());
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToNodeThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception {
    // setup
    ObjectValueFieldConverter instance = new ObjectValueFieldConverter() {
      @Override
      protected Object getFieldValue(Entity entity) throws IllegalArgumentException, IllegalAccessException {
        throw new IllegalAccessException();
      }
    };
    setupInstance(instance);

    // action
    instance.setNodeProperty(nodeMock, containingEntity);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToNodeThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception {
    // setup
    ObjectValueFieldConverter instance = new ObjectValueFieldConverter() {
      @Override
      protected Object getFieldValue(Entity entity) throws IllegalArgumentException, IllegalAccessException {
        throw new IllegalArgumentException();
      }
    };
    setupInstance(instance);

    // action
    instance.setNodeProperty(nodeMock, containingEntity);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToNodeThrowsAConversionExceptionIfGetFormatedValueThrowsAnIllegalArgumentException() throws Exception {
    // setup
    Change change = new Change(87l, "userId", "vreId");
    containingEntity.setObjectValue(change);

    // setup
    ObjectValueFieldConverter instance = new ObjectValueFieldConverter() {
      @Override
      protected Object getFormattedValue(Object fieldValue) throws IllegalArgumentException {
        throw new IllegalArgumentException();
      }
    };
    setupInstance(instance);

    // action
    instance.setNodeProperty(nodeMock, containingEntity);
  }

  @Override
  @Test
  public void addValueToEntitySetTheFieldOfTheEntityWithTheValue() throws Exception {
    // setup
    Change value = new Change(87l, "userId", "vreId");
    when(nodeMock.hasProperty(propertyName)).thenReturn(true);
    when(nodeMock.getProperty(propertyName)).thenReturn(serializeValue(value));

    // action
    instance.addValueToEntity(containingEntity, nodeMock);

    // verify
    assertThat(containingEntity.getObjectValue(), is(equalTo(value)));
    verify(nodeMock).hasProperty(propertyName);
    verify(nodeMock).getProperty(propertyName);
    verifyNoMoreInteractions(nodeMock);
  }

  @Override
  @Test
  public void addValueToEntityDoesNothingIfThePropertyDoesNotExist() throws Exception {
    // setup
    when(nodeMock.hasProperty(propertyName)).thenReturn(false);

    // action
    instance.addValueToEntity(containingEntity, nodeMock);

    // verify
    assertThat(containingEntity.getObjectValue(), is(nullValue()));
    verify(nodeMock).hasProperty(propertyName);
    verifyNoMoreInteractions(nodeMock);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnIllegalAccessExceptionIsThrown() throws Exception {
    // setup
    Change value = new Change(87l, "userId", "vreId");
    when(nodeMock.hasProperty(propertyName)).thenReturn(true);
    when(nodeMock.getProperty(propertyName)).thenReturn(serializeValue(value));

    ObjectValueFieldConverter instance = new ObjectValueFieldConverter() {
      @Override
      protected void fillField(Entity entity, Node node) throws IllegalArgumentException, IllegalAccessException {
        throw new IllegalAccessException();
      }
    };
    setupInstance(instance);

    // action
    instance.addValueToEntity(containingEntity, nodeMock);

  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnAnIllegalArgumentExceptionIsThrown() throws Exception {
    // setup
    Change value = new Change(87l, "userId", "vreId");
    when(nodeMock.hasProperty(propertyName)).thenReturn(true);
    when(nodeMock.getProperty(propertyName)).thenReturn(serializeValue(value));

    ObjectValueFieldConverter instance = new ObjectValueFieldConverter() {
      @Override
      protected void fillField(Entity entity, Node node) throws IllegalArgumentException, IllegalAccessException {
        throw new IllegalArgumentException();
      }
    };
    setupInstance(instance);

    // action
    instance.addValueToEntity(containingEntity, nodeMock);

  }

}
