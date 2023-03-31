package szathmary.peter.bakalarka.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import szathmary.peter.bakalarka.constant.ElectricPhase;
import szathmary.peter.bakalarka.constant.ElectricQuantities;
import szathmary.peter.bakalarka.dto.electric.ElectricQuantitiesDto;
import szathmary.peter.bakalarka.dto.electric.ElectricQuantitiesMinMaxMeanResponseDto;
import szathmary.peter.bakalarka.entity.Current;
import szathmary.peter.bakalarka.entity.GridFrequency;
import szathmary.peter.bakalarka.entity.Voltage;
import szathmary.peter.bakalarka.exception.InvalidElectricQuantityException;
import szathmary.peter.bakalarka.exception.NoDataFound;
import szathmary.peter.bakalarka.service.CurrentElectricService;
import szathmary.peter.bakalarka.service.GridFrequencyElectricService;
import szathmary.peter.bakalarka.service.VoltageElectricService;

@Slf4j
@RestController
@RequestMapping("api/electric-quantities")
public class ElectricQuantityController {

  private final CurrentElectricService currentService;
  private final GridFrequencyElectricService gridFrequencyService;
  private final VoltageElectricService voltageService;

  public ElectricQuantityController(CurrentElectricService currentService,
      GridFrequencyElectricService gridFrequencyService, VoltageElectricService voltageService) {
    this.currentService = currentService;
    this.gridFrequencyService = gridFrequencyService;
    this.voltageService = voltageService;
  }

  @GetMapping("/selected")
  public ResponseEntity<ElectricQuantitiesDto> findSelected(
      @RequestParam List<ElectricQuantities> electricQuantities,
      @RequestParam(required = false) List<ElectricPhase> currentPhaseFilters,
      @RequestParam(required = false) List<ElectricPhase> voltagePhaseFilters)
      throws InvalidElectricQuantityException {
    log.info("All electric quantities: {} with Current phases: {} and Voltage phases: {} requested",
        electricQuantities.toString(), currentPhaseFilters.toString(),
        voltagePhaseFilters.toString());

    List<Current> currents = new ArrayList<>();
    List<GridFrequency> gridFrequencies = new ArrayList<>();
    List<Voltage> voltages = new ArrayList<>();

    processElectricQuantities(electricQuantities, currentPhaseFilters, voltagePhaseFilters,
        (quantity, phaseFilters) -> currents.addAll(currentService.findAll(phaseFilters)),
        quantity -> gridFrequencies.addAll(gridFrequencyService.findAll(null)),
        (quantity, phaseFilters) -> voltages.addAll(voltageService.findAll(phaseFilters)));

    return ResponseEntity.ok().body(new ElectricQuantitiesDto(currents, gridFrequencies, voltages));
  }

  @GetMapping(path = "/between/{startDate}/{endDate}")
  public ResponseEntity<ElectricQuantitiesDto> getAllElectricQuantitiesBetweenDates(
      @PathVariable @NotNull Instant startDate, @PathVariable @NotNull Instant endDate,
      @RequestParam List<ElectricQuantities> electricQuantities,
      @RequestParam(required = false) List<ElectricPhase> currentPhaseFilters,
      @RequestParam(required = false) List<ElectricPhase> voltagePhaseFilters)
      throws InvalidElectricQuantityException {

    log.info(
        "All Electric quantities: {} with Current phases: {} and Voltage phases: {} requested between dates {} and {}",
        electricQuantities, currentPhaseFilters.toString(), voltagePhaseFilters.toString(),
        startDate, endDate);

    List<Current> currents = new ArrayList<>();
    List<GridFrequency> gridFrequencies = new ArrayList<>();
    List<Voltage> voltages = new ArrayList<>();

    processElectricQuantities(electricQuantities, currentPhaseFilters, voltagePhaseFilters,
        (quantity, phaseFilters) -> currents.addAll(
            currentService.findAllBetweenDate(startDate, endDate, phaseFilters)),
        quantity -> gridFrequencies.addAll(
            gridFrequencyService.findAllBetweenDate(startDate, endDate, null)),
        (quantity, phaseFilters) -> voltages.addAll(
            voltageService.findAllBetweenDate(startDate, endDate, phaseFilters)));

    return ResponseEntity.ok().body(new ElectricQuantitiesDto(currents, gridFrequencies, voltages));
  }

  @GetMapping(path = "since/{timestamp}")
  public ResponseEntity<ElectricQuantitiesMinMaxMeanResponseDto> getElectricQuantitiesSince(
      @PathVariable Instant timestamp, @RequestParam List<ElectricQuantities> electricQuantities,
      @RequestParam(required = false) List<ElectricPhase> currentPhaseFilters,
      @RequestParam(required = false) List<ElectricPhase> voltagePhaseFilters)
      throws InvalidElectricQuantityException {
    log.info("All Electric quantities : {} requested since {}", electricQuantities, timestamp);

    List<List<Current>> currents = new ArrayList<>();
    List<List<GridFrequency>> gridFrequencies = new ArrayList<>();
    List<List<Voltage>> voltages = new ArrayList<>();

    processElectricQuantities(electricQuantities, currentPhaseFilters, voltagePhaseFilters,
        (quantity, phaseFilters) -> currents.addAll(
            currentService.getValueSince(timestamp, phaseFilters)),
        quantity -> gridFrequencies.addAll(gridFrequencyService.getValueSince(timestamp, null)),
        (quantity, phaseFilters) -> voltages.addAll(
            voltageService.getValueSince(timestamp, phaseFilters)));

    return createElectricQuantitiesMinMaxMeanResponseDto(currents, gridFrequencies, voltages);
  }

  @GetMapping("/last")
  public ResponseEntity<ElectricQuantitiesDto> getLastElectricQuantities(
      @RequestParam List<ElectricQuantities> electricQuantities,
      @RequestParam(required = false) List<ElectricPhase> currentPhaseFilters,
      @RequestParam(required = false) List<ElectricPhase> voltagePhaseFilters)
      throws InvalidElectricQuantityException {

    log.info("Last value of {} with phases {} and {} requested", electricQuantities,
        currentPhaseFilters, voltagePhaseFilters);

    ArrayList<Current> currents = new ArrayList<>();
    ArrayList<GridFrequency> gridFrequencies = new ArrayList<>();
    ArrayList<Voltage> voltages = new ArrayList<>();

    processElectricQuantities(electricQuantities, currentPhaseFilters, voltagePhaseFilters,
        (quantity, phaseFilters) -> {
          try {
            currents.add(currentService.getLastValue(phaseFilters));
          } catch (NoDataFound e) {
            throw new RuntimeException(e);
          }
        }, quantity -> {
          try {
            gridFrequencies.add(gridFrequencyService.getLastValue(null));
          } catch (NoDataFound e) {
            throw new RuntimeException(e);
          }
        }, (quantity, phaseFilters) -> {
          try {
            voltages.add(voltageService.getLastValue(phaseFilters));
          } catch (NoDataFound e) {
            throw new RuntimeException(e);
          }
        });

    ElectricQuantitiesDto electricQuantitiesDto = ElectricQuantitiesDto.builder().currents(currents)
        .gridFrequencies(gridFrequencies).voltages(voltages).build();

    return ResponseEntity.ok().body(electricQuantitiesDto);
  }

  @GetMapping("/grouped/between/{startDate}/{endDate}")
  public ResponseEntity<ElectricQuantitiesMinMaxMeanResponseDto> getGroupedElectricQuantitiesBetweenDate(
      @PathVariable @NotNull Instant startDate, @PathVariable @NotNull Instant endDate,
      @RequestParam List<ElectricQuantities> electricQuantities,
      @RequestParam(required = false) List<ElectricPhase> currentPhaseFilters,
      @RequestParam(required = false) List<ElectricPhase> voltagePhaseFilters)
      throws InvalidElectricQuantityException {

    log.info(
        "All Electric quantities : {} with currentPhases: {} and voltagePhases: {} requested between {} and {}",
        electricQuantities, currentPhaseFilters, voltagePhaseFilters,
        startDate, endDate);

    List<List<Current>> currents = new ArrayList<>();
    List<List<GridFrequency>> gridFrequencies = new ArrayList<>();
    List<List<Voltage>> voltages = new ArrayList<>();

    processElectricQuantities(electricQuantities, currentPhaseFilters, voltagePhaseFilters,
        (quantity, phaseFilters) -> currents.addAll(
            currentService.getGroupedMinMaxMean(startDate, endDate, phaseFilters)),
        quantity -> gridFrequencies.addAll(
            gridFrequencyService.getGroupedMinMaxMean(startDate, endDate, null)),
        (quantity, phaseFilters) -> voltages.addAll(
            voltageService.getGroupedMinMaxMean(startDate, endDate, phaseFilters)));

    return createElectricQuantitiesMinMaxMeanResponseDto(currents, gridFrequencies, voltages);
  }

  @GetMapping(path = "/{startDate}")
  public ResponseEntity<ElectricQuantitiesMinMaxMeanResponseDto> getAllElectricQuantitiesFromDate(
      @PathVariable @NotNull Instant startDate,
      @RequestParam List<ElectricQuantities> electricQuantities,
      @RequestParam(required = false) List<ElectricPhase> currentPhaseFilters,
      @RequestParam(required = false) List<ElectricPhase> voltagePhaseFilters)
      throws InvalidElectricQuantityException {

    log.info("All Electric quantities : {} requested from date {}", electricQuantities, startDate);

    List<List<Current>> currents = new ArrayList<>();
    List<List<GridFrequency>> gridFrequencies = new ArrayList<>();
    List<List<Voltage>> voltages = new ArrayList<>();

    processElectricQuantities(electricQuantities, currentPhaseFilters, voltagePhaseFilters,
        (quantity, phaseFilters) -> currents.addAll(
            currentService.getAllValuesFromDate(startDate, phaseFilters)),
        quantity -> gridFrequencies.addAll(
            gridFrequencyService.getAllValuesFromDate(startDate, null)),
        (quantity, phaseFilters) -> voltages.addAll(
            voltageService.getAllValuesFromDate(startDate, phaseFilters)));

    return createElectricQuantitiesMinMaxMeanResponseDto(currents, gridFrequencies, voltages);
  }

  @PostMapping
  public ResponseEntity<Void> addElectricQuantities(
      @RequestBody ElectricQuantitiesDto electricQuantitiesRequestDto) {
    log.info("Adding Current: {}, Grid frequency: {}, Voltage: {} in ElectricQuantityController",
        electricQuantitiesRequestDto.getCurrents().size(),
        electricQuantitiesRequestDto.getGridFrequencies().size(),
        electricQuantitiesRequestDto.getVoltages().size());

    if (!electricQuantitiesRequestDto.getCurrents().isEmpty()) {
      log.info("Currents: {}", electricQuantitiesRequestDto.getCurrents());
      this.currentService.saveValues(electricQuantitiesRequestDto.getCurrents());
    }
    if (!electricQuantitiesRequestDto.getGridFrequencies().isEmpty()) {
      log.info("Grid frequencies: {}", electricQuantitiesRequestDto.getGridFrequencies());
      this.gridFrequencyService.saveValues(electricQuantitiesRequestDto.getGridFrequencies());
    }
    if (!electricQuantitiesRequestDto.getVoltages().isEmpty()) {
      log.info("Voltages: {}", electricQuantitiesRequestDto.getVoltages());
      this.voltageService.saveValues(electricQuantitiesRequestDto.getVoltages());
    }

    return ResponseEntity.ok().build();
  }

  @NotNull
  private ResponseEntity<ElectricQuantitiesMinMaxMeanResponseDto> createElectricQuantitiesMinMaxMeanResponseDto(
      List<List<Current>> currents, List<List<GridFrequency>> gridFrequencies,
      List<List<Voltage>> voltages) {
    ElectricQuantitiesMinMaxMeanResponseDto electricQuantitiesMinMaxMeanResponseDto = new ElectricQuantitiesMinMaxMeanResponseDto();

    if (!currents.isEmpty()) {
      electricQuantitiesMinMaxMeanResponseDto.setMinCurrents(currents.get(0));
      electricQuantitiesMinMaxMeanResponseDto.setMeanCurrents(currents.get(1));
      electricQuantitiesMinMaxMeanResponseDto.setMaxCurrents(currents.get(2));
    }

    if (!voltages.isEmpty()) {
      electricQuantitiesMinMaxMeanResponseDto.setMinVoltages(voltages.get(0));
      electricQuantitiesMinMaxMeanResponseDto.setMeanVoltages(voltages.get(1));
      electricQuantitiesMinMaxMeanResponseDto.setMaxVoltages(voltages.get(2));
    }

    if (!gridFrequencies.isEmpty()) {
      electricQuantitiesMinMaxMeanResponseDto.setMinGridFrequencies(gridFrequencies.get(0));
      electricQuantitiesMinMaxMeanResponseDto.setMeanGridFrequencies(gridFrequencies.get(1));
      electricQuantitiesMinMaxMeanResponseDto.setMaxGridFrequencies(gridFrequencies.get(2));
    }

    return ResponseEntity.ok().body(electricQuantitiesMinMaxMeanResponseDto);
  }

  private void processElectricQuantities(List<ElectricQuantities> electricQuantities,
      List<ElectricPhase> currentPhaseFilters, List<ElectricPhase> voltagePhaseFilters,
      BiConsumer<ElectricQuantities, List<ElectricPhase>> currentConsumer,
      Consumer<ElectricQuantities> gridFrequencyConsumer,
      BiConsumer<ElectricQuantities, List<ElectricPhase>> voltageConsumer)
      throws InvalidElectricQuantityException {
    for (ElectricQuantities quantity : electricQuantities) {
      switch (quantity) {
        case CURRENT -> currentConsumer.accept(quantity, currentPhaseFilters);
        case GRID_FREQUENCY -> gridFrequencyConsumer.accept(quantity);
        case VOLTAGE -> voltageConsumer.accept(quantity, voltagePhaseFilters);
        default -> throw new InvalidElectricQuantityException(
            "Invalid electric quantity type: " + quantity);
      }
    }
  }
}
