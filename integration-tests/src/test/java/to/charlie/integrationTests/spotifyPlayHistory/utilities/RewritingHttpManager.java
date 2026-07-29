package to.charlie.integrationTests.spotifyPlayHistory.utilities;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.IHttpManager;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

/**
 * Sends every Spotify request to WireMock instead of the real API.
 *
 * <p>The http manager is the only interception point the library offers. Its data requests would
 * honour a host configured on the SpotifyApi builder, but the authorisation requests overwrite that
 * host with a hardcoded {@code accounts.spotify.com} inside their own {@code build()}, so rewriting
 * the URI here is the only way to catch both. Anything not addressed to Spotify is passed through
 * untouched, and a request that escapes to the real API will fail loudly rather than silently
 * succeed.
 */
@Slf4j
public class RewritingHttpManager implements IHttpManager {

  private static final Set<String> SPOTIFY_HOSTS = Set.of("api.spotify.com", "accounts.spotify.com");

  private final IHttpManager delegate = new SpotifyHttpManager.Builder().build();

  private final String baseUrl;

  public RewritingHttpManager(final String baseUrl) {
    this.baseUrl = baseUrl;
  }

  @Override
  public String get(final URI uri, final Header[] headers)
      throws IOException, SpotifyWebApiException, ParseException {
    return delegate.get(rewrite(uri), headers);
  }

  @Override
  public String post(final URI uri, final Header[] headers, final HttpEntity body)
      throws IOException, SpotifyWebApiException, ParseException {
    return delegate.post(rewrite(uri), headers, body);
  }

  @Override
  public String put(final URI uri, final Header[] headers, final HttpEntity body)
      throws IOException, SpotifyWebApiException, ParseException {
    return delegate.put(rewrite(uri), headers, body);
  }

  @Override
  public String delete(final URI uri, final Header[] headers, final HttpEntity body)
      throws IOException, SpotifyWebApiException, ParseException {
    return delegate.delete(rewrite(uri), headers, body);
  }

  private URI rewrite(final URI uri) {
    // Everything this manager sees is built by SpotifyApi, so an unrecognised host means a request
    // is about to leave for the real internet. Fail rather than let a test quietly depend on it.
    if (!SPOTIFY_HOSTS.contains(uri.getHost())) {
      throw new IllegalStateException("Refusing to send a request to a host that is not Spotify: " + uri);
    }

    final StringBuilder rewritten = new StringBuilder(baseUrl).append(uri.getRawPath());
    if (uri.getRawQuery() != null) {
      rewritten.append('?').append(uri.getRawQuery());
    }

    try {
      final URI target = new URI(rewritten.toString());
      log.debug("Rewrote Spotify request {} to {}", uri, target);

      return target;
    } catch (final URISyntaxException e) {
      throw new IllegalStateException("Could not rewrite Spotify URI " + uri, e);
    }
  }
}
