package seahawk.caloriecounter.domain.api;

import junit.framework.TestCase;

public class TestFoodstuffCategory extends TestCase {
  public void testFoodstuffCategory() {
    assertEquals(0, FoodstuffCategory.values().size());

    FoodstuffCategory category = FoodstuffCategory.findOrCreate("a");
    assertEquals(1, FoodstuffCategory.values().size());
    assertEquals("A", category.toString());
    assertSame(category, FoodstuffCategory.findOrCreate("a"));
    assertEquals(category, FoodstuffCategory.findOrCreate("a"));
    assertEquals(1, FoodstuffCategory.values().size());
    assertSame(category, FoodstuffCategory.findOrCreate("A"));
    assertEquals(1, FoodstuffCategory.values().size());

    FoodstuffCategory category2 = FoodstuffCategory.findOrCreate("apple");
    assertEquals(2, FoodstuffCategory.values().size());
    assertEquals("APPLE", category2.toString());
    assertSame(category2, FoodstuffCategory.findOrCreate("apple"));
    assertEquals(category2, FoodstuffCategory.findOrCreate("Apple"));
    assertEquals(2, FoodstuffCategory.values().size());
    assertSame(category2, FoodstuffCategory.findOrCreate("APPLE"));
    assertEquals(2, FoodstuffCategory.values().size());

    assertFalse(category.equals(category2));

    FoodstuffCategory.remove(category);
    assertEquals(1, FoodstuffCategory.values().size());
    FoodstuffCategory.findOrCreate("A");
    assertEquals(2, FoodstuffCategory.values().size());

    FoodstuffCategory.remove(category);
    FoodstuffCategory.remove(category2);
    assertEquals(0, FoodstuffCategory.values().size());
  }
}
