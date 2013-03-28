package nl.knaw.huygens.repository.resources;

import java.io.IOException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.storage.generic.JsonViews;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.inject.Inject;

@Path("resources/{entityType: [a-zA-Z]+}")
public class RESTAutoResource {

  public static final String ENTITY_PARAM = "entityType";

  private final StorageManager storageManager;
  private final DocumentTypeRegister docTypeRegistry;

  @Inject
  public RESTAutoResource(final StorageManager manager, final DocumentTypeRegister registry) {
    storageManager = manager;
    docTypeRegistry = registry;
  }

  @GET
  @Path("/all")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public List<? extends Document> getAllDocs(@PathParam(ENTITY_PARAM) String entityType, @QueryParam("rows") @DefaultValue("200") int rows, @QueryParam("start") int start) {
    Class<? extends Document> cls = getDocType(entityType);
    return storageManager.getAllLimited(cls, start, rows);
  }

  // TODO: test me
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/all")
  @JsonView(JsonViews.WebView.class)
  public <T extends Document> void getAllDocs(@PathParam(ENTITY_PARAM) String entityType, Document input) throws IOException {
    try {
      @SuppressWarnings("unchecked")
      Class<T> typedCls = (Class<T>) getDocType(entityType);
      @SuppressWarnings("unchecked")
      T typedDoc = (T) input;
      storageManager.addDocument(typedDoc, typedCls);
    } catch (ClassCastException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public Document getDoc(@PathParam(ENTITY_PARAM) String entityType, @PathParam("id") String id) {
    Class<? extends Document> cls = getDocType(entityType);
    Document doc = storageManager.getCompleteDocument(id, cls);
    if (doc == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return doc;
  }

  // TODO: test this! :-)
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  public <T extends Document> void putDoc(@PathParam(ENTITY_PARAM) String entityType, @PathParam("id") String id, Document input) throws IOException {
    try {
      @SuppressWarnings("unchecked")
      Class<T> typedCls = (Class<T>) getDocType(entityType);
      @SuppressWarnings("unchecked")
      T typedDoc = (T) input;
      storageManager.modifyDocument(typedDoc, typedCls);
    } catch (ClassCastException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  // TODO: test this! :-)
  @DELETE
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  public <T extends Document> void putDoc(@PathParam(ENTITY_PARAM) String entityType, @PathParam("id") String id) throws IOException {
    try {
      @SuppressWarnings("unchecked")
      Class<T> typedCls = (Class<T>) getDocType(entityType);
      T typedDoc = storageManager.getDocument(id, typedCls);
      storageManager.removeDocument(typedDoc, typedCls);
    } catch (ClassCastException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  private Class<? extends Document> getDocType(String entityType) {
    Class<? extends Document> cls = docTypeRegistry.getClassFromTypeString(entityType);
    if (cls == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return cls;
  }

}
