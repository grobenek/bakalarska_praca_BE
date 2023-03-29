package szathmary.peter.bakalarka.dto.electric;

import java.time.Instant;
import lombok.Data;
import szathmary.peter.bakalarka.constant.ElectricQuantities;

@Data
public class ElectricQuantityDto {

  private double value;
  private Instant timestamp;

  private ElectricQuantities electricQuantity;
}
