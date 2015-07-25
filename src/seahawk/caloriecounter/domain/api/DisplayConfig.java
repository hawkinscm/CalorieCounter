package seahawk.caloriecounter.domain.api;

public class DisplayConfig {
  private int sizeWidth;
  private int sizeHeight;
  private int locationX;
  private int locationY;

  private int foodstuffsTableSortPersistenceValue;
  private boolean reverseFoodstuffsTableSort;
  private int mealsTableSortPersistenceValue;
  private boolean reverseMealsTableSort;

  public DisplayConfig(int sizeWidth, int sizeHeight, int locationX, int locationY,
                       int foodstuffsTableSortPersistenceValue, boolean reverseFoodstuffsTableSort,
                       int mealsTableSortPersistenceValue, boolean reverseMealsTableSort) {
    this.sizeWidth = sizeWidth;
    this.sizeHeight = sizeHeight;
    this.locationX = locationX;
    this.locationY = locationY;

    this.foodstuffsTableSortPersistenceValue = foodstuffsTableSortPersistenceValue;
    this.reverseFoodstuffsTableSort = reverseFoodstuffsTableSort;
    this.mealsTableSortPersistenceValue = mealsTableSortPersistenceValue;
    this.reverseMealsTableSort = reverseMealsTableSort;
  }

  public int getSizeWidth() {
    return sizeWidth;
  }

  public int getSizeHeight() {
    return sizeHeight;
  }

  public int getLocationX() {
    return locationX;
  }

  public int getLocationY() {
    return locationY;
  }

  public int getFoodstuffsTableSortPersistenceValue() {
    return foodstuffsTableSortPersistenceValue;
  }

  public boolean isReverseFoodstuffsTableSort() {
    return reverseFoodstuffsTableSort;
  }

  public int getMealsTableSortPersistenceValue() {
    return mealsTableSortPersistenceValue;
  }

  public boolean isReverseMealsTableSort() {
    return reverseMealsTableSort;
  }
}
