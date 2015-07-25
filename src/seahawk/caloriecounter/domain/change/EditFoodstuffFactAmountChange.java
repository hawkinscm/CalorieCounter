package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.ChangeNotification;
import seahawk.caloriecounter.domain.api.Foodstuff;
import seahawk.caloriecounter.domain.api.NutrientType;
import seahawk.caloriecounter.domain.api.PositiveDecimalNumber;
import seahawk.caloriecounter.domain.impl.NutritionFactsImpl;

public class EditFoodstuffFactAmountChange extends Change {
  private Foodstuff foodstuff;
  private NutritionFactsImpl nutritionFacts;
  private NutrientType type;
  private PositiveDecimalNumber oldAmount;
  private PositiveDecimalNumber newAmount;

  public EditFoodstuffFactAmountChange(Foodstuff foodstuff, NutrientType type, PositiveDecimalNumber newAmount) {
    this.foodstuff = foodstuff;
    this.nutritionFacts = (NutritionFactsImpl) foodstuff.getFacts();
    this.type = type;
    this.oldAmount = foodstuff.getFacts().getAmount(type);
    this.newAmount = newAmount;
  }

  @Override
  boolean applyChange() {
    if (oldAmount.equals(newAmount))
      return false;

    nutritionFacts.setAmount(type, newAmount);
    return true;
  }

  @Override
  void undo() {
    nutritionFacts.setAmount(type, oldAmount);
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
    return "Edit " + type.name() + " on '" + foodstuff + "'";
  }
}
