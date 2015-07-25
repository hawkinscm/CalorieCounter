package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.ChangeNotification;
import seahawk.caloriecounter.domain.api.Ingredient;
import seahawk.caloriecounter.domain.api.Meal;
import seahawk.caloriecounter.domain.api.PositiveDecimalNumber;
import seahawk.caloriecounter.domain.impl.IngredientImpl;

public class EditMealIngredientAmountChange extends Change {
  private Meal meal;
  private IngredientImpl ingredient;
  private PositiveDecimalNumber oldAmount;
  private PositiveDecimalNumber newAmount;

  public EditMealIngredientAmountChange(Meal meal, Ingredient ingredient, PositiveDecimalNumber newAmount) {
    this.meal = meal;
    this.ingredient = (IngredientImpl) ingredient;
    this.oldAmount = ingredient.getAmount();
    this.newAmount = newAmount;
  }

  @Override
  boolean applyChange() {
    if (oldAmount.equals(newAmount))
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
    return new ChangeNotification(meal, ChangeActionType.EDIT);
  }

  @Override
  public ChangeNotification getUndoChangeNotification() {
    return new ChangeNotification(meal, ChangeActionType.EDIT);
  }

  @Override
  public String getDescription() {
    return "Edit Amount of '" + ingredient.getFood() + "' on Meal '" + meal + "'";
  }
}
