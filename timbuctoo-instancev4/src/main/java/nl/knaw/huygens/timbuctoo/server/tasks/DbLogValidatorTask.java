package nl.knaw.huygens.timbuctoo.server.tasks;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.databaselog.DatabaseFixer;
import nl.knaw.huygens.timbuctoo.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.databaselog.GraphLogValidator;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

public class DbLogValidatorTask extends Task {
  public static final Logger LOG = LoggerFactory.getLogger(DbLogValidatorTask.class);
  private final DatabaseLog logGenerator;
  private final DatabaseFixer databaseFixer;
  private final GraphLogValidator graphLogValidator;

  public DbLogValidatorTask(TinkerpopGraphManager graphManager) {
    super("validatelog");
    logGenerator = new DatabaseLog(graphManager);
    databaseFixer = new DatabaseFixer(graphManager);
    graphLogValidator = new GraphLogValidator(graphManager);
  }

  @Override
  public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
    Stopwatch validateStopWatch = Stopwatch.createStarted();
    graphLogValidator.writeReport(output);
    LOG.info("Log validation took {}", validateStopWatch.stop());
    output.flush();
  }
}
