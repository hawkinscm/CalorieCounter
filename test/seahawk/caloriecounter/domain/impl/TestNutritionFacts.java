package seahawk.caloriecounter.domain.impl;

import junit.framework.TestCase;
import seahawk.caloriecounter.domain.api.NutrientType;
import seahawk.caloriecounter.domain.api.PositiveDecimalNumber;

public class TestNutritionFacts extends TestCase {
	public void testGetSetAmount() {
		NutritionFactsImpl facts = new NutritionFactsImpl();
		facts.setAmount(NutrientType.CALORIES, new PositiveDecimalNumber("3"));
		facts.setAmount(NutrientType.FAT_CALORIES, new PositiveDecimalNumber("4"));
		facts.setAmount(NutrientType.TOTAL_FAT, new PositiveDecimalNumber("6"));
		facts.setAmount(NutrientType.SATURATED_FAT, new PositiveDecimalNumber(".5"));
		facts.setAmount(NutrientType.SUGARS, new PositiveDecimalNumber(".234"));
		facts.setAmount(NutrientType.CHOLESTEROL, new PositiveDecimalNumber(".005"));
		facts.setAmount(NutrientType.SODIUM, new PositiveDecimalNumber(".999"));
		facts.setAmount(NutrientType.CARBOHYDRATES, new PositiveDecimalNumber("68594"));
		facts.setAmount(NutrientType.PROTEIN, new PositiveDecimalNumber("23423.236"));
		
		assertEquals(new PositiveDecimalNumber(3), facts.getAmount(NutrientType.CALORIES));
		assertEquals(new PositiveDecimalNumber(4), facts.getAmount(NutrientType.FAT_CALORIES));
		assertEquals(new PositiveDecimalNumber(6), facts.getAmount(NutrientType.TOTAL_FAT));
		assertEquals(new PositiveDecimalNumber(0.5), facts.getAmount(NutrientType.SATURATED_FAT));
		assertEquals(new PositiveDecimalNumber(0.23), facts.getAmount(NutrientType.SUGARS));
		assertEquals(new PositiveDecimalNumber(0.01), facts.getAmount(NutrientType.CHOLESTEROL));
		assertEquals(new PositiveDecimalNumber(1), facts.getAmount(NutrientType.SODIUM));
		assertEquals(new PositiveDecimalNumber(68594), facts.getAmount(NutrientType.CARBOHYDRATES));
		assertEquals(new PositiveDecimalNumber(23423.24), facts.getAmount(NutrientType.PROTEIN));
	}

	public void testAddSubtract() {
		NutritionFactsImpl facts1 = new NutritionFactsImpl();
		facts1.setAmount(NutrientType.CALORIES, new PositiveDecimalNumber("3"));
		facts1.setAmount(NutrientType.FAT_CALORIES, new PositiveDecimalNumber("4"));
		facts1.setAmount(NutrientType.TOTAL_FAT, new PositiveDecimalNumber("6"));
		facts1.setAmount(NutrientType.SATURATED_FAT, new PositiveDecimalNumber(".5"));
		facts1.setAmount(NutrientType.SUGARS, new PositiveDecimalNumber(".234"));
		facts1.setAmount(NutrientType.CHOLESTEROL, new PositiveDecimalNumber(".005"));
		facts1.setAmount(NutrientType.SODIUM, new PositiveDecimalNumber(".999"));
		facts1.setAmount(NutrientType.CARBOHYDRATES, new PositiveDecimalNumber("68594"));
		facts1.setAmount(NutrientType.PROTEIN, new PositiveDecimalNumber("23423.236"));
		
		NutritionFactsImpl facts2 = new NutritionFactsImpl();
		facts2.setAmount(NutrientType.CALORIES, new PositiveDecimalNumber("5"));
		facts2.setAmount(NutrientType.FAT_CALORIES, new PositiveDecimalNumber("3.2"));
		facts2.setAmount(NutrientType.TOTAL_FAT, new PositiveDecimalNumber("9.6"));
		facts2.setAmount(NutrientType.SATURATED_FAT, new PositiveDecimalNumber(".5"));
		facts2.setAmount(NutrientType.SUGARS, new PositiveDecimalNumber(".77"));
		facts2.setAmount(NutrientType.CHOLESTEROL, new PositiveDecimalNumber(".005"));
		facts2.setAmount(NutrientType.SODIUM, new PositiveDecimalNumber("9"));
		facts2.setAmount(NutrientType.CARBOHYDRATES, new PositiveDecimalNumber(".05"));
		facts2.setAmount(NutrientType.PROTEIN, new PositiveDecimalNumber("1"));
		
		facts1.addAmounts(facts2);
    assertEquals(new PositiveDecimalNumber(8), facts1.getAmount(NutrientType.CALORIES));
		assertEquals(new PositiveDecimalNumber(7.2), facts1.getAmount(NutrientType.FAT_CALORIES));
    assertEquals(new PositiveDecimalNumber(15.6), facts1.getAmount(NutrientType.TOTAL_FAT));
    assertEquals(new PositiveDecimalNumber(1), facts1.getAmount(NutrientType.SATURATED_FAT));
    assertEquals(new PositiveDecimalNumber(1), facts1.getAmount(NutrientType.SUGARS));
    assertEquals(new PositiveDecimalNumber(0.01), facts1.getAmount(NutrientType.CHOLESTEROL));
    assertEquals(new PositiveDecimalNumber(10), facts1.getAmount(NutrientType.SODIUM));
    assertEquals(new PositiveDecimalNumber(68594.05), facts1.getAmount(NutrientType.CARBOHYDRATES));
    assertEquals(new PositiveDecimalNumber(23424.24), facts1.getAmount(NutrientType.PROTEIN));
	}
}
