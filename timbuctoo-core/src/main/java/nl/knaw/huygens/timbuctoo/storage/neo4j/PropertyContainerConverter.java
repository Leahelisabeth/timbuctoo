package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.PropertyContainer;

public interface PropertyContainerConverter<U extends PropertyContainer, T extends Entity> {

  void addValuesToPropertyContainer(U propertyContainer, T entity) throws ConversionException;

  void addValuesToEntity(T entity, U propertyContainer) throws ConversionException;

  void updatePropertyContainer(U propertyContainer, T entity) throws ConversionException;

  void updateModifiedAndRev(U propertyContainer, T entity) throws ConversionException;

}
