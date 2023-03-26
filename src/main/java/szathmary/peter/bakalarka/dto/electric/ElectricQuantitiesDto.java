package szathmary.peter.bakalarka.dto.electric;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import szathmary.peter.bakalarka.entity.Current;
import szathmary.peter.bakalarka.entity.GridFrequency;
import szathmary.peter.bakalarka.entity.Voltage;

@Data
@AllArgsConstructor
@Builder
public class ElectricQuantitiesDto {
  private List<Current> currents;
  private List<GridFrequency> gridFrequencies;
  private List<Voltage> voltages;
}
