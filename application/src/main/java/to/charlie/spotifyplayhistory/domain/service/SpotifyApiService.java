package to.charlie.spotifyplayhistory.domain.service;

import static to.charlie.spotifyplayhistory.domain.TopTimeRangeEnum.LONG_TERM;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.BadRequestException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PagingCursorbased;
import se.michaelthelin.spotify.model_objects.specification.PlayHistory;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import se.michaelthelin.spotify.requests.data.player.GetCurrentUsersRecentlyPlayedTracksRequest;
import to.charlie.spotifyplayhistory.config.FlywayMigrator;
import to.charlie.spotifyplayhistory.config.SpotifyApiFactory;
import to.charlie.spotifyplayhistory.domain.entity.AlbumEntity;
import to.charlie.spotifyplayhistory.domain.entity.ArtistEntity;
import to.charlie.spotifyplayhistory.domain.entity.PlayEntity;
import to.charlie.spotifyplayhistory.domain.entity.Token;
import to.charlie.spotifyplayhistory.domain.entity.TrackEntity;
import to.charlie.spotifyplayhistory.domain.repository.AlbumRepository;
import to.charlie.spotifyplayhistory.domain.repository.ArtistRepository;
import to.charlie.spotifyplayhistory.domain.repository.MigrationRepository;
import to.charlie.spotifyplayhistory.domain.repository.PlayRepository;
import to.charlie.spotifyplayhistory.domain.repository.TokenRepository;
import to.charlie.spotifyplayhistory.domain.repository.TrackRepository;


@Service
public class SpotifyApiService
{
  private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyApiService.class);

  /**
   * Spotify expires a refresh token six months after it was issued. Nothing in the token response
   * says so, so the deadline is worked out from when the token was stored.
   */
  private static final Period REFRESH_TOKEN_LIFETIME = Period.ofMonths(6);

  /**
   * How close to expiry a refresh token has to be before every refresh starts warning about it.
   */
  private static final Duration EXPIRY_WARNING_WINDOW = Duration.ofDays(14);

  /** Only ever one user, so the stored token always sits in the same row. */
  private static final int TOKEN_ID = 1;

  private final FlywayMigrator flywayMigrator;

  public SpotifyApi spotifyApi;

  private final PlayRepository playRepository;

  private final ArtistRepository artistRepository;

  private final MigrationRepository migrationRepository;

  private final TrackRepository trackRepository;

  private final AlbumRepository albumRepository;

  private final TokenRepository tokenRepository;

  private final SpotifyApiFactory spotifyApiFactory;

  /**
   * Set when Spotify refuses the stored refresh token, so the UI can tell the difference between a
   * user who has never signed in and one whose token has expired and needs signing in again. Held
   * in memory only - after a restart there is simply no stored token, which asks for the same thing.
   */
  private volatile boolean reauthorisationRequired;

  public SpotifyApiService(PlayRepository playRepository,
                           ArtistRepository artistRepository,
                           SpotifyApiFactory spotifyApiFactory,
                           TokenRepository tokenRepository,
                           FlywayMigrator flywayMigrator,
                           MigrationRepository migrationRepository,
                           TrackRepository trackRepository,
                           AlbumRepository albumRepository) throws URISyntaxException
  {
    this.playRepository = playRepository;
    this.artistRepository = artistRepository;
    this.flywayMigrator = flywayMigrator;
    this.migrationRepository = migrationRepository;
    this.trackRepository = trackRepository;
    this.albumRepository = albumRepository;
    this.tokenRepository = tokenRepository;
    this.spotifyApiFactory = spotifyApiFactory;

    spotifyApi = spotifyApiFactory.builder().build();

    Optional<Token> token = tokenRepository.findById(TOKEN_ID);

    if (token.isEmpty())
    {
      LOGGER.info("no refresh token present - sign in at /login to start recording");
      return;
    }

    LOGGER.info("Setting refresh token from database");
    spotifyApi.setRefreshToken(token.get().getRefreshToken());
    refreshAccessToken();
  }

  @PostConstruct
  public void migrate() throws InterruptedException
  {
    if (!migrationRepository.existsByIdAndCompleteTrue("V2"))
    {
      flywayMigrator.migrateV1ToV2(spotifyApi);
    }
  }

  @Scheduled(fixedDelayString = "${history.refreshTokenDelayMs:1000000}",
             initialDelayString = "${history.refreshTokenInitialDelayMs:0}")
  public void refreshAuthCode()
  {
    refreshAccessToken();
  }

  /**
   * Trades the stored refresh token for a new access token.
   *
   * <p>Runs synchronously. The refresh used to be fired off with {@code executeAsync} and a
   * {@code thenAccept}, which meant a failure was left sitting in a completion stage nobody looked
   * at: an expired token looked exactly like a successful refresh from here.
   */
  private void refreshAccessToken()
  {
    if (!StringUtils.hasText(spotifyApi.getRefreshToken()))
    {
      LOGGER.info("No refresh token held, nothing to refresh - the user needs to sign in at /login");
      return;
    }

    LOGGER.info("Refreshing access token");

    try
    {
      AuthorizationCodeCredentials credentials = spotifyApi.authorizationCodeRefresh().build().execute();
      onRefreshedCredentials(credentials);
    }
    catch (BadRequestException e)
    {
      // Spotify answers a refresh it will not honour with 400 invalid_grant, which is what an
      // expired token looks like: since 20 July 2026 refresh tokens die six months after they are
      // issued. The library keeps only the error_description, but per RFC 6749 section 5.2 every
      // 400 from the token endpoint means the grant itself is no good, and retrying with the same
      // token cannot fix any of them.
      LOGGER.warn("Spotify rejected the refresh token ({}), discarding it. The user must sign in again at /login",
          e.getMessage());
      discardRefreshToken();
    }
    catch (IOException | ParseException | SpotifyWebApiException e)
    {
      // A network blip, a 5xx or rate limiting says nothing about the token, so it is kept and the
      // next scheduled run tries again with it.
      LOGGER.error("Could not refresh the access token, will try again on the next run", e);
    }
  }

  private void onRefreshedCredentials(AuthorizationCodeCredentials credentials)
  {
    spotifyApi.setAccessToken(credentials.getAccessToken());
    reauthorisationRequired = false;

    // Spotify may hand back a new refresh token on a refresh. When it does the old one stops
    // working, so the new one has to replace what is stored or the next restart signs in with a
    // dead token.
    if (StringUtils.hasText(credentials.getRefreshToken()))
    {
      LOGGER.info("Spotify issued a new refresh token, storing it");
      spotifyApi.setRefreshToken(credentials.getRefreshToken());
      storeRefreshToken(credentials.getRefreshToken());
    }

    LOGGER.info("Refreshed token expires in: {}", credentials.getExpiresIn());
    warnIfRefreshTokenIsNearingExpiry();
  }

  /**
   * Takes the tokens from a completed sign in. Called by the authentication controller once the
   * user has come back from Spotify with an authorisation code.
   */
  public void onAuthorisationCodeExchanged(AuthorizationCodeCredentials credentials) throws URISyntaxException
  {
    spotifyApi = spotifyApiFactory.builder()
        .setAccessToken(credentials.getAccessToken())
        .setRefreshToken(credentials.getRefreshToken())
        .build();

    storeRefreshToken(credentials.getRefreshToken());
    reauthorisationRequired = false;
  }

  private void storeRefreshToken(String refreshToken)
  {
    Token token = new Token();
    token.setId(TOKEN_ID);
    token.setRefreshToken(refreshToken);
    token.setIssuedAt(Instant.now());
    tokenRepository.save(token);
  }

  /**
   * Throws away a refresh token Spotify has refused, and asks for a new sign in. The token is
   * dropped from storage first so that nothing - this run or a later restart - tries it again.
   */
  private void discardRefreshToken()
  {
    tokenRepository.deleteAll();
    forgetTokens();
    reauthorisationRequired = true;
  }

  /**
   * Drops the tokens held in memory, leaving the client signed out. Anything stored is left alone;
   * the reauthorisation flag is cleared because it describes a stored token that was rejected, and
   * after this there is nothing signed in for Spotify to have rejected.
   */
  public void forgetTokens()
  {
    try
    {
      spotifyApi = spotifyApiFactory.builder().build();
    }
    catch (URISyntaxException e)
    {
      // The redirect URI is configuration, and it already parsed once when this bean was built.
      throw new IllegalStateException("Could not rebuild the Spotify client", e);
    }

    reauthorisationRequired = false;
  }

  private void warnIfRefreshTokenIsNearingExpiry()
  {
    Optional<Instant> expiresAt = refreshTokenExpiresAt();

    if (expiresAt.isEmpty())
    {
      return;
    }

    Duration remaining = Duration.between(Instant.now(), expiresAt.get());

    if (remaining.compareTo(EXPIRY_WARNING_WINDOW) < 0)
    {
      LOGGER.warn("The Spotify refresh token expires in {} days ({}). Sign in again at /login before then to avoid a gap in the play history",
          Math.max(remaining.toDays(), 0), expiresAt.get());
    }
  }

  public boolean isLoggedIn()
  {
    return StringUtils.hasText(spotifyApi.getAccessToken());
  }

  /**
   * True when a stored refresh token was rejected by Spotify and the user has to sign in again.
   */
  public boolean isReauthorisationRequired()
  {
    return reauthorisationRequired;
  }

  public Optional<Instant> refreshTokenIssuedAt()
  {
    return tokenRepository.findById(TOKEN_ID).map(Token::getIssuedAt);
  }

  /**
   * When the stored refresh token stops working. Empty when nothing is stored, or when the token
   * predates the column that records the issue date and so has an unknown age.
   */
  public Optional<Instant> refreshTokenExpiresAt()
  {
    return refreshTokenIssuedAt().map(issuedAt -> issuedAt.atZone(ZoneOffset.UTC)
        .plus(REFRESH_TOKEN_LIFETIME)
        .toInstant());
  }

  public void getTopArtists()
  {
    if (!isLoggedIn())
    {
      LOGGER.info("Skipping getting top artists");
      return;
    }
    GetUsersTopArtistsRequest request = spotifyApi.getUsersTopArtists().limit(50).offset(0).time_range(LONG_TERM.getValue()).build();

  }

  @Scheduled(fixedDelayString = "${history.playHistoryDelayMs:600000}",
             initialDelayString = "${history.playHistoryInitialDelayMs:0}")
  public void getPlayHistory()
  {
    if (!isLoggedIn())
    {
      LOGGER.info("Skipping getting play history");
      return;
    }
    LOGGER.info("getting play history");

    GetCurrentUsersRecentlyPlayedTracksRequest request = spotifyApi.getCurrentUsersRecentlyPlayedTracks().limit(50).build();
    request.executeAsync().thenAccept(this::savePlayHistory);
  }

  private void savePlayHistory(PagingCursorbased<PlayHistory> history)
  {
    LOGGER.info("Got play history {} items", history.getItems().length);

    long oldestTime = Long.MAX_VALUE;
    for (PlayHistory item : history.getItems())
    {
      long timePlayed = item.getPlayedAt().getTime();
      TrackSimplified trackSimple = item.getTrack();
      LOGGER.info("Checking item {} at time {}", trackSimple.getName(), timePlayed);
      Optional<PlayEntity> play = playRepository.findById(timePlayed);

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
        LOGGER.info("Skipping song at time {} name {}", timePlayed, trackSimple.getName());
        continue;
      }

      String trackId = trackSimple.getId();
      Track track;
      try
      {
        track = spotifyApi.getTrack(trackId).build().execute();
      }
      catch (IOException | SpotifyWebApiException | ParseException e)
      {
        LOGGER.error("Error getting full track info", e);
        return;
      }

      // save data to postgres
      Set<ArtistEntity> artistEntities = new HashSet<>();

      for (ArtistSimplified trackArtist : track.getArtists())
      {
        String artistId = trackArtist.getId();
        Optional<ArtistEntity> optionalArtist = artistRepository.findById(artistId);
        if (optionalArtist.isPresent())
        {
          artistEntities.add(optionalArtist.get());
        }
        else
        {
          ArtistEntity artistEntity = ArtistEntity.builder().id(artistId).name(trackArtist.getName()).build();
          artistEntities.add(artistEntity);
          artistRepository.save(artistEntity);
        }
      }

      AlbumSimplified album = track.getAlbum();
      String albumId = album.getId();
      Optional<AlbumEntity> optionalAlbum = albumRepository.findById(albumId);
      AlbumEntity albumEntity;
      if (optionalAlbum.isPresent())
      {
        albumEntity = optionalAlbum.get();
      }
      else
      {
        ArtistSimplified[] albumArtists = (album.getArtists());
        Set<ArtistEntity> artistsForAlbum = new HashSet<>();

        for (ArtistSimplified albumArtist : albumArtists)
        {
          Optional<ArtistEntity> optionalArtist = artistRepository.findById(albumArtist.getId());
          ArtistEntity artistEntity;
          if (optionalArtist.isEmpty())
          {
            artistEntity = ArtistEntity.builder().name(albumArtist.getName()).id(albumArtist.getId()).build();
            artistRepository.save(artistEntity);
          }
          else
          {
            artistEntity = optionalArtist.get();
          }

          artistsForAlbum.add(artistEntity);
        }

        albumEntity = AlbumEntity.builder()
            .id(albumId)
            .name(album.getName())
            .artists(artistsForAlbum)
            .type(album.getAlbumType().type)
            .build();
        albumRepository.save(albumEntity);
      }

      Optional<TrackEntity> optionalTrack = trackRepository.findById(trackId);
      TrackEntity trackEntity;
      if (optionalTrack.isPresent())
      {
        trackEntity = optionalTrack.get();
      }
      else
      {
        trackEntity = TrackEntity.builder()
            .id(trackId)
            .trackName(trackSimple.getName())
            .popularity(track.getPopularity())
            .songLength(trackSimple.getDurationMs())
            .artists(artistEntities)
            .album(albumEntity)
            .build();
        trackRepository.save(trackEntity);
      }

      PlayEntity playEntity = PlayEntity.builder().id(timePlayed)
          .trackId(trackId)
          .build();
      playRepository.save(playEntity);
      LOGGER.info("Saved new play at time {} name {}", timePlayed, trackSimple.getName());
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

    int chunk = 50;
    String[] uriArray = trackUris.toArray(new String[0]);
    for (int i = 0; i < uriArray.length; i += chunk)
    {
      String[] uriChunk = Arrays.copyOfRange(uriArray, i, Math.min(uriArray.length, i + chunk));
      SnapshotResult snapshotResult = spotifyApi.addItemsToPlaylist(playlist.getId(), uriChunk).build().execute();
      LOGGER.info("Created playlist {}", snapshotResult.toString());
    }
  }
}
