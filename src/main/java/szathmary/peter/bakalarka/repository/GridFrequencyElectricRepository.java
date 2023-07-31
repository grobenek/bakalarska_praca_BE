package szathmary.peter.bakalarka.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import szathmary.peter.bakalarka.entity.GridFrequency;
import szathmary.peter.bakalarka.repository.abstractInfluxDbRepository.BaseInfluxDbElectricRepository;

import java.time.Instant;

@Slf4j
@Repository
public class GridFrequencyElectricRepository extends BaseInfluxDbElectricRepository<GridFrequency> {

  public GridFrequencyElectricRepository(
      InfluxDBClient influxDBClient,
      @Value("${influxdb.bucket.electric}") String bucketName,
      @Value("${influxdb.org}") String organization) {
    super(influxDBClient, "gridFrequency", bucketName, organization);
  }

  @Override
  protected Class<GridFrequency> getEntityClass() {
    return GridFrequency.class;
  }

  @Override
  protected Point generatePointToSave(Instant currentUtcTime, GridFrequency gridFrequency) {
    if (gridFrequency.getTime().isAfter(currentUtcTime)) {
      log.info(
          "{} is after now ({} in UTC) timestamp, replacing it with {}",
          gridFrequency.getTime(),
          currentUtcTime,
          currentUtcTime);
      gridFrequency.setTime(currentUtcTime);
    }

    return Point.measurement(QUANTITY_NAME)
        .addField("value", gridFrequency.getFrequency())
        .time(gridFrequency.getTime().getEpochSecond(), WritePrecision.S);
  }
}
