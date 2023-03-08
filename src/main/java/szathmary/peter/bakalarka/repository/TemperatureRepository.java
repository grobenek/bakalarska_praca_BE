package szathmary.peter.bakalarka.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import retrofit2.http.Query;
import szathmary.peter.bakalarka.entity.Temperature;

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
          |> filter(fn: (r) => r._measurement == "temperature")""", BUCKET_NAME, startDate, endDate);

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
}
