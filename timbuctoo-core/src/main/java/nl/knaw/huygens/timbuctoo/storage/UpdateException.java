package nl.knaw.huygens.timbuctoo.storage;

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

/**
 * Signals failure of an update because an entity changed.
 */
public class UpdateException extends StorageException {

  private static final long serialVersionUID = 1L;

  public UpdateException() {
    super();
  }

  public UpdateException(String message) {
    super(message);
  }

  public UpdateException(Throwable cause) {
    super(cause);
  }

  public UpdateException(String message, Throwable cause) {
    super(message, cause);
  }

}