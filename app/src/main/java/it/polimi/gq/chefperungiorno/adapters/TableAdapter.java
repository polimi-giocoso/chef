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
 * Created by Giovanni on 13/03/15.
 */
public class TableAdapter extends ArrayAdapter<Dish> {

    private Context context;
    private List<Dish> dishes;
    List<String> selectedItems;
    int layoutId;
    private LayoutInflater inflater;

    public TableAdapter(Context context, List<Dish> dishes, int layoutId, List<String> selectedItems) {
        super(context, layoutId, dishes);
        this.layoutId = layoutId;
        this.context = context;
        this.dishes=dishes;
        this.selectedItems=selectedItems;
        inflater = ((Activity) context).getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        RecordHolder holder = null;
        if (row == null) {
            row = inflater.inflate(layoutId, parent, false);
            row.setPadding(20,20,20,20);
            holder = new RecordHolder();

            holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
            holder.placeHolder = (ImageView) row.findViewById(R.id.item_image2);
            row.setTag(holder);
        } else {
            holder = (RecordHolder) row.getTag();
        }

        Dish item = dishes.get(position);


        holder.placeHolder.setImageResource(context.getResources().getIdentifier("piatto_"+(position+1), "drawable", context.getPackageName()));



        if(selectedItems!=null){
            if(selectedItems.contains(item.getName())){
                String mDrawableName = item.getImageName();
                int resID = context.getResources().getIdentifier(mDrawableName , "drawable", context.getPackageName());
                if(resID==0)
                    resID=R.drawable.food;
                holder.imageItem.setImageResource(resID);            }
        }
        return row;
    }

    static class RecordHolder { ImageView imageItem; ImageView placeHolder;}

}
