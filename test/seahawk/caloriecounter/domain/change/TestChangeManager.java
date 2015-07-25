package seahawk.caloriecounter.domain.change;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.api.date.Date;
import seahawk.caloriecounter.domain.impl.*;

import java.util.HashMap;

public class TestChangeManager extends TestCase {
  private DailyRecordStore emptyDailyRecordStore;

  @Before
  public void setUp() {
    emptyDailyRecordStore = new DailyRecordStoreImpl(new DailyRecordHistoryImpl(new HashMap<Date, NutritionFacts>()));
  }

  @After
  public void tearDown() {
    emptyDailyRecordStore = null;
  }

  public void testChangeManager_foodstuffChanges() {
    ChangeManager manager = new ChangeManager();

    FoodStore foodStore = new FoodStoreImpl();
    Change change = new CreateFoodChange(foodStore, "foodstuff", FoodstuffCategory.findOrCreate("CATEGORY"));
    manager.applyChange(change);
    assertTrue(foodStore.containsFood("foodstuff"));
    assertTrue(manager.foodstuffsChanged());

    manager.undo();
    assertFalse(foodStore.containsFood("foodstuff"));
    assertFalse(manager.foodstuffsChanged());
    manager.redo();
    assertTrue(foodStore.containsFood("foodstuff"));
    manager.markStateSaved();

    String newCategoryName = "NEW CATEGORY";
    change = new EditCategoryGroupChange(foodStore, FoodstuffCategory.findOrCreate("CATEGORY"), newCategoryName);
    manager.applyChange(change);
    assertEquals(FoodstuffCategory.findOrCreate(newCategoryName), foodStore.getFoodstuff("foodstuff").getCategory());
    assertTrue(manager.foodstuffsChanged());

    assertEquals(1, FoodstuffCategory.values().size());
    assertEquals("NEW CATEGORY", FoodstuffCategory.values().iterator().next().toString());
    manager.undo();
    assertEquals(FoodstuffCategory.findOrCreate("CATEGORY"), foodStore.getFoodstuff("foodstuff").getCategory());
    assertFalse(manager.foodstuffsChanged());
    assertEquals(1, FoodstuffCategory.values().size());
    assertEquals("CATEGORY", FoodstuffCategory.values().iterator().next().toString());
    manager.redo();
    assertEquals(FoodstuffCategory.findOrCreate(newCategoryName), foodStore.getFoodstuff("foodstuff").getCategory());
    manager.markStateSaved();

    Food food = foodStore.getFood("foodstuff");
    PositiveDecimalNumber newAmount = new PositiveDecimalNumber(2);
    change = new EditFoodAmountChange(food, newAmount);
    manager.applyChange(change);
    assertEquals(newAmount, food.getAmount());
    assertTrue(manager.foodstuffsChanged());

    manager.undo();
    assertEquals(new PositiveDecimalNumber(1), food.getAmount());
    assertFalse(manager.foodstuffsChanged());
    manager.redo();
    assertEquals(newAmount, food.getAmount());
    manager.markStateSaved();

    String newName = "new foodstuff";
    change = new EditFoodNameChange(foodStore, food, newName);
    manager.applyChange(change);
    assertEquals(newName, food.getName());
    assertTrue(manager.foodstuffsChanged());

    manager.undo();
    assertEquals("foodstuff", food.getName());
    assertFalse(manager.foodstuffsChanged());
    manager.redo();
    assertEquals(newName, food.getName());
    manager.undo();

    FoodstuffCategory category = FoodstuffCategory.findOrCreate(newCategoryName);
    FoodstuffCategory newCategory = FoodstuffCategory.findOrCreate("CATEGORY");
    change = new EditFoodstuffCategoryChange((Foodstuff) food, newCategory);
    manager.applyChange(change);
    assertEquals(newCategory, ((Foodstuff) food).getCategory());
    assertTrue(manager.foodstuffsChanged());

    manager.undo();
    assertEquals(category, ((Foodstuff) food).getCategory());
    assertFalse(manager.foodstuffsChanged());
    manager.redo();
    assertEquals(newCategory, ((Foodstuff) food).getCategory());
    manager.markStateSaved();

    change = new EditFoodstuffFactAmountChange((Foodstuff) food, NutrientType.CALORIES, newAmount);
    manager.applyChange(change);
    assertEquals(newAmount, food.getFacts().getAmount(NutrientType.CALORIES));
    assertTrue(manager.foodstuffsChanged());

    manager.undo();
    assertEquals(new PositiveDecimalNumber(0), food.getFacts().getAmount(NutrientType.CALORIES));
    assertFalse(manager.foodstuffsChanged());
    manager.redo();
    assertEquals(newAmount, food.getFacts().getAmount(NutrientType.CALORIES));
    manager.markStateSaved();

    change = new EditFoodUnitChange(emptyDailyRecordStore, foodStore, food, MeasurementUnit.CUP);
    manager.applyChange(change);
    assertEquals(MeasurementUnit.CUP, food.getUnit());
    assertTrue(manager.foodstuffsChanged());

    manager.undo();
    assertEquals(MeasurementUnit.SERVING_SIZE, food.getUnit());
    assertFalse(manager.foodstuffsChanged());
    manager.redo();
    assertEquals(MeasurementUnit.CUP, food.getUnit());
    manager.markStateSaved();

    assertTrue(foodStore.containsFood("foodstuff"));
    change = new RemoveFoodChange(emptyDailyRecordStore, foodStore, food);
    manager.applyChange(change);
    assertFalse(foodStore.containsFood("foodstuff"));
    assertTrue(manager.foodstuffsChanged());

    manager.undo();
    assertTrue(foodStore.containsFood("foodstuff"));
    assertFalse(manager.foodstuffsChanged());
    manager.redo();
    assertFalse(foodStore.containsFood("foodstuff"));
    manager.markStateSaved();
  }

  public void testChangeManager_mealChanges() {
    ChangeManager manager = new ChangeManager();

    FoodStore foodStore = new FoodStoreImpl();
    Change change = new CreateFoodChange(foodStore, "meal");
    manager.applyChange(change);
    assertTrue(foodStore.containsFood("meal"));
    assertTrue(manager.mealsChanged());

    manager.undo();
    assertFalse(foodStore.containsFood("meal"));
    assertFalse(manager.mealsChanged());
    manager.redo();
    assertTrue(foodStore.containsFood("meal"));
    manager.markStateSaved();

    Meal meal = foodStore.getMeal("meal");
    PositiveDecimalNumber newAmount = new PositiveDecimalNumber(3);
    change = new EditFoodAmountChange(meal, newAmount);
    manager.applyChange(change);
    assertEquals(newAmount, meal.getAmount());
    assertTrue(manager.mealsChanged());

    manager.undo();
    assertEquals(new PositiveDecimalNumber(1), meal.getAmount());
    assertFalse(manager.mealsChanged());
    manager.redo();
    assertEquals(newAmount, meal.getAmount());
    manager.markStateSaved();

    String newName = "new meal";
    change = new EditFoodNameChange(foodStore, meal, newName);
    manager.applyChange(change);
    assertEquals(newName, meal.getName());
    assertTrue(manager.mealsChanged());

    manager.undo();
    assertEquals("meal", meal.getName());
    assertFalse(manager.mealsChanged());
    manager.redo();
    assertEquals(newName, meal.getName());
    manager.undo();

    change = new EditFoodUnitChange(emptyDailyRecordStore, foodStore, meal, MeasurementUnit.GRAM);
    manager.applyChange(change);
    assertEquals(MeasurementUnit.GRAM, meal.getUnit());
    assertTrue(manager.mealsChanged());

    manager.undo();
    assertEquals(MeasurementUnit.SERVING_SIZE, meal.getUnit());
    assertFalse(manager.mealsChanged());
    manager.redo();
    assertEquals(MeasurementUnit.GRAM, meal.getUnit());
    manager.markStateSaved();

    Food ingredient = new FoodstuffImpl("foodstuff", FoodstuffCategory.findOrCreate("CATEGORY"));
    change = new AddMealIngredientChange(meal, ingredient);
    manager.applyChange(change);
    assertEquals(1, meal.getIngredients().size());
    assertTrue(manager.mealsChanged());

    manager.undo();
    assertEquals(0, meal.getIngredients().size());
    assertFalse(manager.mealsChanged());
    manager.redo();
    assertEquals(1, meal.getIngredients().size());
    manager.markStateSaved();

    change = new EditMealIngredientAmountChange(meal, meal.getIngredients().get(0), newAmount);
    manager.applyChange(change);
    assertEquals(newAmount, meal.getIngredients().get(0).getAmount());
    assertTrue(manager.mealsChanged());

    manager.undo();
    assertEquals(new PositiveDecimalNumber(1), meal.getIngredients().get(0).getAmount());
    assertFalse(manager.mealsChanged());
    manager.redo();
    assertEquals(newAmount, meal.getIngredients().get(0).getAmount());
    manager.markStateSaved();

    change = new EditMealIngredientUnitChange(meal, meal.getIngredients().get(0), MeasurementUnit.MILLILITER);
    manager.applyChange(change);
    assertEquals(MeasurementUnit.MILLILITER, meal.getIngredients().get(0).getUnit());
    assertTrue(manager.mealsChanged());

    manager.undo();
    assertEquals(MeasurementUnit.SERVING_SIZE, meal.getIngredients().get(0).getUnit());
    assertFalse(manager.mealsChanged());
    manager.redo();
    assertEquals(MeasurementUnit.MILLILITER, meal.getIngredients().get(0).getUnit());
    manager.markStateSaved();

    change = new RemoveMealIngredientChange(meal, meal.getIngredients().get(0));
    manager.applyChange(change);
    assertEquals(0, meal.getIngredients().size());
    assertTrue(manager.mealsChanged());

    manager.undo();
    assertEquals(1, meal.getIngredients().size());
    assertFalse(manager.mealsChanged());
    manager.redo();
    assertEquals(0, meal.getIngredients().size());
    manager.markStateSaved();

    assertTrue(foodStore.containsFood("meal"));
    change = new RemoveFoodChange(emptyDailyRecordStore, foodStore, meal);
    manager.applyChange(change);
    assertFalse(foodStore.containsFood("meal"));
    assertTrue(manager.mealsChanged());

    manager.undo();
    assertTrue(foodStore.containsFood("meal"));
    assertFalse(manager.mealsChanged());
    manager.redo();
    assertFalse(foodStore.containsFood("meal"));
    manager.markStateSaved();
  }

  public void testChangeManager_dailyRecordChanges() {
    ChangeManager manager = new ChangeManager();

    DailyRecordStore recordStore = emptyDailyRecordStore;
    Date date = new Date("20140202");
    DailyRecord record = new DailyRecordImpl(date);
    ((DailyRecordStoreImpl) recordStore).add(record);
    assertTrue(recordStore.getDates().contains(date));
    assertFalse(manager.dailyRecordsChanged());

    Food food = new MealImpl("meal");
    Change change = new AddDailyRecordFoodChange(record, food);
    manager.applyChange(change);
    assertEquals(1, record.getEatenFoods().size());
    assertTrue(manager.dailyRecordsChanged());

    manager.undo();
    assertEquals(0, record.getEatenFoods().size());
    assertFalse(manager.dailyRecordsChanged());
    manager.redo();
    assertEquals(1, record.getEatenFoods().size());
    manager.markStateSaved();

    PositiveDecimalNumber newAmount = new PositiveDecimalNumber(0.5);
    change = new EditDailyRecordFoodAmountChange(record, record.getEatenFoods().get(0), newAmount);
    manager.applyChange(change);
    assertEquals(newAmount, record.getEatenFoods().get(0).getAmount());
    assertTrue(manager.dailyRecordsChanged());

    manager.undo();
    assertEquals(new PositiveDecimalNumber(1), record.getEatenFoods().get(0).getAmount());
    assertFalse(manager.mealsChanged());
    manager.redo();
    assertEquals(newAmount, record.getEatenFoods().get(0).getAmount());
    manager.markStateSaved();

    change = new EditDailyRecordFoodUnitChange(record, record.getEatenFoods().get(0), MeasurementUnit.PINT);
    manager.applyChange(change);
    assertEquals(MeasurementUnit.PINT, record.getEatenFoods().get(0).getUnit());
    assertTrue(manager.dailyRecordsChanged());

    manager.undo();
    assertEquals(MeasurementUnit.SERVING_SIZE, record.getEatenFoods().get(0).getUnit());
    assertFalse(manager.dailyRecordsChanged());
    manager.redo();
    assertEquals(MeasurementUnit.PINT, record.getEatenFoods().get(0).getUnit());
    manager.markStateSaved();

    change = new RemoveDailyRecordFoodChange(record, record.getEatenFoods().get(0));
    manager.applyChange(change);
    assertEquals(0, record.getEatenFoods().size());
    assertTrue(manager.dailyRecordsChanged());

    manager.undo();
    assertEquals(1, record.getEatenFoods().size());
    assertFalse(manager.dailyRecordsChanged());
    manager.redo();
    assertEquals(0, record.getEatenFoods().size());
    manager.markStateSaved();
  }

  public void testChangeManager_undo_redo_savedChanges() {
    ChangeManager manager = new ChangeManager();
    assertFalse(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertFalse(manager.dataChanged());

    FoodStore foodStore = new FoodStoreImpl();
    Change change = new CreateFoodChange(foodStore, "foodstuff", FoodstuffCategory.findOrCreate("CATEGORY"));
    assertTrue(manager.applyChange(change));
    assertTrue(foodStore.containsFood("foodstuff"));
    assertTrue(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    manager.undo();
    assertFalse(foodStore.containsFood("foodstuff"));
    assertFalse(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertFalse(manager.dataChanged());

    manager.redo();
    assertTrue(foodStore.containsFood("foodstuff"));
    assertTrue(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    manager.markStateSaved();
    assertTrue(foodStore.containsFood("foodstuff"));
    assertFalse(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertFalse(manager.dataChanged());

    manager.undo();
    assertFalse(foodStore.containsFood("foodstuff"));
    assertTrue(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    manager.redo();
    assertTrue(foodStore.containsFood("foodstuff"));
    assertFalse(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertFalse(manager.dataChanged());

    manager.undo();
    change = new CreateFoodChange(foodStore, "foodstuff", FoodstuffCategory.findOrCreate("CATEGORY"));
    assertTrue(manager.applyChange(change));
    assertTrue(foodStore.containsFood("foodstuff"));
    assertTrue(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    change = new CreateFoodChange(foodStore, "meal");
    assertTrue(manager.applyChange(change));
    assertTrue(foodStore.containsFood("meal"));
    assertTrue(manager.foodstuffsChanged());
    assertTrue(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    DailyRecordStore recordStore = emptyDailyRecordStore;
    Date date = new Date("20140101");
    DailyRecord record = new DailyRecordImpl(date);
    ((DailyRecordStoreImpl) recordStore).add(record);
    change = new AddDailyRecordFoodChange(record, foodStore.getFood("meal"));
    assertTrue(manager.applyChange(change));
    assertTrue(record.getEatenFoods().get(0).getFood().equals(foodStore.getFood("meal")));
    assertTrue(manager.foodstuffsChanged());
    assertTrue(manager.mealsChanged());
    assertTrue(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    manager.markStateSaved();
    assertFalse(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertFalse(manager.dataChanged());

    manager.undo();
    assertFalse(record.getEatenFoods().size() > 0);
    assertFalse(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertTrue(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    manager.undo();
    assertFalse(foodStore.containsFood("meal"));
    assertFalse(manager.foodstuffsChanged());
    assertTrue(manager.mealsChanged());
    assertTrue(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    change = new CreateFoodChange(foodStore, "meal");
    assertTrue(manager.applyChange(change));
    assertTrue(foodStore.containsFood("meal"));
    assertFalse(manager.foodstuffsChanged());
    assertTrue(manager.mealsChanged());
    assertTrue(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    manager.redo();
    assertFalse(record.getEatenFoods().size() > 0);
    assertFalse(manager.foodstuffsChanged());
    assertTrue(manager.mealsChanged());
    assertTrue(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    manager.markStateSaved();
    assertFalse(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertFalse(manager.dataChanged());

    manager.undo();
    assertFalse(foodStore.containsFood("meal"));
    assertFalse(manager.foodstuffsChanged());
    assertTrue(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    manager.undo();
    assertFalse(foodStore.containsFood("foodstuff"));
    assertTrue(manager.foodstuffsChanged());
    assertTrue(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertTrue(manager.dataChanged());

    manager.markStateSaved();
    manager.undo();
    assertFalse(foodStore.containsFood("foodstuff"));
    assertFalse(manager.foodstuffsChanged());
    assertFalse(manager.mealsChanged());
    assertFalse(manager.dailyRecordsChanged());
    assertFalse(manager.dataChanged());
  }
}
