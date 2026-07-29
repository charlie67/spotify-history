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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "album", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumEntity
{
  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "type")
  private String type;

  @Column(name = "name")
  private String name;

  @ManyToMany(cascade = CascadeType.PERSIST)
  @JoinTable(name = "artist_albums",
      joinColumns = @JoinColumn(name = "album_id"),
      inverseJoinColumns = @JoinColumn(name = "artist_id"))
  private Set<ArtistEntity> artists = new LinkedHashSet<>();

  @OneToMany(mappedBy = "album")
  private Set<TrackEntity> tracks = new LinkedHashSet<>();
}
