package to.charlie.spotifyplayhistory.domain.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor
@Getter
public class PlayEntity
{
  // this is the time
  @Id
  @Column(name = "id")
  private long id;

  @Column(name = "trackId")
  private String trackId;

  @Column(name = "trackName")
  private String trackName;

  @Column(name = "songLength")
  private long songLength;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = "play_artist",
      joinColumns = @JoinColumn(name = "play_id"),
      inverseJoinColumns = @JoinColumn(name = "artist_id"))
  private Set<ArtistEntity> artists;

  public long getId()
  {
    return id;
  }

  public PlayEntity setId(long time)
  {
    this.id = time;
    return this;
  }

  public String getTrackId()
  {
    return trackId;
  }

  public PlayEntity setTrackId(String trackId)
  {
    this.trackId = trackId;
    return this;
  }

  public String getTrackName()
  {
    return trackName;
  }

  public PlayEntity setTrackName(String trackName)
  {
    this.trackName = trackName;
    return this;
  }

  public long getSongLength()
  {
    return songLength;
  }

  public PlayEntity setSongLength(long songLength)
  {
    this.songLength = songLength;
    return this;
  }

  public Set<ArtistEntity> getArtists()
  {
    return artists;
  }

  public void setArtists(Set<ArtistEntity> artistEntities)
  {
    this.artists = artistEntities;
  }
}
