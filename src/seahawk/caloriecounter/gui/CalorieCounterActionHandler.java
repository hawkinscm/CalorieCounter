package seahawk.caloriecounter.gui;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.api.date.Date;
import seahawk.xmlhandler.XmlException;

import java.util.Observable;

public abstract class CalorieCounterActionHandler extends Observable {
  public abstract void saveAllChangedData() throws Exception;

  public abstract void exit();

  public abstract void undo();

  public abstract void redo();

  public abstract boolean addFoodstuff(String foodName, FoodstuffCategory foodCategory);

  public abstract boolean addMeal(String mealName);

  public abstract boolean copyFood(Food foodToCopy, String newFoodName);

  public abstract boolean removeFood(Food food);

  public abstract boolean changeFoodName(Food food, String newFoodName);

  public abstract boolean setFoodAmount(Food food, PositiveDecimalNumber newAmount);

  public abstract void setFoodUnit(Food food, MeasurementUnit newUnit);

  public abstract boolean setFoodFactAmount(Foodstuff food, NutrientType factType, PositiveDecimalNumber newValue);

  public abstract void setFoodstuffCategory(Foodstuff food, FoodstuffCategory newCategory);

  public abstract void changeCategoryName(FoodstuffCategory category, String newCategoryName);

  public abstract boolean addIngredientToMeal(Meal meal, Food ingredient);

  public abstract void setMealIngredientAmount(Meal meal, Ingredient ingredient, PositiveDecimalNumber newAmount);

  public abstract void setMealIngredientUnit(Meal meal, Ingredient ingredient, MeasurementUnit newUnit);

  public abstract void removeMealIngredient(Meal meal, Ingredient ingredient);

  public abstract DailyRecord loadOrCreateDailyRecord(Date date) throws XmlException;

  public abstract DailyRecord addDailyRecord(Date date);

  public abstract Ingredient addDailyRecordEatenFood(DailyRecord dailyRecord, Food food);

  public abstract void setDailyRecordEatenFoodAmount(DailyRecord dailyRecord, Ingredient ingredient, PositiveDecimalNumber newAmount);

  public abstract void setDailyRecordEatenFoodUnit(DailyRecord dailyRecord, Ingredient ingredient, MeasurementUnit newUnit);

  public abstract void removeDailyRecordEatenFood(DailyRecord dailyRecord, Ingredient eatenFood);

  public abstract String getUndoChangeDescription();

  public abstract String getRedoChangeDescription();
}
