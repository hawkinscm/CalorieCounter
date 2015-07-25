package seahawk.caloriecounter.domain.api;

public enum NutrientType {
  CALORIES("Cal.", NutrientUnit.NO_UNIT),
  TOTAL_FAT("Total Fat", NutrientUnit.GRAM),
  CARBOHYDRATES("Carbs", NutrientUnit.GRAM),
  PROTEIN("Protein", NutrientUnit.GRAM),
  FAT_CALORIES("Fat Cal.", NutrientUnit.NO_UNIT),
  SATURATED_FAT("Sat. Fat", NutrientUnit.GRAM),
  SUGARS("Sugars", NutrientUnit.GRAM),
  CHOLESTEROL("Cholest.", NutrientUnit.MILLIGRAM),
  SODIUM("Sodium", NutrientUnit.MILLIGRAM);

  public static final String FORMER_SUGARS_NAME = "Trans_Fat";

  private String displayName;
  private NutrientUnit unit;

  NutrientType(String displayName, NutrientUnit unit) {
    this.displayName = displayName;
    this.unit = unit;
  }

  public static NutrientType getByIndex(int index) {
    for (NutrientType type : values()) {
      if (type.ordinal() == index) {
        return type;
      }
    }
    return null;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDisplayMeasure(PositiveDecimalNumber amount) {
    if (unit == NutrientUnit.NO_UNIT) {
      return amount.asWholeNumber() + unit.abbreviation;
    }
    return amount + unit.abbreviation;
  }

  public boolean matchesUnitAbbreviation(String unitAbbreviation) {
    return unit.abbreviation.equals(unitAbbreviation);
  }

  private enum NutrientUnit {
    GRAM("g"),
    MILLIGRAM("mg"),
    NO_UNIT("");

    private String abbreviation;

    NutrientUnit(String abbreviation) {
      this.abbreviation = abbreviation;
    }
  }
}
