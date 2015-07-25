package seahawk.caloriecounter.domain.api;

public interface NutritionFacts {
	public PositiveDecimalNumber getAmount(NutrientType type);
}
