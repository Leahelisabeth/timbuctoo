package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

public class FieldWrapperFactoryTest {
  private static final String FIELD_NAME = "fieldName";
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final Class<ObjectValueFieldWrapper> OBJECT_WRAPPER_TYPE = ObjectValueFieldWrapper.class;
  private static final Class<SimpleValueFieldWrapper> SIMPLE_VALUE_WRAPPER_TYPE = SimpleValueFieldWrapper.class;
  private static final Class<NoOpFieldWrapper> NO_OP_WRAPPER_TYPE = NoOpFieldWrapper.class;
  private static final Class<SimpleCollectionFieldWrapper> SIMPLE_COLLECTION_FIELD_WRAPPER_TYPE = SimpleCollectionFieldWrapper.class;
  private static final TestSystemEntityWrapper TEST_SYSTEM_ENTITY = new TestSystemEntityWrapper();
  private FieldWrapperFactory instance;
  private PropertyBusinessRules propertyBusinessRulesMock;

  @Before
  public void setUp() {
    propertyBusinessRulesMock = mock(PropertyBusinessRules.class);

    instance = new FieldWrapperFactory(propertyBusinessRulesMock) {
      @Override
      protected FieldWrapper createSimpleValueFieldWrapper() {
        return mock(SIMPLE_VALUE_WRAPPER_TYPE);
      }

      @Override
      protected FieldWrapper createObjectValueFieldWrapper() {
        return mock(OBJECT_WRAPPER_TYPE);
      }

      @Override
      protected FieldWrapper createNoOpFieldWrapper() {
        return mock(NO_OP_WRAPPER_TYPE);
      }

      @Override
      protected FieldWrapper createSimpleCollectionFieldWrapper() {
        return mock(SIMPLE_COLLECTION_FIELD_WRAPPER_TYPE);
      }
    };
  }

  @Test
  public void wrapCreatesASimpleValueFieldWrapperIfTheFieldContainsAString() throws Exception {
    Field stringField = getField(TYPE, "stringValue");

    testWrap(TEST_SYSTEM_ENTITY, stringField, SIMPLE_VALUE_WRAPPER_TYPE);
  }

  @Test
  public void wrapCreatesASimpleValueFieldWrapperIfTheFieldContainsAPrimitive() throws Exception {
    Field intField = getField(TYPE, "primitiveValue");

    testWrap(TEST_SYSTEM_ENTITY, intField, SIMPLE_VALUE_WRAPPER_TYPE);
  }

  @Test
  public void wrapCreatesASimpleValueFieldWrapperIfTheFieldContainsAPrimitiveWrapper() throws Exception {
    Field longWrapperField = getField(TYPE, "primitiveWrapperValue");

    testWrap(TEST_SYSTEM_ENTITY, longWrapperField, SIMPLE_VALUE_WRAPPER_TYPE);
  }

  @Test
  public void wrapCreatesASimpleCollectionFieldWrapperIfTheFieldContainsAPrimitiveCollection() throws Exception {
    Field longWrapperField = getField(TYPE, "primitiveCollection");

    testWrap(TEST_SYSTEM_ENTITY, longWrapperField, SIMPLE_COLLECTION_FIELD_WRAPPER_TYPE);
  }

  @Test
  public void wrapCreatesASimpleCollectionFieldWrapperIfTheFieldContainsAStringCollection() throws Exception {
    Field longWrapperField = getField(TYPE, "stringCollection");

    testWrap(TEST_SYSTEM_ENTITY, longWrapperField, SIMPLE_COLLECTION_FIELD_WRAPPER_TYPE);
  }

  @Test
  public void wrapCreatesAnObjectFieldWrapperIfTheFieldContainsAnObjectValue() throws Exception {
    Field objectField = getField(TYPE, "objectValue");

    testWrap(TEST_SYSTEM_ENTITY, objectField, OBJECT_WRAPPER_TYPE);
  }

  @Test
  public void wrapCreatesAnObjectFieldWrapperIfTheFieldContainsAnObjectCollection() throws Exception {
    Field objectField = getField(TYPE, "objectCollection");

    testWrap(TEST_SYSTEM_ENTITY, objectField, OBJECT_WRAPPER_TYPE);
  }

  @Test
  public void wrapCreatesAnObjectFieldWrapperIfTheFieldContainsAMap() throws Exception {
    Field objectField = getField(TYPE, "map");

    testWrap(TEST_SYSTEM_ENTITY, objectField, OBJECT_WRAPPER_TYPE);
  }

  @Test
  public void wrapCreatesANoOpFieldWrapperIfTheFieldIsStatic() throws Exception {
    Field field = getField(TYPE, "staticField");
    testWrap(TEST_SYSTEM_ENTITY, field, NO_OP_WRAPPER_TYPE);
  }

  private void testWrap(TestSystemEntityWrapper testSystemEntity, Field field, Class<? extends FieldWrapper> wrapperType) {
    when(propertyBusinessRulesMock.getFieldType(TYPE, field)).thenReturn(FIELD_TYPE);
    when(propertyBusinessRulesMock.getFieldName(TYPE, field)).thenReturn(FIELD_NAME);

    // action
    FieldWrapper fieldWrapper = instance.wrap(TYPE, field);

    // verify
    assertThat(fieldWrapper, is(instanceOf(wrapperType)));

    verify(fieldWrapper).setField(field);
    verify(fieldWrapper).setContainingType(TYPE);
    verify(fieldWrapper).setFieldType(FIELD_TYPE);
    verify(fieldWrapper).setName(FIELD_NAME);
  }

  private Field getField(Class<? extends Entity> type, String fieldName) throws NoSuchFieldException {
    return type.getDeclaredField(fieldName);
  }
}
