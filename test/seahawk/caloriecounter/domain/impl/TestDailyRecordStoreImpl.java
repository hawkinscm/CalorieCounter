package seahawk.caloriecounter.domain.impl;

import junit.framework.TestCase;
import seahawk.caloriecounter.domain.api.NutritionFacts;
import seahawk.caloriecounter.domain.api.date.Date;

import java.util.*;

public class TestDailyRecordStoreImpl extends TestCase {
  public void testDailyRecordStore() {
    DailyRecordStoreImpl store = new DailyRecordStoreImpl(new DailyRecordHistoryImpl(new HashMap<Date, NutritionFacts>()));
    assertEquals(0, store.getDates().size());

    Date today = new Date(Calendar.getInstance());
    assertNull(store.findRecord(today));

    store.add(new DailyRecordImpl(today));
    assertEquals(today, store.findRecord(today).getDate());
    assertEquals(1, store.getDates().size());

    store.add(new DailyRecordImpl(new Date("20140303")));
    assertEquals(2, store.getDates().size());
    assertNotNull(store.findRecord(new Date("20140303")));

    store.addEmptyDailyRecord(new Date("20140303"));
    assertEquals(2, store.getDates().size());
    assertNull(store.findRecord(new Date("20140303")));

    store.addEmptyDailyRecord(new Date("20140304"));
    assertEquals(3, store.getDates().size());

    store.add(new DailyRecordImpl(new Date("20140403")));
    assertEquals(4, store.getDates().size());

    store.remove(new DailyRecordImpl(new Date("20140403")));
    assertEquals(3, store.getDates().size());
  }
}
