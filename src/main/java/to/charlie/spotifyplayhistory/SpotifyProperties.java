package to.charlie.spotifyplayhistory;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@ConfigurationProperties(prefix = "history")
@Configuration("spotifyProperties")
public class SpotifyProperties
{
  private String spotifyClientId;

  private String spotifyClientSecret;

  private String spotifyBaseRedirectUri;

  private String influxUrl;

  private String influxPassword;

  private String influxUsername;

  public String getSpotifyClientId()
  {
    return spotifyClientId;
  }

  public void setSpotifyClientId(String spotifyClientId)
  {
    this.spotifyClientId = spotifyClientId;
  }

  public String getSpotifyClientSecret()
  {
    return spotifyClientSecret;
  }

  public void setSpotifyClientSecret(String spotifyClientSecret)
  {
    this.spotifyClientSecret = spotifyClientSecret;
  }

  public String getSpotifyBaseRedirectUri()
  {
    return spotifyBaseRedirectUri;
  }

  public void setSpotifyBaseRedirectUri(String spotifyBaseRedirectUri)
  {
    this.spotifyBaseRedirectUri = spotifyBaseRedirectUri;
  }

  public String getInfluxUrl()
  {
    return influxUrl;
  }

  public void setInfluxUrl(String influxUrl)
  {
    this.influxUrl = influxUrl;
  }

  public String getInfluxPassword()
  {
    return influxPassword;
  }

  public void setInfluxPassword(String influxPassword)
  {
    this.influxPassword = influxPassword;
  }

  public String getInfluxUsername()
  {
    return influxUsername;
  }

  public void setInfluxUsername(String influxUsername)
  {
    this.influxUsername = influxUsername;
  }
}
