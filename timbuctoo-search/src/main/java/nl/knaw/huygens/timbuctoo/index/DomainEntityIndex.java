package nl.knaw.huygens.timbuctoo.index;

/*
 * #%L
 * Timbuctoo search
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

/**
 * Used for talking to a specific index on the Solr Server that matches the
 * class used as a generic parameter. Takes care of converting POJO objects
 * (that extend {@link nl.knaw.huygens.timbuctoo.model.Entity
 * <code>Entity</code>}) to {@link org.apache.solr.common.SolrInputDocument
 * <code>SolrInputDocument</code>}s.
 * 
 * Note that whenever you update entities through this index, it is the
 * caller's responsibility to call
 * {@link nl.knaw.huygens.timbuctoo.index.DomainEntityIndex#flush flush} to
 * update the index and notify the world that this has happened.
 * 
 * @author Gijs
 * 
 * @param <T>
 *          The generic parameter specifying what kind of POJO objects are used,
 *          and (implicitly) which index to index them in.
 */
class DomainEntityIndex<T extends DomainEntity> implements EntityIndex<T> {

  /**
   * Creates a new {@code DomainEntityIndex} instance.
   */
  public static <U extends DomainEntity> DomainEntityIndex<U> newInstance(StorageManager storageManager, LocalSolrServer server, String core) {
    return new DomainEntityIndex<U>(storageManager, server, core);
  }

  private final StorageManager storageManager;
  private final LocalSolrServer solrServer;
  private final String core;
  private final ModelIterator modelIterator;

  /**
   * Creates an indexer for a primitive domain entity.
   * 
   * @param storageManager
   *          the storage manager for retrieving data
   * @param server
   *          the SolrServer to use for indexing
   * @param core
   *          the Solr core
   */
  private DomainEntityIndex(StorageManager storageManager, LocalSolrServer server, String core) {
    this.storageManager = storageManager;
    this.solrServer = server;
    this.core = core;
    modelIterator = new ModelIterator();
  }

  /**
   * Add a {@link nl.knaw.huygens.timbuctoo.model.Entity
   * <code>Entity</code>} to the index.
   * 
   * @param entities
   *          the <code>Entity</code> to add.
   * @throws IndexException
   *          if adding the entity fails for some reason.
   */
  @Override
  public void add(Class<T> docType, String docId) throws IndexException {
    try {
      List<T> variations = storageManager.getAllVariations(docType, docId);
      solrServer.add(core, getSolrInputDocument(variations));
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Update a {@link nl.knaw.huygens.timbuctoo.model.Entity
   * <code>Entity</code>} already in the index. The existing entity will be
   * found using the ID of the entity you pass.
   * 
   * @param entity
   *          the <code>Entity</code> and it's subtypes to update.
   * @throws IndexException
   *          if adding the entity fails for some reason.
   */
  @Override
  public void modify(Class<T> docType, String docId) throws IndexException {
    try {
      List<T> variations = storageManager.getAllVariations(docType, docId);
      solrServer.add(core, getSolrInputDocument(variations));
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Remove a {@link nl.knaw.huygens.timbuctoo.model.Entity
   * <code>Entity</code>} from the index.
   * 
   * @param docId
   *          the id of the <code>Entity</code> to remove.
   * @throws IndexException
   *          if removing the entity fails for some reason.
   */
  @Override
  public void remove(String docId) throws IndexException {
    try {
      solrServer.deleteById(core, docId);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  @Override
  public void remove(List<String> ids) throws IndexException {
    try {
      for (String id : ids) {
        solrServer.deleteById(core, id);
      }

      //solrServer.deleteById(core, ids);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Remove all items from the index.
   *
   * @throws IndexException
   *          if removing fails for some reason.
   */
  @Override
  public void removeAll() throws IndexException {
    try {
      solrServer.deleteAll(core);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Commit all changes to the SolrServer, and notify the world that the index
   * has been changed. Use responsibly.
   * 
   * @throws IndexException
   */
  @Override
  public void flush() throws IndexException {
    try {
      solrServer.commit(core);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Generate a {@link org.apache.solr.common.SolrInputDocument
   * <code>SolrInputDocument</code>} given the POJO object passed.
   * 
   * @param entities
   *          the entity and it's subtypes that you want a SolrInputDocument for.
   * @return the corresponding SolrInputDocument
   */
  private <U extends T> SolrInputDocument getSolrInputDocument(List<U> entities) {
    SolrInputDocument document = null;
    SolrInputDocGenerator indexer = null;
    for (U entity : entities) {
      if (document == null) {
        indexer = new SolrInputDocGenerator(entity);
      } else {
        indexer = new SolrInputDocGenerator(entity, document);
      }
      modelIterator.processClass(indexer, entity.getClass());
      document = indexer.getResult();
    }
    return document;
  }

  @Override
  public QueryResponse search(Class<T> entityType, SolrQuery query) throws IndexException {
    try {
      return solrServer.search(core, query);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

}
