package szathmary.peter.bakalarka.entity;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import java.time.Instant;
import lombok.Data;

@Data
@Measurement(name = "test")
public class Temperature {

  @Column(name = "_value")
  private double temperature;

  @Column(name = "device", tag = true)
  private String deviceName;

  @Column(name = "room", tag = true)
  private String room;

  @Column(name = "_time", timestamp = true)
  private Instant time;
}
