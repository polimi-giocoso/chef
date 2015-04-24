package it.polimi.gq.chefperungiorno.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by giovanniquattrocchi on 19/02/15.
 */

public class Turn {

    private final Dish dish;
    private final List<String> ingredientsAdded;
    private final Map<String, Set<String>> ingredientIds;

    private final TurnListener listener;
    private final Level level;
    private boolean completed;
    private int numOfIngredients;
    private Date beginDate;

    public Turn(Dish dish, TurnListener listener){
        this.dish = dish;
        this.listener = listener;
        level = Game.getLevelOfDish(dish);
        ingredientsAdded = new ArrayList<String>();
        numOfIngredients = dish.getIngredients().size();
        ingredientIds = new HashMap<String, Set<String>>();
        beginDate=new Date();
    }

    public boolean isCompleted() {
        synchronized (this) {
            return completed;
        }
    }

    public Dish getDish() {
        return dish;
    }

    public void tryIngredient(String name, String id) throws GameAlreadyCompletedException {
        synchronized (this) {

            if(completed)
                throw new GameAlreadyCompletedException();

            Set<String> ids = ingredientIds.get(name);
            if(ids==null)
            {
                ids = new HashSet<String>();
                ingredientIds.put(name, ids);
            }

            ids.add(id);

            if (dish.containsIngredient(name) && !ingredientsAdded.contains(name)) {
                ingredientsAdded.add(name);
                listener.ingredientAdded(this, name);

                int ok = numOfCorrectIngredients();
                int wr = numOfWrongIngredients();
                if(ok==numOfIngredients && wr==0) {
                    completed=true;
                    listener.dishCompleted(this, createResult());
                }
            }
            else if(!dish.containsIngredient(name) && !ingredientsAdded.contains(name)){
                ingredientsAdded.add(name);
                int wr = numOfWrongIngredients();
                listener.wrongIngredientAdded(this, name);
            }
        }
    }

    public void removeIngredient(String name, String id) throws GameAlreadyCompletedException {
        synchronized (this) {
            if(completed)
                throw new GameAlreadyCompletedException();

            if(!ingredientsAdded.contains(name))
                return;

            Set<String> ids = ingredientIds.get(name);
            if(ids==null)
            {
                ids = new HashSet<String>();
                ingredientIds.put(name, ids);
            }

            ids.remove(id);
            if(ids.isEmpty()) {
                ingredientsAdded.remove(name);
            }

            if (dish.containsIngredient(name) && ids.isEmpty()) {
                listener.ingredientRemoved(this, name);
            }
            else if(!dish.containsIngredient(name) && ids.isEmpty()){
                int wr=numOfWrongIngredients();
                listener.wrongIngredientRemoved(this, name, wr!=0);

                int ok=numOfCorrectIngredients();
                if(wr==0 && ok==numOfIngredients){
                    completed=true;
                    listener.dishCompleted(this, createResult());
                }
            }
        }
    }

    public int getNumberOfCompletedIngredients(){
        synchronized (this) {
            return numOfCorrectIngredients();
        }
    }

    private int numOfCorrectIngredients(){
        Set<String> set = new HashSet<String>(this.ingredientsAdded);
        int numOfOk=0;
        for(String i : set){
            if(dish.containsIngredient(i))
                numOfOk++;
        }
        return numOfOk;
    }

    private int numOfWrongIngredients(){
        int numOfWr=0;
        for(String i : ingredientsAdded){
            if(!dish.containsIngredient(i))
                numOfWr++;
        }

        return numOfWr;

    }

    public TurnResult createResult(){
        TurnResult result = new TurnResult();
        result.beginDate=beginDate;
        result.endDate=new Date();
        result.duration = (result.endDate.getTime()-beginDate.getTime())/1000;
        List<String> orderedIngredients=new ArrayList<String>();

        for(String i : ingredientsAdded){
            if(!orderedIngredients.contains(i))
                orderedIngredients.add(i);
            if(orderedIngredients.size()==numOfIngredients)
                break;;
        }

        result.ingredientsOrder=orderedIngredients;

        return result;
    }

    /* returns -1 if this information is hidden because of the level */
    public int getNumberOfIngredients(){
        return level.getNumberOfIngredientForDish(dish);
    }


    public class GameAlreadyCompletedException extends Exception {}


}
