package to.charlie.spotifyplayhistory.postgres;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;


@Entity
public class Play
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
  private Set<Artist> artists;

  public Play()
  {
    // empty
  }

  public long getId()
  {
    return id;
  }

  public Play setId(long time)
  {
    this.id = time;
    return this;
  }

  public String getTrackId()
  {
    return trackId;
  }

  public Play setTrackId(String trackId)
  {
    this.trackId = trackId;
    return this;
  }

  public String getTrackName()
  {
    return trackName;
  }

  public Play setTrackName(String trackName)
  {
    this.trackName = trackName;
    return this;
  }

  public long getSongLength()
  {
    return songLength;
  }

  public Play setSongLength(long songLength)
  {
    this.songLength = songLength;
    return this;
  }

  public Set<Artist> getArtists()
  {
    return artists;
  }

  public void setArtists(Set<Artist> artists)
  {
    this.artists = artists;
  }
}
