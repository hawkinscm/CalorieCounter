package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.api.date.Date;

import java.util.*;

public class DailyRecordImpl implements DailyRecord, Comparable<DailyRecordImpl> {
  private Date date;
	private List<Ingredient> eatenFoods;
	
	public DailyRecordImpl(Date date) {
    this.date = date;
    this.eatenFoods = new ArrayList<>();
  }

  public DailyRecordImpl(Date date, List<Ingredient> eatenFoods) {
    this.date = date;
    this.eatenFoods = new ArrayList<>(eatenFoods);
  }

  @Override
  public Date getDate() {
    return date;
  }

  @Override
  public List<Ingredient> getEatenFoods() {
    return Collections.unmodifiableList(eatenFoods);
  }

  @Override
  public NutritionFacts getFacts() {
    NutritionFactsImpl facts = new NutritionFactsImpl();
    for (Ingredient ingredient : eatenFoods)
      facts.addAmounts(ingredient.getFacts());
    return facts;
  }

  public void addEatenFood(Ingredient eatenFood) {
    eatenFoods.add(eatenFood);
  }
	
	public void removeEatenFood(Ingredient eatenFood) {
		eatenFoods.remove(eatenFood);
	}

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DailyRecordImpl that = (DailyRecordImpl) o;
    return this.date.equals(that.date);
  }

  @Override
  public int hashCode() {
    return date.hashCode();
  }

  @Override
  public int compareTo(DailyRecordImpl dailyRecord) {
    return new Integer(this.date.hashCode()).compareTo(dailyRecord.date.hashCode());
  }
}
