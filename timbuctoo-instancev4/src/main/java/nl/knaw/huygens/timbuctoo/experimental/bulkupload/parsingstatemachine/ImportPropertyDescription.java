package nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine;

import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;

import java.util.function.Function;

public class ImportPropertyDescription {
  private String propertyName;
  private boolean unique;
  private String[] metadata = new String[0];
  private final Integer id;
  private final int order;
  private final Collection currentCollection;
  private final Function<String, Boolean> relationExists;
  private PropertyTypes propertyType = PropertyTypes.BASIC;

  ImportPropertyDescription(Integer id, int order, Collection currentCollection,
                            Function<String, Boolean> relationExists) {
    this.id = id;
    this.order = order;
    this.currentCollection = currentCollection;
    this.relationExists = relationExists;
  }

  public PropertyTypes getType() {
    return propertyType;
  }

  public boolean hasPropertyName() {
    return this.propertyName != null;
  }

  public boolean isValidRelationName() {
    return relationExists.apply(propertyName);
  }

  public boolean isValidPropertyName() {
    return currentCollection.getWriteableProperties().containsKey(propertyName);
  }

  public String getDbPropertyName() {
    final LocalProperty property = currentCollection.getWriteableProperties().get(propertyName);
    return property.getPropName();
  }

  public enum PropertyTypes {
    NUMERIC,
    RELATION,
    BASIC
  }

  public boolean setPropertyName(String propertyName) {
    this.propertyName = propertyName;
    return isValidPropertyName() || isValidRelationName();
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public boolean isUnique() {
    return unique;
  }

  public void setMetadata(String... metadata) {
    this.metadata = metadata;
  }

  public String[] getMetadata() {
    return metadata;
  }

  public Integer getId() {
    return id;
  }

  public int getOrder() {
    return order;
  }

  public boolean setPropertyType(String propertyType) {
    switch (propertyType) {
      case "numeric":
        this.propertyType = PropertyTypes.NUMERIC;
        return true;
      case "relation":
        this.propertyType = PropertyTypes.RELATION;
        return true;
      case "basic":
        this.propertyType = PropertyTypes.BASIC;
        return true;
      default:
        return false;
    }
  }
}
