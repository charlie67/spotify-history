package to.charlie.spotifyplayhistory.influx;

import java.time.Instant;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;


@Measurement(name = "play")
public class InfluxPlayItem
{
  @Column(name = "time")
  private Instant time;

  @Column(name = "trackId")
  private String trackId;

  @Column(name = "trackName")
  private String trackName;

  @Column(name = "artistName")
  private String artistName;

  @Column(name = "artistId")
  private String artistId;

  @Column(name="songLength")
  private long songLength;
}
