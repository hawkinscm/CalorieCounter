package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.impl.DailyRecordImpl;
import seahawk.caloriecounter.domain.impl.IngredientImpl;

public class AddDailyRecordFoodChange extends Change {
  private DailyRecordImpl dailyRecord;
  private Ingredient ingredient;

  public AddDailyRecordFoodChange(DailyRecord dailyRecord, Food food) {
    this.dailyRecord = (DailyRecordImpl) dailyRecord;
    this.ingredient = new IngredientImpl(food);
  }

  public Ingredient getIngredient() {
    return ingredient;
  }

  @Override
  boolean applyChange() {
    dailyRecord.addEatenFood(ingredient);
    return true;
  }

  @Override
  void undo() {
    dailyRecord.removeEatenFood(ingredient);
  }

  @Override
  public ChangeNotification getChangeNotification() {
    return new ChangeNotification(dailyRecord, ChangeActionType.EDIT);
  }

  @Override
  public ChangeNotification getUndoChangeNotification() {
    return new ChangeNotification(dailyRecord, ChangeActionType.EDIT);
  }

  @Override
  public String getDescription() {
    return "Add '" + ingredient.getFood() + "' to Daily Record";
  }
}
