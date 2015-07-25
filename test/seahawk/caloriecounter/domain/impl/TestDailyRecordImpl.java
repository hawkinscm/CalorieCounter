package seahawk.caloriecounter.domain.impl;

import junit.framework.TestCase;
import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.api.date.Date;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class TestDailyRecordImpl extends TestCase {
	public void testConstructor() {
		DailyRecordImpl dailyRecord = new DailyRecordImpl(new Date(Calendar.getInstance()));
		assertEquals(0, dailyRecord.getEatenFoods().size());
	}
	
	public void testGetDate() {
		DailyRecordImpl dailyRecord = new DailyRecordImpl(new Date("20080101"), Collections.<Ingredient>emptyList());
    assertEquals("20080101", dailyRecord.getDate().toString());
    assertEquals(20080101, dailyRecord.hashCode());
	}

	public void testEquals() {
		DailyRecordImpl dailyRecord1 = new DailyRecordImpl(new Date(Calendar.getInstance()));
    assertTrue(dailyRecord1.equals(new DailyRecordImpl(new Date(Calendar.getInstance()))));
	}

	public void testCompareTo() {
		DailyRecordImpl dailyRecord1 = new DailyRecordImpl(new Date("20080102"), Collections.<Ingredient>emptyList());
		DailyRecordImpl dailyRecord2 = new DailyRecordImpl(new Date("20080102"), Collections.<Ingredient>emptyList());
		assertEquals(0, dailyRecord1.compareTo(dailyRecord2));
		dailyRecord2 = new DailyRecordImpl(new Date("20080202"), Collections.<Ingredient>emptyList());
		assertTrue(dailyRecord1.compareTo(dailyRecord2) < 0);
    dailyRecord2 = new DailyRecordImpl(new Date("20070202"), Collections.<Ingredient>emptyList());
		assertTrue(dailyRecord1.compareTo(dailyRecord2) > 0);
    dailyRecord2 = new DailyRecordImpl(new Date("20080103"), Collections.<Ingredient>emptyList());
		assertTrue(dailyRecord1.compareTo(dailyRecord2) < 0);
	}

	public void testFood() {
		DailyRecordImpl dailyRecord = new DailyRecordImpl(new Date(Calendar.getInstance()));
		Ingredient ham = new IngredientImpl(new FoodstuffImpl("ham", FoodstuffCategory.findOrCreate("meat")));
    ((NutritionFactsImpl) ham.getFood().getFacts()).setAmount(NutrientType.CALORIES, new PositiveDecimalNumber(100));
    Ingredient pork = new IngredientImpl(new FoodstuffImpl("pork", FoodstuffCategory.findOrCreate("meat")));
    ((NutritionFactsImpl) pork.getFood().getFacts()).setAmount(NutrientType.CALORIES, new PositiveDecimalNumber(50));
    Ingredient milk = new IngredientImpl(new FoodstuffImpl("milk", FoodstuffCategory.findOrCreate("dairy")));
    ((NutritionFactsImpl) milk.getFood().getFacts()).setAmount(NutrientType.CALORIES, new PositiveDecimalNumber(2));
    ((FoodImpl) milk.getFood()).setUnit(MeasurementUnit.QUART);
    Ingredient meal = new IngredientImpl(new MealImpl("snack"));
    ((FoodImpl) meal.getFood()).setUnit(MeasurementUnit.POUND);

		dailyRecord.addEatenFood(milk);
		dailyRecord.addEatenFood(ham);
		dailyRecord.addEatenFood(pork);
		dailyRecord.addEatenFood(milk);
		dailyRecord.addEatenFood(meal);

    assertEquals(milk.getFood().getName(), dailyRecord.getEatenFoods().get(0).getFood().getName());
		dailyRecord.removeEatenFood(milk);

    assertEquals(ham.getFood().getName(), dailyRecord.getEatenFoods().get(0).getFood().getName());
    assertEquals(pork.getFood().getName(), dailyRecord.getEatenFoods().get(1).getFood().getName());
    assertEquals(milk.getFood().getName(), dailyRecord.getEatenFoods().get(2).getFood().getName());
    assertEquals(meal.getFood().getName(), dailyRecord.getEatenFoods().get(3).getFood().getName());

    ((IngredientImpl) dailyRecord.getEatenFoods().get(0)).setAmount(new PositiveDecimalNumber("2.53"));
    ((IngredientImpl) dailyRecord.getEatenFoods().get(1)).setAmount(new PositiveDecimalNumber(4));
    ((IngredientImpl) dailyRecord.getEatenFoods().get(2)).setUnit(MeasurementUnit.GALLON);
    ((IngredientImpl) dailyRecord.getEatenFoods().get(3)).setUnit(MeasurementUnit.GRAM);

		List<Ingredient> eatenFoods = dailyRecord.getEatenFoods();
    assertEquals(4, eatenFoods.size());
		assertEquals(new PositiveDecimalNumber(2.53), eatenFoods.get(0).getAmount());
		assertEquals(new PositiveDecimalNumber(4.0), eatenFoods.get(1).getAmount());
    assertEquals(MeasurementUnit.GALLON, eatenFoods.get(2).getUnit());
    assertEquals(MeasurementUnit.GRAM, eatenFoods.get(3).getUnit());

    assertEquals(new PositiveDecimalNumber(253 + 200 + 8), dailyRecord.getFacts().getAmount(NutrientType.CALORIES));
	}
}
