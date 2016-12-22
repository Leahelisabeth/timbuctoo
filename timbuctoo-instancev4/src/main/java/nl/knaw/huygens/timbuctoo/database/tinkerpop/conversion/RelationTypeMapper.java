package nl.knaw.huygens.timbuctoo.database.tinkerpop.conversion;

import nl.knaw.huygens.timbuctoo.core.dto.ImmutableRelationType;
import nl.knaw.huygens.timbuctoo.core.dto.RelationType;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;

public class RelationTypeMapper {

  public static RelationType fromVertex(Vertex vertex) {
    return ImmutableRelationType
      .builder()
      .outName(getProp(vertex, "relationtype_regularName", String.class).orElse("<no name>"))
      .inverseName(getProp(vertex, "relationtype_inverseName", String.class).orElse("<no name>"))
      .sourceTypeName(getProp(vertex, "relationtype_sourceTypeName", String.class).orElse(""))
      .targetTypeName(getProp(vertex, "relationtype_targetTypeName", String.class).orElse(""))
      .isReflexive(getProp(vertex, "relationtype_reflexive", Boolean.class).orElse(false))
      .isSymmetric(getProp(vertex, "relationtype_symmetric", Boolean.class).orElse(false))
      .isDerived(getProp(vertex, "relationtype_derived", Boolean.class).orElse(false))
      .timId(UUID.fromString(getProp(vertex, "tim_id", String.class).orElse("")))
      .build();
  }
}
