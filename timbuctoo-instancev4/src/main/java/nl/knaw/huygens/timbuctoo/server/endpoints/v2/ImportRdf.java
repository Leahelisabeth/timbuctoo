package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import nl.knaw.huygens.timbuctoo.rdf.RdfImporter;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Path("/v2.1/rdf/import")
public class ImportRdf {

  private final GraphWrapper graphWrapper;

  public ImportRdf(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  @Consumes("application/n-triples")
  @POST
  public void post(String tripleString, @HeaderParam("VRE_ID") String vreName) {
    Model model = createModel(tripleString);
    new RdfImporter(graphWrapper, vreName).importRdf(model);
  }

  private Model createModel(String tripleString) {
    Model model = ModelFactory.createDefaultModel();
    InputStream in = new ByteArrayInputStream(tripleString.getBytes(StandardCharsets.UTF_8));
    model.read(in, null, "N3");
    return model;
  }
}
