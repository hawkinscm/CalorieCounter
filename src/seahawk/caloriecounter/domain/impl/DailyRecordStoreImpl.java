package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.DailyRecord;
import seahawk.caloriecounter.domain.api.DailyRecordHistory;
import seahawk.caloriecounter.domain.api.DailyRecordStore;
import seahawk.caloriecounter.domain.api.date.Date;

import java.util.*;

public class DailyRecordStoreImpl implements DailyRecordStore {
  private TreeMap<Date, DailyRecord> dailyRecordMap = new TreeMap<>(Collections.reverseOrder());
  private DailyRecordHistory dailyRecordHistory;

  public DailyRecordStoreImpl(DailyRecordHistory dailyRecordHistory) {
    this.dailyRecordHistory = dailyRecordHistory;
  }

  @Override
  public Set<Date> getDates() {
    return dailyRecordMap.keySet();
  }

  @Override
  public DailyRecordHistory getHistory() {
    return dailyRecordHistory;
  }

  @Override
  public DailyRecord findRecord(Date date) {
    return dailyRecordMap.get(date);
  }

  public TreeSet<DailyRecord> getDailyRecords() {
    TreeSet<DailyRecord> dailyRecords = new TreeSet<>();
    for (Date date : dailyRecordMap.keySet()) {
      DailyRecord record = dailyRecordMap.get(date);
      if (record != null)
        dailyRecords.add(record);
    }
    return dailyRecords;
  }

  public void addEmptyDailyRecord(Date date) {
    dailyRecordMap.put(date, null);
  }

  public void add(DailyRecord dailyRecord) {
    dailyRecordMap.put(dailyRecord.getDate(), dailyRecord);
  }

  public void remove(DailyRecord dailyRecord) {
    dailyRecordMap.remove(dailyRecord.getDate());
  }
}
