import peer.Buyer;
import peer.IPeer;
import peer.Seller;
import utils.Logger;
import utils.TraderState;

import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class AsterixAndTheTradingPost {

    public static void main(String[] args) throws RemoteException {

        TraderState.resetTraderState();

        int n = Integer.parseInt(args[0]);  // Number of peers

        IPeer[] peers = new IPeer[n];
        for (int i = 0; i < n; i++) {
            peers[i] = (i % 2 == 0) ? new Buyer(i, n, n-1) : new Seller(i, n, n-1);
        }

        for (int i = 0; i < n; i++) {
            peers[i].setPeers(peers);
        }
        Logger.log("########## START INITIAL SETUP ##########");
        for (int i = 0; i < n; i++) {
            peers[i].start();
        }
        Logger.log("########### END INITIAL SETUP ###########");
    }
}
