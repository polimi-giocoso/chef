package it.polimi.gq.chefperungiorno.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusSignalHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polimi.gq.chefperungiorno.R;
import it.polimi.gq.chefperungiorno.adapters.DishAdapter;
import it.polimi.gq.chefperungiorno.model.Dish;
import it.polimi.gq.chefperungiorno.model.Game;
import it.polimi.gq.chefperungiorno.utils.Commons;


public class ChooseSessionActivity extends Activity {
    private List<Dish> dishes;
    private List<String> selectedItems;
    int numItems;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_choose_session);


        mode = getIntent().getExtras().getString("mode");
        if(mode.equals(Commons.SINGLE_MODE))
            numItems=Commons.DISH_COUNT;
        else {
            numItems = Commons.DISH_COUNT * 2;
            Commons.sharedBus.registerSignalHandlers(this);
        }

        TextView t = (TextView) findViewById(R.id.action_label);
        t.setText(t.getText().toString().replace(" n ", " "+numItems+" "));

        GridView gv = (GridView) findViewById(R.id.grid_dish);

        dishes = Game.allDishes();
        selectedItems=new ArrayList<>();

        DishAdapter dA=new DishAdapter(this, dishes, R.layout.row_items, selectedItems);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Button button = (Button) findViewById(R.id.ok_button);
                Dish dish = dishes.get(position);
               if(selectedItems.contains(dish.getName())){
                   view.setBackgroundColor(Color.TRANSPARENT);
                   selectedItems.remove(dish.getName());
                   button.setEnabled(false);
               }
               else if(selectedItems.size() < numItems) {
                   view.setBackgroundColor(Color.GREEN);
                   selectedItems.add(dish.getName());
                   if(selectedItems.size()==numItems){
                       button.setEnabled(true);
                   }
               }
            }
        });
        gv.setAdapter(dA);
    }

    protected void onDestroy(){

        if(!mode.equals(Commons.SINGLE_MODE))
            Commons.sharedBus.unregisterSignalHandlers(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_session, menu);
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

    public void startSession(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("mode", mode);
        intent.putExtra("dishes", selectedItems.toArray());
        startActivity(intent);
    }

    public void randomSession(View view) {

        List<Dish> list = new ArrayList<>(dishes);
        Collections.shuffle(list);
        String[] res = new String[numItems];

        for(int i=0; i<numItems; i++){
            res[i]=list.get(i).getName();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("mode", mode);
        intent.putExtra("dishes", res);
        startActivity(intent);
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
    @BusSignalHandler(iface="it.polimi.gq.chefperungiorno.multiplay", signal="sendMessage")
    public void sendMessage(String message) throws BusException {
        String[] content = message.split(",");
        final Activity self = this;
        Log.i("Chef","Received a message from the bus "+message);
        if(content[0].equals("stop")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent setIntent = new Intent(self, StartActivity.class);
                    setIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(setIntent);
                }
            });
        }

    }
}
