package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.rml.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Logs the rml mapping errors to the log of the application.
 */
public class LoggingErrorHandler implements ErrorHandler {

  public static final Logger LOG = LoggerFactory.getLogger(LoggingErrorHandler.class);

  @Override
  public void linkError(Map<String, Object> rowData, String childField, String parentCollection, String parentField) {
    if (rowData.get(childField) != null) {
      LOG.error("Row's field '{}' with value '{}' could not be linked to field '{}' of the collection '{}'",
        childField,
        rowData.get(childField),
        parentField,
        parentCollection
      );
    }
  }

  @Override
  public void valueGenerateFailed(String key, String message) {
    LOG.error(key + ": " + message);
  }
}
