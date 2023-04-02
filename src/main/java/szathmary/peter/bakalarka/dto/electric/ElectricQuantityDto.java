package szathmary.peter.bakalarka.dto.electric;

import lombok.Data;
import szathmary.peter.bakalarka.constant.ElectricQuantities;

import java.time.Instant;

@Data
public class ElectricQuantityDto {

  private double value;
  private Instant timestamp;

  private ElectricQuantities electricQuantity;
}
