package seahawk.caloriecounter.domain.api.date;

import junit.framework.TestCase;

import java.util.Calendar;

public class TestDate extends TestCase {
  public void testDate() {
    Calendar today = Calendar.getInstance();
    Date date = new Date(today);
    assertEquals(today.get(Calendar.YEAR), date.getYear());
    assertEquals(today.get(Calendar.MONTH) + 1, date.getMonth());
    assertEquals(today.get(Calendar.DAY_OF_MONTH), date.getDay());

    date = new Date("20140519");
    assertEquals(2014, date.getYear());
    assertEquals(5, date.getMonth());
    assertEquals(19, date.getDay());
    assertEquals("20140519", date.toString());
    assertEquals(20140519, date.hashCode());
  }

  public void testEquals_CompareTo() {
    Date date = new Date("20141102");
    Date sameDate = new Date("20141102");

    assertEquals(date, date);
    assertEquals(sameDate, date);
    assertEquals(date, sameDate);
    assertEquals(0, date.compareTo(date));
    assertEquals(0, date.compareTo(sameDate));
    assertEquals(0, sameDate.compareTo(date));

    Date beforeYear = new Date("20131102");
    Date afterYear = new Date("20151102");
    assertFalse(date.equals(beforeYear));
    assertFalse(date.equals(afterYear));
    assertEquals(1, date.compareTo(beforeYear));
    assertEquals(-1, date.compareTo(afterYear));

    Date beforeMonth = new Date("20141002");
    Date afterMonth = new Date("20141202");
    assertFalse(date.equals(beforeMonth));
    assertFalse(date.equals(afterMonth));
    assertEquals(1, date.compareTo(beforeMonth));
    assertEquals(-1, date.compareTo(afterMonth));

    Date beforeDay = new Date("20141101");
    Date afterDay = new Date("20141103");
    assertFalse(date.equals(beforeDay));
    assertFalse(date.equals(afterDay));
    assertEquals(1, date.compareTo(beforeDay));
    assertEquals(-1, date.compareTo(afterDay));
  }
}
