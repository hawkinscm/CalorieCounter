package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.persistence.CalorieCounterPersistenceHandler;
import junit.framework.TestCase;
import seahawk.xmlhandler.XmlException;

import java.util.Iterator;
import java.util.TreeSet;

public class TestNutritionManager extends TestCase {
	public void testConstructor() throws XmlException {
		NutritionManager manager = new NutritionManager(new CalorieCounterPersistenceHandler("test/data/"));
    manager.loadData();
		assertEquals(manager.getFoodStore().getFoodNames().size(), 0);
	}
	
	public void testMeal() throws XmlException {
		NutritionManager manager = new NutritionManager(new CalorieCounterPersistenceHandler("test/data/"));
    manager.loadData();

		assertTrue(manager.addMeal("dinner"));
		assertTrue(manager.addMeal("lunch"));
		assertTrue(manager.addMeal("breakfast"));
		assertFalse(manager.addMeal("dinner"));
		
		assertFalse(manager.changeFoodName(manager.getMeal("breakfast"), "lunch"));
		assertTrue(manager.changeFoodName(manager.getMeal("dinner"), "supper"));
		
		FoodstuffImpl egg = new FoodstuffImpl("Egg", FoodstuffCategory.findOrCreate("Meat"));
		FoodstuffImpl cheese = new FoodstuffImpl("Cheese", FoodstuffCategory.findOrCreate("Dairy"));
		manager.addIngredient(manager.getMeal("breakfast"), egg);
		manager.addIngredient(manager.getMeal("breakfast"), cheese);

		TreeSet<Meal> meals = manager.getFoodStore().getMeals(FoodSortType.NAME, false);
    assertEquals(3, meals.size());
    assertNotNull(manager.getMeal("supper"));
    assertNotNull(manager.getMeal("lunch"));
    assertNotNull(manager.getMeal("breakfast"));
		
		manager.removeIngredient(manager.getMeal("breakfast"), manager.getMeal("breakfast").getIngredients().get(0));
		manager.removeFood(manager.getMeal("lunch"));

    meals = manager.getFoodStore().getMeals(FoodSortType.NAME, false);
    assertEquals(2, meals.size());
    assertNotNull(manager.getMeal("supper"));
    assertNull(manager.getMeal("lunch"));
    assertNotNull(manager.getMeal("breakfast"));
	}
	
	public void testFoodstuff() throws XmlException {
		NutritionManager manager = new NutritionManager(new CalorieCounterPersistenceHandler("test/data/"));
    manager.loadData();

		assertTrue(manager.addFoodstuff("ham", FoodstuffCategory.findOrCreate("Meat")));
		assertTrue(manager.addFoodstuff("bacon", FoodstuffCategory.findOrCreate("Meat")));
		assertTrue(manager.addFoodstuff("butter", FoodstuffCategory.findOrCreate("Dairy")));
		assertTrue(manager.addFoodstuff("apple", FoodstuffCategory.findOrCreate("Fruit")));
		assertFalse(manager.addFoodstuff("ham", FoodstuffCategory.findOrCreate("Meat")));
    assertFalse(manager.addFoodstuff("ham", FoodstuffCategory.findOrCreate("MEAT")));
    assertFalse(manager.addFoodstuff("ham", FoodstuffCategory.findOrCreate("Dairy")));

    ((NutritionFactsImpl) manager.getFoodstuff("ham").getFacts()).setAmount(NutrientType.FAT_CALORIES, new PositiveDecimalNumber(500));
		assertEquals(new PositiveDecimalNumber(500), manager.getFoodstuff("ham").getFacts().getAmount(NutrientType.FAT_CALORIES));

		assertFalse(manager.changeFoodName(manager.getFoodstuff("ham"), "bacon"));
		assertTrue(manager.changeFoodName(manager.getFoodstuff("ham"), "pork"));
		
		manager.setFoodAmount(manager.getFoodstuff("butter"), new PositiveDecimalNumber(2));
		manager.setFoodUnit(manager.getFoodstuff("butter"), MeasurementUnit.STICK_BUTTER);
		
		manager.removeFood(manager.getFoodstuff("bacon"));
		
		Iterator<Foodstuff> foodIterator = manager.getFoodStore().getFoodstuffs(FoodSortType.NAME, false).iterator();
    assertEquals(manager.getFoodstuff("apple"), foodIterator.next());
		Foodstuff dairy = foodIterator.next();
		assertEquals(new PositiveDecimalNumber(2), dairy.getAmount());
    assertEquals(MeasurementUnit.STICK_BUTTER, dairy.getUnit());
    assertEquals(manager.getFoodstuff("pork"), foodIterator.next());
    assertFalse(foodIterator.hasNext());

		manager.removeFood(manager.getFoodstuff("butter"));

    manager.changeCategoryName(FoodstuffCategory.findOrCreate("MEAT"), "ANIMAL");
    foodIterator = manager.getFoodStore().getFoodstuffs(FoodSortType.NAME, false).iterator();
    assertEquals(manager.getFoodstuff("apple"), foodIterator.next());
    assertEquals(manager.getFoodstuff("pork"), foodIterator.next());
    assertFalse(foodIterator.hasNext());

    foodIterator = manager.getFoodStore().getFoodstuffs(FoodSortType.FOODSTUFF_CATEGORY, false).iterator();
    assertEquals(manager.getFoodstuff("pork"), foodIterator.next());
    assertEquals(manager.getFoodstuff("apple"), foodIterator.next());
    assertFalse(foodIterator.hasNext());

    foodIterator = manager.getFoodStore().getFoodstuffs(FoodSortType.FOODSTUFF_CATEGORY, true).iterator();
    assertEquals(manager.getFoodstuff("apple"), foodIterator.next());
    assertEquals(manager.getFoodstuff("pork"), foodIterator.next());
    assertFalse(foodIterator.hasNext());
	}
	
	public void testCategory() throws XmlException {
		NutritionManager manager = new NutritionManager(new CalorieCounterPersistenceHandler("test/data/"));
    manager.loadData();

		assertTrue(manager.addFoodstuff("ham", FoodstuffCategory.findOrCreate("Meat")));
		assertTrue(manager.addFoodstuff("bacon", FoodstuffCategory.findOrCreate("Dairy")));
		assertTrue(manager.addFoodstuff("butter", FoodstuffCategory.findOrCreate("Fruit")));
		assertTrue(manager.addFoodstuff("apple", FoodstuffCategory.findOrCreate("Psuedo")));
		
		assertFalse(manager.addFoodstuff("apple", FoodstuffCategory.findOrCreate("Dairy")));
		manager.setFoodstuffCategory(manager.getFoodstuff("apple"), FoodstuffCategory.findOrCreate("Dairy"));
    assertEquals(FoodstuffCategory.findOrCreate("Dairy"), manager.getFoodstuff("apple").getCategory());
		
		manager.changeCategoryName(FoodstuffCategory.findOrCreate("Meat"), "Protein");
    assertEquals(FoodstuffCategory.findOrCreate("Protein"), manager.getFoodstuff("ham").getCategory());
	}
}
