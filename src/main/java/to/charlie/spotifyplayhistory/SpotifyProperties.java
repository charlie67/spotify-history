package to.charlie.spotifyplayhistory;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@ConfigurationProperties(prefix = "spotify")
@Configuration("spotifyProperties")
public class SpotifyProperties
{
  private String clientId;
  private String clientSecret;
  private String baseRedirectUri;

  public String getClientId()
  {
    return clientId;
  }

  public void setClientId(String clientId)
  {
    this.clientId = clientId;
  }

  public String getClientSecret()
  {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret)
  {
    this.clientSecret = clientSecret;
  }

  public String getBaseRedirectUri()
  {
    return baseRedirectUri;
  }

  public void setBaseRedirectUri(String baseRedirectUri)
  {
    this.baseRedirectUri = baseRedirectUri;
  }
}
