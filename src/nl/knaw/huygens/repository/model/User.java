package nl.knaw.huygens.repository.model;

import java.util.List;

import nl.knaw.huygens.repository.model.annotations.DoNotRegister;
import nl.knaw.huygens.repository.model.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.model.annotations.IDPrefix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@IDPrefix("USR")
@DocumentTypeName("user")
/*
 * Make sure the User-class is not registered by the DocumentTypeRegistry. The reason is that User has it's own resource class with it's specific authorization.
 * This creates a possibility for a security leak. This would occur when the UserResource URL and the RESTAutoResource URL have a different structure. 
 * Then an unauthorized user can access the User's via the RESTAutoResource.
 * When the User-class is not registered, the class will not be found by the RESTAutoResource and the User will only be available through the UserResource. 
 */
@DoNotRegister
public class User extends SystemDocument {
  private String userId; // a unique id to identify the use.
  private String vreId; // the name of the VRE.
  public String email;
  public String firstName;
  public String lastName;
  public List<String> groups;
  private List<String> roles;
  private String currentVariation;

  @Override
  @JsonIgnore
  public String getDescription() {
    return firstName + " " + lastName;
  }

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    return currentVariation;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String currentVariation) {
    this.currentVariation = currentVariation;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getVreId() {
    return vreId;
  }

  public void setVreId(String vREId) {
    this.vreId = vREId;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }
}
