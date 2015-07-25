package seahawk.caloriecounter.domain.api;

import seahawk.caloriecounter.domain.change.ChangeActionType;

import java.util.List;

public class ChangeNotification {
  private Foodstuff foodstuff;
  private List<Foodstuff> foodstuffs;
  private Meal meal;
  private DailyRecord record;

  private final ChangeActionType changeActionType;

  public ChangeNotification(Foodstuff foodstuff, ChangeActionType actionType) {
    this.foodstuff = foodstuff;
    this.changeActionType = actionType;
  }

  public ChangeNotification(Meal meal, ChangeActionType actionType) {
    this.meal = meal;
    this.changeActionType = actionType;
  }

  public ChangeNotification(Food food, ChangeActionType actionType) {
    if (food.isMeal())
      this.meal = (Meal) food;
    else
      this.foodstuff = (Foodstuff) food;
    this.changeActionType = actionType;
  }

  public ChangeNotification(List<Foodstuff> foodstuffs) {
    this.foodstuffs = foodstuffs;
    this.changeActionType = ChangeActionType.EDIT;
  }

  public ChangeNotification(DailyRecord record, ChangeActionType actionType) {
    this.record = record;
    this.changeActionType = actionType;
  }

  public boolean isFoodstuffChange() {
    return foodstuff != null || foodstuffs != null;
  }

  public Foodstuff getFoodstuff() {
    return foodstuff;
  }

  public Meal getMeal() {
    return meal;
  }

  public DailyRecord getDailyRecord() {
    return record;
  }

  public boolean isMealChange() {
    return meal != null;
  }

  public boolean isDailyRecordChange() {
    return record != null;
  }

  public boolean isAdd() {
    return changeActionType == ChangeActionType.ADD;
  }

  public boolean isEdit() {
    return changeActionType == ChangeActionType.EDIT;
  }

  public boolean isRemove() {
    return changeActionType == ChangeActionType.REMOVE;
  }
}
