package to.charlie.integrationTests.spotifyPlayHistory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainer extends PostgreSQLContainer<PostgresContainer> {

  private static final Logger logger = LoggerFactory.getLogger(PostgresContainer.class);
  private static final String IMAGE_VERSION = "postgres:15.2-alpine";
  private static PostgresContainer container;

  private PostgresContainer() {
    super(IMAGE_VERSION);
  }

  public static PostgresContainer getInstance() {
    if (container == null) {
      container = new PostgresContainer()
          .withUsername("spotify")
          .withPassword("spotify")
          .withDatabaseName("spotify");
      container.start();
      logger.info("PostgresContainer started with URL: {}", container.getJdbcUrl());
      System.setProperty("DB_URL", container.getJdbcUrl());
      System.setProperty("DB_USERNAME", container.getUsername());
      System.setProperty("DB_PASSWORD", container.getPassword());
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    logger.info("PostgresContainer is starting.");
  }

  @Override
  public void stop() {
    logger.info("PostgresContainer is stopping.");
  }
}
