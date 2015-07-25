package seahawk.caloriecounter.domain.persistence;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.api.date.Date;
import seahawk.caloriecounter.domain.api.log.SimpleLogger;
import seahawk.caloriecounter.domain.impl.*;
import seahawk.xmlhandler.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalorieCounterPersistenceHandler {
  private static final String CONFIG_FILENAME = "CC.config";
  private static final String FOODSTUFFS_FILENAME = "foodstuffs.xml";
  private static final String MEALS_FILENAME = "meals.xml";
  private static final String DAILY_RECORD_HISTORY_FILENAME = "history.xml";
  private static final String DATE_REGEX = "(\\d{8})";
  private static final String DAILY_RECORD_FILENAME = "daily_record_" + DATE_REGEX + ".xml";

  private static final PositiveDecimalNumber ZERO = new PositiveDecimalNumber(0);

  private final String dataDirectoryPath;

  public CalorieCounterPersistenceHandler(String dataDirectoryPath) {
    if (!dataDirectoryPath.endsWith("/")) dataDirectoryPath += "/";
    this.dataDirectoryPath = dataDirectoryPath;
  }

  public DisplayConfig loadDisplayConfig() throws XmlException {
    File configFile = new File(dataDirectoryPath + CONFIG_FILENAME);
    if (!configFile.exists()) {
      return null;
    }

    XmlTag configTag = new XmlReader().parseXMLFile(configFile).getSubTag("Config");

    try {
      XmlTag displayTag = configTag.getSubTag("Display");
      XmlTag sizeTag = displayTag.getSubTag("Size");
      XmlTag locationTag = displayTag.getSubTag("Location");
      XmlTag foodstuffsTableSortTag = displayTag.getSubTag("FoodstuffsTableSort");
      XmlTag mealsTableSortTag = displayTag.getSubTag("MealsTableSort");

      int sizeWidth = Integer.parseInt(sizeTag.getAttributeValue("width"));
      int sizeHeight = Integer.parseInt(sizeTag.getAttributeValue("height"));
      int locationX = Integer.parseInt(locationTag.getAttributeValue("x"));
      int locationY = Integer.parseInt(locationTag.getAttributeValue("y"));
      int foodstuffsSortValue = Integer.parseInt(foodstuffsTableSortTag.getAttributeValue("value"));
      boolean reverseFoodstuffs = Boolean.parseBoolean(foodstuffsTableSortTag.getAttributeValue("reverse"));
      int mealsSortValue = Integer.parseInt(mealsTableSortTag.getAttributeValue("value"));
      boolean reverseMeals = Boolean.parseBoolean(mealsTableSortTag.getAttributeValue("reverse"));
      return new DisplayConfig(sizeWidth, sizeHeight, locationX, locationY, foodstuffsSortValue, reverseFoodstuffs, mealsSortValue, reverseMeals);
    }
    catch (NumberFormatException e) {
      throw new XmlException("Corrupted XML value in Config: " + e.getMessage());
    }
  }

  public FoodStore loadFoodStore() throws XmlException {
    FoodStoreImpl foodStore = new FoodStoreImpl();
    populateFoodstuffs(foodStore);
    populateMeals(foodStore);
    return foodStore;
  }

  public FoodStore loadEmptyFoodStore() {
    return new FoodStoreImpl();
  }

  private void populateFoodstuffs(FoodStoreImpl foodStore) throws XmlException {
    File foodstuffsFile = new File(dataDirectoryPath + FOODSTUFFS_FILENAME);
    if (!foodstuffsFile.exists()) {
      return;
    }

    try {
      XmlTag foodstuffsTag = new XmlReader().parseXMLFile(foodstuffsFile).getSubTag("Foodstuffs");

      for (XmlTag foodstuffTag : foodstuffsTag.getSubTags("Foodstuff")) {
        String foodName = foodstuffTag.getAttributeValue("Name");
        FoodstuffCategory category = FoodstuffCategory.findOrCreate(foodstuffTag.getAttributeValue("Category"));
        PositiveDecimalNumber foodstuffAmount = new PositiveDecimalNumber(foodstuffTag.getSubTag("Amount").getContent());
        MeasurementUnit unit = MeasurementUnit.findOrCreate(foodstuffTag.getSubTag("Unit").getContent());

        NutritionFacts facts = parseNutritionFacts(foodstuffTag);

        foodStore.addFood(new FoodstuffImpl(foodName, category, foodstuffAmount, unit, facts));
      }
    }
    catch (XmlException e) {
      SimpleLogger.error("Unable to parse foodstuffs file: " + foodstuffsFile.getAbsolutePath() + ". ", e);
      File backupFile = new File(dataDirectoryPath + FOODSTUFFS_FILENAME + ".bak_" + System.currentTimeMillis());
      try {
        Files.copy(foodstuffsFile.toPath(), backupFile.toPath());
      }
      catch (IOException ex) {
        throw new RuntimeException(e.getMessage(), ex);
      }
      throw e;
    }
  }

  private void populateMeals(FoodStoreImpl foodStore) throws XmlException {
    File mealsFile = new File(dataDirectoryPath + MEALS_FILENAME);
    if (!mealsFile.exists()) {
      return;
    }

    try {
      XmlTag mealsTag = new XmlReader().parseXMLFile(mealsFile).getSubTag("Meals");

      List<XmlTag> mealTags = mealsTag.getSubTags("Meal");
      for (XmlTag mealTag : mealTags) {
        String mealName = mealTag.getAttributeValue("Name");
        PositiveDecimalNumber mealAmount = new PositiveDecimalNumber(mealTag.getSubTag("Amount").getContent());
        MeasurementUnit mealUnit = MeasurementUnit.findOrCreate(mealTag.getSubTag("Unit").getContent());

        foodStore.addFood(new MealImpl(mealName, mealAmount, mealUnit));
      }

      for (XmlTag mealTag : mealTags) {
        MealImpl meal = (MealImpl) foodStore.getMeal(mealTag.getAttributeValue("Name"));
        List<Ingredient> ingredients = loadIngredients(mealTag.getSubTag("Ingredients"), foodStore);
        meal.setIngredients(ingredients);
      }
    }
    catch (XmlException e) {
      SimpleLogger.error("Unable to parse meals file: " + mealsFile.getAbsolutePath() + ". ", e);
      File backupFile = new File(dataDirectoryPath + MEALS_FILENAME + ".bak_" + System.currentTimeMillis());
      try {
        Files.copy(mealsFile.toPath(), backupFile.toPath());
      }
      catch (IOException ex) {
        throw new RuntimeException(e.getMessage(), ex);
      }
      throw e;
    }
  }

  public DailyRecordStore loadDailyRecordStore() {
    //noinspection ConstantConditions
    File dataDirectory = new File(dataDirectoryPath);
    if (!dataDirectory.exists())
      if (!dataDirectory.mkdir())
        throw new IllegalStateException("Cannot find or create directory: " + dataDirectory.getAbsolutePath());

    DailyRecordStoreImpl dailyRecordStore = new DailyRecordStoreImpl(loadDailyRecordHistory());

    final Pattern pattern = Pattern.compile(DAILY_RECORD_FILENAME);
    //noinspection ConstantConditions
    for (File file : dataDirectory.listFiles()) {
      Matcher matcher = pattern.matcher(file.getName());
      if (matcher.matches())
        dailyRecordStore.addEmptyDailyRecord(new Date(matcher.group(1)));
    }
    return dailyRecordStore;
  }

  public DailyRecordStore loadEmptyDailyRecordStore() {
    return new DailyRecordStoreImpl(loadDailyRecordHistory());
  }

  private DailyRecordHistory loadDailyRecordHistory() {
    File historyFile = new File(dataDirectoryPath + DAILY_RECORD_HISTORY_FILENAME);
    if (!historyFile.exists()) {
      return new DailyRecordHistoryImpl(new HashMap<>());
    }

    try {
      XmlTag historyTag = new XmlReader().parseXMLFile(historyFile).getSubTag("DailyRecordHistory");

      Map<Date, NutritionFacts> dailySummaryMap = new HashMap<>();
      List<XmlTag> dailySummaryTags = historyTag.getSubTags("DailySummary");
      for (XmlTag dailySummaryTag : dailySummaryTags) {
        Date date = new Date(dailySummaryTag.getAttributeValue("Date"));

        NutritionFacts facts = parseNutritionFacts(dailySummaryTag);

        dailySummaryMap.put(date, facts);
      }

      return new DailyRecordHistoryImpl(dailySummaryMap);
    }
    catch (XmlException e) {
      SimpleLogger.error("Unable to parse daily record history file: " + historyFile.getAbsolutePath() + ". ", e);
      File backupFile = new File(dataDirectoryPath + DAILY_RECORD_HISTORY_FILENAME + ".bak_" + System.currentTimeMillis());
      try {
        Files.copy(historyFile.toPath(), backupFile.toPath());
      }
      catch (IOException ex) {
        SimpleLogger.error("Unable to back-up record history file: " + historyFile.getAbsolutePath() + ". ", ex);
      }
      return new DailyRecordHistoryImpl(new HashMap<>());
    }
  }

  private NutritionFacts parseNutritionFacts(XmlTag dailySummaryTag) throws XmlException {
    Map<NutrientType, PositiveDecimalNumber> nutrientAmountMap = new HashMap<>();
    for (XmlTag nutrientTypeTag : dailySummaryTag.getSubTag("NutritionFacts").getSubTags()) {
      NutrientType nutrientType;
      if (nutrientTypeTag.getName().equalsIgnoreCase(NutrientType.FORMER_SUGARS_NAME)) {
        nutrientType = NutrientType.SUGARS;
      }
      else {
        nutrientType = NutrientType.valueOf(nutrientTypeTag.getName());
      }
      nutrientAmountMap.put(nutrientType, new PositiveDecimalNumber(nutrientTypeTag.getContent()));
    }
    return new NutritionFactsImpl(nutrientAmountMap);
  }

  public DailyRecord loadDailyRecord(Date date, DailyRecordStore recordStore, FoodStore foodStore) throws XmlException {
    String dailyRecordFilename = DAILY_RECORD_FILENAME.replace(DATE_REGEX, date.toString());
    File file = new File(dataDirectoryPath + dailyRecordFilename);
    if (!file.exists())
      return null;

    try {
      XmlTag dailyRecordTag = new XmlReader().parseXMLFile(file).getSubTag("DailyRecord");

      List<Ingredient> eatenFoods = loadIngredients(dailyRecordTag.getSubTag("Ingredients"), foodStore);

      DailyRecordImpl dailyRecord = new DailyRecordImpl(date, eatenFoods);
      ((DailyRecordStoreImpl) recordStore).add(dailyRecord);
      return dailyRecord;
    }
    catch (XmlException e) {
      SimpleLogger.error("Unable to parse daily record file: " + file.getAbsolutePath() + ". ", e);
      File backupFile = new File(dataDirectoryPath + dailyRecordFilename + ".bak_" + System.currentTimeMillis());
      try {
        Files.copy(file.toPath(), backupFile.toPath());
      }
      catch (IOException ex) {
        throw new RuntimeException(e.getMessage(), ex);
      }
      throw e;
    }
  }

  private List<Ingredient> loadIngredients(XmlTag ingredientsTag, FoodStore foodStore) throws XmlException {
    List<Ingredient> ingredients = new ArrayList<>();
    for (XmlTag ingredientTag : ingredientsTag.getSubTags()) {
      String foodName = ingredientTag.getAttributeValue("Name");
      Food food = foodStore.getFood(foodName);
      if (food == null) {
        continue;
      }
      PositiveDecimalNumber ingredientAmount = new PositiveDecimalNumber(ingredientTag.getSubTag("Amount").getContent());
      MeasurementUnit ingredientUnit = MeasurementUnit.findOrCreate(ingredientTag.getSubTag("Unit").getContent());
      ingredients.add(new IngredientImpl(food, ingredientAmount, ingredientUnit));
    }
    return ingredients;
  }

  public void saveDisplayConfig(DisplayConfig config) throws XmlException {
    XmlTag configTag = new XmlTag("Config");
    XmlTag displayTag = new XmlTag("Display");

    XmlTag sizeTag = new XmlTag("Size");
    sizeTag.addAttribute("width", String.valueOf(config.getSizeWidth()));
    sizeTag.addAttribute("height", String.valueOf(config.getSizeHeight()));
    displayTag.addSubTag(sizeTag);

    XmlTag locationTag = new XmlTag("Location");
    locationTag.addAttribute("x", String.valueOf(config.getLocationX()));
    locationTag.addAttribute("y", String.valueOf(config.getLocationY()));
    displayTag.addSubTag(locationTag);

    XmlTag foodstuffsTableSortTag = new XmlTag("FoodstuffsTableSort");
    foodstuffsTableSortTag.addAttribute("value", String.valueOf(config.getFoodstuffsTableSortPersistenceValue()));
    foodstuffsTableSortTag.addAttribute("reverse", String.valueOf(config.isReverseFoodstuffsTableSort()));
    displayTag.addSubTag(foodstuffsTableSortTag);

    XmlTag mealsTableSortTag = new XmlTag("MealsTableSort");
    mealsTableSortTag.addAttribute("value", String.valueOf(config.getMealsTableSortPersistenceValue()));
    mealsTableSortTag.addAttribute("reverse", String.valueOf(config.isReverseMealsTableSort()));
    displayTag.addSubTag(mealsTableSortTag);

    configTag.addSubTag(displayTag);

    new XmlWriter().writeXMLToFile(configTag, dataDirectoryPath + CONFIG_FILENAME);
  }

  public void saveFoodstuffs(TreeSet<Foodstuff> foodstuffs) throws XmlException {
    XmlTag foodstuffsTag = new XmlTag("Foodstuffs");
    for (Foodstuff foodstuff : foodstuffs) {
      XmlTag foodstuffTag = new XmlTag("Foodstuff");

      addFoodDataToTag(foodstuffTag, foodstuff);

      foodstuffTag.addAttribute("Category", foodstuff.getCategory().toString());

      XmlTag nutritionFactsTag = new XmlTag("NutritionFacts");
      for (NutrientType type : NutrientType.values()) {
        PositiveDecimalNumber amount = foodstuff.getFacts().getAmount(type);
        if (amount.equals(ZERO))
          continue;

        XmlTag nutrientTag = new XmlTag(type.name());
        nutrientTag.setContent(amount.asPreciseDouble());
        nutritionFactsTag.addSubTag(nutrientTag);
      }
      foodstuffTag.addSubTag(nutritionFactsTag);

      foodstuffsTag.addSubTag(foodstuffTag);
    }

    new XmlWriter().writeXMLToFile(foodstuffsTag, dataDirectoryPath + FOODSTUFFS_FILENAME);
  }

  public void saveMeals(TreeSet<Meal> meals) throws XmlException {
    XmlTag mealsTag = new XmlTag("Meals");
    for (Meal meal : meals) {
      XmlTag mealTag = new XmlTag("Meal");

      addFoodDataToTag(mealTag, meal);

      mealTag.addSubTag(createIngredientsTag(meal.getIngredients()));

      mealsTag.addSubTag(mealTag);
    }

    new XmlWriter().writeXMLToFile(mealsTag, dataDirectoryPath + MEALS_FILENAME);
  }

  public void saveDailyRecordHistory(DailyRecordHistory history) throws XmlException {
    XmlTag historyTag = new XmlTag("DailyRecordHistory");

    for (Map.Entry<Date, NutritionFacts> dailySummary : ((DailyRecordHistoryImpl) history).getDailySummaryMap().entrySet()) {
      XmlTag dailySummaryTag = new XmlTag("DailySummary");
      dailySummaryTag.addAttribute(new XmlAttribute("Date", dailySummary.getKey().toString()));

      NutritionFacts facts = dailySummary.getValue();
      XmlTag nutritionFactsTag = new XmlTag("NutritionFacts");
      for (NutrientType type : NutrientType.values()) {
        PositiveDecimalNumber amount = facts.getAmount(type);
        if (amount.equals(ZERO))
          continue;

        XmlTag nutrientTag = new XmlTag(type.name());
        nutrientTag.setContent(amount.asPreciseDouble());
        nutritionFactsTag.addSubTag(nutrientTag);
      }
      dailySummaryTag.addSubTag(nutritionFactsTag);

      historyTag.addSubTag(dailySummaryTag);
    }

    new XmlWriter().writeXMLToFile(historyTag, dataDirectoryPath + DAILY_RECORD_HISTORY_FILENAME);
  }

  public void saveDailyRecord(DailyRecord dailyRecord, DailyRecordHistory history) throws XmlException {
    ((DailyRecordHistoryImpl) history).createOrUpdateDailySummary(dailyRecord);

    XmlTag dailyRecordTag = new XmlTag("DailyRecord");
    dailyRecordTag.addSubTag(createIngredientsTag(dailyRecord.getEatenFoods()));

    String dailyRecordFilename = DAILY_RECORD_FILENAME.replace(DATE_REGEX, dailyRecord.getDate().toString());
    new XmlWriter().writeXMLToFile(dailyRecordTag, dataDirectoryPath + dailyRecordFilename);
  }

  private XmlTag createIngredientsTag(List<Ingredient> ingredients) {
    XmlTag ingredientsTag = new XmlTag("Ingredients");
    for (Ingredient ingredient : ingredients) {
      XmlTag ingredientTag = new XmlTag("Ingredient");

      addFoodDataToTag(ingredientTag, ingredient.getFood().getName(), ingredient.getAmount(), ingredient.getUnit());

      ingredientsTag.addSubTag(ingredientTag);
    }

    return ingredientsTag;
  }

  private void addFoodDataToTag(XmlTag tag, Food food) {
    addFoodDataToTag(tag, food.getName(), food.getAmount(), food.getUnit());
  }

  private void addFoodDataToTag(XmlTag tag, String foodName, PositiveDecimalNumber amount, MeasurementUnit unit) {
    tag.addAttribute("Name", foodName);

    XmlTag amountTag = new XmlTag("Amount");
    amountTag.setContent(amount.asPreciseDouble());
    tag.addSubTag(amountTag);

    XmlTag unitTag = new XmlTag("Unit");
    unitTag.setContent(unit.toString());
    tag.addSubTag(unitTag);
  }
}
