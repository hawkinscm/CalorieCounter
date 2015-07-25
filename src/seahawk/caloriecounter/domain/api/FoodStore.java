package seahawk.caloriecounter.domain.api;

import java.util.TreeSet;

public interface FoodStore {
  public Food getFood(String foodName);

  public Foodstuff getFoodstuff(String foodName) ;

  public Meal getMeal(String mealName);

  public TreeSet<String> getFoodNames();

  public TreeSet<String> getMealNames();

  public TreeSet<Foodstuff> getFoodstuffs(FoodSortType sortType, boolean reverseSort);

  public TreeSet<Meal> getMeals(FoodSortType sortType, boolean reverseSort);

  public boolean containsFood(String foodName);
}
