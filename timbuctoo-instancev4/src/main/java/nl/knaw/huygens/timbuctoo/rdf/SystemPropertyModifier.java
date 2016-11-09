package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Lists;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.tinkerpop.gremlin.structure.Element;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

/**
 * FIXME: deduplicate code in TinkerpopJsonCrudService
 */
public class SystemPropertyModifier {

  public static final List<String> SYSTEM_PROPERTY_NAMES = Lists.newArrayList(
    "rdfUri", "modified", "created", "types", "tim_id", "rdfAlternatives"
  );
  private final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
  private final Clock clock;

  public SystemPropertyModifier(Clock clock) {
    this.clock = clock;
  }

  public void setCreated(Element element, String userId) {
    String value = String.format("{\"timeStamp\":%s,\"userId\":%s}",
      clock.millis(),
      nodeFactory.textNode(userId)
    );
    element.property("created", value);
    element.property("modified", value);
  }

  public void setModified(Element element, String userId) {
    String value = String.format("{\"timeStamp\":%s,\"userId\":%s}",
      clock.millis(),
      nodeFactory.textNode(userId)
    );
    element.property("modified", value);
  }

  public void setTimId(Element element) {
    element.property("tim_id", UUID.randomUUID().toString());
  }

  public void setTimId(Element element, String timId) {
    element.property("tim_id", timId);
  }

  public void setRev(Element element, int rev) {
    element.property("rev", rev);
  }

  public void setIsLatest(Element element, boolean isLatest) {
    element.property("isLatest", isLatest);
  }

  public void setIsDeleted(Element element, boolean isDeleted) {
    element.property("deleted", isDeleted);
  }

}
