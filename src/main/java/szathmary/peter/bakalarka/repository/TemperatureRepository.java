package szathmary.peter.bakalarka.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import szathmary.peter.bakalarka.entity.Temperature;
import szathmary.peter.bakalarka.repository.abstractInfluxDbRepository.BaseInfluxDbRepository;

@Slf4j
@Repository
public class TemperatureRepository extends BaseInfluxDbRepository<Temperature> {

  public TemperatureRepository(InfluxDBClient influxDBClient,
      @Value("${influxdb.bucket.temperature}") String bucketName,
      @Value("${influxdb.org}") String organization) {
    super(influxDBClient, "temperature", bucketName, organization);
  }

  @Override
  protected Class<Temperature> getEntityClass() {
    return Temperature.class;
  }

  @Override
  protected Point generatePointToSave(Instant currentUtcTime, Temperature temperature) {
    if (temperature.getTime().isAfter(currentUtcTime)) {
      log.info("{} is after now ({} in UTC) timestamp, replacing it with {}", temperature.getTime(),
          currentUtcTime, currentUtcTime);
      temperature.setTime(currentUtcTime);
    }

    return Point.measurement(QUANTITY_NAME).addField("value", temperature.getTemperature())
        .time(temperature.getTime().getEpochSecond(), WritePrecision.S);
  }

}