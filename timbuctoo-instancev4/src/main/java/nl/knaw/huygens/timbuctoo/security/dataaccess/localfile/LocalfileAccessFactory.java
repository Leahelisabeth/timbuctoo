package nl.knaw.huygens.timbuctoo.security.dataaccess.localfile;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessNotPossibleException;
import nl.knaw.huygens.timbuctoo.security.dataaccess.LoginAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.UserAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.VreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DirectoryHealthCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.FileHealthCheck;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;
import static org.slf4j.LoggerFactory.getLogger;

public class LocalfileAccessFactory implements AccessFactory {
  private static final Logger LOG = getLogger(LocalfileAccessFactory.class);

  public LocalfileAccessFactory(String authorizationsPath, String loginsFilePath, String usersFilePath) {
    this.authorizationsPath = authorizationsPath;
    this.loginsFilePath = loginsFilePath;
    this.usersFilePath = usersFilePath;
  }

  public LocalfileAccessFactory() {
  }

  @JsonProperty
  private String authorizationsPath;

  @JsonProperty
  private String loginsFilePath;

  @JsonProperty
  private String usersFilePath;

  @Override
  public Iterator<Tuple<String, HealthCheck>> getHealthChecks() {
    List<Tuple<String, HealthCheck>> list = new ArrayList<>();
    list.add(tuple("login file available", new FileHealthCheck(Paths.get(loginsFilePath))));
    list.add(tuple("authorizations directory available", new DirectoryHealthCheck(Paths.get(authorizationsPath))));
    list.add(tuple("users file available", new FileHealthCheck(Paths.get(usersFilePath))));
    return list.iterator();
  }

  @Override
  public LoginAccess getLoginAccess() throws AccessNotPossibleException {
    Path loginFile = Paths.get(loginsFilePath);
    if (!loginFile.toFile().isFile()) {
      LOG.error("File " + loginFile.toAbsolutePath() + " does not exist");
      throw new AccessNotPossibleException("File does not exist");
    }
    return new LocalFileLoginAccess(loginFile);
  }

  @Override
  public UserAccess getUserAccess() throws AccessNotPossibleException {
    Path userPath = Paths.get(usersFilePath);
    if (!userPath.toFile().isFile()) {
      LOG.error("File " + userPath.toAbsolutePath() + " does not exist");
      throw new AccessNotPossibleException("File does not exist");
    }
    return new LocalFileUserAccess(userPath);
  }

  @Override
  public VreAuthorizationAccess getVreAuthorizationAccess() throws AccessNotPossibleException {
    Path authorizationsFolder = Paths.get(authorizationsPath);
    if (!authorizationsFolder.toFile().isDirectory()) {
      if (!authorizationsFolder.toFile().mkdirs()) {
        LOG.error("Directory " + authorizationsFolder.toAbsolutePath() + " does not exist and cannot be created");
        throw new AccessNotPossibleException("Direcory does not exist");
      }
    }
    return new LocalFileVreAuthorizationAccess(authorizationsFolder);
  }
}
