package szathmary.peter.bakalarka.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import szathmary.peter.bakalarka.entity.Temperature;

@Data
@Builder
public class TemperatureMinMaxMeanDto {
  private List<TemperatureDto> minTemperatures;
  private List<TemperatureDto> maxTemperatures;
  private List<TemperatureDto> meanTemperatures;
}
