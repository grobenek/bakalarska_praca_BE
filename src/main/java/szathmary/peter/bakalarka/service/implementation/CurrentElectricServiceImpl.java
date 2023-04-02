package szathmary.peter.bakalarka.service.implementation;

import org.springframework.stereotype.Service;
import szathmary.peter.bakalarka.constant.ElectricPhase;
import szathmary.peter.bakalarka.entity.Current;
import szathmary.peter.bakalarka.exception.NoDataFound;
import szathmary.peter.bakalarka.repository.CurrentElectricRepository;
import szathmary.peter.bakalarka.service.CurrentElectricService;

import java.time.Instant;
import java.util.List;

@Service
public class CurrentElectricServiceImpl implements CurrentElectricService {

  private final CurrentElectricRepository currentRepository;

  public CurrentElectricServiceImpl(CurrentElectricRepository currentRepository) {
    this.currentRepository = currentRepository;
  }

  @Override
  public List<Current> findAll(List<ElectricPhase> phases) {
    return this.currentRepository.findAll(phases);
  }

  @Override
  public List<Current> findAllBetweenDate(Instant startDate, Instant endDate,
      List<ElectricPhase> phases) {
    return this.currentRepository.findAllBetweenDates(startDate, endDate, phases);
  }

  @Override
  public List<List<Current>> getValueSince(Instant since, List<ElectricPhase> phases) {
    Instant endDate = Instant.now();

    if (since.equals(endDate)) {
      return getAllValuesFromDate(since, phases);
    }

    return currentRepository.getGroupedMinMaxMean(since, endDate, phases);
  }

  @Override
  public Current getLastValue(List<ElectricPhase> phases) throws NoDataFound {
    return this.currentRepository.getLastValue(phases);
  }

  @Override
  public List<List<Current>> getGroupedMinMaxMean(Instant startDate, Instant endDate,
      List<ElectricPhase> phases) {
    if (startDate.equals(endDate)) {
      return this.getAllValuesFromDate(startDate, phases);
    }

    return this.currentRepository.getGroupedMinMaxMean(startDate, endDate, phases);
  }

  @Override
  public List<List<Current>> getAllValuesFromDate(Instant startDate, List<ElectricPhase> phases) {
    Instant startOfDay = startDate.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
    Instant endOfDate = startOfDay.plus(java.time.Duration.ofDays(1));

    return this.currentRepository.getGroupedMinMaxMean(startOfDay, endOfDate, phases);
  }

    @Override
    public List<Current> getLastNData(int count, List<ElectricPhase> phases) {
        return this.currentRepository.getLastNValues(phases, count);
    }

    @Override
  public void saveValue(Current valueToSave) {
    this.currentRepository.save(valueToSave);
  }

  @Override
  public void saveValues(List<Current> valuesToSave) {
    this.currentRepository.saveAll(valuesToSave);
  }
}
