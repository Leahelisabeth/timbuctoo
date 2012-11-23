package nl.knaw.huygens.repository.index;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Events;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.util.RepositoryException;

import org.apache.solr.common.SolrInputDocument;

public class DocumentIndexer<T extends Document> {
	private final LocalSolrServer localSolrServer;
	private final String core;
	private final ModelIterator modelIterator;

  public DocumentIndexer(Class<T> entity, ModelIterator iterator, LocalSolrServer server) {
    this.localSolrServer = server;
    this.modelIterator = iterator;
    this.core = entity.getSimpleName().toLowerCase();
  }

  public void add(T entity) throws RepositoryException {
    try {
      localSolrServer.add(entity.getType(), getSolrInputDocument(entity));
    } catch (IndexException e) {
      throw new RepositoryException(e);
    }
  }

  public void modify(T entity) throws RepositoryException {
    try {
      localSolrServer.update(entity.getType(), getSolrInputDocument(entity));
    } catch (IndexException e) {
      throw new RepositoryException(e);
    }
  }

  public void remove(T entity) throws RepositoryException {
    try {
      localSolrServer.delete(entity.getType(), getSolrInputDocument(entity));
    } catch (IndexException e) {
      throw new RepositoryException(e);
    }
  }

  public void flush() throws RepositoryException {
    try {
      localSolrServer.commit(core);
      Hub.getInstance().publish(new Events.IndexChangedEvent());
    } catch (Exception ex) {
      throw new RepositoryException(ex);
    }
  }

  protected SolrInputDocument getSolrInputDocument(T entity) {
    DocIndexer indexer = new DocIndexer(modelIterator, entity);
    modelIterator.processMethods(indexer, entity.getClass().getMethods());
    return indexer.getResult();
  }
}
