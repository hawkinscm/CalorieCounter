package seahawk.caloriecounter.domain.impl;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import seahawk.caloriecounter.domain.api.*;

public class TestIngredient extends TestCase {
	private FoodstuffImpl apple;
	private FoodstuffImpl sugar;
	private FoodstuffImpl bread;
	private FoodstuffImpl ketchup;

  @Before
	public void setUp() {
		apple = new FoodstuffImpl("Apple", FoodstuffCategory.findOrCreate("Fruit"));
		apple.setUnit(MeasurementUnit.OUNCE);
		apple.setAmount(new PositiveDecimalNumber("4"));
		NutritionFactsImpl appleFacts = (NutritionFactsImpl) apple.getFacts();
		appleFacts.setAmount(NutrientType.CALORIES, new PositiveDecimalNumber("50"));
		appleFacts.setAmount(NutrientType.CARBOHYDRATES, new PositiveDecimalNumber("2"));
		appleFacts.setAmount(NutrientType.SUGARS, new PositiveDecimalNumber("1"));
		
		sugar = new FoodstuffImpl("Sugar", FoodstuffCategory.findOrCreate("Spice"));
		sugar.setUnit(MeasurementUnit.TEASPOON);
		NutritionFactsImpl sugarFacts = (NutritionFactsImpl) sugar.getFacts();
		sugarFacts.setAmount(NutrientType.CALORIES, new PositiveDecimalNumber("25"));
		sugarFacts.setAmount(NutrientType.CARBOHYDRATES, new PositiveDecimalNumber("1"));
		
		bread = new FoodstuffImpl("Bread", FoodstuffCategory.findOrCreate("Grain"));
		bread.setUnit(MeasurementUnit.OUNCE);
		bread.setAmount(new PositiveDecimalNumber("8"));
		NutritionFactsImpl breadFacts = (NutritionFactsImpl) bread.getFacts();
		breadFacts.setAmount(NutrientType.CALORIES, new PositiveDecimalNumber("100"));
		breadFacts.setAmount(NutrientType.TOTAL_FAT, new PositiveDecimalNumber("20"));
		breadFacts.setAmount(NutrientType.CARBOHYDRATES, new PositiveDecimalNumber("5"));
		breadFacts.setAmount(NutrientType.PROTEIN, new PositiveDecimalNumber("1"));
		breadFacts.setAmount(NutrientType.FAT_CALORIES, new PositiveDecimalNumber("2"));
		breadFacts.setAmount(NutrientType.SATURATED_FAT, new PositiveDecimalNumber("20"));
		breadFacts.setAmount(NutrientType.SUGARS, new PositiveDecimalNumber("3"));
		breadFacts.setAmount(NutrientType.CHOLESTEROL, new PositiveDecimalNumber("80"));
		breadFacts.setAmount(NutrientType.SODIUM, new PositiveDecimalNumber("0.5"));
		
		ketchup = new FoodstuffImpl("Ketchup", FoodstuffCategory.findOrCreate("Spice"));
		NutritionFactsImpl ketchupFacts = (NutritionFactsImpl) ketchup.getFacts();
		ketchupFacts.setAmount(NutrientType.CALORIES, new PositiveDecimalNumber("5"));
		ketchupFacts.setAmount(NutrientType.CARBOHYDRATES, new PositiveDecimalNumber("2.5"));
		ketchupFacts.setAmount(NutrientType.SATURATED_FAT, new PositiveDecimalNumber("4.22"));
		ketchupFacts.setAmount(NutrientType.SUGARS, new PositiveDecimalNumber(".4"));
	}

  @After
  public void tearDown() {
    apple = null;
    sugar = null;
    bread = null;
    ketchup = null;
  }
	
	public void testConstructor() {
		IngredientImpl iapple = new IngredientImpl(apple);
		assertTrue(iapple.getFacts() != null);
    assertEquals("FRUIT", ((FoodstuffImpl) iapple.getFood()).getCategory().toString());
    assertEquals("Apple", iapple.getFood().getName());
		assertEquals(iapple.getAmount().toString(), "4.0");
		assertTrue(iapple.getUnit() == MeasurementUnit.OUNCE);
		
		FoodstuffImpl salt = new FoodstuffImpl("Salt", FoodstuffCategory.findOrCreate("Spice"));
		salt.setUnit(MeasurementUnit.findOrCreate("TEST UNIT"));
		IngredientImpl isalt = new IngredientImpl(salt);
    assertEquals(MeasurementUnit.findOrCreate("TEST UNIT"), isalt.getUnit());
    salt.setUnit(MeasurementUnit.findOrCreate("Dash"));
    assertEquals("DASH", isalt.getFood().getUnit().toString());
	}
	
	public void testSetValues() {
		IngredientImpl iapple = new IngredientImpl(apple);
		iapple.setAmount(new PositiveDecimalNumber("2.999"));
		iapple.setUnit(MeasurementUnit.GRAM);
		
		assertEquals(iapple.getAmount().toString(), "3.0");
		assertEquals(iapple.getUnit(), MeasurementUnit.GRAM);
	}
	
	public void testGetFacts() {
		IngredientImpl iapple = new IngredientImpl(apple);
		iapple.setAmount(new PositiveDecimalNumber("2"));
		
		NutritionFactsImpl facts = (NutritionFactsImpl) iapple.getFacts();
		assertEquals(new PositiveDecimalNumber(25), facts.getAmount(NutrientType.CALORIES));
		assertEquals(new PositiveDecimalNumber(1), facts.getAmount(NutrientType.CARBOHYDRATES));
		assertEquals(new PositiveDecimalNumber(0.5), facts.getAmount(NutrientType.SUGARS));
		
		IngredientImpl isugar = new IngredientImpl(sugar);
		isugar.setUnit(MeasurementUnit.TABLESPOON);
		
		facts = (NutritionFactsImpl) isugar.getFacts();
		assertEquals(new PositiveDecimalNumber(75), facts.getAmount(NutrientType.CALORIES));
		assertEquals(new PositiveDecimalNumber(3), facts.getAmount(NutrientType.CARBOHYDRATES));
		
		IngredientImpl ibread = new IngredientImpl(bread);
		ibread.setUnit(MeasurementUnit.POUND);
		ibread.setAmount(new PositiveDecimalNumber("2"));
		
		facts = (NutritionFactsImpl) ibread.getFacts();
		assertEquals(new PositiveDecimalNumber(400), facts.getAmount(NutrientType.CALORIES));
		assertEquals(new PositiveDecimalNumber(80), facts.getAmount(NutrientType.TOTAL_FAT));
		assertEquals(new PositiveDecimalNumber(20), facts.getAmount(NutrientType.CARBOHYDRATES));
		assertEquals(new PositiveDecimalNumber(4), facts.getAmount(NutrientType.PROTEIN));
		assertEquals(new PositiveDecimalNumber(8), facts.getAmount(NutrientType.FAT_CALORIES));
		assertEquals(new PositiveDecimalNumber(80), facts.getAmount(NutrientType.SATURATED_FAT));
		assertEquals(new PositiveDecimalNumber(12), facts.getAmount(NutrientType.SUGARS));
		assertEquals(new PositiveDecimalNumber(320), facts.getAmount(NutrientType.CHOLESTEROL));
		assertEquals(new PositiveDecimalNumber(2), facts.getAmount(NutrientType.SODIUM));
		
		IngredientImpl iketchup = new IngredientImpl(ketchup);
		iketchup.setAmount(new PositiveDecimalNumber("3"));
				
		facts = (NutritionFactsImpl) iketchup.getFacts();
		assertEquals(new PositiveDecimalNumber(15), facts.getAmount(NutrientType.CALORIES));
		assertEquals(new PositiveDecimalNumber(7.5), facts.getAmount(NutrientType.CARBOHYDRATES));
		assertEquals(new PositiveDecimalNumber(12.66), facts.getAmount(NutrientType.SATURATED_FAT));
		assertEquals(new PositiveDecimalNumber(1.2), facts.getAmount(NutrientType.SUGARS));
	}

  public void testUpdateOnFoodUnitChange() {
    FoodstuffImpl food = new FoodstuffImpl("name", FoodstuffCategory.findOrCreate("FOOD"), new PositiveDecimalNumber(1), MeasurementUnit.CUP, new NutritionFactsImpl());
    IngredientImpl ingredient = new IngredientImpl(food);
    ingredient.updateOnFoodUnitChange(MeasurementUnit.CUP);
    assertEquals(MeasurementUnit.CUP, ingredient.getUnit());
    assertEquals(new PositiveDecimalNumber(1), ingredient.getAmount());

    ingredient.setUnit(MeasurementUnit.TEASPOON);
    ingredient.updateOnFoodUnitChange(MeasurementUnit.CUP);
    assertEquals(MeasurementUnit.TEASPOON, ingredient.getUnit());
    assertEquals(new PositiveDecimalNumber(1), ingredient.getAmount());

    food.setUnit(MeasurementUnit.TABLESPOON);
    ingredient.updateOnFoodUnitChange(MeasurementUnit.CUP);
    assertEquals(MeasurementUnit.TEASPOON, ingredient.getUnit());
    assertEquals(new PositiveDecimalNumber(1), ingredient.getAmount());

    food.setUnit(MeasurementUnit.GRAM);
    ingredient.updateOnFoodUnitChange(MeasurementUnit.TABLESPOON);
    assertEquals(MeasurementUnit.GRAM, ingredient.getUnit());
    assertEquals(new PositiveDecimalNumber(0.33), ingredient.getAmount());

    ingredient.setUnit(MeasurementUnit.TABLESPOON);
    food.setUnit(MeasurementUnit.POUND);
    ingredient.updateOnFoodUnitChange(MeasurementUnit.TEASPOON);
    assertEquals(MeasurementUnit.POUND, ingredient.getUnit());
    assertEquals(new PositiveDecimalNumber(1), ingredient.getAmount());

    food.setUnit(MeasurementUnit.SERVING_SIZE);
    ingredient.updateOnFoodUnitChange(MeasurementUnit.OUNCE);
    assertEquals(MeasurementUnit.SERVING_SIZE, ingredient.getUnit());
    assertEquals(new PositiveDecimalNumber(16), ingredient.getAmount());

    food.setUnit(MeasurementUnit.findOrCreate("TEMP"));
    ingredient.updateOnFoodUnitChange(MeasurementUnit.SERVING_SIZE);
    assertEquals(MeasurementUnit.findOrCreate("TEMP"), ingredient.getUnit());
    assertEquals(new PositiveDecimalNumber(16), ingredient.getAmount());
  }
}
