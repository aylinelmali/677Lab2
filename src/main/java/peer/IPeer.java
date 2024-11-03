package peer;

import product.Product;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface IPeer extends Remote {
    /**
     * Initializes and starts the peer.
     */
    void start() throws RemoteException;

    /**
     * @return The ID of the peer.
     */
    int getPeerID() throws RemoteException;
}
