package seahawk.caloriecounter.domain.api;

public interface Ingredient {
  public Food getFood();

  public PositiveDecimalNumber getAmount();

  public MeasurementUnit getUnit();

  public NutritionFacts getFacts();
}