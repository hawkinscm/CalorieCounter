package seahawk.caloriecounter.domain.api;

import seahawk.caloriecounter.domain.api.date.Date;

import java.util.List;

public interface DailyRecord {
  public Date getDate();

  public List<Ingredient> getEatenFoods();

  public NutritionFacts getFacts();
}
