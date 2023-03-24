package szathmary.peter.bakalarka.repository.abstractInfluxDbRepository;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.BadRequestException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.NoRepositoryBean;
import szathmary.peter.bakalarka.exception.NoDataFound;

@Slf4j
@NoRepositoryBean
public abstract class BaseInfluxDbRepository<T> {

  protected final InfluxDBClient influxDBClient;

  protected final WriteApi writeApi;

  protected final String QUANTITY_NAME;

  protected final String BUCKET_NAME;

  protected final String ORGANIZATION;

  @Value("${influxdb.logging.enabled}")
  private boolean influxLoggingEnabled;

  public BaseInfluxDbRepository(InfluxDBClient influxDBClient, String quantityName,
      String bucketName, String organization) {
    this.influxDBClient = influxDBClient;
    this.QUANTITY_NAME = quantityName;
    BUCKET_NAME = bucketName;
    ORGANIZATION = organization;
    this.writeApi = this.influxDBClient.makeWriteApi();
  }

  @PostConstruct
  public void init() {
    if (this.influxLoggingEnabled) {
      this.influxDBClient.setLogLevel(LogLevel.BASIC);
    }
  }

  public List<T> findAll() {
    String query = String.format("""
        from(bucket: "%s")
          |> range(start: 0)
          |> filter(fn: (r) => r._measurement == "%s")
          |> sort(columns:["_time"])""", BUCKET_NAME, QUANTITY_NAME);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, this.getEntityClass());
  }

  public List<T> findAllBetweenDates(Instant startDate, Instant endDate) {
    String query = String.format("""
        from(bucket: "%1$s")
          |> range(start: %2$s, stop: %3$s)
          |> filter(fn: (r) => r._measurement == "%4$s")
          |> sort(columns:["_time"])""", BUCKET_NAME, startDate, endDate, QUANTITY_NAME);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, this.getEntityClass());
  }

  public List<T> getValueSince(Instant since) {
    String query = String.format("""
        from(bucket: "%1$s")
          |> range(start: %2$s)
          |> filter(fn: (r) => r._measurement == "%3$s")
          |> sort(columns:["_time"])
        """, BUCKET_NAME, since, QUANTITY_NAME);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, this.getEntityClass());
  }

  public T getLast() throws NoDataFound {
    String query = String.format("""
        from(bucket: "%1$s")
           |> range(start: 0)
           |> filter(fn: (r) => r._measurement == "%2$s")
           |> sort(columns:["_time"])
           |> last()
         """, BUCKET_NAME, QUANTITY_NAME);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    List<T> result = queryApi.query(query, ORGANIZATION, this.getEntityClass());

    if (result.isEmpty()) {
      throw new NoDataFound("Multiple or none temperatures were returned in getLastTemperature()");
    }

    return result.get(0);
  }

  public List<List<T>> findGroupedMinMaxMean(Instant startDate, Instant endDate) {
    long timeRangeMillis = endDate.toEpochMilli() - startDate.toEpochMilli();
    long windowDurationMillis = timeRangeMillis / 400;
    String windowDuration = windowDurationMillis + "ms";

    String minQuery = "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r._measurement == \"%s\") |> aggregateWindow(every: %s, fn: min, createEmpty: false) |> sort(columns:[\"_time\"]) |> yield(name: \"min\")".formatted(
        BUCKET_NAME, startDate, endDate, QUANTITY_NAME, windowDuration);

    String maxQuery = "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r._measurement == \"%s\") |> aggregateWindow(every: %s, fn: max, createEmpty: false) |> sort(columns:[\"_time\"]) |> yield(name: \"max\")".formatted(
        BUCKET_NAME, startDate, endDate, QUANTITY_NAME, windowDuration);

    String meanQuery = "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r._measurement == \"%s\") |> aggregateWindow(every: %s, fn: mean, createEmpty: false) |> sort(columns:[\"_time\"]) |> yield(name: \"mean\")".formatted(
        BUCKET_NAME, startDate, endDate, QUANTITY_NAME, windowDuration);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    List<T> minTemperatureList;
    List<T> maxTemperatureList;
    List<T> meanTemperatureList;
    try {
      minTemperatureList = queryApi.query(minQuery, ORGANIZATION, this.getEntityClass());
      maxTemperatureList = queryApi.query(maxQuery, ORGANIZATION, this.getEntityClass());
      meanTemperatureList = queryApi.query(meanQuery, ORGANIZATION, this.getEntityClass());
    } catch (BadRequestException e) {
      log.error("No data found to aggregate in findGroupedMinMaxMean between {} and {}", startDate,
          endDate);
      return null;
    }

    List<List<T>> listOfLists = new ArrayList<>();
    listOfLists.add(minTemperatureList);
    listOfLists.add(maxTemperatureList);
    listOfLists.add(meanTemperatureList);

    return listOfLists;
  }

  public void save(T t) {
    // time cannot be in future
    Instant currentUtcTime = Instant.now();
    Point pointToSave = generatePointToSave(currentUtcTime, t);

    this.writeApi.writePoint(BUCKET_NAME, ORGANIZATION, pointToSave);
    this.writeApi.flush();
  }

  public void saveAll(List<T> tList) {
    List<Point> points = new ArrayList<>();
    Instant currentUtcTime = Instant.now();

    for (T t : tList) {

      Point pointToSave = generatePointToSave(currentUtcTime, t);
      points.add(pointToSave);
    }

    writeApi.writePoints(points);
    writeApi.flush();
  }

  protected abstract Class<T> getEntityClass();

  protected abstract Point generatePointToSave(Instant currentUtcTime, T t);
}
