package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.ChangeNotification;
import seahawk.caloriecounter.domain.api.DailyRecord;
import seahawk.caloriecounter.domain.api.Ingredient;
import seahawk.caloriecounter.domain.api.MeasurementUnit;
import seahawk.caloriecounter.domain.impl.IngredientImpl;

public class EditDailyRecordFoodUnitChange extends Change {
  private DailyRecord dailyRecord;
  private IngredientImpl ingredient;
  private MeasurementUnit oldUnit;
  private MeasurementUnit newUnit;

  public EditDailyRecordFoodUnitChange(DailyRecord dailyRecord, Ingredient ingredient, MeasurementUnit newUnit) {
    this.dailyRecord = dailyRecord;
    this.ingredient = (IngredientImpl) ingredient;
    this.oldUnit = ingredient.getUnit();
    this.newUnit = newUnit;
  }

  @Override
  boolean applyChange() {
    if (oldUnit == newUnit)
      return false;

    ingredient.setUnit(newUnit);
    return true;
  }

  @Override
  void undo() {
    ingredient.setUnit(oldUnit);
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
    return "Edit Unit of '" + ingredient.getFood() + "' on Daily Record";
  }
}
