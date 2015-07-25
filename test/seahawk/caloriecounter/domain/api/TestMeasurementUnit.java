
package seahawk.caloriecounter.domain.api;

import junit.framework.TestCase;

import java.util.List;

public class TestMeasurementUnit extends TestCase {
  public void testMeasurementUnit() {
    assertEquals(14, MeasurementUnit.values().size());

    assertEquals(1, MeasurementUnit.SERVING_SIZE.getConvertibleUnits().size());
    assertEquals(MeasurementUnit.SERVING_SIZE, MeasurementUnit.SERVING_SIZE.getConvertibleUnits().get(0));

    for (MeasurementUnit unit1 : MeasurementUnit.values()) {
      List<MeasurementUnit> convertibleUnits = unit1.getConvertibleUnits();
      for (MeasurementUnit unit2 : MeasurementUnit.values()) {
        if (convertibleUnits.contains(unit2)) {
          PositiveDecimalNumber reverseLookup = new PositiveDecimalNumber("1.0/" + unit2.convertToUnit(new PositiveDecimalNumber(1.0), unit1).asPreciseDouble());
          assertEquals(unit1 + " failed convert with " + unit2, unit1.convertToUnit(new PositiveDecimalNumber(1.0), unit2).toString(), reverseLookup.toString());
        }
      }
    }

    assertEquals(new PositiveDecimalNumber("4"), MeasurementUnit.QUART.convertToUnit(new PositiveDecimalNumber("1"), MeasurementUnit.CUP));
    assertEquals(new PositiveDecimalNumber("1"), MeasurementUnit.CUP.convertToUnit(new PositiveDecimalNumber("4"), MeasurementUnit.QUART));
    assertEquals(new PositiveDecimalNumber("0.5"), MeasurementUnit.OUNCE.convertToUnit(new PositiveDecimalNumber("8"), MeasurementUnit.POUND));
    assertEquals(new PositiveDecimalNumber("0.33"), MeasurementUnit.TEASPOON.convertToUnit(new PositiveDecimalNumber("1"), MeasurementUnit.TABLESPOON));
    assertEquals(new PositiveDecimalNumber("3"), MeasurementUnit.TABLESPOON.convertToUnit(new PositiveDecimalNumber("1"), MeasurementUnit.TEASPOON));
    assertEquals(new PositiveDecimalNumber("4"), MeasurementUnit.FLUID_OUNCE.convertToUnit(new PositiveDecimalNumber("4"), MeasurementUnit.FLUID_OUNCE));
    assertEquals(new PositiveDecimalNumber("6"), MeasurementUnit.MILLILITER.convertToUnit(new PositiveDecimalNumber("6000"), MeasurementUnit.LITER));

    MeasurementUnit unit = MeasurementUnit.findOrCreate("a");
    assertEquals(15, MeasurementUnit.values().size());
    assertEquals("A", unit.toString());
    assertSame(unit, MeasurementUnit.findOrCreate("a"));
    assertEquals(unit, MeasurementUnit.findOrCreate("a"));
    assertEquals(15, MeasurementUnit.values().size());
    assertSame(unit, MeasurementUnit.findOrCreate("A"));
    assertEquals(15, MeasurementUnit.values().size());

    MeasurementUnit unit2 = MeasurementUnit.findOrCreate("piece");
    assertEquals(16, MeasurementUnit.values().size());
    assertEquals("PIECE", unit2.toString());
    assertSame(unit2, MeasurementUnit.findOrCreate("piece"));
    assertEquals(unit2, MeasurementUnit.findOrCreate("Piece"));
    assertEquals(16, MeasurementUnit.values().size());
    assertSame(unit2, MeasurementUnit.findOrCreate("PIECE"));
    assertEquals(16, MeasurementUnit.values().size());

    assertFalse(unit.equals(unit2));
  }
}
