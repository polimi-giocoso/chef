package it.polimi.gq.chefperungiorno.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by giovanniquattrocchi on 19/02/15.
 */

public class Game {

    private static List<Level> levels=new ArrayList<Level>();
    private static Map<String, Dish> dishes = new HashMap();

    public static void loadData(String jsonString){

        try {
            JSONObject data = new JSONObject(jsonString);
            Map<String, Ingredient> ingredientsMap= new HashMap<String, Ingredient>();
            Iterator<String> it = data.getJSONObject("ingredients").keys();

            while(it.hasNext()){
                String key = it.next();
                JSONObject ingredientObj = data.getJSONObject("ingredients").getJSONObject(key);
                Ingredient ingredient = new Ingredient();
                ingredient.setName(ingredientObj.getString("name"));
                ingredient.setDescription(ingredientObj.getString("description"));
                ingredientsMap.put(key, ingredient);
            }

            it = ((JSONObject)data.get("levels")).keys();

            while(it.hasNext()) {
                String key = it.next();
                int code = Integer.parseInt(key);
                JSONArray dishesArray = data.getJSONObject("levels").getJSONArray(key);
                List<Dish> dishesList = new ArrayList<Dish>();
                for(int i=0; i<dishesArray.length(); i++){
                    JSONObject dishObject = dishesArray.getJSONObject(i);
                    Dish dish = new Dish();
                    dish.setName(dishObject.getString("name"));
                    dish.setImageName(dish.getName().toLowerCase().replace(" ", "_"));
                    JSONArray dishIngredients = dishObject.getJSONArray("ingredients");
                    for(int j=0; j<dishIngredients.length(); j++){
                        String ingredientName = dishIngredients.getString(j);
                        dish.addIngredient(ingredientsMap.get(ingredientName));
                    }
                    dishesList.add(dish);
                    dishes.put(dish.getName(), dish);
                }

                Level level = new Level(dishesList, code) {
                    @Override
                    protected int getNumberOfIngredientForDish(Dish dish) {
                        if(this.levelCode==1)
                            return dish.getIngredients().size();
                        else
                            return -1;
                    }
                };

                levels.add(level);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<Dish> allDishes(){
        ArrayList<Dish> res=new ArrayList<Dish>();
        for(Level l : levels)
            res.addAll(l.getDishes());

       return res;
    }

    public static Dish dishWithName(String dishName){
        return dishes.get(dishName);
    }

    protected static Level getLevelOfDish(Dish dish){
        Level res = null;
        for (Level l : levels)
            if(l.hasDish(dish))
            {
                res = l;
                break;
            }

        return res;
    }



}
