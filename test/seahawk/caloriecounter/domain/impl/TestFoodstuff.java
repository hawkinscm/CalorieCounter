package seahawk.caloriecounter.domain.impl;

import junit.framework.TestCase;
import seahawk.caloriecounter.domain.api.FoodstuffCategory;
import seahawk.caloriecounter.domain.api.MeasurementUnit;
import seahawk.caloriecounter.domain.api.PositiveDecimalNumber;

public class TestFoodstuff extends TestCase {
	public void testConstructor() {
		FoodstuffImpl food = new FoodstuffImpl("name", FoodstuffCategory.findOrCreate("cat"));
		assertEquals(FoodstuffCategory.findOrCreate("CAT"), food.getCategory());
		assertEquals("name", food.getName());
		assertEquals("1.0", food.getAmount().toString());
		assertEquals(MeasurementUnit.SERVING_SIZE, food.getUnit());
		assertNotNull(food.getFacts());
	}
	
	public void testEquals() {
		FoodstuffImpl food1 = new FoodstuffImpl("name", FoodstuffCategory.findOrCreate("cat"));
		FoodstuffImpl food2 = new FoodstuffImpl("name", FoodstuffCategory.findOrCreate("dog"));
		assertTrue(food1.equals(food2));
	}
	
	public void testSetValues() {
		FoodstuffImpl food = new FoodstuffImpl("name", FoodstuffCategory.findOrCreate("cat"));
		food.setCategory(FoodstuffCategory.findOrCreate("Other"));
		food.setName("Food");
		food.setAmount(new PositiveDecimalNumber("2.499"));
    food.setUnit(MeasurementUnit.CUP);
		
		assertEquals(FoodstuffCategory.findOrCreate("OTHER"), food.getCategory());
		assertEquals("Food", food.getName());
		assertEquals(new PositiveDecimalNumber(2.5), food.getAmount());
    assertEquals(MeasurementUnit.CUP, food.getUnit());
	}
}
