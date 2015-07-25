package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.ChangeNotification;
import seahawk.caloriecounter.domain.api.DailyRecord;
import seahawk.caloriecounter.domain.api.Ingredient;
import seahawk.caloriecounter.domain.impl.DailyRecordImpl;

public class RemoveDailyRecordFoodChange extends Change {
  private DailyRecordImpl dailyRecord;
  private Ingredient ingredient;

  public RemoveDailyRecordFoodChange(DailyRecord dailyRecord, Ingredient food) {
    this.dailyRecord = (DailyRecordImpl) dailyRecord;
    this.ingredient = food;
  }

  @Override
  boolean applyChange() {
    dailyRecord.removeEatenFood(ingredient);
    return true;
  }

  @Override
  void undo() {
    dailyRecord.addEatenFood(ingredient);
  }

  @Override
  public ChangeNotification getChangeNotification() {
    return new ChangeNotification(dailyRecord, ChangeActionType.REMOVE);
  }

  @Override
  public ChangeNotification getUndoChangeNotification() {
    return new ChangeNotification(dailyRecord, ChangeActionType.ADD);
  }

  @Override
  public String getDescription() {
    return "Remove '" + ingredient.getFood() + " from Daily Record";
  }
}
