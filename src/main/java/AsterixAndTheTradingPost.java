import peer.Buyer;
import peer.IPeer;
import peer.Seller;
import utils.Logger;
import utils.TraderState;

import java.net.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class AsterixAndTheTradingPost {

    public static int REGISTRY_ID = 1099;

    public static void main(String[] args) throws RemoteException, InterruptedException, NotBoundException {

        TraderState.resetTraderState();

        int n = Integer.parseInt(args[0]);  // Number of peers

        Registry registry = LocateRegistry.createRegistry(REGISTRY_ID);

        // initialize all peers
        for (int i = 0; i < n; i++) {
            Thread thread = getThread(i, n);
            thread.start();
        }

        Thread.sleep(1000); // ensure that all peers are bound

        // retrieve proxies
        IPeer[] peers = new IPeer[n];
        for (int i = 0; i < n; i++) {
            peers[i] = (IPeer) registry.lookup("" + i);
        }

        Logger.log("########## START INITIAL SETUP ##########");
        for (int i = 0; i < n; i++) {
            peers[i].start();
        }
        Logger.log("########### END INITIAL SETUP ###########");

        // do initial election
        peers[0].election(new int[] {});
    }

    private static Thread getThread(int nodeIndex, int nodeAmt) {
        return new Thread(() -> {
            try {
                Registry registry = LocateRegistry.getRegistry("127.0.0.1", REGISTRY_ID);
                IPeer peer = (nodeIndex % 2 == 0) ?
                        new Buyer(nodeIndex, nodeAmt) :
                        new Seller(nodeIndex, nodeAmt);

                registry.rebind("" + peer.getPeerID(), peer);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
