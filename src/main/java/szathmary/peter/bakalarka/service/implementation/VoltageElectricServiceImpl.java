package szathmary.peter.bakalarka.service.implementation;

import org.springframework.stereotype.Service;
import szathmary.peter.bakalarka.constant.ElectricPhase;
import szathmary.peter.bakalarka.entity.Voltage;
import szathmary.peter.bakalarka.exception.NoDataFound;
import szathmary.peter.bakalarka.repository.VoltageElectricRepository;
import szathmary.peter.bakalarka.service.VoltageElectricService;

import java.time.Instant;
import java.util.List;

@Service
public class VoltageElectricServiceImpl implements VoltageElectricService {

  private final VoltageElectricRepository voltageRepository;

  public VoltageElectricServiceImpl(VoltageElectricRepository voltageRepository) {
    this.voltageRepository = voltageRepository;
  }

  @Override
  public List<Voltage> findAll(List<ElectricPhase> phases) {
    return this.voltageRepository.findAll(phases);
  }

  @Override
  public List<Voltage> findAllBetweenDate(Instant startDate, Instant endDate,
      List<ElectricPhase> phases) {
    return this.voltageRepository.findAllBetweenDates(startDate, endDate, phases);
  }

  @Override
  public List<List<Voltage>> getValueSince(Instant since, List<ElectricPhase> phases) {
    Instant endDate = Instant.now();

    if (since.equals(endDate)) {
      return getAllValuesFromDate(since, phases);
    }

    return voltageRepository.getGroupedMinMaxMean(since, endDate, phases);
  }

  @Override
  public Voltage getLastValue(List<ElectricPhase> phases) throws NoDataFound {
    return this.voltageRepository.getLastValue(phases);
  }

  @Override
  public List<List<Voltage>> getGroupedMinMaxMean(Instant startDate, Instant endDate,
      List<ElectricPhase> phases) {
    if (startDate.equals(endDate)) {
      return this.getAllValuesFromDate(startDate, phases);
    }

    return this.voltageRepository.getGroupedMinMaxMean(startDate, endDate, phases);
  }

  @Override
  public List<List<Voltage>> getAllValuesFromDate(Instant startDate, List<ElectricPhase> phases) {
    Instant startOfDay = startDate.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
    Instant endOfDate = startOfDay.plus(java.time.Duration.ofDays(1));

    return this.voltageRepository.getGroupedMinMaxMean(startOfDay, endOfDate, phases);
  }

    @Override
    public List<Voltage> getLastNData(int count, List<ElectricPhase> phases) {
        return this.voltageRepository.getLastNValues(phases, count);
    }

    @Override
  public void saveValue(Voltage valueToSave) {
    this.voltageRepository.save(valueToSave);
  }

  @Override
  public void saveValues(List<Voltage> valuesToSave) {
    this.voltageRepository.saveAll(valuesToSave);
  }
}
