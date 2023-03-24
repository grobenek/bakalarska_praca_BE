package szathmary.peter.bakalarka.repository;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.BadRequestException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import szathmary.peter.bakalarka.entity.Temperature;
import szathmary.peter.bakalarka.exception.NoDataFound;

@Slf4j
@Repository
public class TemperatureRepository {

  private final InfluxDBClient influxDBClient;

  private final WriteApi writeApi;
  @Value("${influxdb.bucket.temperature}")
  private String BUCKET_NAME;
  @Value("${influxdb.org}")
  private String ORGANIZATION;
  @Value("${influxdb.logging.enabled}")
  private boolean influxLoggingEnabled;

  public TemperatureRepository(InfluxDBClient influxDBClient) {
    this.influxDBClient = influxDBClient;
    this.writeApi = this.influxDBClient.makeWriteApi();
  }

  @PostConstruct
  public void init() {
    if (this.influxLoggingEnabled) {
      this.influxDBClient.setLogLevel(LogLevel.BASIC);
    }
  }

  public List<Temperature> findAll() {
    String query = String.format("""
        from(bucket: "%s")
          |> range(start: 0)
          |> filter(fn: (r) => r._measurement == "%s")
          |> sort(columns:["_time"])""", BUCKET_NAME, BUCKET_NAME);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, Temperature.class);
  }

  public List<Temperature> findAllBetweenDate(Instant startDate, Instant endDate) {
    String query = String.format("""
        from(bucket: "%1$s")
          |> range(start: %2$s, stop: %3$s)
          |> filter(fn: (r) => r._measurement == "temperature")
          |> sort(columns:["_time"])""", BUCKET_NAME, startDate, endDate);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, Temperature.class);
  }

  public List<Temperature> getTemperaturesSince(Instant since) {
    String query = String.format("""
        from(bucket: "%1$s")
          |> range(start: %2$s)
          |> filter(fn: (r) => r._measurement == "temperature")
          |> sort(columns:["_time"])
        """, BUCKET_NAME, since);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, Temperature.class);
  }

  public Temperature getLastTemperature() throws NoDataFound {
    String query = String.format("""
        from(bucket: "%1$s")
           |> range(start: 0)
           |> filter(fn: (r) => r._measurement == "temperature")
           |> sort(columns:["_time"])
           |> last()
         """, BUCKET_NAME);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    List<Temperature> result = queryApi.query(query, ORGANIZATION, Temperature.class);

    if (result.isEmpty()) {
      throw new NoDataFound("Multiple or none temperatures were returned in getLastTemperature()");
    }

    return result.get(0);
  }


  public List<List<Temperature>> findGroupedMinMaxMean(Instant startDate, Instant endDate) {
    long timeRangeMillis = endDate.toEpochMilli() - startDate.toEpochMilli();
    long windowDurationMillis = timeRangeMillis / 400;
    String windowDuration = windowDurationMillis + "ms";

    String minQuery =
        "from(bucket: \"" + BUCKET_NAME + "\")" + " |> range(start: " + startDate + ", stop: "
            + endDate + ")" + " |> filter(fn: (r) => r._measurement == \"temperature\")"
            + " |> aggregateWindow(every: " + windowDuration + ", fn: min, createEmpty: false)"
            + " |> sort(columns:[\"_time\"])" + " |> yield(name: \"min\")";

    String maxQuery =
        "from(bucket: \"" + BUCKET_NAME + "\")" + " |> range(start: " + startDate + ", stop: "
            + endDate + ")" + " |> filter(fn: (r) => r._measurement == \"temperature\")"
            + " |> aggregateWindow(every: " + windowDuration + ", fn: max, createEmpty: false)"
            + " |> sort(columns:[\"_time\"])" + " |> yield(name: \"max\")";

    String meanQuery =
        "from(bucket: \"" + BUCKET_NAME + "\")" + " |> range(start: " + startDate + ", stop: "
            + endDate + ")" + " |> filter(fn: (r) => r._measurement == \"temperature\")"
            + " |> aggregateWindow(every: " + windowDuration + ", fn: mean, createEmpty: false)"
            + " |> sort(columns:[\"_time\"])" + " |> yield(name: \"mean\")";

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    List<Temperature> minTemperatureList;
    List<Temperature> maxTemperatureList;
    List<Temperature> meanTemperatureList;
    try {
      minTemperatureList = queryApi.query(minQuery, ORGANIZATION, Temperature.class);
      maxTemperatureList = queryApi.query(maxQuery, ORGANIZATION, Temperature.class);
      meanTemperatureList = queryApi.query(meanQuery, ORGANIZATION, Temperature.class);
    } catch (BadRequestException e) {
      log.error("No data found to aggregate in findGroupedMinMaxMean between {} and {}", startDate,
          endDate);
      return null;
    }

    List<List<Temperature>> listOfLists = new ArrayList<>();
    listOfLists.add(minTemperatureList);
    listOfLists.add(maxTemperatureList);
    listOfLists.add(meanTemperatureList);

    return listOfLists;
  }

  public void save(Temperature temperature) {
    // time cannot be in future
    Instant currentUtcTime = Instant.now();
    if (temperature.getTime().isAfter(currentUtcTime)) {
      log.info("{} is after now ({} in UTC) timestamp, replacing it with {}", temperature.getTime(),
          currentUtcTime, currentUtcTime);
      temperature.setTime(currentUtcTime);
    }

    Point pointToSave = Point.measurement("temperature")
        .addField("value", temperature.getTemperature())
        .time(temperature.getTime().getEpochSecond(), WritePrecision.S);

    this.writeApi.writePoint(BUCKET_NAME, ORGANIZATION, pointToSave);
    this.writeApi.flush();
  }

  public void saveAll(List<Temperature> temperatures) {
    List<Point> points = new ArrayList<>();
    Instant currentUtcTime = Instant.now();

    for (Temperature temperature : temperatures) {

      if (temperature.getTime().isAfter(currentUtcTime)) {
        log.info("{} is after now ({} in UTC) timestamp, replacing it with {}",
            temperature.getTime(), currentUtcTime, currentUtcTime);
        temperature.setTime(currentUtcTime);
      }

      Point pointToSave = Point.measurement("temperature")
          .addField("value", temperature.getTemperature())
          .time(temperature.getTime().getEpochSecond(), WritePrecision.S);
      points.add(pointToSave);
    }

    writeApi.writePoints(points);
    writeApi.flush();
  }

}