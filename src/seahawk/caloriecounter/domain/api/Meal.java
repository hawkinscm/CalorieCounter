package seahawk.caloriecounter.domain.api;

import java.util.List;

public interface Meal extends Food {
  public List<Ingredient> getIngredients();
}
