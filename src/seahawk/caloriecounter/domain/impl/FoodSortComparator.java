package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.*;

import java.util.Comparator;

public class FoodSortComparator implements Comparator<Food> {
  private final FoodSortType foodSortType;
  private final boolean reverseSort;

  public FoodSortComparator(FoodSortType sortType, boolean reverseSort) {
    this.foodSortType = sortType;
    this.reverseSort = reverseSort;
  }

  @Override
  public int compare(Food food1, Food food2) {
    int foodSortTypeCompareValue = compareOnFoodSortType(food1, food2);
    if (foodSortTypeCompareValue == 0) {
      foodSortTypeCompareValue = food1.getName().compareTo(food2.getName());
    }

    if (reverseSort) {
      foodSortTypeCompareValue *= -1;
    }

    return foodSortTypeCompareValue;
  }

  private int compareOnFoodSortType(Food food1, Food food2) {
    if (foodSortType == FoodSortType.NAME) return food1.getName().compareTo(food2.getName());
    if (foodSortType == FoodSortType.AMOUNT) return food1.getAmount().compareTo(food2.getAmount());
    if (foodSortType == FoodSortType.UNIT) return food1.getUnit().compareTo(food2.getUnit());
    if (foodSortType == FoodSortType.FOODSTUFF_CATEGORY) return ((Foodstuff) food1).getCategory().compareTo(((Foodstuff) food2).getCategory());

    NutrientType nutrientType = foodSortType.getNutrientSortType();
    return food1.getFacts().getAmount(nutrientType).compareTo(food2.getFacts().getAmount(nutrientType));
  }
}
