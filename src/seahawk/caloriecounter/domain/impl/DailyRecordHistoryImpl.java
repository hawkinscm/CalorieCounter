package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.DailyRecord;
import seahawk.caloriecounter.domain.api.DailyRecordHistory;
import seahawk.caloriecounter.domain.api.NutritionFacts;
import seahawk.caloriecounter.domain.api.date.Date;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DailyRecordHistoryImpl implements DailyRecordHistory {
  private TreeMap<Date, NutritionFacts> dailySummaryMap;

  public DailyRecordHistoryImpl(Map<Date, NutritionFacts> dailySummaryMap) {
    this.dailySummaryMap = new TreeMap<>(Collections.reverseOrder());
    this.dailySummaryMap.putAll(dailySummaryMap);
  }

  @Override
  public NutritionFacts getDailyFacts(Date date) {
    return dailySummaryMap.get(date);
  }

  @Override
  public Set<Date> getRecordedDates() {
    return dailySummaryMap.keySet();
  }

  public TreeMap<Date, NutritionFacts> getDailySummaryMap() {
    return dailySummaryMap;
  }

  public void createOrUpdateDailySummary(DailyRecord record) {
    dailySummaryMap.put(record.getDate(), record.getFacts());
  }
}
