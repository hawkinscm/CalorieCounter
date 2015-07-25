package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.*;

public class IngredientImpl implements Ingredient {
	private Food food;
	private PositiveDecimalNumber amount;
	private MeasurementUnit unit;

	public IngredientImpl(Food food) {
		this.food = food;
		this.amount = food.getAmount();
		this.unit = food.getUnit();
	}

  public IngredientImpl(Food food, PositiveDecimalNumber amount, MeasurementUnit unit) {
    this.food = food;
    this.amount = amount;
    this.unit = (food.getUnit().getConvertibleUnits().contains(unit)) ? unit : food.getUnit();
  }

  @Override
  public Food getFood() {
    return food;
  }

  @Override
  public PositiveDecimalNumber getAmount() {
    return amount;
  }

  @Override
  public MeasurementUnit getUnit() {
    return unit;
  }

  @Override
  public NutritionFacts getFacts() {
    PositiveDecimalNumber conversionFactor = new PositiveDecimalNumber(amount + "/" + food.getAmount());
    if (!unit.equals(food.getUnit()))
      conversionFactor = unit.convertToUnit(conversionFactor, food.getUnit());
    return new NutritionFactsImpl(food.getFacts(), conversionFactor);
  }
	
	public void setAmount(PositiveDecimalNumber amount) {
		this.amount = amount;
	}
	
	public void setUnit(MeasurementUnit unit) {
    this.unit = unit;
  }

  public void updateOnFoodUnitChange(MeasurementUnit oldFoodUnit) {
    MeasurementUnit newFoodUnit = food.getUnit();
    if (oldFoodUnit.getConvertibleUnits().contains(newFoodUnit))
      return;

    amount = unit.convertToUnit(amount, oldFoodUnit);
    unit = newFoodUnit;
	}

  @Override
  public String toString() {
    return food.getName();
  }
}