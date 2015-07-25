package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.api.date.Date;
import seahawk.caloriecounter.domain.api.log.SimpleLogger;
import seahawk.caloriecounter.domain.change.*;
import seahawk.caloriecounter.domain.persistence.CalorieCounterPersistenceHandler;
import seahawk.xmlhandler.XmlException;

import java.util.Observable;
import java.util.TreeSet;

public class NutritionManager {
  private CalorieCounterPersistenceHandler readWriteHandler;

  private ChangeManager changeManager;

  private FoodStore foodStore;
  private DailyRecordStore dailyRecordStore;

	public NutritionManager(CalorieCounterPersistenceHandler readWriteHandler) {
    this.readWriteHandler = readWriteHandler;

    this.changeManager = new ChangeManager();
	}

  public Foodstuff getFoodstuff(String name) {
    return foodStore.getFoodstuff(name);
  }

  public Meal getMeal(String mealName) {
    return foodStore.getMeal(mealName);
  }

  public FoodStore getFoodStore() {
    return foodStore;
  }

  public DailyRecordStore getDailyRecordStore() {
    return dailyRecordStore;
  }

  public boolean addFoodstuff(String name, FoodstuffCategory category) {
    return changeManager.applyChange(new CreateFoodChange(foodStore, name, category));
  }

  public boolean addMeal(String mealName) {
    return changeManager.applyChange(new CreateFoodChange(foodStore, mealName));
  }

  public boolean copyFood(Food foodToCopy, String newFoodName) {
    return changeManager.applyChange(new CreateFoodChange(foodStore, foodToCopy, newFoodName));
  }

  public boolean removeFood(Food food) {
    return changeManager.applyChange(new RemoveFoodChange(dailyRecordStore, foodStore, food));
  }

  public boolean changeFoodName(Food food, String newName) {
    return changeManager.applyChange(new EditFoodNameChange(foodStore, food, newName));
  }

  public boolean setFoodAmount(Food food, PositiveDecimalNumber newAmount) {
    return changeManager.applyChange(new EditFoodAmountChange(food, newAmount));
  }

  public void setFoodUnit(Food food, MeasurementUnit newUnit) {
    changeManager.applyChange(new EditFoodUnitChange(dailyRecordStore, foodStore, food, newUnit));
  }

  public boolean setFoodFactAmount(Foodstuff foodstuff, NutrientType factType, PositiveDecimalNumber newAmount) {
    return changeManager.applyChange(new EditFoodstuffFactAmountChange(foodstuff, factType, newAmount));
  }

  public void setFoodstuffCategory(Foodstuff food, FoodstuffCategory newCategory) {
    changeManager.applyChange(new EditFoodstuffCategoryChange(food, newCategory));
  }

  public void changeCategoryName(FoodstuffCategory category, String newCategoryName) {
    changeManager.applyChange(new EditCategoryGroupChange(foodStore, category, newCategoryName));
  }

  public boolean addIngredient(Meal meal, Food ingredient) {
    return changeManager.applyChange(new AddMealIngredientChange(meal, ingredient));
  }

  public void setMealIngredientAmount(Meal meal, Ingredient ingredient, PositiveDecimalNumber newAmount) {
    changeManager.applyChange(new EditMealIngredientAmountChange(meal, ingredient, newAmount));
  }

  public void setMealIngredientUnit(Meal meal, Ingredient ingredient, MeasurementUnit newUnit) {
    changeManager.applyChange(new EditMealIngredientUnitChange(meal, ingredient, newUnit));
  }

  public void removeIngredient(Meal meal, Ingredient ingredient) {
    changeManager.applyChange(new RemoveMealIngredientChange(meal, ingredient));
  }

  public DailyRecord loadOrCreateDailyRecord(Date date) throws XmlException {
    DailyRecord record = dailyRecordStore.findRecord(date);
    if (record == null) {
      record = readWriteHandler.loadDailyRecord(date, dailyRecordStore, foodStore);
      if (record == null) {
        record = addDailyRecord(date);
      }
    }

    return record;
  }

  // no change history for daily record creates, no need to save it unless changes are made to it.  No need to undo it.
  public DailyRecord addDailyRecord(Date date) {
    DailyRecord dailyRecord = new DailyRecordImpl(date);
    ((DailyRecordStoreImpl) dailyRecordStore).add(dailyRecord);
    return dailyRecord;
  }

  public Ingredient addDailyRecordFood(DailyRecord dailyRecord, Food food) {
    AddDailyRecordFoodChange change = new AddDailyRecordFoodChange(dailyRecord, food);
    changeManager.applyChange(change);
    return change.getIngredient();
  }

  public void setDailyRecordEatenFoodAmount(DailyRecord dailyRecord, Ingredient ingredient, PositiveDecimalNumber newAmount) {
    changeManager.applyChange(new EditDailyRecordFoodAmountChange(dailyRecord, ingredient, newAmount));
  }

  public void setDailyRecordEatenFoodUnit(DailyRecord dailyRecord, Ingredient ingredient, MeasurementUnit newUnit) {
    changeManager.applyChange(new EditDailyRecordFoodUnitChange(dailyRecord, ingredient, newUnit));
  }

  public void removeDailyRecordEatenFood(DailyRecord dailyRecord, Ingredient eatenFood) {
    changeManager.applyChange(new RemoveDailyRecordFoodChange(dailyRecord, eatenFood));
  }

  public DisplayConfig loadData() throws XmlException {
    try {
      foodStore = readWriteHandler.loadFoodStore();
      dailyRecordStore = readWriteHandler.loadDailyRecordStore();
    }
    finally {
      if (foodStore == null) {
        foodStore = readWriteHandler.loadEmptyFoodStore();
      }
      if (dailyRecordStore == null) {
        dailyRecordStore = readWriteHandler.loadEmptyDailyRecordStore();
      }

      if (FoodstuffCategory.values().isEmpty())
        FoodstuffCategory.findOrCreate("DEFAULT");
    }

    try {
      return readWriteHandler.loadDisplayConfig();
    }
    catch (XmlException e) {
      SimpleLogger.error("Error reading config file: ", e);
      return null;
    }
  }

  public boolean unsavedChangesExist() {
    return changeManager.dataChanged();
  }

  public void saveData(DisplayConfig config) throws XmlException {
    readWriteHandler.saveDisplayConfig(config);

    if (changeManager.foodstuffsChanged())
      readWriteHandler.saveFoodstuffs(foodStore.getFoodstuffs(FoodSortType.NAME, false));

    if (changeManager.mealsChanged())
      readWriteHandler.saveMeals(foodStore.getMeals(FoodSortType.NAME, false));

    if (changeManager.dailyRecordsChanged()) {
      for (DailyRecord dailyRecord : ((DailyRecordStoreImpl) dailyRecordStore).getDailyRecords()) {
        if (dailyRecord != null && changeManager.dailyRecordChanged(dailyRecord)) {
          readWriteHandler.saveDailyRecord(dailyRecord, dailyRecordStore.getHistory());
        }
      }

      readWriteHandler.saveDailyRecordHistory(dailyRecordStore.getHistory());
    }

    changeManager.markStateSaved();
  }

  public Observable getChangeObservable() {
    return changeManager;
  }

  public void undo() {
    changeManager.undo();
  }

  public String getUndoChangeDescription() {
    return changeManager.getNextUndoDescription();
  }

  public void redo() {
    changeManager.redo();
  }

  public String getRedoChangeDescription() {
    return changeManager.getNextRedoDescription();
  }
}
