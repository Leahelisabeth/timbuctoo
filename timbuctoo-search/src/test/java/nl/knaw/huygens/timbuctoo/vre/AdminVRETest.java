package nl.knaw.huygens.timbuctoo.vre;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Place;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARPerson;

import org.junit.BeforeClass;
import org.junit.Test;

public class AdminVRETest {

  private static VRE vre;

  @BeforeClass
  public static void setupVRE() throws IOException {
    vre = new AdminVRE();
  }

  @Test
  public void testBaseEntityTypes() {
    assertTrue(vre.getBaseEntityTypes().contains(Person.class));
    assertTrue(vre.getBaseEntityTypes().contains(Place.class));
    assertFalse(vre.getBaseEntityTypes().contains(User.class));
    assertFalse(vre.getBaseEntityTypes().contains(DCARPerson.class));
  }

  @Test
  public void testTypeAndIdInScope() {
    assertTrue(vre.inScope(Person.class, "id"));
    assertTrue(vre.inScope(Place.class, "id"));
    assertFalse(vre.inScope(DCARPerson.class, "id"));
  }

  @Test
  public void testInstanceInScope() {
    assertTrue(vre.inScope(new Person()));
    assertTrue(vre.inScope(new Place()));
    assertFalse(vre.inScope(new DCARPerson()));
  }

  @Test
  public void testTypeInScope() {
    assertTrue(vre.inScope(Person.class));
    assertTrue(vre.inScope(Place.class));
    assertFalse(vre.inScope(DCARPerson.class));
  }

}