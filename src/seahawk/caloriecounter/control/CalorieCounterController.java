package seahawk.caloriecounter.control;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.api.date.Date;
import seahawk.caloriecounter.domain.api.log.SimpleLogger;
import seahawk.caloriecounter.domain.impl.NutritionManager;
import seahawk.caloriecounter.domain.persistence.CalorieCounterPersistenceHandler;
import seahawk.caloriecounter.gui.CalorieCounterActionHandler;
import seahawk.caloriecounter.gui.MasterFrame;
import seahawk.xmlhandler.XmlException;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Observable;
import java.util.Observer;

class CalorieCounterController extends CalorieCounterActionHandler implements Observer {
  private MasterFrame masterFrame;
  private NutritionManager manager;

  // todo retest
  // fact size in meal M1, meals, and daily records
  // todo test tableS sort load and save

  public CalorieCounterController(CalorieCounterPersistenceHandler readWriteHandler) {
    manager = new NutritionManager(readWriteHandler);
    manager.getChangeObservable().addObserver(this);

    DisplayConfig config = null;
    String loadErrorMessage = null;
    try {
      config = manager.loadData();
    }
    catch (XmlException e) {
      SimpleLogger.error("Error loading data", e);
      loadErrorMessage = "Error loading data! Loading with partial or no data.";
    }

    masterFrame = new MasterFrame(this, manager.getFoodStore(), manager.getDailyRecordStore(), config);
    masterFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    masterFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        exit();
      }
    });

    masterFrame.setVisible(true);
    if (loadErrorMessage != null)
      JOptionPane.showMessageDialog(masterFrame, loadErrorMessage, "Calorie Counter", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void saveAllChangedData() throws Exception {
    manager.saveData(masterFrame.getDisplayConfig());
  }

  @Override
  public void exit() {
    if (manager.unsavedChangesExist()) {
      String saveMsg = "Do you want to save changes?";
      int choice = JOptionPane.showConfirmDialog(masterFrame, saveMsg, "Calorie Counter", JOptionPane.YES_NO_CANCEL_OPTION);
      if (choice == JOptionPane.YES_OPTION) {
        try {
          saveAllChangedData();
        }
        catch (Exception ex) {
          JOptionPane.showMessageDialog(masterFrame, "ERROR: Unable to save data!", "Calorie Counter", JOptionPane.ERROR_MESSAGE);
          SimpleLogger.error(ex);
        }
      }
      else if (choice != JOptionPane.NO_OPTION)
        return;
    }

    masterFrame.dispose();
  }

  @Override
  public void undo() {
    manager.undo();
  }

  @Override
  public void redo() {
    manager.redo();
  }

  @Override
  public boolean addFoodstuff(String foodName, FoodstuffCategory foodCategory) {
    return manager.addFoodstuff(foodName, foodCategory);
  }

  @Override
  public boolean addMeal(String mealName) {
    return manager.addMeal(mealName);
  }

  @Override
  public boolean copyFood(Food foodToCopy, String newFoodName) {
    return manager.copyFood(foodToCopy, newFoodName);
  }

  @Override
  public boolean removeFood(Food food) {
    return manager.removeFood(food);
  }

  @Override
  public boolean changeFoodName(Food food, String newFoodName) {
    return manager.changeFoodName(food, newFoodName);
  }

  @Override
  public boolean setFoodAmount(Food food, PositiveDecimalNumber newAmount) {
    return manager.setFoodAmount(food, newAmount);
  }

  @Override
  public void setFoodUnit(Food food, MeasurementUnit newUnit) {
    manager.setFoodUnit(food, newUnit);
  }

  @Override
  public boolean setFoodFactAmount(Foodstuff food, NutrientType factType, PositiveDecimalNumber newAmount) {
    return manager.setFoodFactAmount(food, factType, newAmount);
  }

  @Override
  public void setFoodstuffCategory(Foodstuff food, FoodstuffCategory newCategory) {
    manager.setFoodstuffCategory(food, newCategory);
  }

  @Override
  public void changeCategoryName(FoodstuffCategory category, String newCategoryName) {
    manager.changeCategoryName(category, newCategoryName);
  }

  @Override
  public boolean addIngredientToMeal(Meal meal, Food ingredient) {
    return manager.addIngredient(meal, ingredient);
  }

  @Override
  public void setMealIngredientAmount(Meal meal, Ingredient ingredient, PositiveDecimalNumber newAmount) {
    manager.setMealIngredientAmount(meal, ingredient, newAmount);
  }

  @Override
  public void setMealIngredientUnit(Meal meal, Ingredient ingredient, MeasurementUnit newUnit) {
    manager.setMealIngredientUnit(meal, ingredient, newUnit);
  }

  @Override
  public void removeMealIngredient(Meal meal, Ingredient ingredient) {
    manager.removeIngredient(meal, ingredient);
  }

  @Override
  public DailyRecord loadOrCreateDailyRecord(Date date) throws XmlException {
    return manager.loadOrCreateDailyRecord(date);
  }

  @Override
  public DailyRecord addDailyRecord(Date date) {
    return manager.addDailyRecord(date);
  }

  @Override
  public Ingredient addDailyRecordEatenFood(DailyRecord dailyRecord, Food food) {
    return manager.addDailyRecordFood(dailyRecord, food);
  }

  @Override
  public void setDailyRecordEatenFoodAmount(DailyRecord dailyRecord, Ingredient ingredient, PositiveDecimalNumber newAmount) {
    manager.setDailyRecordEatenFoodAmount(dailyRecord, ingredient, newAmount);
  }

  @Override
  public void setDailyRecordEatenFoodUnit(DailyRecord dailyRecord, Ingredient ingredient, MeasurementUnit newUnit) {
    manager.setDailyRecordEatenFoodUnit(dailyRecord, ingredient, newUnit);
  }

  @Override
  public void removeDailyRecordEatenFood(DailyRecord dailyRecord, Ingredient eatenFood) {
    manager.removeDailyRecordEatenFood(dailyRecord, eatenFood);
  }

  @Override
  public String getUndoChangeDescription() {
    return manager.getUndoChangeDescription();
  }

  @Override
  public String getRedoChangeDescription() {
    return manager.getRedoChangeDescription();
  }

  @Override
  public void update(Observable o, Object arg) {
    setChanged();
    notifyObservers(arg);
  }

  public static void main(String[] args) {
    JFrame.setDefaultLookAndFeelDecorated(true);
    SwingUtilities.invokeLater(
       new Runnable() {
         public void run() {
           try {
             new CalorieCounterController(new CalorieCounterPersistenceHandler("data/"));
           }
           catch (Exception e) {
             StringWriter writer = new StringWriter();
             e.printStackTrace(new PrintWriter(writer));
             JOptionPane.showMessageDialog(null, "FATAL ERROR: " + writer.toString());
           }
         }
       }
    );
  }
}
