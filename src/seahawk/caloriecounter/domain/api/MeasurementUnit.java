
package seahawk.caloriecounter.domain.api;

import java.util.*;

public class MeasurementUnit implements Comparable<MeasurementUnit> {
  private static double[][] weightConversionTable = {
     {1,        0.0625,     28.3495},
     {16,       1,          453.592,},
     {0.035274, 0.00220462, 1,}};

  private static double[][] volumeConversionTable = {
     {1,        0.333333, 0.166667, 0.0208333,  0.0104167,  0.00520833, 0.00130208,  4.92892, 0.00492892, 0.041667},
     {3,        1,        0.5,      0.0625,     0.03125,    0.015625,   0.00390625,  14.7868, 0.0147868,  0.125},
     {6,        2,        1,        0.125,      0.0625,     0.03125,    0.0078125,   29.5735, 0.0295735,  0.25},
     {48,       16,       8,        1,          0.5,        0.25,       0.0625,      236.588, 0.236588,   2},
     {96,       32,       16,       2,          1,          0.5,        0.125,       473.176, 0.473176,   4},
     {192,      64,       32,       4,          2,          1,          0.25,        946.353, 0.946353,   8},
     {768,      256,      128,      16,         8,          4,          1,           3785.41, 3.78541,    32},
     {0.202884, 0.067628, 0.033814, 0.00422675, 0.00211338, 0.00105669, 0.000264172, 1,       0.001,      0.008453514},
     {202.884,  67.628,   33.814,   4.22675,    2.11338,    1.05669,    0.264172,    1000,    1,          8.453514},
     {24,       8,        4,        0.5,        0.25,       0.125,      0.03125,     118.294, 0.118294,   1}};

  private static final Set<MeasurementUnit> standardMeasurementUnits = new LinkedHashSet<>();
  private static final Set<MeasurementUnit> userMeasurementUnits = new TreeSet<>();

	public static final MeasurementUnit OUNCE = new MeasurementUnit("OUNCE", MeasurementType.WEIGHT, 0);
  public static final MeasurementUnit POUND = new MeasurementUnit("POUND", MeasurementType.WEIGHT, 1);
  public static final MeasurementUnit GRAM = new MeasurementUnit("GRAM", MeasurementType.WEIGHT, 2);

  public static final MeasurementUnit TEASPOON = new MeasurementUnit("TEASPOON", MeasurementType.VOLUME, 0);
  public static final MeasurementUnit TABLESPOON = new MeasurementUnit("TABLESPOON", MeasurementType.VOLUME, 1);
  public static final MeasurementUnit FLUID_OUNCE = new MeasurementUnit("FLUID OUNCE", MeasurementType.VOLUME, 2);
  public static final MeasurementUnit CUP = new MeasurementUnit("CUP", MeasurementType.VOLUME, 3);
  public static final MeasurementUnit PINT = new MeasurementUnit("PINT", MeasurementType.VOLUME, 4);
  public static final MeasurementUnit QUART = new MeasurementUnit("QUART", MeasurementType.VOLUME, 5);
  public static final MeasurementUnit GALLON = new MeasurementUnit("GALLON", MeasurementType.VOLUME, 6);
  public static final MeasurementUnit MILLILITER = new MeasurementUnit("MILLILITER", MeasurementType.VOLUME, 7);
  public static final MeasurementUnit LITER = new MeasurementUnit("LITER", MeasurementType.VOLUME, 8);
  public static final MeasurementUnit STICK_BUTTER = new MeasurementUnit("STICK OF BUTTER", MeasurementType.VOLUME, 9);

  public static final MeasurementUnit SERVING_SIZE = new MeasurementUnit("SERVING SIZE", null, -1);

  private String displayName;
  private MeasurementType measurementType;
  private int convertIndex;

  private MeasurementUnit(String displayName, MeasurementType type, int convertIndex) {
    this.displayName = displayName.toUpperCase();
    this.measurementType = type;
    this.convertIndex = convertIndex;
    standardMeasurementUnits.add(this);
  }

  private MeasurementUnit(String displayName) {
    this.displayName = displayName.toUpperCase();
    this.measurementType = null;
    this.convertIndex = -1;
    userMeasurementUnits.add(this);
  }

  public static MeasurementUnit findOrCreate(String unitName) {
    for (MeasurementUnit unit : standardMeasurementUnits) {
      if (unit.displayName.equalsIgnoreCase(unitName))
        return unit;
    }
    for (MeasurementUnit unit : userMeasurementUnits) {
      if (unit.displayName.equalsIgnoreCase(unitName))
        return unit;
    }
    return new MeasurementUnit(unitName);
  }

  public static Set<MeasurementUnit> values() {
    LinkedHashSet<MeasurementUnit> allValues = new LinkedHashSet<>(standardMeasurementUnits);
    allValues.addAll(userMeasurementUnits);
    return allValues;
  }

  public static MeasurementUnit[] getWeightUnits() {
    List<MeasurementUnit> weightUnits = new ArrayList<>();
    for (MeasurementUnit unit : standardMeasurementUnits)
      if (unit.isWeightUnit())
        weightUnits.add(unit);
    return weightUnits.toArray(new MeasurementUnit[weightUnits.size()]);
  }

  public static MeasurementUnit[] getVolumeUnits() {
    List<MeasurementUnit> volumeUnits = new ArrayList<>();
    for (MeasurementUnit unit : standardMeasurementUnits)
      if (unit.isVolumeUnit())
        volumeUnits.add(unit);
    return volumeUnits.toArray(new MeasurementUnit[volumeUnits.size()]);
  }

  public PositiveDecimalNumber convertToUnit(PositiveDecimalNumber amount, MeasurementUnit unitToConvertTo) {
    if (equals(unitToConvertTo)) {
      return amount;
    }

    if (measurementType == null || measurementType != unitToConvertTo.measurementType) {
      throw new IllegalArgumentException("Can only convert between convertible units. Given: " +
         displayName + " from " + unitToConvertTo.displayName);
    }

    if (measurementType == MeasurementType.WEIGHT)
      return amount.multiply(new PositiveDecimalNumber(weightConversionTable[convertIndex][unitToConvertTo.convertIndex]));
    else
      return amount.multiply(new PositiveDecimalNumber(volumeConversionTable[convertIndex][unitToConvertTo.convertIndex]));
  }

  public List<MeasurementUnit> getConvertibleUnits() {
    List<MeasurementUnit> convertibleUnits = new ArrayList<>();
    if (measurementType == null) {
      convertibleUnits.add(this);
    }
    else {
      for (MeasurementUnit unit : values()) {
        if (unit.measurementType == measurementType)
          convertibleUnits.add(unit);
      }
    }
    return convertibleUnits;
  }

  public boolean isWeightUnit() {
    return measurementType == MeasurementType.WEIGHT;
  }

  public boolean isVolumeUnit() {
    return measurementType == MeasurementType.VOLUME;
  }

  @Override
  public String toString() {
    return displayName;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null || !(o instanceof MeasurementUnit)) return false;

    MeasurementUnit that = (MeasurementUnit) o;
    return this.displayName.equals(that.displayName);
  }

  @Override
  public int hashCode() {
    return displayName.hashCode();
  }

  @Override
  public int compareTo(MeasurementUnit unit) {
    return displayName.compareTo(unit.displayName);
  }

  private enum MeasurementType {
    WEIGHT,
    VOLUME
  }
}
