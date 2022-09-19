package to.charlie.spotifyplayhistory.domain.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity(name = "album")
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

  @Column(name = "json")
  private String rawJson;

  @ManyToMany(cascade = CascadeType.PERSIST)
  @JoinTable(name = "artist_albums",
      joinColumns = @JoinColumn(name = "album_id"),
      inverseJoinColumns = @JoinColumn(name = "artist_id"))
  private Set<ArtistEntity> artists = new LinkedHashSet<>();

}
