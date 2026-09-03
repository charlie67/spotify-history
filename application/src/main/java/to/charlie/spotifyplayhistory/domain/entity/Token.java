package to.charlie.spotifyplayhistory.domain.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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

  /**
   * When this refresh token was handed out by Spotify. Null for tokens stored before the column
   * existed, whose age cannot be known. Spotify expires a refresh token six months after it is
   * issued, so this is what the remaining lifetime is measured from.
   */
  private Instant issuedAt;
}
