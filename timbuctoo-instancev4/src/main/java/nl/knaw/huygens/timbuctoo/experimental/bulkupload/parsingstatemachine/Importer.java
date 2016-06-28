package nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine;

import com.google.common.base.Strings;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.Saver;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.VertexCreatedTwiceException;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Importer {
  private Set<Integer> idsToSkip;
  private ImportPropertyDescriptions properties;
  private final Saver saver;
  private Collection currentCollection;
  private String[] currentProperties;
  private Optional<String> curUniqueIdentifier = Optional.empty();
  private Optional<ImportPropertyDescription> identifierColumn;

  private ImportState currentState = ImportState.NOTHING;

  public Importer(Saver saver) {
    this.saver = saver;
  }

  public Result startCollection(String collectionName) {
    if (currentState != ImportState.NOTHING) {
      return Result.failure("I was not expecting a collection declaration here");
    }
    final Vre vre = saver.getVre();
    final Optional<Collection> collectionOpt = vre.getCollectionForCollectionName(collectionName);
    if (collectionOpt.isPresent()) {
      currentCollection = collectionOpt.get();
      currentState = ImportState.GETTING_DECLARATION;
      properties = new ImportPropertyDescriptions(currentCollection, saver::relationExists);
      identifierColumn = Optional.empty();
      idsToSkip = new HashSet<>();
      return Result.success();
    } else {
      currentState = ImportState.SKIPPING;
      if (collectionName.equals("")) {
        return Result.failure("This collection has no name!");
      } else {
        return Result.failure("Collection " + collectionName + " does not exist in " + vre.getVreName());
      }
    }
  }

  public Result registerPropertyType(int id, String type) {
    if (idsToSkip.contains(id) || currentState == ImportState.SKIPPING) {
      return Result.ignored();
    }
    if (currentState != ImportState.GETTING_DECLARATION) {
      return Result.failure("I was not expecting a property declaration here");
    }

    final ImportPropertyDescription propertyDescription = properties.getOrCreate(id);

    if (!propertyDescription.hasPropertyName()) {
      return Result.failure("You should specify the name before specifying the type");
    } else if ("relation".equals(type)) {
      if (!propertyDescription.isValidRelationName()) {
        idsToSkip.add(id);
        return Result.failure("Relation does not exist: " + propertyDescription.getPropertyName());
      } else {
        propertyDescription.setPropertyType("relation");
        return Result.success();
      }
    } else {
      if (!propertyDescription.isValidPropertyName()) {
        idsToSkip.add(id);
        return Result.failure("Collection " + currentCollection.getCollectionName() + " has no property configured " +
                                "with name " + propertyDescription.getPropertyName());
      } else {
        if (Strings.isNullOrEmpty(type)) {
          type = "basic";
        }
        if (!propertyDescription.setPropertyType(type)) {
          idsToSkip.add(id);
          return Result.failure("Unknown type");
        } else {
          return Result.success();
        }
      }
    }
  }

  public Result registerPropertyName(int id, String name) {
    if (idsToSkip.contains(id) || currentState == ImportState.SKIPPING) {
      return Result.ignored();
    }
    if (currentState != ImportState.GETTING_DECLARATION) {
      return Result.failure("I was not expecting a property declaration here");
    }
    if (properties.getOrCreate(id).setPropertyName(name)) {
      return Result.success();
    } else {
      idsToSkip.add(id);
      return Result.failure("Collection " + currentCollection.getCollectionName() + " has no property or relation " +
                              "configured with name " + name);
    }
  }

  public Result registerUnique(int id, boolean isUnique) {
    if (idsToSkip.contains(id) || currentState == ImportState.SKIPPING) {
      return Result.ignored();
    }
    if (currentState != ImportState.GETTING_DECLARATION) {
      return Result.failure("I was not expecting a uniqueness declaration here");
    }
    if (identifierColumn.isPresent()) {
      return Result.failure("Only one identifier column per collection is possible");
    } else {
      final ImportPropertyDescription identityProp = properties.getOrCreate(id);
      identifierColumn = Optional.of(identityProp);
      identityProp.setUnique(isUnique);
    }
    return Result.success();
  }

  public Result registerMetadata(int id, String metadata) {
    if (idsToSkip.contains(id) || currentState == ImportState.SKIPPING) {
      return Result.ignored();
    }
    if (currentState != ImportState.GETTING_DECLARATION) {
      return Result.failure("I was not expecting a target collection name here");
    }
    final ImportPropertyDescription propertyDescription = properties.getOrCreate(id);
    if (propertyDescription.getType() == null) {
      return Result.failure("You should specify the type before specifying the type subproperties");
    }
    propertyDescription.setMetadata(metadata);
    return Result.success();
  }

  public void startEntity() {
    if (currentState == ImportState.GETTING_DECLARATION) {
      //FIXME check for no mixed relation/property specifications
      currentState = ImportState.GETTING_VALUES;
    }

    currentProperties = new String[properties.getPropertyCount()];
    curUniqueIdentifier = Optional.empty();
  }

  public Result setValue(int id, String value) {
    if (idsToSkip.contains(id) || currentState == ImportState.SKIPPING) {
      return Result.ignored();
    }
    if (currentState != ImportState.GETTING_VALUES) {
      return Result.failure("I was not expecting a value property here");
    }
    Optional<ImportPropertyDescription> propOpt = properties.get(id);
    if (propOpt.isPresent()) {
      ImportPropertyDescription prop = propOpt.get();
      if (prop.isUnique()) {
        curUniqueIdentifier = Optional.of(value);
      }
      currentProperties[prop.getOrder()] = value;
      return Result.ignored();//actual validation will happen during finishEntity
    } else {
      return Result.failure("No property declared for id " + id);
    }
  }

  public HashMap<Integer, Result> finishEntity() {
    if (currentState == ImportState.SKIPPING) {
      return new HashMap<>();
    }
    HashMap<String, Object> propertyValues = new HashMap<>();
    HashMap<ImportPropertyDescription, String> relations = new HashMap<>();
    HashMap<Integer, Result> results = new HashMap<>();
    for (int i = 0, currentPropertiesLength = currentProperties.length; i < currentPropertiesLength; i++) {
      String value = currentProperties[i];
      if (value != null) {
        ImportPropertyDescription desc = properties.getByOrder(i);
        if (!idsToSkip.contains(desc.getId())) {
          if (desc.getType() == ImportPropertyDescription.PropertyTypes.BASIC) {
            propertyValues.put(desc.getDbPropertyName(), value);
            results.put(desc.getId(), Result.success());
          } else if (desc.getType() == ImportPropertyDescription.PropertyTypes.NUMERIC) {
            try {
              propertyValues.put(desc.getDbPropertyName(), Integer.parseInt(value));
              results.put(desc.getId(), Result.success());
            } catch (NumberFormatException e) {
              results.put(desc.getId(), Result.failure(value + " is not a valid number (fractions are not supported)"));
            }
          } else if (desc.getType() == ImportPropertyDescription.PropertyTypes.RELATION) {
            relations.put(desc, value);
          }
        }
      }
    }

    try {
      Vertex vertex = saver.setVertexProperties(currentCollection, curUniqueIdentifier, propertyValues);
      for (Map.Entry<ImportPropertyDescription, String> entry : relations.entrySet()) {
        final ImportPropertyDescription prop = entry.getKey();
        final String value = entry.getValue();
        try {
          final Optional<String> result = saver.makeRelation(vertex, prop.getPropertyName(), currentCollection,
                                                             prop.getMetadata()[0], value);
          if (result.isPresent()) {
            results.put(prop.getId(), Result.failure(result.get()));
          } else {
            results.put(prop.getId(), Result.success());
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          results.put(prop.getId(), Result.failure("Relation " + prop.getPropertyName() + " of " +
                                                     currentCollection.getCollectionName() + " has no target " +
                                                     "collection set"));
          idsToSkip.add(prop.getId());
        }
      }
    } catch (VertexCreatedTwiceException e) {
      results = new HashMap<>();
      results.put(identifierColumn.get().getId(), Result.failure("A property with this identifier already exists"));
    }

    return results;
  }

  public Result finishCollection() {
    currentState = ImportState.NOTHING;
    Collection prevCollection = currentCollection;
    currentCollection = null;
    return toResult(saver.checkLeftoverVerticesThatWereExpected(prevCollection));
  }

  public Result finishImport() {
    return toResult(saver.checkLeftoverCollectionsThatWereExpected())
      .and(toResult(saver.checkRelationtypesThatWereExpected()));
  }

  private Result toResult(Optional<String> failure) {
    return failure.map(Result::failure).orElse(Result.success());
  }

  public int getPropertyCount() {
    return properties.getPropertyCount();
  }

  private enum ImportState {
    NOTHING,
    GETTING_DECLARATION,
    GETTING_VALUES,
    SKIPPING
  }
}
