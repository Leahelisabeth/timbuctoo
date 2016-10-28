package nl.knaw.huygens.timbuctoo.database.dto;

import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.model.Change;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class UpdateEntity {
  private final UUID id;
  private final List<TimProperty<?>> properties;
  private final int rev;
  private final Instant updateInstant;
  private Change modified;

  public UpdateEntity(UUID id, List<TimProperty<?>> properties, int rev, Instant updateInstant) {
    this.id = id;
    this.properties = properties;
    this.rev = rev;
    this.updateInstant = updateInstant;
  }

  public UUID getId() {
    return id;
  }

  public List<TimProperty<?>> getProperties() {
    return properties;
  }

  public int getRev() {
    return rev;
  }

  public Instant getUpdateInstant() {
    return updateInstant;
  }

  public void setModified(Change modified) {
    this.modified = modified;
  }

  public Change getModified() {
    return modified;
  }
}