package to.charlie.spotifyplayhistory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import se.michaelthelin.spotify.IHttpManager;
import se.michaelthelin.spotify.SpotifyHttpManager;


@Configuration
@EnableScheduling
public class SpringConfig
{
  /**
   * The http manager every {@link se.michaelthelin.spotify.SpotifyApi} is built with. Exposed as a
   * bean so the integration tests can replace it with one that points at their mock Spotify.
   */
  @Bean
  public IHttpManager spotifyHttpManager()
  {
    return new SpotifyHttpManager.Builder().build();
  }
}
