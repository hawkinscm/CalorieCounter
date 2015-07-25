package seahawk.caloriecounter.domain.impl;

import seahawk.caloriecounter.domain.api.NutrientType;
import seahawk.caloriecounter.domain.api.NutritionFacts;
import seahawk.caloriecounter.domain.api.PositiveDecimalNumber;

import java.util.HashMap;
import java.util.Map;

public class NutritionFactsImpl implements NutritionFacts {
	private Map<NutrientType, PositiveDecimalNumber> nutrientAmountMap;
	
	public NutritionFactsImpl() {
		nutrientAmountMap = new HashMap<>();
	}

  public NutritionFactsImpl(Map<NutrientType, PositiveDecimalNumber> nutrientAmountMap) {
    this.nutrientAmountMap = new HashMap<>(nutrientAmountMap);
  }

  public NutritionFactsImpl(NutritionFacts nutritionFacts) {
    this.nutrientAmountMap = new HashMap<>(((NutritionFactsImpl) nutritionFacts).nutrientAmountMap);
  }

  public NutritionFactsImpl(NutritionFacts nutritionFacts, PositiveDecimalNumber conversionFactor) {
    if (conversionFactor == new PositiveDecimalNumber(1)) {
      this.nutrientAmountMap = new HashMap<>(((NutritionFactsImpl) nutritionFacts).nutrientAmountMap);
    }
    else {
      nutrientAmountMap = new HashMap<>(NutrientType.values().length);
      for (NutrientType type : ((NutritionFactsImpl) nutritionFacts).nutrientAmountMap.keySet())
        nutrientAmountMap.put(type, ((NutritionFactsImpl) nutritionFacts).nutrientAmountMap.get(type).multiply(conversionFactor));
    }
  }

  @Override
	public PositiveDecimalNumber getAmount(NutrientType type) {
		return nutrientAmountMap.containsKey(type) ? nutrientAmountMap.get(type) : new PositiveDecimalNumber(0);
	}

	public void setAmount(NutrientType type, PositiveDecimalNumber amount) {
    nutrientAmountMap.put(type, amount);
	}

  public void addAmounts(NutritionFacts facts) {
    for (NutrientType type : ((NutritionFactsImpl) facts).nutrientAmountMap.keySet()) {
      PositiveDecimalNumber amount = ((NutritionFactsImpl) facts).nutrientAmountMap.get(type);
      if (this.nutrientAmountMap.containsKey(type)) {
        nutrientAmountMap.put(type, nutrientAmountMap.get(type).add(amount));
      }
      else {
        nutrientAmountMap.put(type, amount);
      }
    }
	}
}
