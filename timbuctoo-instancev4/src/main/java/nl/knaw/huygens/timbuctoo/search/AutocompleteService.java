package nl.knaw.huygens.timbuctoo.search;

import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.database.TimbuctooActions;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;

import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

public class AutocompleteService {
  private final UrlGenerator autoCompleteUrlFor;
  private final Vres mappings;
  private final TimbuctooActions timbuctooActions;

  public AutocompleteService(UrlGenerator autoCompleteUrlFor, Vres mappings,
                             TimbuctooActions timbuctooActions) {
    this.timbuctooActions = timbuctooActions;
    this.autoCompleteUrlFor = autoCompleteUrlFor;
    this.mappings = mappings;
  }

  public JsonNode search(String collectionName, Optional<String> query, Optional<String> type)
    throws InvalidCollectionException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    int limit = query.isPresent() ? 50 : 1000;
    String queryString = query.orElse(null);
    if (collection.getAbstractType().equals("keyword")) {
      List<ReadEntity> results = timbuctooActions.findKeywordByDisplayName(collection, type.get(), queryString, limit);
      return jsnA(results.stream().map(entity -> jsnO(
        "value", jsn(entity.getDisplayName()),
        "key", jsn(autoCompleteUrlFor.apply(collectionName, entity.getId(), entity.getRev()).toString())
      )));
    } else {
      List<ReadEntity> results = timbuctooActions.findByDisplayName(collection, queryString, limit);
      return jsnA(results.stream().map(entity -> jsnO(
        "value", jsn(entity.getDisplayName()),
        "key", jsn(autoCompleteUrlFor.apply(collectionName, entity.getId(), entity.getRev()).toString())
      )));
    }
  }

  // TODO move to use with DisplayNameSearch dto
  private String parseQuery(Optional<String> queryParam) {
    if (!queryParam.isPresent()) {
      return "*";
    }

    return queryParam.get()
                     .replaceAll("^\\*", "")
                     .replaceAll("\\s", " AND ");
  }

  public static class AutocompleteServiceFactory {
    private final TinkerpopGraphManager graphManager;
    private final UrlGenerator autoCompleteUri;
    private final Vres vres;

    public AutocompleteServiceFactory(TinkerpopGraphManager graphManager, UrlGenerator autoCompleteUri, Vres vres) {
      this.graphManager = graphManager;
      this.autoCompleteUri = autoCompleteUri;
      this.vres = vres;
    }

    public AutocompleteService create(TimbuctooActions timbuctooActions) {
      return new AutocompleteService(autoCompleteUri, vres, timbuctooActions);
    }
  }
}
