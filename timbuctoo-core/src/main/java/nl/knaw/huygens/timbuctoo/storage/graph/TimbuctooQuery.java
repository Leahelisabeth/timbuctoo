package nl.knaw.huygens.timbuctoo.storage.graph;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TimbuctooQuery {

  private Map<String, Object> hasProperties;
  private boolean searchByType;
  private Set<String> distinctValues;
  private boolean searchLatestOnly;

  public TimbuctooQuery() {
    this(Maps.<String, Object> newHashMap());

  }

  TimbuctooQuery(Map<String, Object> hasProperties) {
    this.searchLatestOnly(true);
    this.hasProperties = hasProperties;
    this.distinctValues = Sets.newHashSet();
  }

  /**
   * Uses the property only when the value is not null.
   * @param name the name of the property
   * @param value the value of the property
   * @return the current instance
   */
  public TimbuctooQuery hasNotNullProperty(String name, Object value) {
    if (value != null) {
      hasProperties.put(name, value);
    }
    return this;
  }

  /**
   * A method to search of the type the query is created for or not. Default is false.
   * @param searchByType boolean true or false
   * @return the current instance
   */
  public TimbuctooQuery searchByType(boolean searchByType) {
    this.searchByType = searchByType;
    return this;
  }

  /**
   * Be able to search for the latest version of a type only. Default is true.
   * @param searchLatestOnly the value true or false
   * @return the current instance
   */
  public TimbuctooQuery searchLatestOnly(boolean searchLatestOnly) {
    this.searchLatestOnly = searchLatestOnly;
    return this;
  }

  public boolean searchLatestOnly() {
    return searchLatestOnly;
  }

  /**
   * Method to add a filter to make sure a certain property and a value combination 
   * exists only once in a search result.  
   * @param propertyName the name of the property that should have a distinct value
   * @return the current instance
   */
  public TimbuctooQuery hasDistinctValue(String propertyName) {
    this.distinctValues.add(propertyName);
    return this;
  }

  public <T> T createGraphQuery(AbstractGraphQueryBuilder<T> queryCreator) throws NoSuchFieldException {
    queryCreator.setHasProperties(hasProperties);
    queryCreator.setSearchByType(searchByType);

    return queryCreator.build();

  }

}