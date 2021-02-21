package to.charlie.spotifyplayhistory.influx;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Pong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import to.charlie.spotifyplayhistory.SpotifyProperties;


@Service
public class InfluxDbService
{
  private final InfluxDB influxDB;

  @Autowired
  public InfluxDbService(SpotifyProperties spotifyProperties)
  {
    this.influxDB = InfluxDBFactory.connect(spotifyProperties.getInfluxUrl(),
                                            spotifyProperties.getInfluxUsername(),
                                            spotifyProperties.getInfluxPassword());
    Pong response = this.influxDB.ping();
    if (response.getVersion().equalsIgnoreCase("unknown"))
    {
      throw new IllegalArgumentException("Can't connect to database");
    }

    influxDB.createDatabase("spotify");
    influxDB.createRetentionPolicy("defaultPolicy", "spotify", "730d", 1, true);
    influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
  }

  public void write(BatchPoints batchPoints)
  {
    influxDB.write(batchPoints);
  }
}
