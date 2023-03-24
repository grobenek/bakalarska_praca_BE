package szathmary.peter.bakalarka.service;

import java.time.Instant;
import java.util.List;
import szathmary.peter.bakalarka.constant.ElectricPhase;
import szathmary.peter.bakalarka.exception.NoDataFound;

public interface BaseInfluxdbElectricService<T> {

  List<T> findAll(List<ElectricPhase> phases);

  List<T> findAllBetweenDate(Instant startDate, Instant endDate, List<ElectricPhase> phases);

  List<List<T>> getValueSince(Instant since, List<ElectricPhase> phases);

  T getLastValue(List<ElectricPhase> phases) throws NoDataFound;

  List<List<T>> getGroupedMinMaxMean(Instant startDate, Instant endDate, List<ElectricPhase> phases);

  List<List<T>> getAllValuesFromDate(Instant startDate, List<ElectricPhase> phases);

  void saveValue(T valueToSave);

  void saveValues(List<T> valuesToSave);
}
