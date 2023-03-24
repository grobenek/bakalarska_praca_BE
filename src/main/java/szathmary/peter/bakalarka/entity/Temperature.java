package szathmary.peter.bakalarka.entity;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Measurement(name = "test")
public class Temperature {

  @Column(name = "_value")
  private double temperature;

  @Column(name = "_time", timestamp = true)
  private Instant time;
}
