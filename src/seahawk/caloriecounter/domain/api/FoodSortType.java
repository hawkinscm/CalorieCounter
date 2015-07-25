package seahawk.caloriecounter.domain.api;

import java.util.HashSet;
import java.util.Set;

public class FoodSortType {
  private static final Set<FoodSortType> nutrientSortTypes = new HashSet<>();
  static {
    int nextValue = 4;
    for (NutrientType type : NutrientType.values())
      nutrientSortTypes.add(new FoodSortType(nextValue++, type));
  }

  public final static FoodSortType NAME = new FoodSortType(0);
  public final static FoodSortType AMOUNT = new FoodSortType(1);
  public final static FoodSortType UNIT = new FoodSortType(2);
  public final static FoodSortType FOODSTUFF_CATEGORY = new FoodSortType(3);

  private final int persistenceValue;
  private NutrientType nutrientSortType;

  private FoodSortType(int persistenceValue) {
    this.persistenceValue = persistenceValue;
  }

  private FoodSortType(int persistenceValue, NutrientType type) {
    this(persistenceValue);
    nutrientSortType = type;
  }

  public static FoodSortType getByPersistenceValue(int persistenceValue) {
    if (persistenceValue == 0)
      return NAME;
    if (persistenceValue == 1)
      return AMOUNT;
    if (persistenceValue == 2)
      return UNIT;
    if (persistenceValue == 3)
      return FOODSTUFF_CATEGORY;

    for (FoodSortType sortType : nutrientSortTypes)
      if (sortType.persistenceValue == persistenceValue)
        return sortType;

    throw new IllegalArgumentException("persistenceValue " + persistenceValue + " not supported");
  }

  public int getPersistenceValue() {
    return persistenceValue;
  }

  public static FoodSortType getBy(NutrientType type) {
    for (FoodSortType sortType : nutrientSortTypes)
      if (sortType.nutrientSortType == type)
        return sortType;

    return null;
  }

  public NutrientType getNutrientSortType() {
    return nutrientSortType;
  }
}
