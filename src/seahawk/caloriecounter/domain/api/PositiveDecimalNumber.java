package seahawk.caloriecounter.domain.api;

public class PositiveDecimalNumber implements Comparable<PositiveDecimalNumber> {
  private final double value;
  private String formattedString = null;

  public PositiveDecimalNumber(String decimalNumberStr) {
    int dividerIndex = decimalNumberStr.indexOf('/');
    if (dividerIndex > 0 && dividerIndex + 1 < decimalNumberStr.length()) {
      value = Double.parseDouble(decimalNumberStr.substring(0, dividerIndex).trim()) /
              Double.parseDouble(decimalNumberStr.substring(dividerIndex + 1).trim());
    }
    else {
      value = Double.parseDouble(decimalNumberStr.trim());
    }
    if (value < 0) {
      throw new NumberFormatException("negative values not allowed: " + decimalNumberStr);
    }
  }

  public PositiveDecimalNumber(double value) {
    if (value < 0) {
      throw new NumberFormatException("negative values not allowed: " + value);
    }
    this.value = value;
  }

  public PositiveDecimalNumber add(PositiveDecimalNumber number) {
    return new PositiveDecimalNumber(this.value + number.value);
  }

  public PositiveDecimalNumber multiply(PositiveDecimalNumber number) {
    return new PositiveDecimalNumber(this.value * number.value);
  }

  public String asWholeNumber() {
    return String.valueOf(Math.round(value));
  }

  public String asPreciseDouble() {
    return String.valueOf(value);
  }

  public double getValue() {
    return value;
  }

  @Override
  public String toString() {
    if (formattedString == null) {
      formattedString = String.valueOf(Math.round(100.0 * value) / 100.0);
    }
    return formattedString;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof PositiveDecimalNumber)) return false;

    PositiveDecimalNumber that = (PositiveDecimalNumber) o;
    return this.toString().equals(that.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public int compareTo(PositiveDecimalNumber other) {
    return Double.compare(this.value, other.value);
  }
}
