package seahawk.caloriecounter.domain.api;

import seahawk.caloriecounter.domain.api.date.Date;

import java.util.Set;

public interface DailyRecordHistory {
  public NutritionFacts getDailyFacts(Date date);

  public Set<Date> getRecordedDates();
}
