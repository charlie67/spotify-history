package to.charlie.spotifyplayhistory.domain.entity;

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
