package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.Food;
import seahawk.caloriecounter.domain.api.MeasurementUnit;
import seahawk.caloriecounter.domain.api.PositiveDecimalNumber;

public abstract class FoodImpl implements Food, Comparable<FoodImpl> {
	
	private String name;
	private PositiveDecimalNumber amount;
	private MeasurementUnit unit;

  FoodImpl(String name, PositiveDecimalNumber amount, MeasurementUnit unit) {
    this.name = name;
    this.amount = amount;
    this.unit = unit;
  }

  FoodImpl(FoodImpl food, String newName) {
    this.name = newName;
    this.amount = food.amount;
    this.unit = food.unit;
  }

  @Override
	public String getName() {
		return name;
	}

  @Override
  public PositiveDecimalNumber getAmount() {
    return amount;
  }

  @Override
  public MeasurementUnit getUnit() {
    return unit;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAmount(PositiveDecimalNumber amount) {
		this.amount = amount;
	}

  public void setUnit(MeasurementUnit unit) {
		this.unit = unit;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null || !(o instanceof FoodImpl)) return false;

    FoodImpl that = (FoodImpl) o;
    return this.getClass().equals(that.getClass()) && getName().equals(that.getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public int compareTo(FoodImpl food) {
    return getName().compareTo(food.getName());
  }
}
