package nl.knaw.huygens.repository.importer;

import java.util.List;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.index.DocumentIndexer;
import nl.knaw.huygens.repository.index.IndexerFactory;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.util.Progress;
import nl.knaw.huygens.repository.util.RepositoryException;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class SolrIndexer {

  public static void main(String[] args) throws Exception {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new BasicInjectionModule(config));
    SolrIndexerRunner runner = injector.getInstance(SolrIndexerRunner.class);
    System.exit(runner.run());
  }

  public static class SolrIndexerRunner {
    private final Configuration config;
    private final IndexerFactory indices;
    private final Storage storage;
    private final DocTypeRegistry docTypeRegistry;

    @Inject
    public SolrIndexerRunner(Configuration config, IndexerFactory indices, Storage storage, DocTypeRegistry docTypeRegistry) {
      this.config = config;
      this.indices = indices;
      this.storage = storage;
      this.docTypeRegistry = docTypeRegistry;
    }

    public int run() {
      int rv = 0;
      for (String doctype : config.getSettings("indexeddoctypes")) {
        Class<? extends Document> cls = docTypeRegistry.getClassFromTypeString(doctype);
        try {
          indexAllDocuments(cls);
        } catch (Exception e) {
          e.printStackTrace();
          rv = 1;
          break;
        }
      }
      return rv;
    }

    private <T extends Document> void indexAllDocuments(Class<T> type) throws Exception {
      System.out.printf("%n=== Indexing documents of type '%s'%n", type.getSimpleName());

      DocumentIndexer<T> indexer = indices.getIndexForType(type);
      StorageIterator<T> list = storage.getAllByType(type);

      try {
        Progress progress = new Progress();
        while (list.hasNext()) {
          progress.step();
          T doc = list.next();
          List<T> allVariations = storage.getAllVariations(type, doc.getId());
          if (!doc.isDeleted()) {
            try {
              indexer.add(allVariations);
            } catch (RepositoryException e) {
              System.out.println("\nError while indexing publication " + doc.getId());
              throw e;
            }
          }
        }
        progress.done();
      } finally {
        list.close();
      }
      try {
        indexer.flush();
      } catch (Exception e) {
        System.err.println("Error committing changes: " + e.getMessage());
      }
      System.out.printf("%nIndexing done!%n");
    }
  }

}
