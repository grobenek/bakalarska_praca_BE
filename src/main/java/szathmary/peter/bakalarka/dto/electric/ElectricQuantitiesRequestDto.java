package szathmary.peter.bakalarka.dto.electric;

import java.util.List;
import lombok.Data;
import szathmary.peter.bakalarka.constant.ElectricPhase;
import szathmary.peter.bakalarka.constant.ElectricQuantities;

@Data
public class ElectricQuantitiesRequestDto {
  private List<ElectricQuantities> electricQuantities;
  private List<ElectricPhase> currentPhaseFilters;
  private List<ElectricPhase> voltagePhaseFilters;
}