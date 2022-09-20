package to.charlie.spotifyplayhistory.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.tracks.GetSeveralTracksRequest;
import to.charlie.spotifyplayhistory.domain.entity.AlbumEntity;
import to.charlie.spotifyplayhistory.domain.entity.ArtistEntity;
import to.charlie.spotifyplayhistory.domain.entity.MigrationEntity;
import to.charlie.spotifyplayhistory.domain.entity.TrackEntity;
import to.charlie.spotifyplayhistory.domain.repository.ArtistRepository;
import to.charlie.spotifyplayhistory.domain.repository.MigrationRepository;
import to.charlie.spotifyplayhistory.domain.repository.PlayRepository;
import to.charlie.spotifyplayhistory.domain.repository.TrackRepository;


@Component
@RequiredArgsConstructor
@Slf4j
public class FlywayMigrator
{
  private final PlayRepository playRepository;

  private final ArtistRepository artistRepository;

  private final TrackRepository trackRepository;

  private final MigrationRepository migrationRepository;

  public void migrateV1ToV2(SpotifyApi spotifyApi) throws InterruptedException
  {
    log.info("Starting migrator");
    ArrayList<String> allTrackIds = new ArrayList<>();
    playRepository.findAll().forEach(playEntity -> allTrackIds.add(playEntity.getTrackId()));

    int chunk = 50;
    String[] trackIdArray = allTrackIds.toArray(new String[0]);
    log.info("Got {} tracks to update", trackIdArray.length);
    for (int i = 0; i < trackIdArray.length; i += chunk)
    {
      String[] trackIdChunk = Arrays.copyOfRange(trackIdArray, i, Math.min(trackIdArray.length, i + chunk));
      GetSeveralTracksRequest request = spotifyApi.getSeveralTracks(trackIdChunk).build();

      boolean requestDone = false;

      while (!requestDone)
      {
        try
        {
          executeAndSave(request);
          requestDone = true;
        }
        catch (SpotifyWebApiException tooManyRequestsException)
        {
          log.error("Web API exception waiting 31 seconds", tooManyRequestsException);
          Thread.sleep(31000);
        }
        catch (Exception e)
        {
          log.error("Error getting tracks ", e);
        }
      }
    }
    migrationRepository.save(MigrationEntity.builder().id("V2").complete(true).build());
    log.info("Migrator done");
  }

  private void executeAndSave(GetSeveralTracksRequest request) throws SpotifyWebApiException, IOException, ParseException
  {
    log.info("Making track request");
    Track[] tracks = request.execute();

    for (Track track : tracks)
    {
      if (trackRepository.existsById(track.getId()))
      {
        continue;
      }

      Set<ArtistEntity> artistsForSong = new HashSet<>();

      for (ArtistSimplified artist : track.getArtists())
      {
        Optional<ArtistEntity> optionalArtist = artistRepository.findById(artist.getId());
        ArtistEntity artistEntity;
        if (optionalArtist.isEmpty())
        {
          artistEntity = ArtistEntity.builder().name(artist.getName()).id(artist.getId()).build();
          artistRepository.save(artistEntity);
        }
        else
        {
          artistEntity = optionalArtist.get();
        }

        artistsForSong.add(artistEntity);
      }
      AlbumSimplified albumSimplified = track.getAlbum();

      // album and genre information
      AlbumEntity album = AlbumEntity.builder()
          .id(albumSimplified.getId())
          .name(albumSimplified.getName())
          .type(albumSimplified.getAlbumType().getType())
          .build();

      TrackEntity trackEntity = TrackEntity.builder()
          .trackName(track.getName())
          .id(track.getId())
          .popularity(track.getPopularity())
          .songLength(track.getDurationMs())
          .artists(artistsForSong)
          .album(album)
          .build();

      trackRepository.save(trackEntity);
    }
  }

}
