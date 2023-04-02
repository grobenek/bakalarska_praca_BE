package szathmary.peter.bakalarka.dto.electric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import szathmary.peter.bakalarka.entity.Current;
import szathmary.peter.bakalarka.entity.GridFrequency;
import szathmary.peter.bakalarka.entity.Voltage;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectricQuantitiesMinMaxMeanResponseDto {
  private List<Current> minCurrents;
  private List<Current> meanCurrents;
  private List<Current> maxCurrents;
  private List<GridFrequency> minGridFrequencies;
  private List<GridFrequency> meanGridFrequencies;
  private List<GridFrequency> maxGridFrequencies;
  private List<Voltage> minVoltages;
  private List<Voltage> meanVoltages;
  private List<Voltage> maxVoltages;
}
