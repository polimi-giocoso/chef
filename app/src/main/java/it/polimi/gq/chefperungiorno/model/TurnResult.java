package it.polimi.gq.chefperungiorno.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.polimi.gq.chefperungiorno.utils.GMailSender;

/**
 * Created by giovanniquattrocchi on 02/03/15.
 */
public class TurnResult {

    protected String dishName;
    protected Date beginDate;
    protected Date endDate;
    protected long duration;
    protected List<String> correctIngredients;
    protected List<String> wrongIngredients;

    public String getDishName() {
        return dishName;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public long getDuration() {
        return duration;
    }

    public List<String> getCorrectIngredients() {
        return correctIngredients;
    }

    public List<String> getWrongIngredients() {
        return wrongIngredients;
    }

    @Override
    public String toString() {

        String ing = null;
        for (String i : correctIngredients) {
            if (ing == null)
                ing = i;
            else
                ing = ing + "," + i;
        }

        String errors = "errors(";
        if (wrongIngredients != null && !wrongIngredients.isEmpty()) {
            String wrongs = null;
            for (String i : wrongIngredients) {
                if (i == null || i.equals("null"))
                    continue;
                if (wrongs == null)
                    wrongs = i;
                else
                    wrongs = wrongs + "," + i;
            }
            errors += wrongs;
        }

        errors += ")";

        return dishName + "," + beginDate.getTime() + "," + endDate.getTime() + "," + duration + "," + ing + "," + errors;
    }

     /* public static void main(String[] args){
        try {

            TurnResult r = new TurnResult();
            r.duration = 5;
            r.wrongIngredients = Collections.singletonList("Piselli");
            r.correctIngredients = Collections.singletonList("Carote");
            r.beginDate = new Date();
            r.endDate = new Date();
            r.dishName = "Macedonia 1";


            GMailSender sender = new GMailSender("appgiocoso@gmail.com", "p0L1T3CN1C0");
            sender.sendMail("Chef per un giorno", r.toString(),
                    "Giocoso",
                    "giovanni.e.quattrocchi@gmail.com");
        }
        catch(Exception e){
            e.printStackTrace();
        }
     } */

}
