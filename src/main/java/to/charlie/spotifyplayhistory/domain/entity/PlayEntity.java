package to.charlie.spotifyplayhistory.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity(name = "play")
@Table(name = "play", schema = "public")
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

  //  @OneToOne(cascade = CascadeType.ALL, optional = false)
  //  @JoinColumn(name = "track_id")
  //  private TrackEntity trackEntity;

  @Column(name = "track_id")
  private String trackId;
}
