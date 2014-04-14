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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.validation.DuplicateException;
import nl.knaw.huygens.timbuctoo.validation.RelationDuplicationValidator;
import nl.knaw.huygens.timbuctoo.validation.ValidationException;

import org.junit.Before;
import org.junit.Test;

public class RelationDuplicationValidatorTest {
  private Storage storageMock;
  private RelationDuplicationValidator instance;
  private String firstId = "Id00001";
  private String secondId = "Id00002";
  private String typeId = "typeId";

  @Before
  public void setUp() {
    storageMock = mock(Storage.class);
    instance = new RelationDuplicationValidator(storageMock);
  }

  @Test
  public void testValidateNewValidItem() throws IOException, ValidationException {
    Relation example = createRelation(firstId, secondId, typeId);
    Relation inverseExample = createRelation(secondId, firstId, typeId);

    Relation entityToValidate = createRelation(firstId, secondId, typeId);
    entityToValidate.setSourceType("sourceType");
    entityToValidate.setTargetType("targetType");
    entityToValidate.setTypeType("typeType");

    // action
    instance.validate(entityToValidate);

    // verify
    verify(storageMock).findItem(Relation.class, example);
    verify(storageMock).findItem(Relation.class, inverseExample);
  }

  @Test(expected = DuplicateException.class)
  public void testValidateExactSameItemExists() throws IOException, ValidationException {
    Relation example = createRelation(firstId, secondId, typeId);
    Relation entityToValidate = createRelation(firstId, secondId, typeId);
    entityToValidate.setSourceType("sourceType");
    entityToValidate.setTargetType("targetType");
    entityToValidate.setTypeType("typeType");

    Relation itemFound = createRelation(firstId, secondId, typeId);
    itemFound.setSourceType("sourceType");
    itemFound.setTargetType("targetType");
    itemFound.setTypeType("typeType");

    //when
    when(storageMock.findItem(Relation.class, example)).thenReturn(itemFound);

    try {
      // action
      instance.validate(itemFound);
    } finally {
      // verify
      verify(storageMock).findItem(Relation.class, example);
      verifyNoMoreInteractions(storageMock);
    }
  }

  @Test(expected = DuplicateException.class)
  public void testValidateExactInverseItemExists() throws ValidationException, IOException {
    Relation example = createRelation(firstId, secondId, typeId);
    Relation inverseExample = createRelation(secondId, firstId, typeId);

    Relation entityToValidate = createRelation(firstId, secondId, typeId);
    entityToValidate.setSourceType("sourceType");
    entityToValidate.setTargetType("targetType");
    entityToValidate.setTypeType("typeType");

    Relation itemFound = createRelation(secondId, firstId, typeId);
    itemFound.setSourceType("sourceType");
    itemFound.setTargetType("targetType");
    itemFound.setTypeType("typeType");

    // when
    when(storageMock.findItem(Relation.class, inverseExample)).thenReturn(itemFound);

    try {
      // action
      instance.validate(entityToValidate);
    } finally {
      // verify
      verify(storageMock).findItem(Relation.class, example);
      verify(storageMock).findItem(Relation.class, inverseExample);
    }
  }

  @Test(expected = IOException.class)
  public void testValidateStorageThrowsAnExceptionOnExampleSearch() throws ValidationException, IOException {
    Relation example = createRelation(firstId, secondId, typeId);

    Relation entityToValidate = createRelation(firstId, secondId, typeId);
    entityToValidate.setSourceType("sourceType");
    entityToValidate.setTargetType("targetType");
    entityToValidate.setTypeType("typeType");

    Relation itemFound = createRelation(secondId, firstId, typeId);
    itemFound.setSourceType("sourceType");
    itemFound.setTargetType("targetType");
    itemFound.setTypeType("typeType");

    // when
    doThrow(IOException.class).when(storageMock).findItem(Relation.class, example);

    try {
      // action
      instance.validate(entityToValidate);
    } finally {
      // verify
      verify(storageMock).findItem(Relation.class, example);
      verifyNoMoreInteractions(storageMock);
    }
  }

  @Test(expected = IOException.class)
  public void testValidateStorageThrowsAnExceptionOnInverseExampleSearch() throws ValidationException, IOException {
    Relation example = createRelation(firstId, secondId, typeId);
    Relation inverseExample = createRelation(secondId, firstId, typeId);

    Relation entityToValidate = createRelation(firstId, secondId, typeId);
    entityToValidate.setSourceType("sourceType");
    entityToValidate.setTargetType("targetType");
    entityToValidate.setTypeType("typeType");

    Relation itemFound = createRelation(secondId, firstId, typeId);
    itemFound.setSourceType("sourceType");
    itemFound.setTargetType("targetType");
    itemFound.setTypeType("typeType");

    // when
    doThrow(IOException.class).when(storageMock).findItem(Relation.class, inverseExample);

    try {
      // action
      instance.validate(entityToValidate);
    } finally {
      // verify
      verify(storageMock).findItem(Relation.class, example);
      verify(storageMock).findItem(Relation.class, inverseExample);
    }
  }

  private Relation createRelation(String sourceId, String targetId, String typeId) {
    Relation example = new Relation();
    example.setSourceId(sourceId);
    example.setTargetId(targetId);
    example.setTypeId(typeId);
    return example;
  }
}