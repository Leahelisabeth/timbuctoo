package nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine;

public class ImportPropertyDescription {
  private String propertyName;
  private boolean unique;
  private String[] metadata = new String[0];
  private final Integer id;
  private final int order;
  private PropertyTypes propertyType = PropertyTypes.BASIC;

  ImportPropertyDescription(Integer id, int order) {
    this.id = id;
    this.order = order;
  }

  public PropertyTypes getType() {
    return propertyType;
  }

  public enum PropertyTypes {
    NUMERIC,
    RELATION,
    BASIC
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
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
