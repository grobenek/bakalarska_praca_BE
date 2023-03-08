package szathmary.peter.bakalarka.dto;

import java.time.Instant;
import lombok.Data;

@Data
public class TemperatureDto {

  private Instant time;
  private double temperature;
}
