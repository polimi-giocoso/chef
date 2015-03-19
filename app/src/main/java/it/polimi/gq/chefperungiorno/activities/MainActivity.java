package it.polimi.gq.chefperungiorno.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.annotation.BusSignalHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import it.polimi.gq.chefperungiorno.R;
import it.polimi.gq.chefperungiorno.adapters.DishAdapter;
import it.polimi.gq.chefperungiorno.adapters.IngredientAdapter;
import it.polimi.gq.chefperungiorno.adapters.TableAdapter;
import it.polimi.gq.chefperungiorno.model.Dish;
import it.polimi.gq.chefperungiorno.model.Game;
import it.polimi.gq.chefperungiorno.model.Ingredient;
import it.polimi.gq.chefperungiorno.model.Turn;
import it.polimi.gq.chefperungiorno.model.TurnListener;
import it.polimi.gq.chefperungiorno.model.TurnResult;
import it.polimi.gq.chefperungiorno.utils.Commons;
import it.polimi.gq.chefperungiorno.utils.GMailSender;

import com.gimbal.android.BeaconEventListener;
import com.gimbal.android.BeaconManager;
import com.gimbal.android.BeaconSighting;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceManager;

import javax.sql.CommonDataSource;

public class MainActivity extends Activity implements TurnListener {

    private List<Dish> dishes;
    private List<IngredientAdapter.IngredientType> types;
    private Turn turn;
    private BeaconEventListener beaconSightingListener;
    private BeaconManager beaconManager;
    private IngredientAdapter ia;
    private TableAdapter dA;
    private String mode;
    int index;
    int maxIndex;
    private List<String> selectedItems;


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

        beaconSightingListener=new

                BeaconEventListener() {
                    @Override
                    public void onBeaconSighting (BeaconSighting sighting){
                        int rssi = sighting.getRSSI();
                        Log.i("Proximity", "SIGHT ID: " + sighting.getBeacon().getName() + " RSSI: " + rssi + "");

                        if (turn == null)
                            return;

                        try {
                            if (rssi > Commons.PROXIMITY_THRESHOLD)
                                turn.tryIngredient(sighting.getBeacon().getName());
                            else
                                turn.removeIngredient(sighting.getBeacon().getName());

                        } catch (Turn.GameAlreadyCompletedException e) {
                        }

                    }
                };

        setup();

    }

    public void setup(){

      dishes = new ArrayList<Dish>();
      Object[] names = (Object[]) getIntent().getExtras().get("dishes");
      for (Object d : names)
          dishes.add(Game.dishWithName((String) d));


        if(!mode.equals(Commons.MULTI_MODE_SLAVE)){

            index=0;
            maxIndex=Commons.DISH_COUNT;
        }
        else{
            index = Commons.DISH_COUNT;
            maxIndex = index + Commons.DISH_COUNT;
        }

        selectedItems=new ArrayList<String>();
        dA = new TableAdapter(this, dishes, R.layout.big_row_items, selectedItems);
        GridView gv = (GridView) findViewById(R.id.table);
        gv.setAdapter(dA);

        types = new ArrayList<IngredientAdapter.IngredientType>();


        ia = new IngredientAdapter(this, types, R.layout.ing_row_item);
        gv = (GridView) findViewById(R.id.ingredients);
        gv.setAdapter(ia);

        nextTurn();


    }

    public void nextTurn(){

        if(index == maxIndex){

            new AlertDialog.Builder(this)
                    .setTitle(R.string.congrats)
                    .setMessage(getResources().getString(R.string.session_end))
                    .setPositiveButton(R.string.ok, null).show();

            return;

        }

        types.clear();

        Dish d = dishes.get(index++);

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

        turn = new Turn(d, this);

    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    public void onStart(){
        super.onStart();

        Gimbal.setApiKey(this.getApplication(), "0a32127b-b7e9-4314-adb7-489876c4ba3d");
        beaconManager = new BeaconManager();
        beaconManager.addListener(beaconSightingListener);
        PlaceManager.getInstance().startMonitoring();
        beaconManager.startListening();
        System.out.println("Starting beacons");

    }


    public void onStop(){
        super.onStop();
        System.out.println("Stop");
        synchronized(this) {
            if (beaconManager != null) {
                beaconManager.removeListener(beaconSightingListener);
                beaconManager.stopListening();
            }
        }
       
    }

    protected void onDestroy(){
        if(!mode.equals(Commons.SINGLE_MODE))
            Commons.sharedBus.unregisterSignalHandlers(this);
        super.onDestroy();


    }


    @Override
    public void dishCompleted(final Turn g, final TurnResult result) {
        if (g == turn) {
            Log.i("Main","Completed");


            Commons.sendMessage("dish," + g.getDish().getName());

            Commons.sendEmail(result, this);

            if(index==maxIndex){
                selectedItems.add(g.getDish().getName());
                dA.notifyDataSetChanged();
                nextTurn();

            }
            else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.congrats_dish)
                        .setMessage(getResources().getString(R.string.turn_done))
                        .setPositiveButton(R.string.cont, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedItems.add(g.getDish().getName());
                                dA.notifyDataSetChanged();
                                nextTurn();
                            }
                        }).setCancelable(false).show();
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
    /*
    @Override
    public void serviceStarted() {
        Log.i("Proximity", "Proximity Service successfully started!!!");
        if(visitManager!=null)
            visitManager.stop();
        visitManager = ProximityFactory.getInstance().createVisitManager();
        visitManager.setVisitListener(this);
        visitManager.start();
    }

    @Override
    public void startServiceFailed(int errorCode, String message) {
        String logMsg = String.format("Proximity Service failed with error code %d, message: %s!", errorCode, message);
        Log.d("Proximity", logMsg);
        //check for the error Code for Bluetooth status check
        if (errorCode == ProximityError.PROXIMITY_BLUETOOTH_IS_OFF.getCode()) {
            //turn on the bluetooth and once the bluetooth is ON call startService again.
        }
    }

    @Override
    public void didArrive(Visit visit) {
        Log.i("Proximity", "VISIT");
        if(turn==null)
            return;
    }

    @Override
    public void receivedSighting(Visit visit, Date date, Integer rssi) {
        Log.i("Proximity", "SIGHT ID: "+visit.getTransmitter().getName()+" RSSI: "+rssi+"");

        if(turn==null)
            return;

        try {
             if(rssi>-60)
                turn.tryIngredient(visit.getTransmitter().getName());
             else
                turn.removeIngredient(visit.getTransmitter().getName());

        }
        catch (Turn.GameAlreadyCompletedException e)
        {
        }
    }

    @Override
    public void didDepart(Visit visit) {

        Log.i("Proximity", "DEPART");

        if(turn==null)
            return;

        try {
            turn.removeIngredient(visit.getTransmitter().getName());
        }
        catch (Turn.GameAlreadyCompletedException e)
        {
        }

    }
*/
    @BusSignalHandler(iface="it.polimi.gq.chefperungiorno.multiplay", signal="sendMessage")
    public void sendMessage(final String message) throws BusException {
        String[] content = message.split(",");
        final Activity self = this;
        Log.i("Chef","Received a message from the bus "+message);
        if(content[0].equals("stop"))
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent setIntent = new Intent(self, StartActivity.class);
                    setIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(setIntent);
                }
            });

        }
        else if(content[0].equals("dish")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] content = message.split(",");
                    selectedItems.add(content[1]);
                    dA.notifyDataSetChanged();
                }
            });
        }

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




}
