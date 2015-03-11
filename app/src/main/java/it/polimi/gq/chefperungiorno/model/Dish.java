package it.polimi.gq.chefperungiorno.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by giovanniquattrocchi on 19/02/15.
 */
public class Dish {

    private String name;
    private String imageName;

    private Map<String, Ingredient> ingredients=new HashMap<String, Ingredient>();


    public boolean containsIngredient(String name){
        return ingredients.containsKey(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    protected void addIngredient(Ingredient ingredient){
        ingredients.put(ingredient.getName(), ingredient);
    }

    protected Collection<Ingredient> getIngredients(){
        return ingredients.values();
    }

    protected Ingredient getIngredient(String name){
        return ingredients.get(name);
    }

    public String toString(){
        return name+": "+ingredients.values();
    }
}
