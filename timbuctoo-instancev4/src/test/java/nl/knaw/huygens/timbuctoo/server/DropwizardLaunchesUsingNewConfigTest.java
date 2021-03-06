package nl.knaw.huygens.timbuctoo.server;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardLaunchesUsingNewConfigTest {

  @ClassRule
  public static final DropwizardAppRule<TimbuctooConfiguration> RULE = new DropwizardAppRule<>(
      TimbuctooV4.class,
      resourceFilePath("testrunstate/config-newstyle.yaml"),
      config("databaseConfiguration.databasePath", resourceFilePath("testrunstate/database")),
      config("securityConfiguration.localfile.authorizationsPath", resourceFilePath("testrunstate/authorizations")),
      config("securityConfiguration.localfile.usersFilePath", resourceFilePath("testrunstate/users.json")),
      config("securityConfiguration.localfile.loginsFilePath", resourceFilePath("testrunstate/logins.json"))
    );


  @Test
  public void dropwizardLaunches() throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(String.format("http://localhost:%d/healthcheck", RULE.getAdminPort()));

    Response response = target.request().get();

    assertThat(response.getStatus()).isEqualTo(200);
  }
}
