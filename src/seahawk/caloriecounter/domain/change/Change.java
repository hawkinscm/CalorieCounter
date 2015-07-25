package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.ChangeNotification;

abstract class Change {
  abstract boolean applyChange();

  abstract void undo();

  public abstract ChangeNotification getChangeNotification();

  public abstract ChangeNotification getUndoChangeNotification();

  public abstract String getDescription();

  boolean isFoodstuffChange() {
    return getChangeNotification().isFoodstuffChange();
  }

  boolean isMealChange() {
    return getChangeNotification().isMealChange();
  }

  boolean isDailyRecordChange() {
    return getChangeNotification().isDailyRecordChange();
  }
}
