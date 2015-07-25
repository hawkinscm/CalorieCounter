package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.impl.FoodImpl;

public class EditFoodAmountChange extends Change {
  private FoodImpl food;
  private PositiveDecimalNumber oldAmount;
  private PositiveDecimalNumber newAmount;

  public EditFoodAmountChange(Food food, PositiveDecimalNumber newAmount) {
    this.food = (FoodImpl) food;
    this.oldAmount = food.getAmount();
    this.newAmount = newAmount;
  }

  @Override
  boolean applyChange() {
    if (oldAmount.equals(newAmount))
      return false;

    food.setAmount(newAmount);
    return true;
  }

  @Override
  void undo() {
    food.setAmount(oldAmount);
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
    return "Edit Amount on '" + food + "'";
  }
}
