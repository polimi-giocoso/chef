package it.polimi.gq.chefperungiorno.model;

import java.util.List;

/**
 * Created by giovanniquattrocchi on 20/02/15.
 */
abstract class Level {

    protected final List<Dish> dishes;

    protected final int levelCode; // eg: 1 for Level 1

    protected Level(List<Dish> dishes, int levelCode){
        this.dishes = dishes;
        this.levelCode= levelCode;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public boolean hasDish(Dish dish){
        return dishes.contains(dish);
    }

    public Dish getDish(String name){
        for(Dish d : dishes)
            if(d.getName().equals(name))
                return d;

        return null;
    }

    abstract protected int getNumberOfIngredientForDish(Dish dish);

    public String toString(){
        return "level code: "+ levelCode + " dishes: " + dishes;
    }

}
