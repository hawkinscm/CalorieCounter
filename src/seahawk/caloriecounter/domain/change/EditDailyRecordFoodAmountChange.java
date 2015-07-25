package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.impl.IngredientImpl;

public class EditDailyRecordFoodAmountChange extends Change {
  private DailyRecord dailyRecord;
  private IngredientImpl ingredient;
  private PositiveDecimalNumber oldAmount;
  private PositiveDecimalNumber newAmount;

  public EditDailyRecordFoodAmountChange(DailyRecord dailyRecord, Ingredient ingredient, PositiveDecimalNumber newAmount) {
    this.dailyRecord = dailyRecord;
    this.ingredient = (IngredientImpl) ingredient;
    this.oldAmount = ingredient.getAmount();
    this.newAmount = newAmount;
  }

  @Override
  boolean applyChange() {
    if (oldAmount == newAmount)
      return false;

    ingredient.setAmount(newAmount);
    return true;
  }

  @Override
  void undo() {
    ingredient.setAmount(oldAmount);
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
    return "Edit Amount of '" + ingredient.getFood() + "' on Daily Record";
  }
}
