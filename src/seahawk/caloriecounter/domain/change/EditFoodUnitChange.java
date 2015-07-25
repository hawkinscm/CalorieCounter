package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.api.date.Date;
import seahawk.caloriecounter.domain.impl.FoodImpl;
import seahawk.caloriecounter.domain.impl.IngredientImpl;

public class EditFoodUnitChange extends Change {
  private DailyRecordStore dailyRecordStore;
  private FoodStore foodStore;
  private FoodImpl food;
  private MeasurementUnit oldUnit;
  private MeasurementUnit newUnit;

  public EditFoodUnitChange(DailyRecordStore dailyRecordStore, FoodStore foodstore, Food food, MeasurementUnit newUnit) {
    this.foodStore = foodstore;
    this.dailyRecordStore = dailyRecordStore;
    this.food = (FoodImpl) food;
    this.oldUnit = food.getUnit();
    this.newUnit = newUnit;
  }

  @Override
  boolean applyChange() {
    if (oldUnit == newUnit)
      return false;

    food.setUnit(newUnit);

    updateIngredientsContainingThisFood(oldUnit);

    return true;
  }

  @Override
  void undo() {
    food.setUnit(oldUnit);
    updateIngredientsContainingThisFood(newUnit);
  }

  private void updateIngredientsContainingThisFood(MeasurementUnit previousUnit) {
    for (Meal meal : foodStore.getMeals(FoodSortType.NAME, false)) {
      if (meal.equals(food))
        continue;

      for (Ingredient ingredient : meal.getIngredients()) {
        if (ingredient.getFood().equals(food)) {
          ((IngredientImpl) ingredient).updateOnFoodUnitChange(previousUnit);
        }
      }
    }

    for (Date date : dailyRecordStore.getDates()) {
      DailyRecord dailyRecord = dailyRecordStore.findRecord(date);
      if (dailyRecord == null)
        continue;

      for (Ingredient ingredient : dailyRecord.getEatenFoods()) {
        if (ingredient.getFood().equals(food)) {
          ((IngredientImpl) ingredient).updateOnFoodUnitChange(previousUnit);
        }
      }
    }
  }

  @Override
  public ChangeNotification getChangeNotification() {
    return new ChangeNotification(food, ChangeActionType.EDIT);
  }

  @Override
  public ChangeNotification getUndoChangeNotification() {
    return new ChangeNotification(food, ChangeActionType.EDIT);
  }

  @Override
  public String getDescription() {
    return "Edit Unit on '" + food + "'";
  }
}
