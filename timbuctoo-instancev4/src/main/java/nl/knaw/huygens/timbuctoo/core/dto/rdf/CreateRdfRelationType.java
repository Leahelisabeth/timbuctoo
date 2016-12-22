package nl.knaw.huygens.timbuctoo.core.dto.rdf;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(strictBuilder = true)
public interface CreateRdfRelationType {
  @Value.Parameter
  String getRelationPredicateUri();
}
