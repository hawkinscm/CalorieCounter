package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.ChangeNotification;
import seahawk.caloriecounter.domain.api.Foodstuff;
import seahawk.caloriecounter.domain.api.FoodstuffCategory;
import seahawk.caloriecounter.domain.impl.FoodstuffImpl;

public class EditFoodstuffCategoryChange extends Change {
  private FoodstuffImpl foodstuff;
  private FoodstuffCategory oldCategory;
  private FoodstuffCategory newCategory;

  public EditFoodstuffCategoryChange(Foodstuff foodstuff, FoodstuffCategory newCategory) {
    this.foodstuff = (FoodstuffImpl) foodstuff;
    this.oldCategory = foodstuff.getCategory();
    this.newCategory = newCategory;
  }

  @Override
  boolean applyChange() {
    if (oldCategory == newCategory)
      return false;

    foodstuff.setCategory(newCategory);
    return true;
  }

  @Override
  void undo() {
    foodstuff.setCategory(oldCategory);
  }

  @Override
  public ChangeNotification getChangeNotification() {
    return new ChangeNotification(foodstuff, ChangeActionType.EDIT);
  }

  @Override
  public ChangeNotification getUndoChangeNotification() {
    return new ChangeNotification(foodstuff, ChangeActionType.EDIT);
  }

  @Override
  public String getDescription() {
    return "Edit Category on '" + foodstuff + "'";
  }
}
