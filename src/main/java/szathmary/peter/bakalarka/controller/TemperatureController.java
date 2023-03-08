package szathmary.peter.bakalarka.controller;

import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import szathmary.peter.bakalarka.dto.TemperatureDto;
import szathmary.peter.bakalarka.entity.Temperature;
import szathmary.peter.bakalarka.service.implementation.TemperatureServiceImpl;

@Slf4j
@RestController
@RequestMapping("/api/temperature")
@CrossOrigin(origins = "*")
public class TemperatureController {

  private final TemperatureServiceImpl temperatureService;
  private final ModelMapper modelMapper;

  @Autowired
  public TemperatureController(TemperatureServiceImpl temperatureService) {
    this.temperatureService = temperatureService;
    this.modelMapper = new ModelMapper();
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<TemperatureDto>> getAllTemperatures() {
    log.info("getAllTemperatures requestedin TemperatureController");

    List<Temperature> temperatureList = this.temperatureService.findAll();

    List<TemperatureDto> temperatureDtos = temperatureList.stream()
        .map(temperature -> this.modelMapper.map(temperature, TemperatureDto.class)).toList();

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

    List<TemperatureDto> temperatureDtos = temperatureList.stream()
        .map(temperature -> this.modelMapper.map(temperature, TemperatureDto.class)).toList();

    log.info("{} temperatures returned between {} and {} in TemperatureController",
        temperatureDtos.size(), startDate, endDate);

    return ResponseEntity.ok().body(temperatureDtos);
  }

  @RequestMapping(path = "since/{timestamp}", method = RequestMethod.GET)
  public ResponseEntity<List<TemperatureDto>> getTemperaturesSince(
      @PathVariable @NotNull Instant timestamp) {
    log.info("All tempereatures since {} requested in TemperatureController", timestamp);

    List<Temperature> temperatureList = this.temperatureService.getTemperaturesSince(timestamp);

    List<TemperatureDto> temperatureDtos = temperatureList.stream()
        .map(temperature -> this.modelMapper.map(temperature, TemperatureDto.class)).toList();

    log.info("{} temperatures since {} returned in TemperatureController", temperatureDtos.size(),
        timestamp);

    return ResponseEntity.ok().body(temperatureDtos);
  }
}
