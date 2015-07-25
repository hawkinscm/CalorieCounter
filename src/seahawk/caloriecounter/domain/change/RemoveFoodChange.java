package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.api.date.Date;
import seahawk.caloriecounter.domain.impl.FoodStoreImpl;

public class RemoveFoodChange extends Change {
  private DailyRecordStore dailyRecordStore;
  private FoodStoreImpl foodStore;
  private Food food;

  public RemoveFoodChange(DailyRecordStore dailyRecordStore, FoodStore foodStore, Food food) {
    this.dailyRecordStore = dailyRecordStore;
    this.foodStore = (FoodStoreImpl) foodStore;
    this.food = food;
  }

  @Override
  boolean applyChange() {
    for (Date date : dailyRecordStore.getDates()) {
      if (dailyRecordStore.findRecord(date) != null)
        for (Ingredient eatenFood : dailyRecordStore.findRecord(date).getEatenFoods())
          if (eatenFood.getFood().equals(food))
            return false;
    }

    for (Meal meal : foodStore.getMeals(FoodSortType.NAME, false)) {
      for (Ingredient ingredient : meal.getIngredients())
        if (ingredient.getFood().equals(food))
          return false;
    }

    return foodStore.removeFood(food);
  }

  @Override
  void undo() {
    foodStore.addFood(food);
  }

  @Override
  public ChangeNotification getChangeNotification() {
    return new ChangeNotification(food, ChangeActionType.REMOVE);
  }

  @Override
  public ChangeNotification getUndoChangeNotification() {
    return new ChangeNotification(food, ChangeActionType.ADD);
  }

  @Override
  public String getDescription() {
    return "Delete '" + food + "'";
  }
}
