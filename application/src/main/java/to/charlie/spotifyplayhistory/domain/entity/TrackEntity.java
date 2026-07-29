package to.charlie.spotifyplayhistory.domain.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "track", schema = "public")
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
  private String id;

  @Column(name = "name")
  private String trackName;

  @Column(name = "song_length")
  private long songLength;

  @Column(name = "popularity")
  private int popularity;

  @ManyToMany(cascade = CascadeType.PERSIST)
  @JoinTable(name = "track_artists",
      joinColumns = @JoinColumn(name = "track_id"),
      inverseJoinColumns = @JoinColumn(name = "artists_id"))
  private Set<ArtistEntity> artists = new LinkedHashSet<>();

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "album_id")
  private AlbumEntity album;
}
