package to.charlie.spotifyplayhistory.domain.entity;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity(name = "artist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistEntity
{
  @Column(name = "name")
  private String name;

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "json")
  private String rawJson;

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }
    ArtistEntity that = (ArtistEntity) o;
    return Objects.equals(name, that.name) && Objects.equals(id, that.id) && Objects.equals(rawJson,
                                                                                            that.rawJson);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(name, id, rawJson);
  }
}
