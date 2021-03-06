package nl.knaw.huygens.util;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import nl.knaw.huygens.timbuctoo.server.TimbuctooV4;

public class DropwizardMaker {
  public static DropwizardAppRule<TimbuctooConfiguration> makeTimbuctoo() {
    return new DropwizardAppRule<>(
      TimbuctooV4.class,
      ResourceHelpers.resourceFilePath("specrunstate/acceptance_test_config.yaml"),
      ConfigOverride.config("databasePath", ResourceHelpers.resourceFilePath("specrunstate/database")),
      ConfigOverride.config("authorizationsPath", ResourceHelpers.resourceFilePath("specrunstate/authorizations")),
      ConfigOverride.config("usersFilePath", ResourceHelpers.resourceFilePath("specrunstate/users.json")),
      ConfigOverride.config("loginsFilePath", ResourceHelpers.resourceFilePath("specrunstate/logins.json"))
    );
  }
}
