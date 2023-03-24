package szathmary.peter.bakalarka.repository;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import java.time.Instant;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.NoRepositoryBean;
import szathmary.peter.bakalarka.exception.NoDataFound;

@NoRepositoryBean
public abstract class BaseInfluxDbRepository<T> {

  protected final InfluxDBClient influxDBClient;

  protected final WriteApi writeApi;

  @Value("${influxdb.logging.enabled}")
  private boolean influxLoggingEnabled;

  public BaseInfluxDbRepository(InfluxDBClient influxDBClient) {
    this.influxDBClient = influxDBClient;
    this.writeApi = this.influxDBClient.makeWriteApi();
  }

  @PostConstruct
  public void init() {
    if (this.influxLoggingEnabled) {
      this.influxDBClient.setLogLevel(LogLevel.BASIC);
    }
  }

  public abstract List<T> findAll();

  public abstract List<T> findAllBetweenDates(Instant startDate, Instant endDate);

  public abstract List<T> getValueSince(Instant since);

  public abstract T getLast() throws NoDataFound;

  public abstract List<List<T>> findGroupedMinMaxMean(Instant startDate, Instant endDate);

  public abstract void save(T t);

  public abstract void saveAll(List<T> tList);
}
