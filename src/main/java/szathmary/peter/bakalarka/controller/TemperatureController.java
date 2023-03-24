package szathmary.peter.bakalarka.controller;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import szathmary.peter.bakalarka.dto.TemperatureDto;
import szathmary.peter.bakalarka.dto.TemperatureMinMaxMeanDto;
import szathmary.peter.bakalarka.entity.Temperature;
import szathmary.peter.bakalarka.exception.NoDataFound;
import szathmary.peter.bakalarka.service.TemperatureService;
import szathmary.peter.bakalarka.util.mapper.TemperatureMapper;

@Slf4j
@RestController
@RequestMapping("/api/temperature")
@CrossOrigin(origins = "*")
public class TemperatureController {

  private final TemperatureService temperatureService;
  private final DecimalFormat decimalFormat;

  @Autowired
  public TemperatureController(TemperatureService temperatureService) {
    this.temperatureService = temperatureService;
    this.decimalFormat = new DecimalFormat("#.##");
    this.decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
  }

  @GetMapping
  public ResponseEntity<List<TemperatureDto>> getAllTemperatures() {
    log.info("getAllTemperatures requestedin TemperatureController");

    List<Temperature> temperatureList = this.temperatureService.findAll();

    List<TemperatureDto> temperatureDtos = temperatureList.stream().map(TemperatureMapper::toDto)
        .toList();

    log.info("Returned {} temperatures in TemperatureController", temperatureDtos.size());

    return ResponseEntity.ok().body(temperatureDtos);
  }

  @GetMapping(path = "/between/{startDate}/{endDate}")
  public ResponseEntity<List<TemperatureDto>> getAllTemperaturesBetweenDates(
      @PathVariable @NotNull Instant startDate, @PathVariable @NotNull Instant endDate) {
    log.info("All temperatures between Date {} and {} requested in TemperatureController",
        startDate, endDate);

    List<Temperature> temperatureList = this.temperatureService.findAllBetweenDate(startDate,
        endDate);

    List<TemperatureDto> temperatureDtos = temperatureList.stream().map(TemperatureMapper::toDto)
        .toList();

    log.info("{} temperatures returned between {} and {} in TemperatureController",
        temperatureDtos.size(), startDate, endDate);

    return ResponseEntity.ok().body(temperatureDtos);
  }

  @GetMapping(path = "since/{timestamp}")
  public ResponseEntity<TemperatureMinMaxMeanDto> getTemperaturesSince(
      @PathVariable @NotNull Instant timestamp) throws NoDataFound {
    log.info("All tempereatures since {} requested in TemperatureController", timestamp);

    List<List<Temperature>> temperatures = this.temperatureService.getTemperaturesSince(timestamp);

    return this.mapToMinMaxMeanTemperatureDto(temperatures);
  }

  @PostMapping
  public ResponseEntity<Void> addTemperatures(@RequestBody List<TemperatureDto> temperatureDtos) {
    log.info("Adding {} temperatures in TemperatureController", temperatureDtos.size());

    List<Temperature> temperatures = temperatureDtos.stream().map(TemperatureMapper::toEntity)
        .collect(Collectors.toList());

    this.temperatureService.saveTemperatures(temperatures);

    log.info("{} temperatures added in TemperatureController", temperatures.size());

    return ResponseEntity.ok().build();
  }

  @PostMapping("/single")
  public ResponseEntity<Void> addTemperature(@RequestBody TemperatureDto temperatureDto) {
    log.info("Adding one temperature {} in TemperatureController", temperatureDto.getTemperature());

    Temperature temperature = TemperatureMapper.toEntity(temperatureDto);
    temperatureService.saveTemperature(temperature);

    log.info("Temperature {} added in TemperatureController", temperatureDto.getTemperature());

    return ResponseEntity.ok().build();
  }


  @GetMapping(path = "/last")
  public ResponseEntity<TemperatureDto> getLastTemperature() throws NoDataFound {
    log.info("Last temperature requester in TemperatureController");

    Temperature lastTemperature = this.temperatureService.getLastTemperature();

    TemperatureDto temperatureDto = TemperatureMapper.toDto(lastTemperature);

    double temperature = temperatureDto.getTemperature();
    String formattedTemperature = this.decimalFormat.format(temperature);
    log.info("Last temperature with value {} returned in TemperatureController",
        formattedTemperature);

    return ResponseEntity.ok().body(temperatureDto);
  }

  @GetMapping(path = "/grouped/between/{startDate}/{endDate}")
  public ResponseEntity<TemperatureMinMaxMeanDto> getGroupedTemperatureBetweenDate(
      @PathVariable @NotNull Instant startDate, @PathVariable @NotNull Instant endDate)
      throws NoDataFound {
    log.info("Group temperatures between {} and {} requested in TemperatureController", startDate,
        endDate);

    List<List<Temperature>> groupedTemperatures = this.temperatureService.getGroupedMinMaxMean(
        startDate, endDate);

    return this.mapToMinMaxMeanTemperatureDto(groupedTemperatures);
  }

  @GetMapping(path = "/{starDate}")
  public ResponseEntity<TemperatureMinMaxMeanDto> getAllTemperaturesFromDate(
      @PathVariable @NotNull Instant starDate) throws NoDataFound {
    log.info("getAllTemperaturesFromDate requester in TemperatureControleer");

    List<List<Temperature>> temperatures = this.temperatureService.getAllTemperaturesFromDate(
        starDate);

    return this.mapToMinMaxMeanTemperatureDto(temperatures);
  }

  @NotNull
  private ResponseEntity<TemperatureMinMaxMeanDto> mapToMinMaxMeanTemperatureDto(
      List<List<Temperature>> temperatures) throws NoDataFound {

    if (temperatures == null) {
      throw new NoDataFound("No data found for to aggregate");
    }

    List<List<TemperatureDto>> groupedTemperaturesDto = temperatures.stream()
        .map(list -> list.stream().map(TemperatureMapper::toDto).toList()).toList();

    TemperatureMinMaxMeanDto temperatureMinMaxMeanDto = TemperatureMinMaxMeanDto.builder()
        .minTemperatures(groupedTemperaturesDto.get(0))
        .maxTemperatures(groupedTemperaturesDto.get(1))
        .meanTemperatures(groupedTemperaturesDto.get(2)).build();

    log.info("Min: {}, Max: {}, Mean: {} temperatures returned in TemperatureController",
        temperatureMinMaxMeanDto.getMinTemperatures().size(),
        temperatureMinMaxMeanDto.getMaxTemperatures().size(),
        temperatureMinMaxMeanDto.getMeanTemperatures().size());

    return ResponseEntity.ok().body(temperatureMinMaxMeanDto);
  }
}
