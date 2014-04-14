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

import nl.knaw.huygens.timbuctoo.model.Relation;

public class RelationValidator implements Validator<Relation> {

  private final RelationDuplicationValidator relationDuplicationValidator;
  private final RelationTypeConformationValidator relationTypeConformationValidator;
  private final RelationReferenceValidator relationReferenceValidator;

  public RelationValidator(RelationTypeConformationValidator relationTypeConformationValidator, RelationReferenceValidator relationFieldValidator, RelationDuplicationValidator relationDuplicationValidator) {
    this.relationTypeConformationValidator = relationTypeConformationValidator;
    this.relationDuplicationValidator = relationDuplicationValidator;
    this.relationReferenceValidator = relationFieldValidator;
  }

  @Override
  public void validate(Relation entityToValidate) throws ValidationException, IOException {
    relationTypeConformationValidator.validate(entityToValidate);
    relationReferenceValidator.validate(entityToValidate);
    relationDuplicationValidator.validate(entityToValidate);
  }

}