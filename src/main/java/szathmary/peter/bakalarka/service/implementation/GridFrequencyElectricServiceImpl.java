package szathmary.peter.bakalarka.service.implementation;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import szathmary.peter.bakalarka.constant.ElectricPhase;
import szathmary.peter.bakalarka.entity.GridFrequency;
import szathmary.peter.bakalarka.exception.NoDataFound;
import szathmary.peter.bakalarka.repository.GridFrequencyElectricRepository;
import szathmary.peter.bakalarka.service.GridFrequencyElectricService;

@Service
public class GridFrequencyElectricServiceImpl implements GridFrequencyElectricService {

  private final GridFrequencyElectricRepository gridFrequencyRepository;

  public GridFrequencyElectricServiceImpl(GridFrequencyElectricRepository gridFrequencyRepository) {
    this.gridFrequencyRepository = gridFrequencyRepository;
  }

  @Override
  public List<GridFrequency> findAll(List<ElectricPhase> phases) {
    return this.gridFrequencyRepository.findAll(phases);
  }

  @Override
  public List<GridFrequency> findAllBetweenDate(Instant startDate, Instant endDate,
      List<ElectricPhase> phases) {
    return this.gridFrequencyRepository.findAllBetweenDates(startDate, endDate, phases);
  }

  @Override
  public List<List<GridFrequency>> getValueSince(Instant since, List<ElectricPhase> phases) {
    Instant endDate = Instant.now();

    if (since.equals(endDate)) {
      return getAllValuesFromDate(since, phases);
    }

    return gridFrequencyRepository.getGroupedMinMaxMean(since, endDate, phases);
  }

  @Override
  public GridFrequency getLastValue(List<ElectricPhase> phases) throws NoDataFound {
    return this.gridFrequencyRepository.getLastValue(phases);
  }

  @Override
  public List<List<GridFrequency>> getGroupedMinMaxMean(Instant startDate, Instant endDate,
      List<ElectricPhase> phases) {
    if (startDate.equals(endDate)) {
      return this.getAllValuesFromDate(startDate, phases);
    }

    return this.gridFrequencyRepository.getGroupedMinMaxMean(startDate, endDate, phases);
  }

  @Override
  public List<List<GridFrequency>> getAllValuesFromDate(Instant startDate,
      List<ElectricPhase> phases) {
    Instant startOfDay = startDate.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
    Instant endOfDate = startOfDay.plus(java.time.Duration.ofDays(1));

    return this.gridFrequencyRepository.getGroupedMinMaxMean(startOfDay, endOfDate, phases);
  }

  @Override
  public void saveValue(GridFrequency valueToSave) {
    this.gridFrequencyRepository.save(valueToSave);
  }

  @Override
  public void saveValues(List<GridFrequency> valuesToSave) {
    this.gridFrequencyRepository.saveAll(valuesToSave);
  }
}
