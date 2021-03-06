package it.polimi.gq.chefperungiorno.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SignalEmitter;
import org.alljoyn.bus.Status;

import it.polimi.gq.chefperungiorno.alljoyn.MultiPlayerInterface;
import it.polimi.gq.chefperungiorno.alljoyn.MultiPlayerService;
import it.polimi.gq.chefperungiorno.model.TurnResult;

/**
 * Created by giovanniquattrocchi on 04/03/15.
 */
public class Commons {

    static {
        System.loadLibrary("alljoyn_java");
    }

    final static public String SINGLE_MODE = "single";
    final static public String MULTI_MODE_SLAVE = "multi-slave";
    final static public String MULTI_MODE_MASTER = "multi-master";

    final static public int DISH_COUNT_OPT_1 = 2;
    final static public int DISH_COUNT_OPT_2 = 4;

    final static public int PROXIMITY_THRESHOLD = -61;
    final static public int DEPART_THRESHOLD = -69;

    // Higher means more resistant to interferences
    final static public int HYSTERESIS_TOLERANCE = 5;

    public static BusAttachment sharedBus;
    public static MultiPlayerService sharedInterface;

    public static final short CONTACT_PORT=42;
    public static String key="it.polimi.gq.chefperungiorno.master";
    public static BusListener sharedBusListener;

    public static void reset(){
        if(sharedBus!=null)
        {
            if(sharedBusListener!=null)
                sharedBus.unregisterBusListener(sharedBusListener);
            sharedBus.unbindSessionPort(CONTACT_PORT);
            sharedBus.leaveSession(Commons.sessionId);
            sharedBus.leaveHostedSession(Commons.sessionId);
            sharedBus.leaveJoinedSession(Commons.sessionId);
            Commons.sessionId=0;
            sharedBus.releaseName(key);
            sharedBus.cancelAdvertiseName(key, SessionOpts.TRANSPORT_ANY);
            sharedBus.cancelFindAdvertisedName(key);
          //  sharedBus.disconnect();
          //  sharedBus.release();
          //  sharedBus=null;
        }
        if(sharedBusListener==null) {
            sharedBusListener = new BusListener() {
                @Override
                public void foundAdvertisedName(String name,
                                                short transport,
                                                String namePrefix) {
                    sharedBus.enableConcurrentCallbacks();
                    short contactPort = Commons.CONTACT_PORT;
                    SessionOpts sessionOpts = new SessionOpts();
                    Mutable.IntegerValue sessionId = new Mutable.IntegerValue();
                    Status status = sharedBus.joinSession(Commons.key,
                            contactPort,
                            sessionId, sessionOpts,
                            new SessionListener());
                    Commons.setSessionId(sessionId.value);

                }
            };
        }
        sharedBus=new BusAttachment("CPUG"+System.currentTimeMillis(), BusAttachment.RemoteMessage.Receive);
        sharedInterface = new MultiPlayerService();
        sharedBus.registerBusObject(sharedInterface, "/multiPlayerService");
    }

    private static int sessionId;

    public static void setSessionId(int id){
        sessionId=id;
        Log.i("session", ""+sessionId);
    }
    public static void sendMessage(String message){
        Log.i("session", ""+sessionId);
        SignalEmitter emitter = new SignalEmitter(sharedInterface,
                sessionId,
                SignalEmitter.GlobalBroadcast.Off);
        MultiPlayerInterface myInterface = emitter.getInterface(MultiPlayerInterface.class);
        try {
            myInterface.sendMessage(message);
        } catch (BusException e) {
            e.printStackTrace();
        }
    }


    public static void sendEmail(final String message, Context context) {
        final String mail = PrefUtils.getFromPrefs(context, PrefUtils.PREFS_MAIL_KEY, null);
        if (mail == null)
            return;

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    GMailSender sender = new GMailSender("appgiocoso@gmail.com", "p0L1T3CN1C0");
                    sender.sendMail("Chef per un giorno", message,
                            "Giocoso",
                            mail
                    );
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }

                return null;
            }
        }.execute();
    }
}
