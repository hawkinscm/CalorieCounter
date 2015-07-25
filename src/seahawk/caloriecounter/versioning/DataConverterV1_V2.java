package seahawk.caloriecounter.versioning;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.api.date.Date;
import seahawk.caloriecounter.domain.impl.*;
import seahawk.caloriecounter.domain.persistence.CalorieCounterPersistenceHandler;
import seahawk.xmlhandler.XmlException;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataConverterV1_V2 {
  public static void main(String args[]) {
    DataConverterV1_V2 converter = new DataConverterV1_V2();

    FoodStoreImpl foodStore = new FoodStoreImpl();
    try (FileReader reader = new FileReader("fooddata00.xml")) {
      converter.convertAndPopulateFoodStore(reader, foodStore);
    }
    catch (Exception e) {
      e.printStackTrace();
      return;
    }

    DailyRecordStoreImpl recordStore;
    try (FileReader reader = new FileReader("dailydata00.xml")) {
      recordStore = converter.convertDailyRecords(reader, foodStore);
    }
    catch (Exception e) {
      e.printStackTrace();
      return;
    }

    try {
      CalorieCounterPersistenceHandler handler = new CalorieCounterPersistenceHandler("data_v2");
      handler.saveFoodstuffs(foodStore.getFoodstuffs(FoodSortType.NAME, false));
      handler.saveMeals(foodStore.getMeals(FoodSortType.NAME, false));
      for (DailyRecord record : recordStore.getDailyRecords())
        handler.saveDailyRecord(record, recordStore.getHistory());
      handler.saveDailyRecordHistory(recordStore.getHistory());
    }
    catch (XmlException e) {
      e.printStackTrace();
    }
  }

  private FileReader in;
  private int lineCount;

  private void convertAndPopulateFoodStore(FileReader reader, FoodStoreImpl foodStore) throws Exception {
    try {
      in = reader;
      lineCount = 1;

      checkTag("NutritionManager");

      checkTag("Categories");
      String tag = parseTag();
      while(!tag.equals("/Categories")) {
        if(!tag.equals("Category"))
          tagErr("Category", tag);
        parseText();

        checkTagName("/Category");
        tag = parseTag();
      }

      checkTag("FoodStuffs");
      tag = parseTag();
      while(!tag.equals("/FoodStuffs")) {
        if(!tag.equals("FoodStuff"))
          tagErr("FoodStuff", tag);

        FoodstuffCategory category = FoodstuffCategory.findOrCreate(getTagText("Category"));
        FoodstuffImpl food = new FoodstuffImpl(getTagText("Name"), category);

        tag = parseTag();
        if(tag.equals("Amount")) {
          food.setAmount(new PositiveDecimalNumber(Double.parseDouble(parseText())));
          checkTagName("/Amount");
          tag = parseTag();
        }
        if(tag.equals("Unit")) {
          food.setUnit(MeasurementUnit.findOrCreate(parseText()));
          checkTagName("/Unit");
          tag = parseTag();
        }

        if(!tag.equals("NutritionFacts"))
          tagErr("NutritionFacts", tag);

        tag = parseTag();
        while(!tag.equals("/NutritionFacts")) {
          PositiveDecimalNumber amount = new PositiveDecimalNumber(Double.parseDouble(parseText()));
          ((NutritionFactsImpl) food.getFacts()).setAmount(getNutrientType(tag), amount);

          checkTagName("/" + tag);
          tag = parseTag();
        }

        checkTag("/FoodStuff");
        foodStore.addFood(food);
        tag = parseTag();
      }

      checkTag("Meals");
      tag = parseTag();
      while(!tag.equals("/Meals")) {
        if(!tag.equals("Meal"))
          tagErr("Meal", tag);

        String mealName = getTagText("Name");
        MealImpl meal = (MealImpl) foodStore.getMeal(mealName);
        if(meal == null)
          meal = new MealImpl(mealName);

        tag = parseTag();
        if(tag.equals("Amount")) {
          meal.setAmount(new PositiveDecimalNumber(Double.parseDouble(parseText())));
          checkTagName("/Amount");
          tag = parseTag();
        }
        if(tag.equals("Unit")) {
          meal.setUnit(MeasurementUnit.findOrCreate(parseText()));
          checkTagName("/Unit");
          tag = parseTag();
        }

        while(!tag.equals("/Meal")) {
          if(!tag.equals("Ingredient"))
            tagErr("Ingredient", tag);

          String category = getTagText("Category");
          Food food;
          if(category.equals("MEAL")) {
            mealName = getTagText("Name");
            food = foodStore.getMeal(mealName);
            if(food == null) {
              food = new MealImpl(mealName);
              foodStore.addFood(food);
            }
          }
          else
            food = foodStore.getFoodstuff(getTagText("Name"));

          PositiveDecimalNumber amount = new PositiveDecimalNumber(Double.parseDouble(getTagText("Amount")));
          String unitStr = getTagText("Unit");
          MeasurementUnit unit = (unitStr.equals("OTHER")) ? null : MeasurementUnit.findOrCreate(unitStr);

          meal.addIngredient(new IngredientImpl(food, amount, unit));
          checkTag("/Ingredient");

          tag = parseTag();
        }

        foodStore.addFood(meal);
        tag = parseTag();
      }

      for (Meal meal : foodStore.getMeals(FoodSortType.NAME, false))
        for (Ingredient ingredient : meal.getIngredients()) {
          if (ingredient.getUnit() == null) {
            ((IngredientImpl) ingredient).setUnit(ingredient.getFood().getUnit());
          }
          else if (!ingredient.getUnit().getConvertibleUnits().contains(ingredient.getFood().getUnit())) {
            System.out.println(meal.getName() + "  " + ingredient.getFood().getName() + " " + ingredient.getUnit() + " : " + ingredient.getFood().getUnit());
            ((IngredientImpl) ingredient).setUnit(ingredient.getFood().getUnit());
          }
        }

      checkTag("/NutritionManager");
    }
    catch (Exception e) {
      throw new Exception("Error:(Line: " + lineCount + ")\n" + e.getMessage());
    }
  }

  private DailyRecordStoreImpl convertDailyRecords(FileReader reader, FoodStore foodStore) throws Exception {
    List<DailyRecord> dailyRecords = new ArrayList<>();
    Map<Date, NutritionFacts> dailySummaryMap = new HashMap<>();
    try {
      in = reader;
      lineCount = 1;

      checkTag("NutritionManager");

      checkTag("DailyRecords");
      String tag = parseTag();
      while(!tag.equals("/DailyRecords")) {
        if(!tag.equals("DailyRecord"))
          tagErr("DailyRecord", tag);

        Date date = convertFromV1Date(getTagText("Date"));
        DailyRecordImpl record = new DailyRecordImpl(date);

        tag = parseTag();
        while(!tag.equals("/DailyRecord")) {
          if(!tag.equals("Ingredient"))
            tagErr("Ingredient", tag);

          String category = getTagText("Category");
          Food food;
          if(category.equals("MEAL")) {
            String name = getTagText("Name");
            food = foodStore.getMeal(name);
            if(food == null)
              System.out.println(name);
          }
          else
            food = foodStore.getFoodstuff(getTagText("Name"));

          PositiveDecimalNumber amount = new PositiveDecimalNumber(Double.parseDouble(getTagText("Amount")));
          String unitStr = getTagText("Unit");
          MeasurementUnit unit = (unitStr.equals("OTHER")) ? null : MeasurementUnit.findOrCreate(unitStr);
          if (unit == null) {
            unit = food.getUnit();
          }
          else if (!unit.getConvertibleUnits().contains(food.getUnit())) {
            System.out.println(record.getDate() + "  " + food.getName() + " " + unit + " : " + food.getUnit());
            unit = food.getUnit();
          }

          record.addEatenFood(new IngredientImpl(food, amount, unit));

          checkTag("/Ingredient");

          tag = parseTag();
        }

        dailyRecords.add(record);
        dailySummaryMap.put(record.getDate(), record.getFacts());
        tag = parseTag();
      }

      checkTag("/NutritionManager");

      DailyRecordStoreImpl dailyRecordStore = new DailyRecordStoreImpl(new DailyRecordHistoryImpl(dailySummaryMap));
      for (DailyRecord record : dailyRecords)
        dailyRecordStore.add(record);
      return dailyRecordStore;
    }
    catch (Exception e) {
      throw new Exception("Error:(Line: " + lineCount + ")\n" + e.getMessage());
    }
  }

  private void tagErr(String expected, String found) throws Exception {
    throw new Exception("Expected <" + expected + ">\n" +
       "Found <" + found + ">");
  }

  private char getNextChar() throws Exception {
    char inChar = (char)in.read();
    if(inChar == '\n')
      lineCount++;

    return inChar;
  }

  private String parseTag() throws Exception {
    while(in.ready()) {
      char curChar = getNextChar();

      if(curChar == '<')
        return parseTagName();
    }
    throw new Exception("Expected: <...\nFound: EOF");
  }

  private String parseTagName() throws Exception {
    String tag = "";
    while(in.ready()) {
      char curChar = getNextChar();
      if(curChar == '>')
        return tag;
      if(!Character.isWhitespace(curChar))
        tag += curChar;
    }

    throw new Exception("No closing tag bracket: \"<" + tag + "\"");
  }

  private void checkTag(String name) throws Exception {
    String tag = parseTag();
    if(!tag.equals(name))
      tagErr(name, tag);
  }

  private void checkTagName(String name) throws Exception {
    String tag = parseTagName();
    if(!tag.equals(name))
      tagErr(name, tag);
  }

  private String parseText() throws Exception {
    String text = "";
    while(in.ready()) {
      char curChar = getNextChar();
      if(curChar == '<')
        return text;
      if(curChar != '\n' && curChar != '\r')
        text += curChar;
    }

    throw new Exception("Expected Tag after tag innertext: \"" + text + "\"");
  }

  private String getTagText(String tagName) throws Exception {
    checkTag(tagName);
    String text = parseText();
    checkTagName("/" + tagName);

    return text;
  }

  private NutrientType getNutrientType(String displayName) {
    displayName = displayName.replace('_', ' ');
    for (NutrientType type : NutrientType.values())
      if (type.getDisplayName().equals(displayName))
        return type;
    throw new IllegalArgumentException("Not a valid NutrientType: " + displayName);
  }

  private Date convertFromV1Date(String v1Date) {
    String[] datePieces = v1Date.trim().split("/");
    String year = datePieces[2];
    String month = datePieces[0];
    if (month.length() == 1)
      month = "0" + month;
    String day = datePieces[1];
    if (day.length() == 1)
      day = "0" + day;
    return new Date(year + month + day);
  }
}
