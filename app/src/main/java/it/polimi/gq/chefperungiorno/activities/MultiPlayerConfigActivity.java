package it.polimi.gq.chefperungiorno.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.annotation.BusSignalHandler;

import it.polimi.gq.chefperungiorno.R;
import it.polimi.gq.chefperungiorno.alljoyn.MultiPlayerService;
import it.polimi.gq.chefperungiorno.utils.Commons;

public class MultiPlayerConfigActivity extends Activity {

    private  BusAttachment mBus;
    static int sessionId;
    static String joinerName;
    private boolean created;
    private boolean destroyed;
    boolean sessionEstablished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_multi_player_config);
    }

    protected void onDestroy(){
        destroyed=true;
        if(created) {
            Commons.sharedBus.unregisterSignalHandlers(this);
            Commons.sharedBus.unregisterBusListener(Commons.sharedBusListener);
        }
        super.onDestroy();


    }


    public void join(View view){

        created=true;
        TextView textView = (TextView) findViewById(R.id.status_label);

        View b1 = (View) findViewById(R.id.button_host);
        View b2 = (View) findViewById(R.id.button_join);
        b1.setEnabled(false);
        b2.setEnabled(false);

        mBus=Commons.sharedBus;
        final Activity self = this;
        MultiPlayerService mySignalInterface = Commons.sharedInterface;

        Status status = mBus.connect();

        if (status != Status.OK) {
            System.exit(0);
            return;
        }

        mBus.registerBusListener(Commons.sharedBusListener);


        status = Commons.sharedBus.registerSignalHandlers(this);
        if(status!=Status.OK) {
            System.exit(0);
            return;
        }


        status = mBus.findAdvertisedName(Commons.key);
        if (status != Status.OK) {
            System.exit(0);
            return;
        }

        textView.setText(R.string.wait_begin);



    }



    public void host(View view){
        created=true;
        TextView textView = (TextView) findViewById(R.id.status_label);

        View b1 = (View) findViewById(R.id.button_host);
        View b2 = (View) findViewById(R.id.button_join);
        b1.setEnabled(false);
        b2.setEnabled(false);

        mBus=Commons.sharedBus;
        final Activity self = this;
        MultiPlayerService mySignalInterface = Commons.sharedInterface;

        Status status = mBus.connect();
        if (status != Status.OK) {
            System.exit(0);
            return;
        }

        int flags = 0; //do not use any request name flags
        status = mBus.requestName(Commons.key, flags);
        if (status != Status.OK) {
            System.exit(0);
            return;
        }

        status = mBus.advertiseName(Commons.key,
                SessionOpts.TRANSPORT_ANY);
        if (status != Status.OK) {
            System.out.println("Status = " + status);
            System.exit(0);
            return;
        }

        Mutable.ShortValue contactPort = new Mutable.ShortValue(Commons.CONTACT_PORT);
        SessionOpts sessionOpts = new SessionOpts();
        sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
        sessionOpts.isMultipoint = false;
        sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
        sessionOpts.transports = SessionOpts.TRANSPORT_ANY;

        status = mBus.bindSessionPort(contactPort, sessionOpts,
                new SessionPortListener() {
                    public boolean acceptSessionJoiner(short sessionPort, String joiner,
                                                       SessionOpts sessionOpts) {
                        if (sessionPort == Commons.CONTACT_PORT) {
                            return true;
                        } else {
                            return false;
                        }

                    }
                    public void sessionJoined(short sessionPort, int id, String joiner) {
                        sessionEstablished = true;
                        Commons.setSessionId(id);
                        joinerName = joiner;
                    }
                });

        status = Commons.sharedBus.registerSignalHandlers(this);
        textView.setText(R.string.wait_slave);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!sessionEstablished) {
                        if(destroyed)
                            return;
                        Thread.sleep(10);
                    }
                    self.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(self, ChooseSessionActivity.class);
                            intent.putExtra("mode", Commons.MULTI_MODE_MASTER);
                            startActivity(intent);
                        }
                    });

                }
                catch (Exception e){
                }
            }
        }).start();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_multi_player_config, menu);
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




    @BusSignalHandler(iface="it.polimi.gq.chefperungiorno.multiplay", signal="sendMessage")
    public void sendMessage(String message) throws BusException {

        final Activity self = this;
        Log.i("Chef","Received a message from the bus "+message);

        String[] content = message.split(",");

        if(content[0].equals("dishes")){
           final String[] dishes = new String[4];

            for(int i=1; i<1+Commons.DISH_COUNT*2; i++){
                dishes[i-1]=content[i];
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(self, MainActivity.class);
                    intent.putExtra("mode", Commons.MULTI_MODE_SLAVE);
                    intent.putExtra("dishes", dishes);

                    startActivity(intent);
                }
            });
        }
        else if(content[0].equals("stop")) {
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

    @Override
    public void onBackPressed() {
        if(!created) {
            Intent setIntent = new Intent(this, StartActivity.class);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(setIntent);
        }
        else{

            final Activity self = this;
            new AlertDialog.Builder(this)
                    .setTitle(R.string.warning)
                    .setMessage(getResources().getString(R.string.sureBack))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Commons.sendMessage("stop");
                            Intent setIntent = new Intent(self, StartActivity.class);
                            setIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(setIntent);
                        }
                    }).setNegativeButton(R.string.no, null).setCancelable(false).show();

        }
    }


}
