package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.google.common.base.Stopwatch;
import nl.knaw.huygens.timbuctoo.databaselog.DatabaseFixer;
import nl.knaw.huygens.timbuctoo.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HistoryToLogMigration implements DatabaseMigration{
  private static final Logger LOG = LoggerFactory.getLogger(HistoryToLogMigration.class);

  @Override
  public void beforeMigration(GraphWrapper graphManager) {
    // no before needed.
  }

  @Override
  public void execute(GraphWrapper graphWrapper) throws IOException {
    // Add the missing Vertices and Edges, before creating a log.
    Stopwatch fixStopwatch = Stopwatch.createStarted();
    DatabaseFixer databaseFixer = new DatabaseFixer(graphWrapper);
    databaseFixer.fix();
    LOG.info("Fixing the database took {}", fixStopwatch.stop());

    Stopwatch generateStopwatch = Stopwatch.createStarted();
    DatabaseLog databaseLog = new DatabaseLog(graphWrapper);
    databaseLog.generate();
    LOG.info("Log creation took {}", generateStopwatch.stop());
  }
}
