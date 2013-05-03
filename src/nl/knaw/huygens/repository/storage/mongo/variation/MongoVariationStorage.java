package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.storage.mongo.MongoStorage;

public interface MongoVariationStorage extends MongoStorage {

  <T extends Document> List<T> getAllVariations(Class<T> type, String id) throws IOException;

  /**
   * Get the given variation of a document.
   * @param type
   * @param id
   * @param variation
   * @return
   * @throws IOException 
   */
  <T extends DomainDocument> T getVariation(Class<T> type, String id, String variation) throws IOException;
}
