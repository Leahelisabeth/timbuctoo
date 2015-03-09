package nl.knaw.huygens.timbuctoo.model.cnw;

import java.text.MessageFormat;
import java.util.List;

import nl.knaw.huygens.facetedsearch.model.FacetType;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Person.Gender;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class CNWPerson extends Person {

	private String name = "";
	private String koppelnaam = "";
	private List<String> networkDomains = Lists.newArrayList();

	private List<String> domains = Lists.newArrayList();
	private List<String> subdomains = Lists.newArrayList();
	private List<String> characteristics = Lists.newArrayList();

	private List<String> memberships = Lists.newArrayList(); // Lidmaatschappen : Om te zetten naar facet met sorteerbare lijst
	private String biodesurl = "";//Bioport url, Link, mogelijkheid tot doorklikken
	private String dbnlUrl = "";//Link, mogelijkheid tot doorklikken
	private List<CNWLink> verwijzingen = Lists.newArrayList();
	private String notities = ""; //Bronvermeldingen, tekstveld; Interface geen facet, wel zichtbaar in pop up (Onderscheid Korte of lange presentatie)
	private String opmerkingen = ""; //Tekstveld, met vast onderdeel (Afgesloten: XXXX-XX-XX);	Interface geen facet, wel zichtbaar in pop up
	private String aantekeningen = ""; // Kladblok: Niet zichtbaar voor gebruiker, wel bewaren
	private AltNames altNames = new AltNames();
	private List<String> relatives = Lists.newArrayList();

	private Datable cnwBirthYear;
	private Datable cnwDeathYear;
	private String birthdateQualifier = "";
	private String deathdateQualifier = "";

	// Container class, for entity reducer
	private static class AltNames {
		public List<AltName> list;

		public AltNames() {
			list = Lists.newArrayList();
		}

	}

	@IndexAnnotation(title = "Geslacht", fieldName = "dynamic_s_gender", isFaceted = true, canBeEmpty = true)
	public Gender getGender() {
		return super.getGender();
	}

	@IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_s_birthDate", isFaceted = false, canBeEmpty = true), //
			@IndexAnnotation(fieldName = "dynamic_k_birthDate", canBeEmpty = true, isSortable = true) })
	public Datable getBirthDate() {
		return super.getBirthDate();
	}

	@IndexAnnotation(fieldName = "dynamic_s_birthdatequalifier", canBeEmpty = true, isFaceted = false)
	public String getBirthdateQualifier() {
		return birthdateQualifier;
	}

	@IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_s_deathDate", isFaceted = false, canBeEmpty = true), //
			@IndexAnnotation(fieldName = "dynamic_k_deathDate", isSortable = true, canBeEmpty = true) })
	public Datable getDeathDate() {
		return super.getDeathDate();
	}

	@IndexAnnotation(fieldName = "dynamic_s_deathdatequalifier", canBeEmpty = true, isFaceted = false)
	public String getDeathdateQualifier() {
		return deathdateQualifier;
	}

	@IndexAnnotation(title = "Lidmaatschap(pen)", fieldName = "dynamic_s_membership", canBeEmpty = true, isFaceted = true)
	public List<String> getMemberships() {
		return memberships;
	}

	public void setActivities(List<String> activities) {
		this.memberships = activities;
	}

	public String getBiodesurl() {
		return biodesurl;
	}

	public void setBiodesurl(String biodesurl) {
		this.biodesurl = biodesurl;
	}

	public String getDbnlUrl() {
		return dbnlUrl;
	}

	public void setDbnlUrl(String dbnlUrl) {
		this.dbnlUrl = dbnlUrl;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}

	@IndexAnnotation(title = "Domein(en)", fieldName = "dynamic_s_domain", canBeEmpty = false, isFaceted = true)
	public List<String> getDomains() {
		return domains;
	}

	public void setSubDomains(List<String> subdomains) {
		this.subdomains = subdomains;
	}

	@IndexAnnotation(title = "Subdomein(en)", fieldName = "dynamic_s_subdomain", canBeEmpty = true, isFaceted = true)
	public List<String> getSubDomains() {
		return subdomains;
	}

	public void setCharacteristics(List<String> characteristicList) {
		this.characteristics = characteristicList;
	}

	@IndexAnnotation(title = "Karakteristiek(en)", fieldName = "dynamic_s_characteristic", canBeEmpty = false, isFaceted = true)
	public List<String> getCharacteristics() {
		return characteristics;
	}

	public void setKoppelnaam(String koppelnaam) {
		this.koppelnaam = koppelnaam;
	}

	@IndexAnnotation(title = "Koppelnaam", fieldName = "dynamic_s_koppelnaam", canBeEmpty = false, isFaceted = true)
	public String getKoppelnaam() {
		return koppelnaam;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAantekeningen() {
		return aantekeningen;
	}

	public void setAantekeningen(String aantekeningen) {
		this.aantekeningen = aantekeningen;
	}

	public String getNotities() {
		return notities;
	}

	public void setNotities(String notities) {
		this.notities = notities;
	}

	public String getOpmerkingen() {
		return opmerkingen;
	}

	public void setOpmerkingen(String opmerkingen) {
		this.opmerkingen = opmerkingen;
	}

	public List<CNWLink> getVerwijzingen() {
		return verwijzingen;
	}

	public void setVerwijzingen(List<CNWLink> verwijzingen) {
		this.verwijzingen = verwijzingen;
	}

	public void setNetworkDomains(List<String> networkDomains) {
		this.networkDomains = networkDomains;
	}

	public List<String> getNetworkDomains() {
		return networkDomains;
	}

	@IndexAnnotation(fieldName = "dynamic_s_types", isFaceted = false)
	public List<String> getTypes() {
		return super.getTypes();
	}

	@JsonIgnore
	@IndexAnnotation(title = "Netwerk(en)", fieldName = "dynamic_s_networkdomain", canBeEmpty = false, isFaceted = true)
	public String getNetworkDomainString() {
		return Joiner.on(" en ").join(networkDomains);
	}

	//	public String getDbngUrl() {
	//		return dbngUrl;
	//	}
	//
	//	public void setDbngUrl(String dbngUrl) {
	//		this.dbngUrl = dbngUrl;
	//	}
	//	public String getBntlUrl() {
	//		return bntlUrl;
	//	}
	//
	//	public void setBntlUrl(String bntlUrl) {
	//		this.bntlUrl = bntlUrl;
	//	}
	//
	//	public String getCenUrlAfz() {
	//		return cenUrlAfz;
	//	}
	//
	//	public void setCenUrlAfz(String cenUrlAfz) {
	//		this.cenUrlAfz = cenUrlAfz;
	//	}
	//
	//	public String getCenUrlOntv() {
	//		return cenUrlOntv;
	//	}
	//
	//	public void setCenUrlOntv(String cenUrlOntv) {
	//		this.cenUrlOntv = cenUrlOntv;
	//	}
	//	public String getNametype() {
	//		return nametype;
	//	}
	//
	//	public void setNametype(String nametype) {
	//		this.nametype = nametype;
	//	}
	//
	//	public String getOccupation() {
	//		return occupation;
	//	}
	//
	//	public void setOccupation(String occupation) {
	//		this.occupation = occupation;
	//	}
	//
	//	public String getOpmPolitics() {
	//		return opmPolitics;
	//	}
	//
	//	public void setOpmPolitics(String opmPolitics) {
	//		this.opmPolitics = opmPolitics;
	//	}
	//
	//	public String getPolitics() {
	//		return politics;
	//	}
	//
	//	public void setPolitics(String politics) {
	//		this.politics = politics;
	//	}
	//
	//	public String getWoonplaats() {
	//		return woonplaats;
	//	}
	//
	//	public void setWoonplaats(String woonplaats) {
	//		this.woonplaats = woonplaats;
	//	}
	//	public String getLevensbeschouwing() {
	//		return levensbeschouwing;
	//	}
	//
	//	public void setLevensbeschouwing(String levensbeschouwing) {
	//		this.levensbeschouwing = levensbeschouwing;
	//	}
	//
	//	public String getLiteratuur() {
	//		return literatuur;
	//	}
	//
	//	public void setLiteratuur(String literatuur) {
	//		this.literatuur = literatuur;
	//	}
	//	public String getEducation() {
	//		return education;
	//	}
	//
	//	public void setEducation(String education) {
	//		this.education = education;
	//	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	@IndexAnnotation(title = "Alternatieve naam", fieldName = "dynamic_s_altname", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
	public List<AltName> getAltNames() {
		return altNames.list;
	}

	public void setAltNames(List<AltName> altNames) {
		this.altNames.list = altNames;
	}

	@IndexAnnotation(fieldName = "dynamic_s_relatives", canBeEmpty = true, isFaceted = false)
	public List<String> getRelatives() {
		return relatives;
	}

	public void setShortDescription(String ignoredparameter) {
		// shortDescription should be a generated field 
	}

	@IndexAnnotation(fieldName = "dynamic_s_shortdescription", canBeEmpty = false, isFaceted = false)
	public String getShortDescription() {
		String charString = characteristics.isEmpty() ? "" : ", " + Joiner.on(", ").join(characteristics);
		return MessageFormat.format("{0} ({1}-{2}){3}",//
				StringUtils.defaultIfBlank(getName(), getKoppelnaam()),//
				extractYear(getBirthDate()), //
				extractYear(getDeathDate()), //
				charString);
	}

	private String extractYear(Datable deathDate) {
		String deathYear = deathDate == null ? "?" : deathDate.toString();
		return deathYear;
	}

	public void addRelative(String relative) {
		relatives.add(relative);
	}

	public void setCnwBirthYear(Integer cnwBirthYear) {
		this.cnwBirthYear = new Datable(String.valueOf(cnwBirthYear));
	}

	@IndexAnnotation(fieldName = "dynamic_i_birthyear", facetType = FacetType.RANGE, isFaceted = false)
	public Datable getCnwBirthYear() {
		return cnwBirthYear;
	}

	public void setBirthDateQualifier(String qualifier) {
		birthdateQualifier = qualifier;
	}

	@IndexAnnotation(fieldName = "dynamic_i_deathyear", facetType = FacetType.RANGE, isFaceted = false)
	public Datable getCnwDeathYear() {
		return cnwDeathYear;
	}

	public void setCnwDeathYear(Integer cnwDeathYear) {
		this.cnwDeathYear = new Datable(String.valueOf(cnwDeathYear));
	}

	public void setDeathDateQualifier(String qualifier) {
		deathdateQualifier = qualifier;
	}

}
