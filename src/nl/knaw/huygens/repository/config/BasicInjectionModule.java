package nl.knaw.huygens.repository.config;

import javax.validation.Validation;
import javax.validation.Validator;

import nl.knaw.huygens.repository.persistence.DefaultPersistenceManager;
import nl.knaw.huygens.repository.persistence.PersistenceManager;
import nl.knaw.huygens.repository.persistence.handle.HandleManager;
import nl.knaw.huygens.repository.server.security.NoSecurityOAuthAuthorizationServerConnector;
import nl.knaw.huygens.repository.server.security.OAuthAuthorizationServerConnector;
import nl.knaw.huygens.repository.server.security.apis.ApisAuthorizationServerConnector;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.mongo.variation.MongoStorageFacade;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class BasicInjectionModule extends AbstractModule {

  private final Configuration config;
  private final DocTypeRegistry registry;

  public BasicInjectionModule(Configuration config) {
    this.config = config;
    registry = new DocTypeRegistry(config.getSetting("model-packages"));
    new ConfigValidator(config, registry).validate();
  }

  @Override
  protected void configure() {
    Names.bindProperties(binder(), config.getAll());
    bind(Configuration.class).toInstance(config);
    bind(DocTypeRegistry.class).toInstance(registry);
    if (config.getBooleanSetting("apis.enabled")) {
      bind(OAuthAuthorizationServerConnector.class).to(ApisAuthorizationServerConnector.class);
    } else {
      bind(OAuthAuthorizationServerConnector.class).to(NoSecurityOAuthAuthorizationServerConnector.class);
    }

    // TODO: Refactor to make Storage use MongoModifiableStorage and VariationStorage use MongoModifialbleVariationStorage. 
    bind(Storage.class).to(MongoStorageFacade.class);
  }

  @Provides
  @Singleton
  PersistenceManager providePersistenceManager() {
    // TODO improve by injecting configuration into HandleManager
    if (config.getBooleanSetting("handle.enabled", true)) {
      return HandleManager.newHandleManager(config);
    } else {
      return new DefaultPersistenceManager();
    }
  }

  @Provides
  @Singleton
  Validator provideValidator() {
    return Validation.buildDefaultValidatorFactory().getValidator();
  }

}
