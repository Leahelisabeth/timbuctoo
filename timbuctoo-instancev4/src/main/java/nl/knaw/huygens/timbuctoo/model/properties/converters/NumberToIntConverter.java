package nl.knaw.huygens.timbuctoo.model.properties.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.StringExcelDescription;

import java.io.IOException;

public class NumberToIntConverter implements Converter {

  @Override
  public Object jsonToTinkerpop(JsonNode json) throws IOException {
    if (json.isNumber()) {
      return json.asInt();
    } else {
      throw new IOException("should be a number.");
    }
  }

  @Override
  public JsonNode tinkerpopToJson(Object value) throws IOException {
    if (value != null && value instanceof Integer) {
      return JsonNodeFactory.instance.numberNode((int) value);
    } else {
      throw new IOException("should be a number");
    }
  }

  public String getTypeIdentifier() {
    return "text";
  }

  @Override
  public ExcelDescription tinkerPopToExcel(Object value, String typeId) throws IOException {
    if (value == null) {
      return new StringExcelDescription("", typeId);
    } else {
      return new StringExcelDescription(tinkerpopToJson(value).asText(), typeId);
    }
  }
}
