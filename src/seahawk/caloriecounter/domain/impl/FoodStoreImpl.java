package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.*;

import java.util.HashMap;
import java.util.TreeSet;

public class FoodStoreImpl implements FoodStore {
	private HashMap<String, Foodstuff> foodstuffNameMap;
  private HashMap<String, Meal> mealNameMap;

  public FoodStoreImpl() {
    this.foodstuffNameMap = new HashMap<>();
    this.mealNameMap = new HashMap<>();
  }

  @Override
  public Food getFood(String foodName) {
    if (foodstuffNameMap.containsKey(foodName))
      return getFoodstuff(foodName);

    return getMeal(foodName);
  }

  @Override
  public Foodstuff getFoodstuff(String foodName) {
    return foodstuffNameMap.get(foodName);
  }

  @Override
  public Meal getMeal(String mealName) {
    return mealNameMap.get(mealName);
  }

  @Override
  public TreeSet<String> getFoodNames() {
    TreeSet<String> foodNames = new TreeSet<>(foodstuffNameMap.keySet());
    foodNames.addAll(mealNameMap.keySet());
    return foodNames;
  }

  @Override
  public TreeSet<String> getMealNames() {
    return new TreeSet<>(mealNameMap.keySet());
  }

  @Override
  public TreeSet<Foodstuff> getFoodstuffs(FoodSortType sortType, boolean reverseSort) {
    TreeSet<Foodstuff> foodstuffs = new TreeSet<>(new FoodSortComparator(sortType, reverseSort));
    foodstuffs.addAll(foodstuffNameMap.values());
    return foodstuffs;
  }

  @Override
  public TreeSet<Meal> getMeals(FoodSortType sortType, boolean reverseSort) {
    TreeSet<Meal> meals = new TreeSet<>(new FoodSortComparator(sortType, reverseSort));
    meals.addAll(mealNameMap.values());
    return meals;
  }

  @Override
  public boolean containsFood(String foodName) {
    return foodstuffNameMap.containsKey(foodName) || mealNameMap.containsKey(foodName);
  }

  public boolean addFood(Food food) {
    if (foodstuffNameMap.containsKey(food.getName()) || mealNameMap.containsKey(food.getName()))
      return false;

    if (food.isMeal())
      mealNameMap.put(food.getName(), (Meal) food);
    else
      foodstuffNameMap.put(food.getName(), (Foodstuff) food);
    return true;
  }

  public boolean removeFood(Food food) {
    return food.isMeal() ? removeMeal((Meal) food) : removeFoodstuff((Foodstuff) food);
  }

  private boolean removeFoodstuff(Foodstuff foodstuff) {
    return (foodstuffNameMap.remove(foodstuff.getName()) != null);
  }

  private boolean removeMeal(Meal meal) {
    return (mealNameMap.remove(meal.getName()) != null);
  }
}
