package seahawk.caloriecounter.domain.api;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class FoodstuffCategory implements Comparable<FoodstuffCategory> {
  private static final Set<FoodstuffCategory> categories = new TreeSet<>();

  private String displayName;

  private FoodstuffCategory(String displayName) {
    this.displayName = displayName.toUpperCase();
    categories.add(this);
  }

  public static FoodstuffCategory findOrCreate(String categoryName) {
    categoryName = categoryName.toUpperCase();
    for (FoodstuffCategory category : categories) {
      if (category.displayName.equals(categoryName))
        return category;
    }
    return new FoodstuffCategory(categoryName);
  }

  public static void remove(FoodstuffCategory category) {
    categories.remove(category);
  }

  public static Set<FoodstuffCategory> values() {
    return Collections.unmodifiableSet(categories);
  }

  @Override
  public String toString() {
    return displayName;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null || !(o instanceof FoodstuffCategory)) return false;

    FoodstuffCategory that = (FoodstuffCategory) o;
    return this.displayName.equals(that.displayName);
  }

  @Override
  public int hashCode() {
    return displayName.hashCode();
  }

  @Override
  public int compareTo(FoodstuffCategory category) {
    return displayName.compareTo(category.displayName);
  }
}
