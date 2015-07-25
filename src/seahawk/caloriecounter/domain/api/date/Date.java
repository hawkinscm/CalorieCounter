package seahawk.caloriecounter.domain.api.date;

import java.util.Calendar;

public class Date implements Comparable<Date> {
  private int year;
  private Month month;
  private int day;

  public Date(Calendar calendar) {
    year = calendar.get(Calendar.YEAR);
    month = Month.getByNumericCode(calendar.get(Calendar.MONTH) + 1);
    day = calendar.get(Calendar.DAY_OF_MONTH);
  }

  public Date(String dateStr) {
    year = Integer.parseInt(dateStr.substring(0, 4));
    month = Month.getByNumericCode(Integer.parseInt(dateStr.substring(4, 6)));
    day = Integer.parseInt(dateStr.substring(6, 8));
    if (year < 1 || day < 1 || day > 31)
      throw new IllegalArgumentException("Invalid date: " + dateStr);
  }

  public int getYear() {
    return year;
  }

  public int getMonth() {
    return month.getNumericCode();
  }

  public int getDay() {
    return day;
  }

  @Override
  public String toString() {
    return String.valueOf(hashCode());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Date that = (Date) o;
    return (this.day == that.day &&
            this.month.equals(that.month) &&
            this.year == that.year);
  }

  @Override
  public int hashCode() {
    return (year * 10000) + (month.getNumericCode() * 100) + day;
  }

  @Override
  public int compareTo(Date date) {
    return new Integer(hashCode()).compareTo(date.hashCode());
  }
}
