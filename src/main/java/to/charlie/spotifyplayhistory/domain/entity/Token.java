package to.charlie.spotifyplayhistory.domain.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Token
{
  @Id
  private int id;

  private String refreshToken;

  public int getId()
  {
    return id;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  public String getRefreshToken()
  {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken)
  {
    this.refreshToken = refreshToken;
  }
}
