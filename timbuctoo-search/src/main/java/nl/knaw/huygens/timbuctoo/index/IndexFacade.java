package nl.knaw.huygens.timbuctoo.index;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.NoSuchFacetException;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexFacade implements SearchManager, IndexManager {

  private static final Logger LOG = LoggerFactory.getLogger(IndexFacade.class);
  private final ScopeManager scopeManager;
  private final TypeRegistry typeRegistry;
  private final StorageManager storageManager;
  private final SortableFieldFinder sortableFieldFinder;

  public IndexFacade(ScopeManager scopeManager, TypeRegistry typeRegistry, StorageManager storageManager, SortableFieldFinder sortableFieldFinder) {
    this.scopeManager = scopeManager; // TODO place functionality of ScopeManager in VREManager
    this.typeRegistry = typeRegistry;
    this.storageManager = storageManager;
    this.sortableFieldFinder = sortableFieldFinder;
  }

  @Override
  public <T extends DomainEntity> void addEntity(Class<T> type, String id) throws IndexException {
    IndexChanger indexAdder = new IndexChanger() {

      @Override
      public void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException {
        index.add(variations);

      }
    };
    changeIndex(type, id, indexAdder);

  }

  private <T extends DomainEntity> void changeIndex(Class<T> type, String id, IndexChanger indexChanger) throws IndexException {
    Class<? extends DomainEntity> baseType = baseTypeFor(type);
    List<? extends DomainEntity> variations = null;

    variations = storageManager.getAllVariations(baseType, id);
    if (variations == null || variations.isEmpty()) {
      throw new IndexException("Could not retrieve variations for type " + type + "with id " + id);
    }

    for (Scope scope : scopeManager.getAllScopes()) {
      Index index = scopeManager.getIndexFor(scope, baseType);
      List<? extends DomainEntity> filteredVariations = scope.filter(variations);

      indexChanger.executeIndexAction(index, filteredVariations);
    }
  }

  private <T extends DomainEntity> Class<? extends DomainEntity> baseTypeFor(Class<T> type) {
    Class<? extends DomainEntity> baseType = TypeRegistry.toDomainEntity(typeRegistry.getBaseClass(type));
    return baseType;
  }

  @Override
  public <T extends DomainEntity> void updateEntity(Class<T> type, String id) throws IndexException {
    IndexChanger indexUpdater = new IndexChanger() {

      @Override
      public void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException {
        index.update(variations);
      }
    };
    changeIndex(type, id, indexUpdater);
  }

  @Override
  public <T extends DomainEntity> void deleteEntity(Class<T> type, String id) throws IndexException {
    Class<? extends DomainEntity> baseType = baseTypeFor(type);

    for (Scope scope : scopeManager.getAllScopes()) {
      Index index = scopeManager.getIndexFor(scope, baseType);

      index.deleteById(id);
    }

  }

  @Override
  public <T extends DomainEntity> void deleteEntities(Class<T> type, List<String> ids) throws IndexException {
    Class<? extends DomainEntity> baseType = baseTypeFor(type);

    for (Scope scope : scopeManager.getAllScopes()) {
      Index index = scopeManager.getIndexFor(scope, baseType);

      index.deleteById(ids);
    }

  }

  @Override
  public void deleteAllEntities() throws IndexException {
    List<Index> allIndexes = scopeManager.getAllIndexes();
    for (Index index : allIndexes) {
      index.clear();
    }
  }

  /**
   * @deprecated use SearchManager.search
   */
  @Override
  @Deprecated
  public <T extends DomainEntity> QueryResponse search(Scope scope, Class<T> type, SolrQuery query) throws IndexException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IndexStatus getStatus() {
    IndexStatus indexStatus = creatIndexStatus();

    List<Scope> scopes = scopeManager.getAllScopes();

    for (Scope scope : scopes) {
      for (Class<? extends DomainEntity> type : scope.getBaseEntityTypes()) {
        Index index = scopeManager.getIndexFor(scope, type);
        try {
          indexStatus.addCount(scope, type, index.getCount());
        } catch (IndexException e) {
          LOG.error("Failed to obtain status: {}", e.getMessage());
        }
      }
    }

    return indexStatus;
  }

  protected IndexStatus creatIndexStatus() {
    return new IndexStatus();
  }

  @Override
  public void commitAll() throws IndexException {
    for (Index index : scopeManager.getAllIndexes()) {
      index.commit();
    }
  }

  @Override
  public void close() throws IndexException {
    for (Index index : scopeManager.getAllIndexes()) {
      try {
        index.close();
      } catch (IndexException ex) {
        LOG.error("closing of index {} went wrong", index.getName(), ex);
      }
    }

  }

  @Override
  public Set<String> findSortableFields(Class<? extends DomainEntity> type) {
    return sortableFieldFinder.findFields(type);
  }

  @Override
  public <T extends FacetedSearchParameters<T>> SearchResult search(Scope scope, Class<? extends DomainEntity> type, FacetedSearchParameters<T> searchParameters) throws SearchException {
    Index index = scopeManager.getIndexFor(scope, type);

    return index.search(searchParameters);
  }

  private static interface IndexChanger {
    void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException;
  }

  @Override
  public SearchResult search(Scope scope, Class<? extends DomainEntity> type, SearchParameters searchParameters) throws IndexException, NoSuchFacetException {
    // TODO Auto-generated method stub
    return null;
  }
}