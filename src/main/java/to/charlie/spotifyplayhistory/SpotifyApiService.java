package to.charlie.spotifyplayhistory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionException;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PagingCursorbased;
import se.michaelthelin.spotify.model_objects.specification.PlayHistory;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.data.player.GetCurrentUsersRecentlyPlayedTracksRequest;
import to.charlie.spotifyplayhistory.config.SpotifyProperties;
import to.charlie.spotifyplayhistory.domain.entity.Artist;
import to.charlie.spotifyplayhistory.domain.entity.Play;
import to.charlie.spotifyplayhistory.domain.entity.Token;
import to.charlie.spotifyplayhistory.domain.repository.ArtistRepository;
import to.charlie.spotifyplayhistory.domain.repository.PlayRepository;
import to.charlie.spotifyplayhistory.domain.repository.TokenRepository;


@Service
public class SpotifyApiService
{
  private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyApiService.class);

  public SpotifyApi spotifyApi;

  private final PlayRepository playRepository;

  private final ArtistRepository artistRepository;

  public SpotifyApiService(PlayRepository playRepository,
                           ArtistRepository artistRepository,
                           SpotifyProperties spotifyProperties,
                           TokenRepository tokenRepository) throws URISyntaxException
  {
    this.playRepository = playRepository;
    this.artistRepository = artistRepository;

    Optional<Token> token = tokenRepository.findById(1);
    token.ifPresent(value -> {
      LOGGER.info("Setting refresh token from database");
      try
      {
        spotifyApi = new SpotifyApi.Builder()
            .setClientId(spotifyProperties.getSpotifyClientId())
            .setClientSecret(spotifyProperties.getSpotifyClientSecret())
            .setRedirectUri(new URI(spotifyProperties.getSpotifyBaseRedirectUri()))
            .setRefreshToken(value.getRefreshToken())
            .build();

        AuthorizationCodeRefreshRequest request = spotifyApi.authorizationCodeRefresh().build();
        AuthorizationCodeCredentials credentials = request.execute();
        spotifyApi.setAccessToken(credentials.getAccessToken());
        LOGGER.info("Refresh credentials expire in: {}", credentials.getExpiresIn());
      }
      catch (IOException | ParseException | URISyntaxException | SpotifyWebApiException e)
      {
        LOGGER.error("unable to refresh authorisation code ", e);
        // don't let this happen again :grrr:
        tokenRepository.deleteAll();
      }
    });

    if (token.isEmpty())
    {
      spotifyApi = new SpotifyApi.Builder()
          .setClientId(spotifyProperties.getSpotifyClientId())
          .setClientSecret(spotifyProperties.getSpotifyClientSecret())
          .setRedirectUri(new URI(spotifyProperties.getSpotifyBaseRedirectUri()))
          .build();

      LOGGER.info("no refresh token present");
    }
  }

  // todo start this after the token has been set by the controller
  @Scheduled(fixedDelay = 1000000)
  public void refreshAuthCode()
  {
    LOGGER.info("Refreshing access token");

    try
    {
      final AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
          .build();

      authorizationCodeRefreshRequest.executeAsync().thenAccept(this::authCodeCallback);
    }
    catch (CompletionException e)
    {
      LOGGER.error("Error refreshing token: ", e);
    }
  }

  public void authCodeCallback(AuthorizationCodeCredentials authorizationCodeCredentials)
  {
    // Set access token for further "spotifyApi" object usage
    spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

    LOGGER.info("Refreshed token expires in: {}", authorizationCodeCredentials.getExpiresIn());
  }

  //  @Scheduled(fixedDelay = 600000)
  //  public void getPlayHistory()
  //  {
  //    if (!StringUtils.hasText(spotifyApi.getAccessToken()))
  //    {
  //      LOGGER.info("Skipping getting play history");
  //      return;
  //    }
  //    LOGGER.info("getting play history");
  //
  //    GetCurrentUsersRecentlyPlayedTracksRequest request = spotifyApi.getCurrentUsersRecentlyPlayedTracks().limit(50).build();
  //    request.executeAsync().thenAccept(this::savePlayHistory);
  //  }

  private void savePlayHistory(PagingCursorbased<PlayHistory> history)
  {
    LOGGER.info("Got play history");

    long oldestTime = Long.MAX_VALUE;
    for (PlayHistory item : history.getItems())
    {
      var track = item.getTrack();
      long timePlayed = item.getPlayedAt().getTime();
      Optional<Play> play = playRepository.findById(timePlayed);

      if (timePlayed < oldestTime)
      {
        // subtract 1 because we don't want this item to come up again in the search
        oldestTime = timePlayed - 1;
      }

      // if it already exists move on
      // or if this is not a track - could be a podcast episode
      if (play.isPresent() || item.getTrack().getType() != ModelObjectType.TRACK)
      {
        //if there is one item in there then it follows that the rest should be there
        LOGGER.info("Skipping song at time {}", timePlayed);
        continue;
      }

      String trackId = track.getId();
      String trackName = track.getName();
      long songLength = track.getDurationMs();

      // save data to postgres
      Play pgPlay = new Play();
      Set<Artist> artists = new HashSet<>();

      for (ArtistSimplified trackArtist : track.getArtists())
      {
        String artistId = trackArtist.getId();
        Optional<Artist> optionalArtist = artistRepository.findByArtistId(artistId);
        if (optionalArtist.isPresent())
        {
          artists.add(optionalArtist.get());
        }
        else
        {
          Artist pgArtist = new Artist();
          pgArtist.setArtistId(artistId).setArtistName(trackArtist.getName());
          artists.add(pgArtist);
        }
      }

      pgPlay.setId(timePlayed)
          .setTrackId(trackId)
          .setTrackName(trackName)
          .setSongLength(songLength)
          .setArtists(artists);
      playRepository.save(pgPlay);
    }

    if (history.getNext() != null)
    {
      String nextUrl = history.getNext();
      // have another page of results to get (at least)
      LOGGER.info("more results to get {}", nextUrl);

      LOGGER.info("Calling again with before value of {}", oldestTime);
      GetCurrentUsersRecentlyPlayedTracksRequest request = spotifyApi.getCurrentUsersRecentlyPlayedTracks()
          .limit(50)
          .before(new Date(oldestTime))
          .build();
      request.executeAsync().thenAccept(this::savePlayHistory);
    }
  }

  public void createPlaylistWithNameAndTracks(String name,
                                              Set<String> trackIds) throws IOException, ParseException, SpotifyWebApiException
  {
    User user = spotifyApi.getCurrentUsersProfile().build().execute();
    String userId = user.getId();

    Playlist playlist = spotifyApi.createPlaylist(userId, name).build().execute();
    List<String> trackUris = new ArrayList<>(trackIds.size());

    trackIds.forEach(id -> {
      String uri = "spotify:track:" + id;
      trackUris.add(uri);
    });

    String[] uriArray = trackUris.toArray(new String[0]);
    SnapshotResult snapshotResult = spotifyApi.addItemsToPlaylist(playlist.getId(), uriArray).build().execute();
    LOGGER.info("Created playlist {}", snapshotResult.toString());
  }
}
