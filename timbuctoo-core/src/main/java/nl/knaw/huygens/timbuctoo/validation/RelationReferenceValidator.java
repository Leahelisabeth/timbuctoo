package nl.knaw.huygens.timbuctoo.validation;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.Storage;

public class RelationReferenceValidator implements Validator<Relation> {

  private final TypeRegistry typeRegistry;
  private final Storage storage;

  public RelationReferenceValidator(TypeRegistry typeRegistry, Storage storage) {
    this.typeRegistry = typeRegistry;
    this.storage = storage;
  }

  @Override
  public void validate(Relation entityToValidate) throws ValidationException, IOException {
    String sourceType = entityToValidate.getSourceType();
    String sourceId = entityToValidate.getSourceId();

    if (!entityExists(sourceType, sourceId)) {
      throw new ValidationException(createValidationMessage(sourceType, sourceId));
    }

    String targetType = entityToValidate.getTargetType();
    String targetId = entityToValidate.getTargetId();
    if (!entityExists(targetType, targetId)) {
      throw new ValidationException(createValidationMessage(targetType, targetId));
    }

  }

  private String createValidationMessage(String type, String id) {
    return String.format("Entity of type %s with id %s does not exist.", type, id);
  }

  private boolean entityExists(String typeString, String id) throws IOException {
    Class<? extends Entity> sourceType = typeRegistry.getTypeForIName(typeString);
    return storage.getItem(sourceType, id) != null;
  }

}