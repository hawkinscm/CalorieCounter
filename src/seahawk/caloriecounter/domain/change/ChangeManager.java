package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.DailyRecord;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Stack;

public class ChangeManager extends Observable {
  private Stack<Change> undoStack = new Stack<>();
  private Stack<Change> redoStack = new Stack<>();

  private LinkedList<Change> unsavedChanges = new LinkedList<>();

  public boolean applyChange(Change change) {
    boolean successfulChange = change.applyChange();

    if (successfulChange) {
      undoStack.push(change);
      redoStack.clear();
      unsavedChanges.add(change);

      setChanged();
      notifyObservers(change.getChangeNotification());
    }

    return successfulChange;
  }

  public void undo() {
    if (undoStack.isEmpty())
      return;

    Change change = undoStack.pop();
    change.undo();
    redoStack.push(change);

    if (!unsavedChanges.remove(change))
      unsavedChanges.add(change);

    setChanged();
    notifyObservers(change.getUndoChangeNotification());
  }

  public String getNextUndoDescription() {
    return undoStack.isEmpty() ? null : undoStack.peek().getDescription();
  }

  public void redo() {
    if (redoStack.isEmpty())
      return;

    Change change = redoStack.pop();
    change.applyChange();
    undoStack.push(change);

    if (!unsavedChanges.remove(change))
      unsavedChanges.add(change);

    setChanged();
    notifyObservers(change.getChangeNotification());
  }

  public String getNextRedoDescription() {
    return redoStack.isEmpty() ? null : redoStack.peek().getDescription();
  }

  public boolean foodstuffsChanged() {
    for (Change change : unsavedChanges) {
      if (change.isFoodstuffChange())
        return true;
    }
    return false;
  }

  public boolean mealsChanged() {
    for (Change change : unsavedChanges) {
      if (change.isMealChange())
        return true;
    }
    return false;
  }

  public boolean dailyRecordsChanged() {
    for (Change change : unsavedChanges) {
      if (change.isDailyRecordChange())
        return true;
    }
    return false;
  }

  public boolean dailyRecordChanged(DailyRecord record) {
    for (Change change : unsavedChanges) {
      if (change.isDailyRecordChange() && change.getChangeNotification().getDailyRecord().equals(record))
        return true;
    }
    return false;
  }

  public boolean dataChanged() {
    return !unsavedChanges.isEmpty();
  }

  public void markStateSaved() {
    unsavedChanges.clear();
  }
}
