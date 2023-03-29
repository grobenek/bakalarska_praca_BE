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
import szathmary.peter.bakalarka.constant.ElectricPhase;
import szathmary.peter.bakalarka.exception.NoDataFound;

@Slf4j
@NoRepositoryBean
public abstract class BaseInfluxDbElectricRepository<T> {

  protected final InfluxDBClient influxDBClient;

  protected final WriteApi writeApi;

  protected final String QUANTITY_NAME;

  private final String BUCKET_NAME;

  private final String ORGANIZATION;

  @Value("${influxdb.logging.enabled}")
  private boolean influxLoggingEnabled;

  public BaseInfluxDbElectricRepository(InfluxDBClient influxDBClient, String quantityName,
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

  public List<T> findAll(List<ElectricPhase> phases) {
    String query = String.format("""
        from(bucket: "%s")
          |> range(start: 0)
          |> filter(fn: (r) => r._measurement == "%s"%s)
          |> sort(columns:["_time"])""", BUCKET_NAME, QUANTITY_NAME, generatePhaseFilter(phases));

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, this.getEntityClass());
  }

  public List<T> findAllBetweenDates(Instant startDate, Instant endDate,
      List<ElectricPhase> phases) {
    String query = String.format("""
            from(bucket: "%1$s")
              |> range(start: %2$s, stop: %3$s)
              |> filter(fn: (r) => r._measurement == "%4$s"%5$s)
              |> sort(columns:["_time"])""", BUCKET_NAME, startDate, endDate, QUANTITY_NAME,
        generatePhaseFilter(phases));

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, this.getEntityClass());
  }

  public List<T> getValueSince(Instant since, List<ElectricPhase> phases) {
    String query = String.format("""
            from(bucket: "%1$s")
              |> range(start: %2$s)
              |> filter(fn: (r) => r._measurement == "%3$s"%4$s)
              |> sort(columns:["_time"])""", BUCKET_NAME, since, QUANTITY_NAME,
        generatePhaseFilter(phases));

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, this.getEntityClass());
  }

  public T getLastValue(List<ElectricPhase> phases) throws NoDataFound {
    String query = String.format("""
        from(bucket: "%1$s")
           |> range(start: 0)
           |> filter(fn: (r) => r._measurement == "%2$s"%3$s)
           |> sort(columns:["_time"])
           |> last()
         """, BUCKET_NAME, QUANTITY_NAME, generatePhaseFilter(phases));

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    List<T> result = queryApi.query(query, ORGANIZATION, this.getEntityClass());

    if (result.isEmpty()) {
      throw new NoDataFound("Multiple or none temperatures were returned in getLastTemperature()");
    }

    return result.get(0);
  }

  public List<List<T>> getGroupedMinMaxMean(Instant startDate, Instant endDate, List<ElectricPhase> phases) {
    long timeRangeMillis = endDate.toEpochMilli() - startDate.toEpochMilli();
    long windowDurationMillis = timeRangeMillis / 400;
    String windowDuration = windowDurationMillis + "ms";

    String subquery = String.format("""
    from(bucket: "%s")
      |> range(start: %s, stop: %s)
      |> filter(fn: (r) => r._measurement == "%s"%s)
      |> aggregateWindow(every: %s, fn: %s, createEmpty: false)
      |> sort(columns:["_time"])""", BUCKET_NAME, startDate, endDate, QUANTITY_NAME,
        generatePhaseFilter(phases), windowDuration, "%s");

    String minQuery = String.format(subquery, "min") + " |> yield(name: \"min\")";
    String maxQuery = String.format(subquery, "max") + " |> yield(name: \"max\")";
    String meanQuery = String.format(subquery, "mean") + " |> yield(name: \"mean\")";

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    List<T> minTemperatureList;
    List<T> maxTemperatureList;
    List<T> meanTemperatureList;
    try {
      minTemperatureList = queryApi.query(minQuery, ORGANIZATION, this.getEntityClass());
      maxTemperatureList = queryApi.query(maxQuery, ORGANIZATION, this.getEntityClass());
      meanTemperatureList = queryApi.query(meanQuery, ORGANIZATION, this.getEntityClass());
    } catch (BadRequestException e) {
      log.error("No data found to aggregate in findGroupedMinMaxMean between {} and {}\n Error message: {}", startDate,
          endDate, e.getMessage());
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

    writeApi.writePoints(this.BUCKET_NAME, this.ORGANIZATION, points);
    writeApi.flush();
  }

  protected abstract Class<T> getEntityClass();

  protected abstract Point generatePointToSave(Instant currentUtcTime, T t);

  /**
   * Generates tag statements for query based on provided phases list
   *
   * @param phases phases list to be generated
   * @return generated phases statements for queries
   */
  private String generatePhaseFilter(List<ElectricPhase> phases) {
    if (phases == null || phases.isEmpty()) {
      return "";
    }

    StringBuilder phaseFilter = new StringBuilder(" and (");
    for (int i = 0; i < phases.size(); i++) {
      phaseFilter.append(String.format("r.phase == \"%s\"", phases.get(i).toString()));
      if (i < phases.size() - 1) {
        phaseFilter.append(" or ");
      }
    }
    phaseFilter.append(")");

    return phaseFilter.toString();
  }
}