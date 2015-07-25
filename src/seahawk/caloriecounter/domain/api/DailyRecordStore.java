package seahawk.caloriecounter.domain.api;

import seahawk.caloriecounter.domain.api.date.Date;

import java.util.Set;

public interface DailyRecordStore {
  public Set<Date> getDates();

  public DailyRecordHistory getHistory();

  public DailyRecord findRecord(Date date);
}
