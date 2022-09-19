package to.charlie.spotifyplayhistory.domain.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayEntity
{
  // this is the time
  @Id
  @Column(name = "id")
  private long id;

  @OneToOne(cascade = CascadeType.ALL, optional = false)
  @JoinColumn(name = "track_id")
  private TrackEntity trackEntity;
}
