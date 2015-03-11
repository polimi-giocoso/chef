package it.polimi.gq.chefperungiorno.alljoyn;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;

/**
 * Created by giovanniquattrocchi on 06/03/15.
 */
public class MultiPlayerService implements MultiPlayerInterface, BusObject {

    public MultiPlayerService(){}

    @Override
    public void sendMessage(String message) throws BusException{

    }


}
