package it.polimi.gq.chefperungiorno.beacons;

import android.util.Log;

import com.gimbal.android.BeaconEventListener;
import com.gimbal.android.BeaconSighting;

import java.util.HashMap;

import it.polimi.gq.chefperungiorno.model.Turn;
import it.polimi.gq.chefperungiorno.utils.Commons;

/**
 * Created by giovanniquattrocchi on 24/04/15.
 */
public class ChefBeaconListener extends BeaconEventListener {

    private Turn turn;

    private HashMap<String, Integer> hysteresisAvoidance = new HashMap<String, Integer>();

    @Override
    public void onBeaconSighting(BeaconSighting sighting) {

        super.onBeaconSighting(sighting);

        int rssi = sighting.getRSSI();
        String id = sighting.getBeacon().getIdentifier();
        String name = sighting.getBeacon().getName();
        //Log.i("Main", "Seen "+name+" "+rssi+" "+id);
        if (turn == null)
            return;

        try {

            Integer i = 0;

            if (rssi > Commons.PROXIMITY_THRESHOLD) {

                if (rssi < Commons.PROXIMITY_THRESHOLD + Commons.HYSTERESIS_TOLERANCE) {
                    Log.d("ChefBeaconListener", "IN " + name + " " +  rssi);
                    i = hysteresisAvoidance.get(id);

                    if (i == null) {
                        i = 0;
                    }

                    i++;
                    System.out.println("Hey");
                    if (i == 10) {
                        i = 0;
                        turn.tryIngredient(name, id);
                    }
                } else {
                    turn.tryIngredient(name, id);
                }
            } else if (rssi < Commons.DEPART_THRESHOLD) {
                Log.d("ChefBeaconListener", "OUT " + name + " " +  rssi);
                turn.removeIngredient(name, id);
            }

            hysteresisAvoidance.put(sighting.getBeacon().getIdentifier(), i);

        } catch (Turn.GameAlreadyCompletedException e) {
        }
    }

    public void setTurn(Turn turn) {
        hysteresisAvoidance.clear();
        this.turn = turn;
    }

}
