package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.impl.FoodstuffImpl;

import java.util.ArrayList;
import java.util.List;

public class EditCategoryGroupChange extends Change {
  private FoodStore foodStore;
  private String oldCategoryName;
  private String newCategoryName;

  private boolean isNewlyCreatedCategory;

  private List<FoodstuffImpl> foodstuffsChanged = new ArrayList<>();

  public EditCategoryGroupChange(FoodStore foodStore, FoodstuffCategory oldCategory, String newCategoryName) {
    this.foodStore = foodStore;
    this.oldCategoryName = oldCategory.toString();
    this.newCategoryName = newCategoryName;
  }

  @Override
  boolean applyChange() {
    FoodstuffCategory oldCategory = FoodstuffCategory.findOrCreate(oldCategoryName);

    int initialSize = FoodstuffCategory.values().size();
    FoodstuffCategory newCategory = FoodstuffCategory.findOrCreate(newCategoryName);
    isNewlyCreatedCategory = FoodstuffCategory.values().size() > initialSize;

    if (oldCategory == newCategory)
      return false;

    for (Foodstuff foodstuff : foodStore.getFoodstuffs(FoodSortType.NAME, false)) {
      if (foodstuff.getCategory() == oldCategory) {
        FoodstuffImpl oldCategoryFoodstuff = (FoodstuffImpl) foodstuff;
        oldCategoryFoodstuff.setCategory(newCategory);
        foodstuffsChanged.add(oldCategoryFoodstuff);
      }
    }

    FoodstuffCategory.remove(oldCategory);
    return true;
  }

  @Override
  void undo() {
    FoodstuffCategory oldCategory = FoodstuffCategory.findOrCreate(oldCategoryName);
    FoodstuffCategory newCategory = FoodstuffCategory.findOrCreate(newCategoryName);

    while (!foodstuffsChanged.isEmpty())
      foodstuffsChanged.remove(0).setCategory(oldCategory);

    if (isNewlyCreatedCategory)
      FoodstuffCategory.remove(newCategory);
  }

  @Override
  public ChangeNotification getChangeNotification() {
    return new ChangeNotification(new ArrayList<Foodstuff>(foodstuffsChanged));
  }

  @Override
  public ChangeNotification getUndoChangeNotification() {
    return new ChangeNotification(new ArrayList<Foodstuff>(foodstuffsChanged));
  }

  @Override
  public String getDescription() {
    return "Rename Category '" + oldCategoryName.toUpperCase() + "' to '" + newCategoryName.toUpperCase() + "'";
  }
}
