package szathmary.peter.bakalarka.entity;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import szathmary.peter.bakalarka.constant.ElectricPhase;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Measurement(name = "current")
public class Current {

  @Column(name = "_value")
  private double current;

  @Column(name = "_time", timestamp = true)
  private Instant time;

  @Column(name = "phase", tag = true)
  private ElectricPhase phase;
}
