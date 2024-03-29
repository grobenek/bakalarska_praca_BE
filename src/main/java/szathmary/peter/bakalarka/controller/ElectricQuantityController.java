package szathmary.peter.bakalarka.controller;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
@RestController
@RequestMapping("api/electric-quantities")
public class ElectricQuantityController {

  private final CurrentElectricService currentService;
  private final GridFrequencyElectricService gridFrequencyService;
  private final VoltageElectricService voltageService;

  public ElectricQuantityController(
      CurrentElectricService currentService,
      GridFrequencyElectricService gridFrequencyService,
      VoltageElectricService voltageService) {
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
    log.info(
        "All electric quantities: {} with Current phases: {} and Voltage phases: {} requested",
        electricQuantities.toString(),
        currentPhaseFilters.toString(),
        voltagePhaseFilters.toString());

    List<Current> currents = new ArrayList<>();
    List<GridFrequency> gridFrequencies = new ArrayList<>();
    List<Voltage> voltages = new ArrayList<>();

    processElectricQuantities(
        electricQuantities,
        currentPhaseFilters,
        voltagePhaseFilters,
        (quantity, phaseFilters) -> currents.addAll(currentService.findAll(phaseFilters)),
        quantity -> gridFrequencies.addAll(gridFrequencyService.findAll(null)),
        (quantity, phaseFilters) -> voltages.addAll(voltageService.findAll(phaseFilters)));

    return ResponseEntity.ok().body(new ElectricQuantitiesDto(currents, gridFrequencies, voltages));
  }

  @GetMapping("/last/{count}")
  public ResponseEntity<ElectricQuantitiesDto> getLastNElectricalQuantities(
      @PathVariable Integer count,
      @RequestParam List<ElectricQuantities> electricQuantities,
      @RequestParam(required = false) List<ElectricPhase> currentPhaseFilters,
      @RequestParam(required = false) List<ElectricPhase> voltagePhaseFilters)
      throws InvalidElectricQuantityException {
    log.info(
        "{} last {} with current filters {} and voltage filters {} requested",
        count,
        electricQuantities,
        currentPhaseFilters,
        voltagePhaseFilters);

    List<Current> currents = new ArrayList<>();
    List<GridFrequency> gridFrequencies = new ArrayList<>();
    List<Voltage> voltages = new ArrayList<>();

    processElectricQuantities(
        electricQuantities,
        currentPhaseFilters,
        voltagePhaseFilters,
        (quantity, phaseFilters) ->
            currents.addAll(currentService.getLastNData(count, phaseFilters)),
        quantity -> gridFrequencies.addAll(gridFrequencyService.getLastNData(count, null)),
        (quantity, phaseFilters) ->
            voltages.addAll(voltageService.getLastNData(count, phaseFilters)));

    log.info(
        "{} last currents, {} last voltages and {} last grid frequencies returned",
        currents.size(),
        voltages.size(),
        gridFrequencies.size());

    return ResponseEntity.ok().body(new ElectricQuantitiesDto(currents, gridFrequencies, voltages));
  }

  @GetMapping("/between/{startDate}/{endDate}")
  public ResponseEntity<ElectricQuantitiesDto> getAllElectricQuantitiesBetweenDates(
      @PathVariable @NotNull Instant startDate,
      @PathVariable @NotNull Instant endDate,
      @RequestParam List<ElectricQuantities> electricQuantities,
      @RequestParam(required = false) List<ElectricPhase> currentPhaseFilters,
      @RequestParam(required = false) List<ElectricPhase> voltagePhaseFilters)
      throws InvalidElectricQuantityException {

    log.info(
        "All Electric quantities: {} with Current phases: {} and Voltage phases: {} requested between dates {} and {}",
        electricQuantities,
        currentPhaseFilters.toString(),
        voltagePhaseFilters.toString(),
        startDate,
        endDate);

    List<Current> currents = new ArrayList<>();
    List<GridFrequency> gridFrequencies = new ArrayList<>();
    List<Voltage> voltages = new ArrayList<>();

    processElectricQuantities(
        electricQuantities,
        currentPhaseFilters,
        voltagePhaseFilters,
        (quantity, phaseFilters) ->
            currents.addAll(currentService.findAllBetweenDate(startDate, endDate, phaseFilters)),
        quantity ->
            gridFrequencies.addAll(
                gridFrequencyService.findAllBetweenDate(startDate, endDate, null)),
        (quantity, phaseFilters) ->
            voltages.addAll(voltageService.findAllBetweenDate(startDate, endDate, phaseFilters)));

    return ResponseEntity.ok().body(new ElectricQuantitiesDto(currents, gridFrequencies, voltages));
  }

  @GetMapping(path = "since/{timestamp}")
  public ResponseEntity<ElectricQuantitiesMinMaxMeanResponseDto> getElectricQuantitiesSince(
      @PathVariable Instant timestamp,
      @RequestParam List<ElectricQuantities> electricQuantities,
      @RequestParam(required = false) List<ElectricPhase> currentPhaseFilters,
      @RequestParam(required = false) List<ElectricPhase> voltagePhaseFilters)
      throws InvalidElectricQuantityException {
    log.info("All Electric quantities : {} requested since {}", electricQuantities, timestamp);

    List<List<Current>> currents = new ArrayList<>();
    List<List<GridFrequency>> gridFrequencies = new ArrayList<>();
    List<List<Voltage>> voltages = new ArrayList<>();

    processElectricQuantities(
        electricQuantities,
        currentPhaseFilters,
        voltagePhaseFilters,
        (quantity, phaseFilters) ->
            currents.addAll(currentService.getValueSince(timestamp, phaseFilters)),
        quantity -> gridFrequencies.addAll(gridFrequencyService.getValueSince(timestamp, null)),
        (quantity, phaseFilters) ->
            voltages.addAll(voltageService.getValueSince(timestamp, phaseFilters)));

    return createElectricQuantitiesMinMaxMeanResponseDto(currents, gridFrequencies, voltages);
  }

  @GetMapping("/last")
  public ResponseEntity<ElectricQuantitiesDto> getLastElectricQuantities(
      @RequestParam List<ElectricQuantities> electricQuantities,
      @RequestParam(required = false) List<ElectricPhase> currentPhaseFilters,
      @RequestParam(required = false) List<ElectricPhase> voltagePhaseFilters)
      throws InvalidElectricQuantityException {

    log.info(
        "Last value of {} with current phases {} and voltage phases {} requested",
        electricQuantities,
        currentPhaseFilters,
        voltagePhaseFilters);

    ArrayList<Current> currents = new ArrayList<>();
    ArrayList<GridFrequency> gridFrequencies = new ArrayList<>();
    ArrayList<Voltage> voltages = new ArrayList<>();

    processElectricQuantities(
        electricQuantities,
        currentPhaseFilters,
        voltagePhaseFilters,
        (quantity, phaseFilters) -> {
          try {
            currents.add(currentService.getLastValue(phaseFilters));
          } catch (NoDataFound e) {
            throw new RuntimeException(e);
          }
        },
        quantity -> {
          try {
            gridFrequencies.add(gridFrequencyService.getLastValue(null));
          } catch (NoDataFound e) {
            throw new RuntimeException(e);
          }
        },
        (quantity, phaseFilters) -> {
          try {
            voltages.add(voltageService.getLastValue(phaseFilters));
          } catch (NoDataFound e) {
            throw new RuntimeException(e);
          }
        });

    ElectricQuantitiesDto electricQuantitiesDto =
        ElectricQuantitiesDto.builder()
            .currents(currents)
            .gridFrequencies(gridFrequencies)
            .voltages(voltages)
            .build();

    log.info("Last value {} returned", electricQuantitiesDto.toString());

    return ResponseEntity.ok().body(electricQuantitiesDto);
  }

  @GetMapping("/grouped/between/{startDate}/{endDate}")
  public ResponseEntity<ElectricQuantitiesMinMaxMeanResponseDto>
      getGroupedElectricQuantitiesBetweenDate(
          @PathVariable @NotNull Instant startDate,
          @PathVariable @NotNull Instant endDate,
          @RequestParam List<ElectricQuantities> electricQuantities,
          @RequestParam(required = false) List<ElectricPhase> currentPhaseFilters,
          @RequestParam(required = false) List<ElectricPhase> voltagePhaseFilters)
          throws InvalidElectricQuantityException {

    log.info(
        "All Electric quantities : {} with currentPhases: {} and voltagePhases: {} requested between {} and {}",
        electricQuantities,
        currentPhaseFilters,
        voltagePhaseFilters,
        startDate,
        endDate);

    List<List<Current>> currents = new ArrayList<>();
    List<List<GridFrequency>> gridFrequencies = new ArrayList<>();
    List<List<Voltage>> voltages = new ArrayList<>();

    processElectricQuantities(
        electricQuantities,
        currentPhaseFilters,
        voltagePhaseFilters,
        (quantity, phaseFilters) ->
            currents.addAll(currentService.getGroupedMinMaxMean(startDate, endDate, phaseFilters)),
        quantity ->
            gridFrequencies.addAll(
                gridFrequencyService.getGroupedMinMaxMean(startDate, endDate, null)),
        (quantity, phaseFilters) ->
            voltages.addAll(voltageService.getGroupedMinMaxMean(startDate, endDate, phaseFilters)));

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

    processElectricQuantities(
        electricQuantities,
        currentPhaseFilters,
        voltagePhaseFilters,
        (quantity, phaseFilters) ->
            currents.addAll(currentService.getAllValuesFromDate(startDate, phaseFilters)),
        quantity ->
            gridFrequencies.addAll(gridFrequencyService.getAllValuesFromDate(startDate, null)),
        (quantity, phaseFilters) ->
            voltages.addAll(voltageService.getAllValuesFromDate(startDate, phaseFilters)));

    return createElectricQuantitiesMinMaxMeanResponseDto(currents, gridFrequencies, voltages);
  }

  @PostMapping
  public ResponseEntity<Void> addElectricQuantities(
      @RequestBody ElectricQuantitiesDto electricQuantitiesRequestDto) {
    log.info(
        "Adding Current: {}, Grid frequency: {}, Voltage: {} in ElectricQuantityController",
        electricQuantitiesRequestDto.getCurrents().size(),
        electricQuantitiesRequestDto.getGridFrequencies().size(),
        electricQuantitiesRequestDto.getVoltages().size());

    if (!electricQuantitiesRequestDto.getCurrents().isEmpty()) {
      this.currentService.saveValues(electricQuantitiesRequestDto.getCurrents());
    }
    if (!electricQuantitiesRequestDto.getGridFrequencies().isEmpty()) {
      this.gridFrequencyService.saveValues(electricQuantitiesRequestDto.getGridFrequencies());
    }
    if (!electricQuantitiesRequestDto.getVoltages().isEmpty()) {
      this.voltageService.saveValues(electricQuantitiesRequestDto.getVoltages());
    }

    return ResponseEntity.ok().build();
  }

  @NotNull
  private ResponseEntity<ElectricQuantitiesMinMaxMeanResponseDto>
      createElectricQuantitiesMinMaxMeanResponseDto(
          List<List<Current>> currents,
          List<List<GridFrequency>> gridFrequencies,
          List<List<Voltage>> voltages) {
    ElectricQuantitiesMinMaxMeanResponseDto electricQuantitiesMinMaxMeanResponseDto =
        new ElectricQuantitiesMinMaxMeanResponseDto();

    int currentsSize = 0;
    int voltagesSize = 0;
    int gridFrequenciesSize = 0;

    if (!currents.isEmpty()) {
      electricQuantitiesMinMaxMeanResponseDto.setMinCurrents(currents.get(0));
      electricQuantitiesMinMaxMeanResponseDto.setMeanCurrents(currents.get(1));
      electricQuantitiesMinMaxMeanResponseDto.setMaxCurrents(currents.get(2));

      currentsSize = electricQuantitiesMinMaxMeanResponseDto.getMinCurrents().size();
    }

    if (!voltages.isEmpty()) {
      electricQuantitiesMinMaxMeanResponseDto.setMinVoltages(voltages.get(0));
      electricQuantitiesMinMaxMeanResponseDto.setMeanVoltages(voltages.get(1));
      electricQuantitiesMinMaxMeanResponseDto.setMaxVoltages(voltages.get(2));

      voltagesSize = electricQuantitiesMinMaxMeanResponseDto.getMinVoltages().size();
    }

    if (!gridFrequencies.isEmpty()) {
      electricQuantitiesMinMaxMeanResponseDto.setMinGridFrequencies(gridFrequencies.get(0));
      electricQuantitiesMinMaxMeanResponseDto.setMeanGridFrequencies(gridFrequencies.get(1));
      electricQuantitiesMinMaxMeanResponseDto.setMaxGridFrequencies(gridFrequencies.get(2));

      gridFrequenciesSize = electricQuantitiesMinMaxMeanResponseDto.getMinGridFrequencies().size();
    }

    log.info(
        "Returned {} currents, {} voltages, {} grid frequencies",
        currentsSize,
        voltagesSize,
        gridFrequenciesSize);

    return ResponseEntity.ok().body(electricQuantitiesMinMaxMeanResponseDto);
  }

  private void processElectricQuantities(
      List<ElectricQuantities> electricQuantities,
      List<ElectricPhase> currentPhaseFilters,
      List<ElectricPhase> voltagePhaseFilters,
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
