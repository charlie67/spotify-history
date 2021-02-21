package to.charlie.spotifyplayhistory.influx;

import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.stereotype.Service;


@Service
public class InfluxDbService
{
  private final InfluxDB influxDB;

  public InfluxDbService()
  {
    this.influxDB = InfluxDBFactory.connect("http://localhost:8086", "admin", "supersecretpassword");
    Pong response = this.influxDB.ping();
    if (response.getVersion().equalsIgnoreCase("unknown")) {
      throw new IllegalArgumentException("Can't connect to database");
    }

    influxDB.createDatabase("spotify");
    influxDB.createRetentionPolicy("defaultPolicy", "spotify", "30d", 1, true);
    influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
  }

  public void write(BatchPoints batchPoints)
  {
    influxDB.write(batchPoints);
  }

  public int countValue(long time)
  {
    Query query = new Query("Select * FROM play WHERE time = " + time, "spotify");
    QueryResult queryResult = influxDB.query(query);

    InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
    List<InfluxPlayItem> memoryPointList = resultMapper.toPOJO(queryResult, InfluxPlayItem.class);

    return memoryPointList.size();
  }
}
