package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.ChangeNotification;
import seahawk.caloriecounter.domain.api.Food;
import seahawk.caloriecounter.domain.impl.FoodStoreImpl;
import seahawk.caloriecounter.domain.impl.FoodstuffImpl;
import seahawk.caloriecounter.domain.api.FoodStore;
import seahawk.caloriecounter.domain.api.FoodstuffCategory;
import seahawk.caloriecounter.domain.impl.MealImpl;

public class CreateFoodChange extends Change {
  private FoodStoreImpl foodStore;
  private Food food;

  public CreateFoodChange(FoodStore foodStore, String foodName, FoodstuffCategory foodCategory) {
    this.foodStore = (FoodStoreImpl) foodStore;
    this.food = new FoodstuffImpl(foodName, foodCategory);
  }

  public CreateFoodChange(FoodStore foodStore, String mealName) {
    this.foodStore = (FoodStoreImpl) foodStore;
    this.food = new MealImpl(mealName);
  }

  public CreateFoodChange(FoodStore foodStore, Food foodToCopy, String newFoodName) {
    this.foodStore = (FoodStoreImpl) foodStore;
    if (foodToCopy.isMeal())
      this.food = new MealImpl((MealImpl) foodToCopy, newFoodName);
    else
      this.food = new FoodstuffImpl((FoodstuffImpl) foodToCopy, newFoodName);
  }

  @Override
  boolean applyChange() {
    return foodStore.addFood(food);
  }

  @Override
  void undo() {
    foodStore.removeFood(food);
  }

  @Override
  public ChangeNotification getChangeNotification() {
    return new ChangeNotification(food, ChangeActionType.ADD);
  }

  @Override
  public ChangeNotification getUndoChangeNotification() {
    return new ChangeNotification(food, ChangeActionType.REMOVE);
  }

  @Override
  public String getDescription() {
    return "Create '" + food + "'";
  }
}
