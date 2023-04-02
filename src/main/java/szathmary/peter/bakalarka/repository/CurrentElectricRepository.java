package szathmary.peter.bakalarka.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import szathmary.peter.bakalarka.entity.Current;
import szathmary.peter.bakalarka.repository.abstractInfluxDbRepository.BaseInfluxDbElectricRepository;

import java.time.Instant;

@Slf4j
@Repository
public class CurrentElectricRepository extends BaseInfluxDbElectricRepository<Current> {

  public CurrentElectricRepository(InfluxDBClient influxDBClient,
      @Value("${influxdb.bucket.electric}") String bucketName,
      @Value("${influxdb.org}") String organization) {
    super(influxDBClient, "current", bucketName, organization);
  }

  @Override
  protected Point generatePointToSave(Instant currentUtcTime, Current current) {
    if (current.getTime().isAfter(currentUtcTime)) {
      log.info("{} is after now ({} in UTC) timestamp, replacing it with {}", current.getTime(),
          currentUtcTime, currentUtcTime);
      current.setTime(currentUtcTime);
    }

    return Point.measurement(QUANTITY_NAME).addTag("phase", current.getPhase().toString())
        .addField("value", current.getCurrent())
        .time(current.getTime().getEpochSecond(), WritePrecision.S);
  }

  @Override
  protected Class<Current> getEntityClass() {
    return Current.class;
  }
}
