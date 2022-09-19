package to.charlie.spotifyplayhistory.domain.entity;

import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.Hibernate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity(name = "track")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackEntity
{
  // same as the id from spotify
  @Id
  @Column(name = "id", nullable = false)
  private String trackId;

  @Column(name = "name")
  private String trackName;

  @Column(name = "song_length")
  private long songLength;

  @Column(name = "popularity")
  private int popularity;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = "play_artist",
      joinColumns = @JoinColumn(name = "play_id"),
      inverseJoinColumns = @JoinColumn(name = "artist_id"))
  private Set<ArtistEntity> artists;

  @ManyToOne
  @JoinColumn(name = "album_entity_id")
  private AlbumEntity album;

  @Column(name = "json")
  private String rawJson;

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
    {
      return false;
    }
    TrackEntity that = (TrackEntity) o;
    return trackId != null && Objects.equals(trackId, that.trackId);
  }

  @Override
  public int hashCode()
  {
    return getClass().hashCode();
  }
}
