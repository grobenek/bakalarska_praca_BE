package szathmary.peter.bakalarka.constant;

public enum ElectricPhase {
  L1("L1"),
  L2("L2"),
  L3("L3");

  private final String phaseCode;

  ElectricPhase(String phaseCode) {
    this.phaseCode = phaseCode;
  }

  public String toString() {
    return phaseCode;
  }
}
