package to.charlie.integrationTests.spotifyPlayHistory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import se.michaelthelin.spotify.IHttpManager;
import to.charlie.integrationTests.spotifyPlayHistory.utilities.Context;
import to.charlie.integrationTests.spotifyPlayHistory.utilities.DataLoader;
import to.charlie.integrationTests.spotifyPlayHistory.utilities.RewritingHttpManager;

@Configuration
@ComponentScan(basePackages = {"to.charlie.spotifyplayhistory", "to.charlie.integrationTests.spotifyPlayHistory"})
@EnableAutoConfiguration
public class TestConfig {

  @Bean
  public Context context() {
    return new Context();
  }

  @Bean
  public DataLoader loader(final Context context) {
    return new DataLoader(context);
  }

  /**
   * Takes precedence over the application's own http manager so every SpotifyApi built by
   * SpotifyApiFactory talks to the WireMock container instead of the real Spotify. It has to be
   * named differently from the bean in SpringConfig, otherwise it is rejected as an override rather
   * than being applied as the primary candidate.
   */
  @Bean
  @Primary
  public IHttpManager wiremockSpotifyHttpManager(
          @Value("${WIREMOCK_BASE_URL}") final String wiremockBaseUrl) {
    return new RewritingHttpManager(wiremockBaseUrl);
  }
}
