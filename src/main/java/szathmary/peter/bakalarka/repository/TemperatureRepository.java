package szathmary.peter.bakalarka.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import szathmary.peter.bakalarka.entity.Temperature;
import szathmary.peter.bakalarka.exception.NoDataFound;

@Repository
public class TemperatureRepository {

  private final InfluxDBClient influxDBClient;
  @Value("${influxdb.bucket}")
  private final String BUCKET_NAME = "temperature";
  @Value("${influxdb.org}")
  private final String ORGANIZATION = "Bakalarka";

  public TemperatureRepository(InfluxDBClient influxDBClient) {
    this.influxDBClient = influxDBClient;
  }

  public List<Temperature> findAll() {
    String query = String.format("""
        from(bucket: "%s")
          |> range(start: 0)
          |> filter(fn: (r) => r._measurement == "%s")""", BUCKET_NAME, BUCKET_NAME);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, Temperature.class);
  }

  public List<Temperature> findAllBetweenDate(Instant startDate, Instant endDate) {
    String query = String.format("""
            from(bucket: "%1$s")
              |> range(start: %2$s, stop: %3$s)
              |> filter(fn: (r) => r._measurement == "temperature")""", BUCKET_NAME, startDate,
        endDate);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, Temperature.class);
  }

  public List<Temperature> getTemperaturesSince(Instant since) {
    String query = String.format("""
        from(bucket: "%1$s")
          |> range(start: %2$s)
          |> filter(fn: (r) => r._measurement == "temperature")
        """, BUCKET_NAME, since);

    QueryApi queryApi = this.influxDBClient.getQueryApi();

    return queryApi.query(query, ORGANIZATION, Temperature.class);
  }

  public Temperature getLastTemperature() throws NoDataFound {
    String query = String.format("""
        from(bucket: "%1$s")
           |> range(start: 0)
           |> filter(fn: (r) => r._measurement == "temperature")
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

    String minQuery = "from(bucket: \"" + BUCKET_NAME + "\")"
        + " |> range(start: " + startDate + ", stop: " + endDate + ")"
        + " |> filter(fn: (r) => r._measurement == \"temperature\")"
        + " |> aggregateWindow(every: " + windowDuration + ", fn: min, createEmpty: true)"
        + " |> yield(name: \"min\")";

    String maxQuery = "from(bucket: \"" + BUCKET_NAME + "\")"
        + " |> range(start: " + startDate + ", stop: " + endDate + ")"
        + " |> filter(fn: (r) => r._measurement == \"temperature\")"
        + " |> aggregateWindow(every: " + windowDuration + ", fn: min, createEmpty: true)"
        + " |> yield(name: \"max\")";

    String meanQuery = "from(bucket: \"" + BUCKET_NAME + "\")"
        + " |> range(start: " + startDate + ", stop: " + endDate + ")"
        + " |> filter(fn: (r) => r._measurement == \"temperature\")"
        + " |> aggregateWindow(every: " + windowDuration + ", fn: min, createEmpty: true)"
        + " |> yield(name: \"mean\")";



    QueryApi queryApi = this.influxDBClient.getQueryApi();
    List<Temperature> minTemperatureList = queryApi.query(minQuery, ORGANIZATION, Temperature.class);
    List<Temperature> maxTemperatureList = queryApi.query(maxQuery, ORGANIZATION, Temperature.class);
    List<Temperature> meanTemperatureList = queryApi.query(meanQuery, ORGANIZATION, Temperature.class);

    System.out.println(minTemperatureList.size());
    System.out.println(maxTemperatureList.size());
    System.out.println(meanTemperatureList.size());

    List<List<Temperature>> listOfLists = new ArrayList<>();
    listOfLists.add(minTemperatureList);
    listOfLists.add(maxTemperatureList);
    listOfLists.add(meanTemperatureList);

    return listOfLists;
  }
}