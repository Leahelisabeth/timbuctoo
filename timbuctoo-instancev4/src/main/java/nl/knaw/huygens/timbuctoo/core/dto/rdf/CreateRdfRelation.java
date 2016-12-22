package nl.knaw.huygens.timbuctoo.core.dto.rdf;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(strictBuilder = true)
public interface CreateRdfRelation {
  @Value.Parameter
  String getPredicateUri();

  @Value.Parameter
  String getSubjectPredicateUri();

  @Value.Parameter
  String getObjectPredicateUri();
}
