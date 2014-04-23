package nl.knaw.huygens.timbuctoo.index;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.vre.Scope;

public class ScopeManager {

  private static final NoOpIndex NO_OP_INDEX = new NoOpIndex();
  private final List<Scope> scopes;
  private final Map<String, Index> indexes;
  private final IndexNameCreator indexNameCreator;

  public ScopeManager(List<Scope> scopes, Map<String, Index> indexes, IndexNameCreator indexNameCreator) {
    this.scopes = scopes;
    this.indexNameCreator = indexNameCreator;
    this.indexes = indexes;
  }

  public List<Scope> getAllScopes() {
    return scopes;
  }

  public Index getIndexFor(Scope scope, Class<? extends DomainEntity> type) {
    String indexName = indexNameCreator.getIndexNameFor(scope, type);

    Index index = indexes.get(indexName);
    if (index == null) {
      index = NO_OP_INDEX;
    }

    return index;
  }

  public List<Index> getAllIndexes() {
    // TODO Auto-generated method stub
    return null;

  }

  //--------------------------------------------------------------
  protected static class NoOpIndex implements Index {

    @Override
    public void add(List<? extends DomainEntity> variations) {
      // TODO Auto-generated method stub

    }

    @Override
    public void update(List<? extends DomainEntity> variations) throws IndexException {
      // TODO Auto-generated method stub

    }

    @Override
    public void deleteById(String id) {
      // TODO Auto-generated method stub

    }

    @Override
    public void deleteById(List<String> ids) {
      // TODO Auto-generated method stub

    }

    @Override
    public void clear() {
      // TODO Auto-generated method stub

    }

    @Override
    public long getCount() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public void commit() {
      // TODO Auto-generated method stub

    }

    @Override
    public void close() {
      // TODO Auto-generated method stub

    }

    @Override
    public String getName() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T extends FacetedSearchParameters<T>> SearchResult search(FacetedSearchParameters<T> searchParamaters) {
      // TODO Auto-generated method stub
      return null;
    }
  }

}