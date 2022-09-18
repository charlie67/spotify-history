package to.charlie.spotifyplayhistory.domain.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity()
public class ArtistEntity
{
  @Column(name = "artistName")
  private String artistName;

  @Id
  @Column(name = "artistId")
  private String artistId;

  @ManyToMany(mappedBy = "artists")
  private Set<PlayEntity> playEntitySet;

  public ArtistEntity()
  {
    // none
  }

  public String getArtistName()
  {
    return artistName;
  }

  public ArtistEntity setArtistName(String artistName)
  {
    this.artistName = artistName;
    return this;
  }

  public String getArtistId()
  {
    return artistId;
  }

  public ArtistEntity setArtistId(String artistId)
  {
    this.artistId = artistId;
    return this;
  }
}
