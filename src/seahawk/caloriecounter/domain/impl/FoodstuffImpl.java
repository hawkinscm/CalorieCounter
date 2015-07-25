package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.*;

public class FoodstuffImpl extends FoodImpl implements Foodstuff {
	private FoodstuffCategory category;
	private NutritionFacts nutritionFacts;

	public FoodstuffImpl(String name, FoodstuffCategory category) {
    super(name, new PositiveDecimalNumber("1.0"), MeasurementUnit.SERVING_SIZE);
    this.category = category;
		nutritionFacts = new NutritionFactsImpl();
	}

  public FoodstuffImpl(String name, FoodstuffCategory category, PositiveDecimalNumber amount, MeasurementUnit unit, NutritionFacts facts) {
    super(name, amount, unit);
    this.category = category;
    nutritionFacts = new NutritionFactsImpl(facts);
  }

  public FoodstuffImpl(FoodstuffImpl foodstuff, String newName) {
    super(foodstuff, newName);
    this.category = foodstuff.category;
    this.nutritionFacts = new NutritionFactsImpl(foodstuff.nutritionFacts);
  }

  @Override
	public FoodstuffCategory getCategory() {
		return category;
	}

  @Override
  public boolean isMeal() {
    return false;
  }

  @Override
  public NutritionFacts getFacts() {
    return nutritionFacts;
  }

  public void setCategory(FoodstuffCategory category) {
    this.category = category;
  }
}
