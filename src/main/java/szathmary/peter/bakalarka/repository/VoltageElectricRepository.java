package szathmary.peter.bakalarka.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import szathmary.peter.bakalarka.entity.Voltage;
import szathmary.peter.bakalarka.repository.abstractInfluxDbRepository.BaseInfluxDbElectricRepository;

import java.time.Instant;

@Slf4j
@Repository
public class VoltageElectricRepository extends BaseInfluxDbElectricRepository<Voltage> {

  @Value("${influxdb.org}")
  private String ORGANIZATION;

  @Autowired
  public VoltageElectricRepository(
      InfluxDBClient influxDBClient,
      @Value("${influxdb.bucket.electric}") String bucketName,
      @Value("${influxdb.org}") String organization) {
    super(influxDBClient, "voltage", bucketName, organization);
  }

  @Override
  protected Class<Voltage> getEntityClass() {
    return Voltage.class;
  }

  @Override
  protected Point generatePointToSave(Instant currentUtcTime, Voltage voltage) {
    if (voltage.getTime().isAfter(currentUtcTime)) {
      log.info(
          "{} is after now ({} in UTC) timestamp, replacing it with {}",
          voltage.getTime(),
          currentUtcTime,
          currentUtcTime);
      voltage.setTime(currentUtcTime);
    }

    return Point.measurement(QUANTITY_NAME)
        .addTag("phase", voltage.getPhase().toString())
        .addField("value", voltage.getVoltage())
        .time(voltage.getTime().getEpochSecond(), WritePrecision.S);
  }
}
