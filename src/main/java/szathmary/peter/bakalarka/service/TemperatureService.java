package szathmary.peter.bakalarka.service;

import java.time.Instant;
import java.util.List;
import szathmary.peter.bakalarka.entity.Temperature;

public interface TemperatureService {
  List<Temperature> findAll();

  List<Temperature> findAllBetweenDate(Instant startDate, Instant endDate);

  List<Temperature> getTemperaturesSince(Instant since);
}
