package seahawk.caloriecounter.domain.api.log;

import java.io.IOException;
import java.util.logging.*;

public class SimpleLogger {
  private static final String logFilename = "data/CC.log";

  private static Logger logger;

  static {
    logger = Logger.getLogger("CalorieCounterLogger");

    try {
      FileHandler fileHandler = new FileHandler(logFilename, true);
      fileHandler.setFormatter(new SimpleFormatter());
      fileHandler.setLevel(Level.WARNING);
      logger.addHandler(fileHandler);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public static void info(String message) {
    logger.log(Level.INFO, message);
  }

  @SuppressWarnings("UnusedDeclaration")
  public static void warning(String message) {
    logger.log(Level.WARNING, message);
  }

  @SuppressWarnings("UnusedDeclaration")
  public static void error(String message) {
    logger.log(Level.SEVERE, message);
  }

  public static void error(String message, Throwable thrown) {
    logger.log(Level.SEVERE, message, thrown);
  }

  @SuppressWarnings("UnusedDeclaration")
  public static void error(Throwable thrown) {
    logger.log(Level.SEVERE, "", thrown);
  }
}
