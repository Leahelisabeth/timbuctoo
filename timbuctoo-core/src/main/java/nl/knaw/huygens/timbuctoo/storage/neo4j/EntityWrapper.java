package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.CREATED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class EntityWrapper<T> {

  private List<FieldWrapper> fieldWrappers;
  private T entity;
  private String id;
  private Change modified;
  private Change created;
  private int revision;

  public EntityWrapper() {
    fieldWrappers = Lists.newArrayList();
  }

  public void addValuesToNode(Node node) throws IllegalArgumentException, IllegalAccessException {
    // TODO make difference between types of the field wrappers.
    addName(node);
    for (FieldWrapper fieldWrapper : fieldWrappers) {
      fieldWrapper.addValueToNode(node);
    }
  }

  private void addName(Node node) {
    node.addLabel(DynamicLabel.label(TypeNames.getInternalName(entity.getClass())));
  }

  public void addAdministrativeValues(Node node) {
    node.setProperty(ID_PROPERTY_NAME, id);
    node.setProperty(REVISION_PROPERTY_NAME, revision);
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      node.setProperty(CREATED_PROPERTY_NAME, objectMapper.writeValueAsString(created));
      node.setProperty(MODIFIED_PROPERTY_NAME, objectMapper.writeValueAsString(modified));
    } catch (JsonProcessingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public T createEntityFromNode(Node node) {
    return null;
  }

  public void addFieldWrapper(FieldWrapper fieldWrapper) {
    fieldWrappers.add(fieldWrapper);
  }

  public void setEntity(T entity) {
    this.entity = entity;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setModified(Change modified) {
    this.modified = modified;

  }

  public void setCreated(Change created) {
    this.created = created;
  }

  public void setRev(int revision) {
    this.revision = revision;
  }

  public String getId() {
    return id;
  }

}