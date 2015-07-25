package seahawk.caloriecounter.domain.api;

public interface Food {
  public boolean isMeal();

	public String getName();

  public PositiveDecimalNumber getAmount();

  public MeasurementUnit getUnit();

  public NutritionFacts getFacts();
}
