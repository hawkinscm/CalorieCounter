package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MealImpl extends FoodImpl implements Meal {
	private List<Ingredient> ingredients;

	public MealImpl(String name) {
    this(name, new PositiveDecimalNumber("1.0"), MeasurementUnit.SERVING_SIZE);
	}

  public MealImpl(String name, PositiveDecimalNumber amount, MeasurementUnit unit) {
    super(name, amount, unit);
    this.ingredients = new ArrayList<>();
  }

  public MealImpl(MealImpl meal, String newName) {
    super(meal, newName);
    this.ingredients = new ArrayList<>(meal.ingredients);
  }

  public void setIngredients(List<Ingredient> ingredients) {
    this.ingredients.clear();
    this.ingredients.addAll(ingredients);
  }

  @Override
  public boolean isMeal() {
    return true;
  }

  @Override
	public NutritionFacts getFacts() {
		NutritionFactsImpl facts = new NutritionFactsImpl();
		for (Ingredient ingredient : ingredients)
      facts.addAmounts(ingredient.getFacts());
		return facts;
	}

  @Override
  public List<Ingredient> getIngredients() {
    return Collections.unmodifiableList(ingredients);
  }

  public void addIngredient(Ingredient ingredient) {
		ingredients.add(ingredient);
	}

  public void removeIngredient(Ingredient ingredient) {
		ingredients.remove(ingredient);
	}
}
