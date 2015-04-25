package it.polimi.gq.chefperungiorno.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.annotation.BusSignalHandler;

import java.util.ArrayList;
import java.util.List;

import it.polimi.gq.chefperungiorno.R;
import it.polimi.gq.chefperungiorno.adapters.IngredientAdapter;
import it.polimi.gq.chefperungiorno.adapters.TableAdapter;
import it.polimi.gq.chefperungiorno.beacons.ChefBeaconListener;
import it.polimi.gq.chefperungiorno.model.Dish;
import it.polimi.gq.chefperungiorno.model.Game;
import it.polimi.gq.chefperungiorno.model.Turn;
import it.polimi.gq.chefperungiorno.model.TurnListener;
import it.polimi.gq.chefperungiorno.model.TurnResult;
import it.polimi.gq.chefperungiorno.utils.Commons;

import com.gimbal.android.BeaconManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceManager;

public class MainActivity extends Activity implements TurnListener {

    private List<Dish> dishes;
    private Turn turn;

    private ChefBeaconListener beaconEventListener;

    private BeaconManager beaconManager;

    private IngredientAdapter ia;
    private TableAdapter dA;
    private List<IngredientAdapter.IngredientType> types;
    private List<String> completedDishes;

    private String mode;

    private int currentDishIndex;
    private int dishMaxIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final MainActivity self = this;
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        mode = getIntent().getExtras().getString("mode");

        if(!mode.equals(Commons.SINGLE_MODE)){
            Status status = Commons.sharedBus.registerSignalHandlers(this);
            if(status!=Status.OK)
                System.exit(0);
        }

        System.out.println("Create");

        if(mode.equals(Commons.MULTI_MODE_MASTER)){

            String m ="dishes";
            Object[] names = (Object[]) getIntent().getExtras().get("dishes");
            for (Object d : names) {
                m+=","+(String)d;
            }
            Commons.sendMessage(m);

        }

        beaconEventListener = new ChefBeaconListener();


        Log.i("MAIN", "Init");

        Gimbal.setApiKey(this.getApplication(), "0a32127b-b7e9-4314-adb7-489876c4ba3d");

        System.out.println("Starting beacons");
        beaconManager = new BeaconManager();
        beaconManager.addListener(beaconEventListener);
        PlaceManager.getInstance().startMonitoring();
        beaconManager.startListening();

        setup();

    }

    public void setup(){

      dishes = new ArrayList<Dish>();
      Object[] names = (Object[]) getIntent().getExtras().get("dishes");
      for (Object d : names)
          dishes.add(Game.dishWithName((String) d));

        if(mode.equals(Commons.MULTI_MODE_SLAVE)){
            currentDishIndex =Commons.DISH_COUNT_OPT_1;
            dishMaxIndex =Commons.DISH_COUNT_OPT_2;
            ImageView v = (ImageView) findViewById(R.id.team);
            v.setImageResource(R.drawable.purple_team);
        }
        else if(mode.equals(Commons.MULTI_MODE_MASTER)){
            currentDishIndex =0;
            dishMaxIndex =Commons.DISH_COUNT_OPT_1;
        }
        else{
            currentDishIndex = 0;
            dishMaxIndex = names.length;
        }

        completedDishes =new ArrayList<String>();
        List<Integer> placeholders=new ArrayList<Integer>();
        placeholders.add(R.drawable.piatto_1);
        placeholders.add(R.drawable.piatto_2);
        if(!mode.equals(Commons.SINGLE_MODE)) {
            placeholders.add(R.drawable.piatto_3);
            placeholders.add(R.drawable.piatto_4);
        }
        else if(names.length==Commons.DISH_COUNT_OPT_2) {
            placeholders.add(R.drawable.piatto_1);
            placeholders.add(R.drawable.piatto_2);
        }

        dA = new TableAdapter(this, dishes, R.layout.big_row_items, completedDishes, placeholders);
        GridView gv = (GridView) findViewById(R.id.table);
        gv.setAdapter(dA);

        types = new ArrayList<IngredientAdapter.IngredientType>();


        ia = new IngredientAdapter(this, types, R.layout.ing_row_item);
        gv = (GridView) findViewById(R.id.ingredients);
        gv.setAdapter(ia);

        nextTurn();

    }

    public void nextTurn(){

        if(currentDishIndex == dishMaxIndex){

            final Dialog builder = new Dialog(this);
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
            builder.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));

            beaconEventListener.setTurn(null);

            ImageButton button = new ImageButton(this);
            button.setImageResource(R.drawable.dialog_ok);
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    builder.dismiss();
                }
            });
            builder.addContentView(button, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            builder.setCancelable(false);
            builder.show();
            return;
        }

        types.clear();

        Dish d = dishes.get(currentDishIndex++);

        int resID = getResources().getIdentifier(d.getImageName(), "drawable", getPackageName());
        if (resID == 0)
            resID = R.drawable.food;

        ImageView imageView = (ImageView) findViewById(R.id.selected_dish);
        imageView.setImageResource(resID);

        turn = new Turn(d, this);

        int i=turn.getNumberOfIngredients();

        for(int j=0; j<i; j++){
            types.add(IngredientAdapter.IngredientType.EMPTY);
        }

        ia.notifyDataSetChanged();

        beaconEventListener.setTurn(turn);

    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    public void onStart(){
        super.onStart();


    }

    public void onStop(){
        super.onStop();
    }

    protected void onDestroy(){
        synchronized(this) {
            if (beaconManager != null) {
                beaconManager.removeListener(beaconEventListener);
                beaconManager.stopListening();
            }
        }
        if(!mode.equals(Commons.SINGLE_MODE))
            Commons.sharedBus.unregisterSignalHandlers(this);
        super.onDestroy();
    }

    @Override
    public void dishCompleted(final Turn g, final TurnResult result) {
        if (g == turn) {
            Log.i("Main","Completed");


            Commons.sendMessage("dish," + g.getDish().getName());

            Commons.sendEmail(result.toString(), this);

            if(currentDishIndex == dishMaxIndex){
                completedDishes.add(g.getDish().getName());
                dA.notifyDataSetChanged();
                nextTurn();

            }
            else {

                final Dialog builder = new Dialog(this);
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));


                ImageButton button = new ImageButton(this);
                button.setImageResource(R.drawable.dialog_next);
                button.setBackgroundColor(Color.TRANSPARENT);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                completedDishes.add(g.getDish().getName());
                                dA.notifyDataSetChanged();
                                nextTurn();
                                builder.dismiss();
                            }
                        });
                    }
                });
                builder.addContentView(button, new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                builder.setCancelable(false);
                builder.show();

            }

        }
    }

    @Override
    public void ingredientAdded(Turn g, String ingrName) {
        Log.i("Main", "+1");
        if(g.getNumberOfIngredients()<0){
            types.add(0, IngredientAdapter.IngredientType.OK);
        }
        else{
            int j=0;
            for(IngredientAdapter.IngredientType t : types){
                if(t==IngredientAdapter.IngredientType.EMPTY)
                    break;
                j++;
            }

            types.set(j, IngredientAdapter.IngredientType.OK);

        }

        ia.notifyDataSetChanged();
    }

    @Override
    public void ingredientRemoved(Turn g, String ingrName) {
        Log.i("Main","-1");

        if(g.getNumberOfIngredients()<0){
            types.remove(0);
        }
        else{
            int j=types.size()-1;
            for( ; j>=0; j--){
                IngredientAdapter.IngredientType t = types.get(j);
                if(t==IngredientAdapter.IngredientType.OK)
                    break;
            }
            types.set(j, IngredientAdapter.IngredientType.EMPTY);
        }

        ia.notifyDataSetChanged();
    }

    @Override
    public void wrongIngredientAdded(Turn g, String i) {
        /*
        TextView scoreView = (TextView) findViewById(R.id.wrongTextView);
        scoreView.setText("X");*/
        if(types.size()==0 || types.get(types.size()-1) != IngredientAdapter.IngredientType.WRONG)
            types.add(IngredientAdapter.IngredientType.WRONG);

        Log.i("Main","+X");

        ia.notifyDataSetChanged();

    }

    @Override
    public void wrongIngredientRemoved(Turn g, String i, boolean hasOtherWrongIngredients) {

        Log.i("Main","-X");

        if(hasOtherWrongIngredients)
            return;
        if(types.size()>0 && types.get(types.size()-1) == IngredientAdapter.IngredientType.WRONG)
            types.remove(types.size()-1);

        ia.notifyDataSetChanged();

        //TextView scoreView = (TextView) findViewById(R.id.wrongTextView);
        //scoreView.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        final Activity self = this;
        new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(getResources().getString(R.string.sureBack))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!mode.equals(Commons.SINGLE_MODE))
                            Commons.sendMessage("stop");
                        Intent setIntent = new Intent(self, StartActivity.class);
                        setIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setIntent);

                    }
                }).setNegativeButton(R.string.no, null).setCancelable(false).show();

    }


/*beaconSightingListener = new PlaceEventListener() {
            @Override
            public void onVisitStart(Visit visit) {
                super.onVisitStart(visit);
                try {
                    Log.i("Main", "visit start"+visit.getPlace().getName());
                    turn.tryIngredient(visit.getPlace().getName(), visit.getPlace().getIdentifier());
                }
                catch (Turn.GameAlreadyCompletedException e) {
                }
            }

            @Override
            public void onVisitEnd(Visit visit) {
                super.onVisitEnd(visit);
                try {
                    Log.i("Main", "visit end");
                    turn.removeIngredient(visit.getPlace().getName(), visit.getPlace().getIdentifier());
                }
                catch (Turn.GameAlreadyCompletedException e) {
                }
            }
        };*/

}
