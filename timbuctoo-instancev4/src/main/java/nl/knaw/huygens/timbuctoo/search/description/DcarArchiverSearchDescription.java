package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.fulltext.FullTextSearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import nl.knaw.huygens.timbuctoo.search.description.sort.SortDescription;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.search.description.Property.localProperty;
import static nl.knaw.huygens.timbuctoo.search.description.fulltext.FullTextSearchDescription
  .createLocalSimpleFullTextSearchDescription;
import static nl.knaw.huygens.timbuctoo.search.description.sort.SortFieldDescription.newSortFieldDescription;

class DcarArchiverSearchDescription extends AbstractSearchDescription {
  private final PropertyDescriptor displayNameDescriptor;
  private final PropertyDescriptor idDescriptor;
  private final List<FacetDescription> facetDescriptions;
  private final List<String> sortableFields;
  private final List<String> fullTextSearchFields;
  private final Map<String, PropertyDescriptor> dataPropertyDescriptors;
  private final List<FullTextSearchDescription> fullTextSearchDescriptions;
  private final SortDescription sortDescription;

  public DcarArchiverSearchDescription(PropertyDescriptorFactory propertyDescriptorFactory,
                                       FacetDescriptionFactory facetDescriptionFactory) {
    displayNameDescriptor = propertyDescriptorFactory.getLocal("dcararchiver_nameEng", String.class);
    idDescriptor = propertyDescriptorFactory.getLocal(SearchDescription.ID_DB_PROP, String.class);
    facetDescriptions = createFacetDescriptions(facetDescriptionFactory);
    sortableFields = Lists.newArrayList("dynamic_sort_title", "dynamic_k_period");
    fullTextSearchFields =
      Lists.newArrayList("dynamic_t_history", "dynamic_t_nameEng", "dynamic_t_nameNLD", "dynamic_t_notes");

    dataPropertyDescriptors = createDataPropertyDescriptors(propertyDescriptorFactory);
    fullTextSearchDescriptions = createFullTextSearchDescriptions();
    sortDescription = createSortDescription();
  }

  private SortDescription createSortDescription() {
    PropertyParserFactory propertyParserFactory = new PropertyParserFactory();
    newSortFieldDescription()
      .withName("dynamic_sort_title")
      .withDefaultValue("")
      .withProperty(localProperty()
        .withName("titleEng")
        .withParser(propertyParserFactory.getParser(String.class))
      )
      .build();
    // TODO add date range sortfield build from startDate and endDate.
    return new SortDescription(Lists.newArrayList());
  }

  private ArrayList<FullTextSearchDescription> createFullTextSearchDescriptions() {

    return Lists.newArrayList(
      createLocalSimpleFullTextSearchDescription("dynamic_t_history", "dcararchiver_history"),
      createLocalSimpleFullTextSearchDescription("dynamic_t_nameEng", "dcararchiver_nameEng"),
      createLocalSimpleFullTextSearchDescription("dynamic_t_nameNLD", "dcararchiver_nameNld"),
      createLocalSimpleFullTextSearchDescription("dynamic_t_notes", "dcararchiver_notes"));
  }

  private Map<String, PropertyDescriptor> createDataPropertyDescriptors(PropertyDescriptorFactory pdf) {
    Map<String, PropertyDescriptor> propertyDescriptors = Maps.newHashMap();

    propertyDescriptors.put("_id", pdf.getLocal("tim_id", String.class));
    propertyDescriptors.put("beginDate", pdf.getLocal("dcararchiver_beginDate", String.class));
    propertyDescriptors.put("endDate", pdf.getLocal("dcararchiver_endDate", String.class));
    propertyDescriptors.put("nameEng", pdf.getLocal("dcararchiver_nameEng", String.class));
    propertyDescriptors.put("types", pdf.getLocal("dcararchiver_types", List.class));

    return propertyDescriptors;
  }

  private List<FacetDescription> createFacetDescriptions(FacetDescriptionFactory fdf) {
    return Lists.newArrayList(
      fdf.createMultiValueListFacetDescription("dynamic_s_type", "dcararchiver_value"),
      fdf.createListFacetDescription("dynamic_s_place", String.class, "dcarkeyword_value", "has_archiver_place"),
      fdf.createListFacetDescription("dynamic_s_person", PersonNames.class, "person_names", "has_archiver_person"),
      fdf.createListFacetDescription("dynamic_s_subject", String.class, "dcarkeyword_value", "has_archiver_keyword")
    );
    // TODO add date range facet build from startDate and endDate.
  }

  @Override
  protected List<FacetDescription> getFacetDescriptions() {
    return facetDescriptions;
  }

  @Override
  protected Map<String, PropertyDescriptor> getDataPropertyDescriptors() {
    return dataPropertyDescriptors;
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
  public List<String> getSortableFields() {
    return sortableFields;
  }

  @Override
  public List<String> getFullTextSearchFields() {
    return fullTextSearchFields;
  }

  @Override
  public String getType() {
    return "dcararchiver";
  }

  @Override
  protected List<FullTextSearchDescription> getFullTextSearchDescriptions() {
    return fullTextSearchDescriptions;
  }

  @Override
  protected SortDescription getSortDescription() {
    return sortDescription;
  }
}