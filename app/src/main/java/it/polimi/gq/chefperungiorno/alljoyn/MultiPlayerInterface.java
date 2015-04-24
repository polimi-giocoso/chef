package it.polimi.gq.chefperungiorno.alljoyn;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;

/**
 * Created by giovanniquattrocchi on 06/03/15.
 */
@BusInterface(name = "it.polimi.gq.chefperungiorno.multiplay")
public interface MultiPlayerInterface {

    @BusSignal
    public void sendMessage(String message) throws BusException;

}
