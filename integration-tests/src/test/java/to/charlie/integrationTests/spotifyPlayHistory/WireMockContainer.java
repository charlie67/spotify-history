package to.charlie.integrationTests.spotifyPlayHistory;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class WireMockContainer extends GenericContainer<WireMockContainer> {

  private static final Logger logger = LoggerFactory.getLogger(WireMockContainer.class);
  private static final DockerImageName IMAGE = DockerImageName.parse("wiremock/wiremock:3.9.1");
  private static final int WIREMOCK_PORT = 8080;
  private static WireMockContainer container;

  private WireMockContainer() {
    super(IMAGE);
    withExposedPorts(WIREMOCK_PORT);
  }

  public static WireMockContainer getInstance() {
    if (container == null) {
      container = new WireMockContainer();
      container.start();
      final String baseUrl =
          "http://" + container.getHost() + ":" + container.getMappedPort(WIREMOCK_PORT);
      logger.info("WireMockContainer started with base URL: {}", baseUrl);
      // RewritingHttpManager sends every Spotify call here instead of api.spotify.com and
      // accounts.spotify.com. No default stubs are registered: the scheduled jobs are held off
      // with a long initial delay in the test properties, so nothing calls Spotify until a
      // scenario asks it to.
      System.setProperty("WIREMOCK_BASE_URL", baseUrl);
    }
    return container;
  }

  /**
   * A client for the running container, used by the steps to register stubs and read the request
   * journal.
   */
  public static WireMock client() {
    final WireMockContainer instance = getInstance();

    return new WireMock(instance.getHost(), instance.getMappedPort(WIREMOCK_PORT));
  }

  @Override
  public void start() {
    super.start();
    logger.info("WireMockContainer is starting.");
  }

  @Override
  public void stop() {
    //do nothing, JVM handles shut down
    logger.info("WireMockContainer is stopping.");
  }
}
