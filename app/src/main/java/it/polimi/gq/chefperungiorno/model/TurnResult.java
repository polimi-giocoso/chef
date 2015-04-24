package it.polimi.gq.chefperungiorno.model;

import java.util.Date;
import java.util.List;

/**
 * Created by giovanniquattrocchi on 02/03/15.
 */
public class TurnResult {

    public String dishName;
    public Date beginDate;
    public Date endDate;
    public long duration;
    public List<String> correctIngredients;
    public List<String> wrongIngredients;
}
