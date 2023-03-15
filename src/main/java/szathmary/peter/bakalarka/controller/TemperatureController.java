package szathmary.peter.bakalarka.controller;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import szathmary.peter.bakalarka.dto.TemperatureDto;
import szathmary.peter.bakalarka.dto.TemperatureMinMaxMeanDto;
import szathmary.peter.bakalarka.entity.Temperature;
import szathmary.peter.bakalarka.exception.NoDataFound;
import szathmary.peter.bakalarka.service.TemperatureService;
import szathmary.peter.bakalarka.service.implementation.TemperatureServiceImpl;
import szathmary.peter.bakalarka.util.mapper.TemperatureMapper;

@Slf4j
@RestController
@RequestMapping("/api/temperature")
@CrossOrigin(origins = "*")
public class TemperatureController {

  private final TemperatureService temperatureService;
  private final DecimalFormat decimalFormat;

  @Autowired
  public TemperatureController(TemperatureServiceImpl temperatureService) {
    this.temperatureService = temperatureService;
    this.decimalFormat = new DecimalFormat("#.##");
    this.decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<TemperatureDto>> getAllTemperatures() {
    log.info("getAllTemperatures requestedin TemperatureController");

    List<Temperature> temperatureList = this.temperatureService.findAll();

    List<TemperatureDto> temperatureDtos = temperatureList.stream().map(TemperatureMapper::toDto)
        .toList();

    log.info("Returned {} temperatures in TemperatureController", temperatureDtos.size());

    return ResponseEntity.ok().body(temperatureDtos);
  }

  @RequestMapping(path = "/between/{startDate}/{endDate}", method = RequestMethod.GET)
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

  @RequestMapping(path = "since/{timestamp}", method = RequestMethod.GET)
  public ResponseEntity<List<TemperatureDto>> getTemperaturesSince(
      @PathVariable @NotNull Instant timestamp) {
    log.info("All tempereatures since {} requested in TemperatureController", timestamp);

    List<Temperature> temperatureList = this.temperatureService.getTemperaturesSince(timestamp);

    List<TemperatureDto> temperatureDtos = temperatureList.stream().map(TemperatureMapper::toDto)
        .toList();

    log.info("{} temperatures since {} returned in TemperatureController", temperatureDtos.size(),
        timestamp);

    return ResponseEntity.ok().body(temperatureDtos);
  }

  @RequestMapping(path = "/last", method = RequestMethod.GET)
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

  @RequestMapping(path = "/grouped/between/{startDate}/{endDate}", method = RequestMethod.GET)
  public ResponseEntity<TemperatureMinMaxMeanDto> getGroupedTemperatureBetweenDate(
      @PathVariable @NotNull Instant startDate, @PathVariable @NotNull Instant endDate) {
    log.info("Group temperatures between {} and {} requested in TemperatureController", startDate,
        endDate);

    List<List<Temperature>> groupedTemperatures = this.temperatureService.getGroupedMinMaxMean(
        startDate, endDate);

    List<List<TemperatureDto>> groupedTemperaturesDto = groupedTemperatures.stream()
        .map(list -> list.stream().map(TemperatureMapper::toDto).toList()).toList();

    TemperatureMinMaxMeanDto temperatureMinMaxMeanDto = TemperatureMinMaxMeanDto.builder()
        .minTemperatures(groupedTemperaturesDto.get(0))
        .maxTemperatures(groupedTemperaturesDto.get(1))
        .meanTemperatures(groupedTemperaturesDto.get(2)).build();

    log.info("Min: {}, Max: {}, Mean: {} temperatures returned in TemperatureController",
        temperatureMinMaxMeanDto.getMinTemperatures(),
        temperatureMinMaxMeanDto.getMaxTemperatures(),
        temperatureMinMaxMeanDto.getMeanTemperatures());

    return ResponseEntity.ok().body(temperatureMinMaxMeanDto);
  }
}
