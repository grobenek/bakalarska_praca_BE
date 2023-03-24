package szathmary.peter.bakalarka.controller;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import szathmary.peter.bakalarka.service.CurrentElectricService;
import szathmary.peter.bakalarka.service.GridFrequencyElectricService;
import szathmary.peter.bakalarka.service.VoltageElectricService;
@Slf4j
@RestController
@RequestMapping("/api/electric")
@CrossOrigin(origins = "*")
public class ElectricQuantityController {

  private final CurrentElectricService currentService;
  private final GridFrequencyElectricService gridFrequencyService;
  private final VoltageElectricService voltageService;

  private final DecimalFormat decimalFormat;

  public ElectricQuantityController(CurrentElectricService currentService,
      GridFrequencyElectricService gridFrequencyService, VoltageElectricService voltageService) {
    this.currentService = currentService;
    this.gridFrequencyService = gridFrequencyService;
    this.voltageService = voltageService;
    this.decimalFormat = new DecimalFormat("#.##");
    this.decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
  }


}
