package to.charlie.spotifyplayhistory.domain.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@NoArgsConstructor
@Getter
@Setter
public class Token
{
  @Id
  private int id;

  private String refreshToken;
}
