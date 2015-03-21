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
    private LayoutInflater inflater;
    private boolean showText;
    public DishAdapter(Context context, List<Dish> dishes, int layoutId, List<String> selectedItems) {
        super(context, layoutId, dishes);
        this.layoutId = layoutId;
        this.context = context;
        this.dishes=dishes;
        this.selectedItems=selectedItems;
        inflater = ((Activity) context).getLayoutInflater();
        showText=true;
    }

    public void setNotShowText(){
        showText=false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        RecordHolder holder = null;
        if (row == null) {
            row = inflater.inflate(layoutId, parent, false);
            holder = new RecordHolder();
            if(showText)
                holder.txtTitle = (TextView) row.findViewById(R.id.item_text);

            holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
            holder.placeHolder = (ImageView) row.findViewById(R.id.item_image2);
            row.setTag(holder);
        } else {
            holder = (RecordHolder) row.getTag();
        }

        Dish item = dishes.get(position);

        if(showText)
          holder.txtTitle.setText(item.getName());
        if(holder.placeHolder!=null)
            holder.placeHolder.setImageResource(context.getResources().getIdentifier("piatto_"+(position+3), "drawable", context.getPackageName()));

        String mDrawableName = item.getImageName();
        int resID = context.getResources().getIdentifier(mDrawableName , "drawable", context.getPackageName());
        if(resID==0)
            resID=R.drawable.food;
        holder.imageItem.setImageResource(resID);

        if(selectedItems!=null){
            if(selectedItems.contains(item.getName())){
                row.setBackgroundColor(Color.parseColor("#a9a9a9"));
                holder.txtTitle.setTextColor(Color.DKGRAY);
            }
            else
            {
                row.setBackgroundColor(Color.TRANSPARENT);
                holder.txtTitle.setTextColor(Color.GRAY);

            }
        }
        return row;
    }

    static class RecordHolder { TextView txtTitle; ImageView imageItem; ImageView placeHolder;}

}
