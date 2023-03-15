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
  public List<Temperature> getTemperaturesSince(Instant since) {
    return this.temperatureRepository.getTemperaturesSince(since);
  }

  @Override
  public Temperature getLastTemperature() throws NoDataFound {

    return this.temperatureRepository.getLastTemperature();
  }

  @Override
  public List<List<Temperature>> getGroupedMinMaxMean(Instant startDay, Instant endDay) {
    return this.temperatureRepository.findGroupedMinMaxMean(startDay, endDay);
  }
}
