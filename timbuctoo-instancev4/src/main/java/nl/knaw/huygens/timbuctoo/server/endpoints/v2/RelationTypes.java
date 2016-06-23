package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import nl.knaw.huygens.timbuctoo.relationtypes.RelationTypeDescription;
import nl.knaw.huygens.timbuctoo.relationtypes.RelationTypeService;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v2.1/system/relationtypes")
public class RelationTypes {


  private final RelationTypeService relationTypeService;
  private final GraphWrapper wrapper;

  public RelationTypes(GraphWrapper wrapper) {
    this.wrapper = wrapper;
    this.relationTypeService = new RelationTypeService(wrapper);
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get(@QueryParam("iname") String name) {
    List<RelationTypeDescription> relationTypeDescriptions = relationTypeService.get(name);
    return Response.ok(relationTypeDescriptions).build();
  }

  @POST
  @Consumes(APPLICATION_FORM_URLENCODED)
  public Response post(@FormParam("name") String name, @FormParam("inversename") String inversename,
                       @FormParam("source") String source, @FormParam("target") String target) {
    try (Transaction tx = wrapper.getGraph().tx()) {
      wrapper.getGraph().traversal().V()
        .has(T.label, LabelP.of("relationtype"))
        .has("relationtype_regularName", name)
        .drop()
        .toList();
      wrapper.getGraph().traversal()
        .addV("relationtype")
        .property("relationtype_regularName", name)
        .property("relationtype_inverseName", inversename)
        .property("relationtype_sourceTypeName", source)
        .property("relationtype_targetTypeName", target)
        .property("created", "{\"timeStamp\":1411642606354,\"userId\":\"Ronald\"}")
        .property("modified", "{\"timeStamp\":1411642606354,\"userId\":\"Ronald\"}")
        .property("isLatest", true)
        .property("relationtype_derived", false)
        .property("relationtype_reflexive", false)
        .property("relationtype_symmetric", false)
        .property("rev", 1)
        .property("tim_id", java.util.UUID.randomUUID().toString())
        .property("types", "[\"relationtype\"]")
        .toList();
      tx.commit();
      return Response.ok().entity("Relation created!").build();
    }
  }
}
