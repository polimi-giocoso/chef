package it.polimi.gq.chefperungiorno.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.polimi.gq.chefperungiorno.R;
import it.polimi.gq.chefperungiorno.model.Dish;

/**
 * Created by giovanniquattrocchi on 04/03/15.
 */
public class IngredientAdapter extends ArrayAdapter<IngredientAdapter.IngredientType> {

    public enum IngredientType{
        EMPTY, OK, WRONG
    }

    private Context context;
    private List<IngredientType> types;
    int layoutId;

    public IngredientAdapter(Context context, List<IngredientType> dishes, int layoutId) {
        super(context, layoutId, dishes);
        this.layoutId = layoutId;
        this.context = context;
        this.types=dishes;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutId, parent, false);
            row.setPadding(10,10,10,10);
            holder = new RecordHolder();
            holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
            row.setTag(holder);
        } else {
            holder = (RecordHolder) row.getTag();
        }
        IngredientType item = types.get(position);
        int resID;
        if(item==IngredientType.EMPTY)
            resID = R.drawable.ingredient;
        else if(item==IngredientType.OK)
            resID = R.drawable.ok_ingredient;
        else
            resID = R.drawable.wr_ingredient;

        holder.imageItem.setImageResource(resID);

        return row;

    }

    static class RecordHolder { ImageView imageItem; }

}
