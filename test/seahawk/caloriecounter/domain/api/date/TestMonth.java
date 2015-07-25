package seahawk.caloriecounter.domain.api.date;

import junit.framework.TestCase;

public class TestMonth extends TestCase {
  public void testMonth() {
    assertEquals(1, Month.JANUARY.getNumericCode());
    assertEquals(2, Month.FEBRUARY.getNumericCode());
    assertEquals(3, Month.MARCH.getNumericCode());
    assertEquals(4, Month.APRIL.getNumericCode());
    assertEquals(5, Month.MAY.getNumericCode());
    assertEquals(6, Month.JUNE.getNumericCode());
    assertEquals(7, Month.JULY.getNumericCode());
    assertEquals(8, Month.AUGUST.getNumericCode());
    assertEquals(9, Month.SEPTEMBER.getNumericCode());
    assertEquals(10, Month.OCTOBER.getNumericCode());
    assertEquals(11, Month.NOVEMBER.getNumericCode());
    assertEquals(12, Month.DECEMBER.getNumericCode());

    assertEquals(Month.JANUARY, Month.getByNumericCode(1));
    assertEquals(Month.FEBRUARY, Month.getByNumericCode(2));
    assertEquals(Month.MARCH, Month.getByNumericCode(3));
    assertEquals(Month.APRIL, Month.getByNumericCode(4));
    assertEquals(Month.MAY, Month.getByNumericCode(5));
    assertEquals(Month.JUNE, Month.getByNumericCode(6));
    assertEquals(Month.JULY, Month.getByNumericCode(7));
    assertEquals(Month.AUGUST, Month.getByNumericCode(8));
    assertEquals(Month.SEPTEMBER, Month.getByNumericCode(9));
    assertEquals(Month.OCTOBER, Month.getByNumericCode(10));
    assertEquals(Month.NOVEMBER, Month.getByNumericCode(11));
    assertEquals(Month.DECEMBER, Month.getByNumericCode(12));
    try {
      Month.getByNumericCode(0);
      fail("should throw Exception");
    }
    catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), true);
    }
    try {
      Month.getByNumericCode(13);
      fail("should throw Exception");
    }
    catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), true);
    }

    for (Month month : Month.values())
      assertEquals(month.getDisplayName().toUpperCase(), month.name());
  }
}
