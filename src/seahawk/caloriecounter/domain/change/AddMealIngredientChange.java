package seahawk.caloriecounter.domain.change;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.impl.IngredientImpl;
import seahawk.caloriecounter.domain.impl.MealImpl;

public class AddMealIngredientChange extends Change {
  private MealImpl meal;
  private Ingredient ingredient;

  public AddMealIngredientChange(Meal meal, Food food) {
    this.meal = (MealImpl) meal;
    this.ingredient = new IngredientImpl(food);
  }

  @Override
  boolean applyChange() {
    if (ingredient.getFood().equals(meal) || containsMeal(ingredient.getFood()))
      return false;

    meal.addIngredient(ingredient);
    return true;
  }

  private boolean containsMeal(Food food) {
    if (!food.isMeal())
      return false;

    for (Ingredient nextIngredient : ((Meal) food).getIngredients()) {
      if (nextIngredient.getFood().equals(meal) || containsMeal(nextIngredient.getFood()))
        return true;
    }
    return false;
  }

  @Override
  void undo() {
    meal.removeIngredient(ingredient);
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
    return "Add '" + ingredient.getFood() + "' to Meal '" + meal + "'";
  }
}
