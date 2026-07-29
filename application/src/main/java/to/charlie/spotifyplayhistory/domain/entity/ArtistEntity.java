package to.charlie.spotifyplayhistory.domain.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "artist", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistEntity
{
  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "name")
  private String name;

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
    return Objects.equals(name, that.name) && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(name, id);
  }
}
