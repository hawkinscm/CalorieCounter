package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.ChangeNotification;
import seahawk.caloriecounter.domain.api.Ingredient;
import seahawk.caloriecounter.domain.api.Meal;
import seahawk.caloriecounter.domain.impl.MealImpl;

public class RemoveMealIngredientChange extends Change {
  private MealImpl meal;
  private Ingredient ingredient;

  public RemoveMealIngredientChange(Meal meal, Ingredient ingredient) {
    this.meal = (MealImpl) meal;
    this.ingredient = ingredient;
  }

  @Override
  boolean applyChange() {
    meal.removeIngredient(ingredient);
    return true;
  }

  @Override
  void undo() {
    meal.addIngredient(ingredient);
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
    return "Remove '" + ingredient.getFood() + "' from Meal '" + meal + "'";
  }
}
