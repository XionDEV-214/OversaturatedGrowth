package com.eisiadev.oversaturatedgrowth.accessory;

/** Duck interface that MixinPlayerCalorieMeter adds to Gluttonous Growth's PlayerCalorieMeter. */
public interface IOGCapacityBonus {

    int og$getCapacityBonus();

    void og$setCapacityBonus(int bonus);

    /** The stored max calories, without the accessory bonus. */
    int og$rawMaxCalories();
}
