package seahawk.caloriecounter.domain.api;

import junit.framework.TestCase;

public class TestPositiveDecimalNumber extends TestCase {
  public void testDivision() {
    assertEquals("1.0", new PositiveDecimalNumber("1/1").toString());
    assertEquals("2.0", new PositiveDecimalNumber("2/1").toString());
    assertEquals("0.5", new PositiveDecimalNumber("1/2").toString());
    assertEquals("0.06", new PositiveDecimalNumber("1/16").toString());
    assertEquals("0.33", new PositiveDecimalNumber("1/3").toString());
    assertEquals("0.67", new PositiveDecimalNumber("2/3").toString());
  }

  public void testAsPreciseDouble() {
    assertEquals("1.0", new PositiveDecimalNumber("1/1").asPreciseDouble());
    assertEquals("2.0", new PositiveDecimalNumber("2/1").asPreciseDouble());
    assertEquals("0.5", new PositiveDecimalNumber("1/2").asPreciseDouble());
    assertEquals("0.0625", new PositiveDecimalNumber("1/16").asPreciseDouble());
    assertEquals("0.3333333333333333", new PositiveDecimalNumber("1/3").asPreciseDouble());
    assertEquals("0.6666666666666666", new PositiveDecimalNumber("2/3").asPreciseDouble());
  }

  public void testAddition() {
    PositiveDecimalNumber num1 = new PositiveDecimalNumber("1.1");
    PositiveDecimalNumber num2 = new PositiveDecimalNumber("3.42");
    assertEquals("4.52", num1.add(num2).toString());
    assertEquals("4.52", num2.add(num1).toString());

    PositiveDecimalNumber num3 = new PositiveDecimalNumber("0");
    assertEquals(num1, num1.add(num3));
    assertEquals(num2, num3.add(num2));
    assertEquals(num3, num3.add(num3));
    assertEquals("0.0", num3.add(num3).toString());

    PositiveDecimalNumber num4 = new PositiveDecimalNumber("0.58");
    assertEquals("4.0", num2.add(num4).toString());
    assertEquals("4.0", num4.add(num2).toString());

    PositiveDecimalNumber num5 = new PositiveDecimalNumber("999.99");
    assertEquals("1001.09", num1.add(num5).toString());
    assertEquals("1003.41", num2.add(num5).toString());
    assertEquals("999.99", num5.add(num3).toString());
    assertEquals("1000.57", num5.add(num4).toString());
    assertEquals("1999.98", num5.add(num5).toString());

    PositiveDecimalNumber num6 = new PositiveDecimalNumber(".01");
    assertEquals("1000.0", num6.add(num5).toString());

    PositiveDecimalNumber num7 = new PositiveDecimalNumber("1.");
    assertEquals("2.1", num7.add(num1).toString());
    assertEquals("4.42", num7.add(num2).toString());
    assertEquals("1.0", num7.add(num3).toString());
    assertEquals("1.58", num4.add(num7).toString());
    assertEquals("1000.99", num5.add(num7).toString());
    assertEquals("1.01", num6.add(num7).toString());
    assertEquals("2.0", num7.add(num7).toString());
  }

  public void testMultiply() {
    PositiveDecimalNumber num1 = new PositiveDecimalNumber("1.1");
    PositiveDecimalNumber num2 = new PositiveDecimalNumber("3.42");
    assertEquals("3.76", num1.multiply(num2).toString());
    assertEquals("3.76", num2.multiply(num1).toString());

    PositiveDecimalNumber num3 = new PositiveDecimalNumber("0");
    assertEquals(num3, num1.multiply(num3));
    assertEquals(num3, num3.multiply(num2));
    assertEquals(num3, num3.multiply(num3));

    PositiveDecimalNumber num4 = new PositiveDecimalNumber("0.58");
    assertEquals("1.98", num2.multiply(num4).toString());
    assertEquals("1.98", num4.multiply(num2).toString());

    PositiveDecimalNumber num5 = new PositiveDecimalNumber("999.99");
    assertEquals("1099.99", num1.multiply(num5).toString());
    assertEquals("3419.97", num2.multiply(num5).toString());
    assertEquals("0.0", num5.multiply(num3).toString());
    assertEquals("579.99", num5.multiply(num4).toString());

    PositiveDecimalNumber num6 = new PositiveDecimalNumber(".01");
    assertEquals("10.0", num6.multiply(num5).toString());

    PositiveDecimalNumber num7 = new PositiveDecimalNumber("1.");
    assertEquals(num1, num7.multiply(num1));
    assertEquals(num2, num7.multiply(num2));
    assertEquals(num3, num7.multiply(num3));
    assertEquals(num4, num4.multiply(num7));
    assertEquals(num5, num5.multiply(num7));
    assertEquals(num6, num6.multiply(num7));
    assertEquals(num7, num7.multiply(num7));
  }
}
