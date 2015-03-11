package it.polimi.gq.chefperungiorno.adapters;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.List;

import it.polimi.gq.chefperungiorno.R;
import it.polimi.gq.chefperungiorno.model.Dish;


/**
 * Created by giovanniquattrocchi on 21/02/15.
 */
public class DishAdapter extends ArrayAdapter<Dish> {

    private Context context;
    private List<Dish> dishes;
    List<String> selectedItems;
    int layoutId;


    public DishAdapter(Context context, List<Dish> dishes, int layoutId, List<String> selectedItems) {
        super(context, layoutId, dishes);
        this.layoutId = layoutId;
        this.context = context;
        this.dishes=dishes;
        this.selectedItems=selectedItems;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.i("Chef", "refresh");
        View row = convertView;
        RecordHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutId, parent, false);
            row.setPadding(20,20,20,20);
            holder = new RecordHolder();
            holder.txtTitle = (TextView)
            row.findViewById(R.id.item_text);
            holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
            row.setTag(holder);
        } else {
            holder = (RecordHolder) row.getTag();
        }
        Dish item = dishes.get(position);
        holder.txtTitle.setText(item.getName());
        String mDrawableName = item.getImageName();
        int resID = context.getResources().getIdentifier(mDrawableName , "drawable", context.getPackageName());
        if(resID==0)
            resID=R.drawable.food;
        holder.imageItem.setImageResource(resID);

        if(selectedItems!=null){
            if(selectedItems.contains(item.getName())){
                row.setBackgroundColor(Color.GREEN);
            }
            else
            {
                row.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        return row;
    }

    static class RecordHolder { TextView txtTitle; ImageView imageItem; }

}
