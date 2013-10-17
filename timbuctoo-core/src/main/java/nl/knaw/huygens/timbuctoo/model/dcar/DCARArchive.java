package nl.knaw.huygens.timbuctoo.model.dcar;

import java.util.List;

import nl.knaw.huygens.timbuctoo.facet.FacetType;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.Archive;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.util.PeriodHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class DCARArchive extends Archive {

  /** Migration: Name of source file */
  private String origFilename;

  /** ING Forms: "Ref. code country"; refcode facet */
  private List<String> countries;

  /** ING Forms: "Ref. code repository"; refcode facet */
  private String refCodeArchive;

  /** ING Forms: "Reference code" */
  private String refCode;

  /** ING Forms: "Code or indication of sub-fonds" */
  private String subCode;

  /** ING Forms: "Indication of series, Nos." */
  private String series;

  /** ING Forms: "Item, No." */
  private String itemNo;

  /** ING Forms: "Title" */
  private String titleNld;

  /** ING Forms: "English title"; text searchable */
  private String titleEng;

  /** ING Forms: "Begin date"; date facet */
  private String beginDate;

  /** ING Forms: "End date"; date facet */
  private String endDate;

  /** ING Forms: "Period description" */
  private String periodDescription;

  /** ING Forms: "Extent" */
  private String extent;

  /** ING Forms: "Additional finding aid" */
  private String findingAid;

  /** ING Forms: "Name(s) of Creator(s)"; as relation */

  /** ING Forms: "Scope and content" */
  private String scope;

  /** ING Forms: "Keyword(s) geography"; as relation; place facet */

  /** ING Forms: "Keyword(s) subject"; as relation; subject facet */

  /** ING Forms: "Keyword(s) person"; as relation; person facet */

  /** ING Forms: "Remarks"; text searchable */
  private String notes;

  /** ING Forms: "Record made by-" */
  private String madeBy;

  /** ING Forms: "Reminders" */
  private String reminders;

  /** ING Forms: "Title related overhead level of description; as relation" */

  /** ING Forms: "Title(s) related underlying level(s) of description; as relation" */

  /** ING Forms: "Other related units of description; as relation" */

  public DCARArchive() {
    countries = Lists.newArrayList();
  }

  @Override
  public String getDisplayName() {
    return getTitleEng();
  }

  public String getOrigFilename() {
    return origFilename;
  }

  public void setOrigFilename(String origFilename) {
    this.origFilename = origFilename;
  }

  public List<String> getCountries() {
    return countries;
  }

  public void setCountries(List<String> countries) {
    this.countries = countries;
  }

  public void addCountry(String country) {
    countries.add(country);
  }

  public String getRefCodeArchive() {
    return refCodeArchive;
  }

  public void setRefCodeArchive(String refCodeArchive) {
    this.refCodeArchive = refCodeArchive;
  }

  public String getRefCode() {
    return refCode;
  }

  public void setRefCode(String refCode) {
    this.refCode = refCode;
  }

  public String getSubCode() {
    return subCode;
  }

  public void setSubCode(String subCode) {
    this.subCode = subCode;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_refcode", canBeEmpty = true, isFaceted = true)
  public String getIndexedRefCode() {
    StringBuilder builder = new StringBuilder();
    for (String country : getCountries()) {
      appendTo(builder, country);
    }
    appendTo(builder, getRefCodeArchive());
    return builder.toString();
  }

  private void appendTo(StringBuilder builder, String text) {
    if (text != null && text.length() != 0) {
      if (builder.length() != 0) {
        builder.append(' ');
      }
      builder.append(text);
    }
  }

  public String getSeries() {
    return series;
  }

  public void setSeries(String series) {
    this.series = series;
  }

  public String getItemNo() {
    return itemNo;
  }

  public void setItemNo(String itemNo) {
    this.itemNo = itemNo;
  }

  public String getTitleNld() {
    return titleNld;
  }

  public void setTitleNld(String title) {
    titleNld = title;
  }

  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_sort_title", canBeEmpty = true, isFaceted = false, isSortable = true),
      @IndexAnnotation(fieldName = "dynamic_t_text", canBeEmpty = true, isFaceted = false, isSortable = false) })
  public String getTitleEng() {
    return titleEng;
  }

  public void setTitleEng(String title) {
    titleEng = title;
  }

  @IndexAnnotation(fieldName = "dynamic_s_begin_date", canBeEmpty = true, isFaceted = true, facetType = FacetType.DATE)
  public String getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(String date) {
    beginDate = date;
  }

  @IndexAnnotation(fieldName = "dynamic_s_end_date", canBeEmpty = true, isFaceted = true, facetType = FacetType.DATE)
  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String date) {
    endDate = date;
  }

  @JsonIgnore
  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_sort_period", canBeEmpty = true, isFaceted = false, facetType = FacetType.PERIOD, isSortable = true),
      @IndexAnnotation(fieldName = "dynamic_s_period", canBeEmpty = true, isFaceted = true, facetType = FacetType.PERIOD, isSortable = false) })
  public String getActivePeriod() {
    return PeriodHelper.createPeriod(beginDate, endDate);
  }

  public String getPeriodDescription() {
    return periodDescription;
  }

  public void setPeriodDescription(String description) {
    periodDescription = description;
  }

  public String getExtent() {
    return extent;
  }

  public void setExtent(String extent) {
    this.extent = extent;
  }

  public String getFindingAid() {
    return findingAid;
  }

  public void setFindingAid(String findingAid) {
    this.findingAid = findingAid;
  }

  @JsonIgnore
  public List<EntityRef> getCreators() {
    return getRelations().get("is_created_by");
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_place", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getPlaceKeywords() {
    return getRelations().get("has_place");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_subject", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getSubjectKeywords() {
    return getRelations().get("has_keyword");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_person", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getPersons() {
    return getRelations().get("has_person");
  }

  @IndexAnnotation(fieldName = "dynamic_t_text", canBeEmpty = true, isFaceted = false)
  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getMadeBy() {
    return madeBy;
  }

  public void setMadeBy(String madeBy) {
    this.madeBy = madeBy;
  }

  public String getReminders() {
    return reminders;
  }

  public void setReminders(String reminders) {
    this.reminders = reminders;
  }

  @JsonIgnore
  public List<EntityRef> getOverheadArchives() {
    return getRelations().get("has_parent_archive");
  }

  @JsonIgnore
  public List<EntityRef> getUnderlyingArchives() {
    return getRelations().get("has_child_archive");
  }

  @JsonIgnore
  public List<EntityRef> getRelatedUnitArchives() {
    return getRelations().get("has_sibling_archive");
  }

}
