package seahawk.caloriecounter.domain.impl;

import junit.framework.TestCase;
import seahawk.caloriecounter.domain.api.*;

import java.util.List;

public class TestMeal extends TestCase {
	public void testConstructor() {
		MealImpl meal = new MealImpl("dinner");
		assertEquals("dinner", meal.getName());
    assertEquals(0, meal.getIngredients().size());
		assertNotNull(meal.getFacts());
	}
	
	public void testEquals() {
		MealImpl meal1 = new MealImpl("dinner");
		MealImpl meal2 = new MealImpl("lunch");
		assertFalse(meal1.equals(meal2));
		meal2.setName("dinner");
    assertEquals(meal1, meal2);
	}
	
	public void testAddRemoveIngredient() {
		MealImpl meal = new MealImpl("dinner");
		FoodstuffImpl ham = new FoodstuffImpl("ham", FoodstuffCategory.findOrCreate("meat"));
    ((NutritionFactsImpl) ham.getFacts()).setAmount(NutrientType.CALORIES, new PositiveDecimalNumber("300"));
    ((NutritionFactsImpl) ham.getFacts()).setAmount(NutrientType.SODIUM, new PositiveDecimalNumber("10"));
		FoodstuffImpl salt = new FoodstuffImpl("salt", FoodstuffCategory.findOrCreate("spice"));
    ((NutritionFactsImpl) salt.getFacts()).setAmount(NutrientType.SODIUM, new PositiveDecimalNumber("200"));
		
		IngredientImpl iham = new IngredientImpl(ham);
		iham.setAmount(new PositiveDecimalNumber("2"));
		iham.setUnit(MeasurementUnit.SERVING_SIZE);
		IngredientImpl isalt = new IngredientImpl(salt);

		meal.addIngredient(iham);
		meal.addIngredient(isalt);
		
		List<Ingredient> ingredients = meal.getIngredients();
    assertEquals(2, ingredients.size());
    assertEquals(iham, ingredients.get(0));
    assertEquals(isalt, ingredients.get(1));
		
		assertEquals(new PositiveDecimalNumber("600"), meal.getFacts().getAmount(NutrientType.CALORIES));
		assertEquals(new PositiveDecimalNumber("220"), meal.getFacts().getAmount(NutrientType.SODIUM));
		
		meal.removeIngredient(iham);
    ingredients = meal.getIngredients();
    assertEquals(1, ingredients.size());
    assertEquals(isalt, ingredients.get(0));
		
		assertEquals(new PositiveDecimalNumber(0), meal.getFacts().getAmount(NutrientType.CALORIES));
		assertEquals(new PositiveDecimalNumber(200), meal.getFacts().getAmount(NutrientType.SODIUM));
		
		meal.removeIngredient(isalt);
    ingredients = meal.getIngredients();
    assertEquals(0, ingredients.size());
		assertEquals(new PositiveDecimalNumber(0), meal.getFacts().getAmount(NutrientType.SODIUM));
	}
}
