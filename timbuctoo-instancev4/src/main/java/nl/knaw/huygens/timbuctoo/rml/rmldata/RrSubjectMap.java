package nl.knaw.huygens.timbuctoo.rml.rmldata;


import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import org.apache.jena.graph.Node;

import java.util.Optional;

public class RrSubjectMap {
  private final RrTermMap termMap;

  public RrSubjectMap(RrTermMap termMap) {
    this.termMap = termMap;
  }

  public Optional<Node> generateValue(Row row) {
    return termMap.generateValue(row);
  }

  @Override
  public String toString() {
    return String.format("    SubjectMap: \n%s",
      this.termMap
    );
  }

}
