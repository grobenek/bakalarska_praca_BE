package szathmary.peter.bakalarka.service.implementation;

import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import szathmary.peter.bakalarka.entity.Temperature;
import szathmary.peter.bakalarka.exception.NoDataFound;
import szathmary.peter.bakalarka.repository.TemperatureRepository;
import szathmary.peter.bakalarka.service.TemperatureService;

@Slf4j
@Service
public class TemperatureServiceImpl implements TemperatureService {

  private final TemperatureRepository temperatureRepository;

  public TemperatureServiceImpl(TemperatureRepository temperatureRepository) {
    this.temperatureRepository = temperatureRepository;
  }

  @Override
  public List<Temperature> findAll() {
    return this.temperatureRepository.findAll();
  }

  @Override
  public List<Temperature> findAllBetweenDate(Instant startDate, Instant endDate) {
    return this.temperatureRepository.findAllBetweenDate(startDate, endDate);
  }

  @Override
  public List<List<Temperature>> getTemperaturesSince(Instant since) {
    Instant endDate = Instant.now();

    if (since.equals(endDate)) {
      return getAllTemperaturesFromDate(since);
    }

    return temperatureRepository.findGroupedMinMaxMean(since, endDate);
  }

  @Override
  public Temperature getLastTemperature() throws NoDataFound {

    return this.temperatureRepository.getLastTemperature();
  }

  @Override
  public List<List<Temperature>> getGroupedMinMaxMean(Instant startDate, Instant endDate) {
    if (startDate.equals(endDate)) {
      return this.getAllTemperaturesFromDate(startDate);
    }

    return this.temperatureRepository.findGroupedMinMaxMean(startDate, endDate);
  }

  @Override
  public List<List<Temperature>> getAllTemperaturesFromDate(Instant startDate) {
    Instant startOfDay = startDate.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
    Instant endOfDate = startOfDay.plus(java.time.Duration.ofDays(1));

    return this.temperatureRepository.findGroupedMinMaxMean(startOfDay, endOfDate);
  }

  @Override
  public void saveTemperatures(List<Temperature> temperaturesToSave) {
    this.temperatureRepository.saveAll(temperaturesToSave);
  }

  @Override
  public void saveTemperature(Temperature temperatureToSave) {
    this.temperatureRepository.save(temperatureToSave);
  }
}
