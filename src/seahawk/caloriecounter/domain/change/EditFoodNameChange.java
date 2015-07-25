package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.ChangeNotification;
import seahawk.caloriecounter.domain.api.Food;
import seahawk.caloriecounter.domain.impl.FoodImpl;
import seahawk.caloriecounter.domain.impl.FoodStoreImpl;
import seahawk.caloriecounter.domain.api.FoodStore;

public class EditFoodNameChange extends Change {
  private FoodStoreImpl foodStore;
  private FoodImpl food;
  private String oldName;
  private String newName;

  public EditFoodNameChange(FoodStore foodStore, Food food, String newName) {
    this.foodStore = (FoodStoreImpl) foodStore;
    this.food = (FoodImpl) food;
    this.oldName = food.getName();
    this.newName = newName;
  }

  @Override
  boolean applyChange() {
    if (!foodStore.containsFood(newName) && foodStore.removeFood(food)) {
      food.setName(newName);
      return foodStore.addFood(food);
    }
    return false;
  }

  @Override
  void undo() {
    foodStore.removeFood(food);
    food.setName(oldName);
    foodStore.addFood(food);
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
    return "Rename Food '" + oldName + "' to '" + newName + "'";
  }
}
