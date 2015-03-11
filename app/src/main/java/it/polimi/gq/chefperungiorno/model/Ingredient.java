package it.polimi.gq.chefperungiorno.model;

/**
 * Created by giovanniquattrocchi on 19/02/15.
 */
public class Ingredient {

    private String name;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString(){
        return name;
    }
}
