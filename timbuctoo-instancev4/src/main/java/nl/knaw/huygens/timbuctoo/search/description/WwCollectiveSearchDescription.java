package nl.knaw.huygens.timbuctoo.search.description;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nl.knaw.huygens.timbuctoo.model.CollectiveType;
import nl.knaw.huygens.timbuctoo.model.DocumentType;
import nl.knaw.huygens.timbuctoo.model.Gender;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.fulltext.FullTextSearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;

import java.util.List;
import java.util.Map;

public class WwCollectiveSearchDescription extends AbstractSearchDescription implements SearchDescription {
  private static final List<String> SORTABLE_FIELDS = Lists.newArrayList();
  private static final List<String> FULL_TEXT_SEARCH_FIELDS = Lists.newArrayList("dynamic_t_name");
  private final List<FacetDescription> facetDescriptions;
  private final String type = "wwcollective";
  private final PropertyDescriptorFactory propertyDescriptorFactory;
  private final Map<String, PropertyDescriptor> dataDescriptors;
  private final PropertyDescriptor displayNameDescriptor;
  private final PropertyDescriptor idDescriptor;

  public WwCollectiveSearchDescription(PropertyDescriptorFactory propertyDescriptorFactory,
                                       FacetDescriptionFactory facetDescriptionFactory) {
    facetDescriptions = createFacetDescriptions(facetDescriptionFactory);

    this.propertyDescriptorFactory = propertyDescriptorFactory;
    dataDescriptors = createDataDescriptors();
    displayNameDescriptor = createDisplayNameDescriptor();
    idDescriptor = propertyDescriptorFactory.getLocal(ID_DB_PROP, String.class);
  }

  private PropertyDescriptor createDisplayNameDescriptor() {
    PropertyDescriptor descriptor = propertyDescriptorFactory.getLocal("wwcollective_name", String.class);
    return descriptor;
  }

  private Map<String, PropertyDescriptor> createDataDescriptors() {
    Map<String, PropertyDescriptor> dataDescriptors = Maps.newHashMap();
    dataDescriptors.put("_id", propertyDescriptorFactory.getLocal(ID_DB_PROP, String.class));

    return dataDescriptors;
  }

  @Override
  protected List<FacetDescription> getFacetDescriptions() {
    return facetDescriptions;
  }

  @Override
  protected Map<String, PropertyDescriptor> getDataPropertyDescriptors() {
    return dataDescriptors;
  }

  @Override
  protected PropertyDescriptor getDisplayNameDescriptor() {
    return displayNameDescriptor;
  }

  @Override
  protected PropertyDescriptor getIdDescriptor() {
    return idDescriptor;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  protected List<FullTextSearchDescription> getFullTextSearchDescriptions() {
    return Lists.newArrayList();
  }

  @Override
  public List<String> getSortableFields() {
    return SORTABLE_FIELDS;
  }

  @Override
  public List<String> getFullTextSearchFields() {
    return FULL_TEXT_SEARCH_FIELDS;
  }
  
  private List<FacetDescription> createFacetDescriptions(FacetDescriptionFactory facetDescriptionFactory) {
	    return Lists.newArrayList(
//	            facetDescriptionFactory.createDatableRangeFacetDescription("dynamic_i_date", "wwdocument_date"),
//	            facetDescriptionFactory.createListFacetDescription(
//	                    "dynamic_s_origin", LocationNames.class, "names", "hasPublishLocation"),
//
//	            facetDescriptionFactory.createListFacetDescription(
//	                    "dynamic_s_language", String.class, "wwlanguage_name", "hasWorkLanguage"),
//
//	            facetDescriptionFactory.createListFacetDescription(
//	                    "dynamic_s_genre", String.class, "wwkeyword_value", "hasGenre"),
//
//	            facetDescriptionFactory.createListFacetDescription(
//	                    "dynamic_s_sources", String.class, "wwdocument_title", "hasDocumentSource"),
//
//	            facetDescriptionFactory.createChangeRangeFacetDescription("dynamic_i_modified", "modified"),

	            facetDescriptionFactory.createListFacetDescription(
	                    "dynamic_s_type", CollectiveType.class, "wwcollective_type")
	    );
	  }

}
