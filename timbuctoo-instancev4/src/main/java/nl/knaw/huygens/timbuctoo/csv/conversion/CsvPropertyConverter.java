package nl.knaw.huygens.timbuctoo.csv.conversion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.core.PropertyConverter;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.core.dto.property.AltNamesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.ArrayOfLimitedValuesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.ArrayProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.DatableProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.DefaultFullPersonNameProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.DefaultLocationNameProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.EncodedStringOfLimitedValuesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.HyperLinksProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.PersonNamesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.StringOfLimitedValuesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.StringProperty;
import nl.knaw.huygens.timbuctoo.rdf.LinkTriple;
import nl.knaw.huygens.timbuctoo.rdf.LiteralTriple;
import nl.knaw.huygens.timbuctoo.rdf.Triple;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class CsvPropertyConverter extends PropertyConverter<String> {
  private final String subjectUri;
  private final ObjectMapper objectMapper;

  public CsvPropertyConverter(Collection collection, String subjectUri) {
    super(collection);
    this.subjectUri = subjectUri;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  protected ArrayProperty createArrayProperty(String propertyName, String value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected AltNamesProperty createAltNamesProperty(String propertyName, String value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected DatableProperty createDatableProperty(String propertyName, String value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected DefaultFullPersonNameProperty createDefaultFullPersonNameProperty(String propertyName, String value)
    throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected DefaultLocationNameProperty createDefaultLocationNameProperty(String propertyName, String value)
    throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected HyperLinksProperty createHyperLinksProperty(String propertyName, String value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected PersonNamesProperty createPersonNamesProperty(String propertyName, String value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected ArrayOfLimitedValuesProperty createArrayOfLimitedValuesProperty(String propertyName, String value)
    throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected EncodedStringOfLimitedValuesProperty createEncodedStringOfLimitedValuesProperty(String propertyName,
                                                                                            String value)
    throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected StringProperty createStringProperty(String propertyName, String value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected StringOfLimitedValuesProperty createStringOfLimitedValues(String propertyName, String value)
    throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Tuple<String, String> to(AltNamesProperty property) throws IOException {
    // TODO find a better way to serialize to n-triples
    String objectValue = objectMapper.writeValueAsString(property.getValue());
    return tuple(property.getName(), "\"" + objectValue + "\"");
  }

  @Override
  public Tuple<String, String> to(DatableProperty property) throws IOException {
    return tuple(property.getName(), "\"" + property.getValue() + "\"");
  }

  @Override
  public Tuple<String, String> to(DefaultFullPersonNameProperty property) throws IOException {
    return tuple(property.getName(), "\"" + property.getValue() + "\"");
  }

  @Override
  public Tuple<String, String> to(DefaultLocationNameProperty property) throws IOException {
//    return tuple(property.getName(), Lists.newArrayList());
    return tuple(property.getName(), "\"" + property.getValue() + "\"");
  }

  @Override
  public Tuple<String, String> to(HyperLinksProperty property) throws IOException {
    String blankNode = "_:" + UUID.randomUUID();
    String propertyName = property.getName();
    String pred = createMetadata(propertyName);
    ArrayList<LinkTriple> triples = Lists.newArrayList(new LinkTriple(subjectUri, pred, blankNode));
    JsonNode jsonNode = objectMapper.readTree(property.getValue());
    for (Iterator<JsonNode> elements = jsonNode.elements(); elements.hasNext(); ) {
      JsonNode jn = elements.next();
      jn.fieldNames().forEachRemaining(field -> {
        String predicate = pred + field;
        String fieldValue = jn.get(field).asText();
        String object = StringUtils.isBlank(fieldValue) ? "\"\"" : fieldValue;
        triples.add(new LinkTriple(blankNode, predicate, object));
      });
    }

    return tuple(propertyName, triples.toString());
  }

  @Override
  public Tuple<String, String> to(PersonNamesProperty property) throws IOException {
    // TODO find a better way to serialize to n-triples
    String objectValue = objectMapper.writeValueAsString(property.getValue());
    return tuple(property.getName(), "\"" + objectValue + "\"");
  }

  @Override
  public Tuple<String, String> to(ArrayOfLimitedValuesProperty property) throws IOException {
    return mapArrayProperty(property.getName(), property.getValue());
  }

  @Override
  public Tuple<String, String> to(EncodedStringOfLimitedValuesProperty property) throws IOException {
    return tuple(property.getName(), "\"" + property.getValue() + "\"");
  }

  @Override
  public Tuple<String, String> to(StringProperty property) throws IOException {
    return tuple(property.getName(), "\"" + property.getValue() + "\"");
  }

  @Override
  public Tuple<String, String> to(StringOfLimitedValuesProperty property) throws IOException {
    return tuple(property.getName(), "\"" + property.getValue() + "\"");
  }

  @Override
  public Tuple<String, String> to(ArrayProperty property) throws IOException {
    return mapArrayProperty(property.getName(), property.getValue());

  }

  private String createMetadata(String metadataName) {
    // FIXME find a way to look up the predicate of a property
    return String.format("http://timbuctoo.huygens.knaw.nl/%s", metadataName);
  }

  private Tuple<String, String> mapArrayProperty(String propertyName, String value) throws IOException {
    List<String> values = objectMapper.readValue(value, new TypeReference<List<String>>() {
    });
    String collect = "";
    for (String val: values) {
      collect += " " + val;
    }
    return tuple(propertyName, collect);
  }
}
