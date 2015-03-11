package it.polimi.gq.chefperungiorno.model;

/**
 * Created by giovanniquattrocchi on 23/02/15.
 */
public interface TurnListener {

    public void dishCompleted(Turn g, TurnResult result);
    public void ingredientAdded(Turn g, String i);
    public void ingredientRemoved(Turn g, String i);
    public void wrongIngredientAdded(Turn g, String i);
    public void wrongIngredientRemoved(Turn g, String i, boolean hasOtherWrongIngredients);

}
