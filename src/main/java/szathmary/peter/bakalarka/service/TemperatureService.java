package szathmary.peter.bakalarka.service;

import java.time.Instant;
import java.util.List;
import szathmary.peter.bakalarka.entity.Temperature;
import szathmary.peter.bakalarka.exception.NoDataFound;

public interface TemperatureService {
  List<Temperature> findAll();

  List<Temperature> findAllBetweenDate(Instant startDate, Instant endDate);

  List<Temperature> getTemperaturesSince(Instant since);

  Temperature getLastTemperature() throws NoDataFound;

  List<List<Temperature>> getGroupedMinMaxMean(Instant startDay, Instant endDay);
}
