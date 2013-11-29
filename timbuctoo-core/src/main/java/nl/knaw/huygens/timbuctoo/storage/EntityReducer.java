package nl.knaw.huygens.timbuctoo.storage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoChanges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class EntityReducer {

  private static final Logger LOG = LoggerFactory.getLogger(EntityReducer.class);

  protected static final String BASE_MODEL_PACKAGE = "model";

  protected final TypeRegistry typeRegistry;
  protected final ObjectMapper jsonMapper;
  protected final FieldMapper fieldMapper;

  public EntityReducer(TypeRegistry registry) {
    typeRegistry = registry;
    jsonMapper = new ObjectMapper();
    fieldMapper = new FieldMapper();
  }

  public <T extends Entity> T reduceVariation(Class<T> type, JsonNode tree) throws StorageException, JsonProcessingException {
    return reduceVariation(type, tree, null);
  }

  public <T extends Entity> T reduceVariation(Class<T> type, JsonNode tree, String variation) throws StorageException, JsonProcessingException {
    checkNotNull(tree);

    // For the time being I'm not quite sure wether variation should be used at all
    // because we can arrange things by looking at the type.

    return reduceEntity(type, tree);
  }

  // TODO This is the "old" behaviour, but we need to re-think the resposibilities
  // Who knows about the way variations are stored? It's either the storage layer.
  // in which case reduceAllVariations shouldn't be part of the reducer, or it is
  // the inducer/reducer, in which case adding maintaining the variation list
  // should be part of the inducer and not of MongoStorage.
  public <T extends Entity> List<T> reduceAllVariations(Class<T> type, JsonNode tree) throws IOException {
    checkNotNull(tree);

    List<T> entities = Lists.newArrayList();

    JsonNode variations = tree.findValue(DomainEntity.VARIATIONS);
    if (variations != null) {
      for (JsonNode node : ImmutableList.copyOf(variations.elements())) {
        String variation = node.textValue();
        Class<? extends Entity> varType = typeRegistry.getTypeForIName(variation);
        if (varType != null && type.isAssignableFrom(varType)) {
          T entity = type.cast(reduceVariation(varType, tree));
          entities.add(entity);
        } else {
          LOG.error("Not a variation of {}: {}", type, variation);
        }
      }
    }

    return entities;
  }

  public <T extends Entity> T reduceRevision(Class<T> type, JsonNode tree) throws IOException {
    checkNotNull(tree);

    ArrayNode versionsNode = (ArrayNode) tree.get("versions");
    JsonNode node = versionsNode.get(0);

    return reduceVariation(type, node);
  }

  public <T extends Entity> MongoChanges<T> reduceAllRevisions(Class<T> type, JsonNode tree) throws IOException {
    checkNotNull(tree);

    ArrayNode versionsNode = (ArrayNode) tree.get("versions");
    MongoChanges<T> changes = null;

    for (int i = 0; versionsNode.hasNonNull(i); i++) {
      T item = reduceVariation(type, versionsNode.get(i));
      if (i == 0) {
        changes = new MongoChanges<T>(item.getId(), item);
      } else {
        changes.getRevisions().add(item);
      }
    }

    return changes;
  }

  // -------------------------------------------------------------------

  private <T extends Entity> T reduceEntity(Class<T> type, JsonNode tree) throws StorageException {
    try {
      T entity = createEntityInstance(type);

      Map<String, Field> fieldMap = fieldMapper.getCompositeFieldMap(type, type, Entity.class);
      for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
        String key = entry.getKey();
        JsonNode node = tree.findValue(key);
        if (node != null) {
          Field field = entry.getValue();
          Object value = convertJsonNodeToValue(field.getType(), node);
          setValue(field, entity, value);
          LOG.debug("Assigned: {} := {}", field.getName(), value);
        } else {
          LOG.debug("No value for property {}", key);
        }
      }

      return entity;
    } catch (Exception e) {
      // TODO improve error handling
      throw new StorageException(e);
    }
  }

  /**
   * Encapsulates creation of an entity.
   * If this fails we're simply done....
   */
  private <T extends Entity> T createEntityInstance(Class<T> type) {
    try {
      return type.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Encapsulates assigning of a value to the field of an object.
   * If this fails we're simply done....
   */
  private void setValue(Field field, Object object, Object value) {
    try {
      field.setAccessible(true);
      field.set(object, value);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> Object convertJsonNodeToValue(Class<T> fieldType, JsonNode node) throws IOException {
    if (node.isArray()) {
      return createCollection(node);
    } else if (fieldType == Integer.class || fieldType == int.class) {
      return node.asInt();
    } else if (fieldType == Boolean.class || fieldType == boolean.class) {
      return node.asBoolean();
    } else if (fieldType == Character.class || fieldType == char.class) {
      return node.asText().charAt(0);
    } else if (fieldType == Double.class || fieldType == double.class) {
      return node.asDouble();
    } else if (fieldType == Float.class || fieldType == float.class) {
      return (float) node.asDouble();
    } else if (fieldType == Long.class || fieldType == long.class) {
      return node.asLong();
    } else if (fieldType == Short.class || fieldType == short.class) {
      return (short) node.asInt();
    } else if (Class.class.isAssignableFrom(fieldType)) {
      try {
        return Class.forName(node.asText());
      } catch (ClassNotFoundException e) {
        throw new IOException(e);
      }
    } else if (Datable.class.isAssignableFrom(fieldType)) {
      return new Datable(node.asText());
    } else if (PersonName.class.isAssignableFrom(fieldType)) {
      return jsonMapper.readValue(node.toString(), PersonName.class);
    }

    return node.asText();
  }

  private Object createCollection(JsonNode value) throws IOException, JsonParseException, JsonMappingException {
    return jsonMapper.readValue(value.toString(), new TypeReference<List<? extends Object>>() {});
  }

}
