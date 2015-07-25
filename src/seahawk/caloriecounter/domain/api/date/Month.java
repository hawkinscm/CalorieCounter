package seahawk.caloriecounter.domain.api.date;

public enum Month {
  JANUARY(1, "January"),
  FEBRUARY(2, "February"),
  MARCH(3, "March"),
  APRIL(4, "April"),
  MAY(5, "May"),
  JUNE(6, "June"),
  JULY(7, "July"),
  AUGUST(8, "August"),
  SEPTEMBER(9, "September"),
  OCTOBER(10, "October"),
  NOVEMBER(11, "November"),
  DECEMBER(12, "December");

  private int numericCode;
  private String displayName;

  private Month(int numericCode, String displayName) {
    this.numericCode = numericCode;
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getNumericCode() {
    return numericCode;
  }

  public static Month getByNumericCode(int numericCode) {
    for (Month month : values())
      if (month.numericCode == numericCode)
        return month;

    throw new IllegalArgumentException("No month represented by given numeric code: " + numericCode);
  }
}
