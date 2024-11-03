package peer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPeer extends Remote {
    /**
     * Initializes and starts the peer.
     */
    void start() throws RemoteException;

    /**
     * @return The ID of the peer.
     */
    int getPeerID() throws RemoteException;

    void election(int[] tags) throws RemoteException;

    /**
     * @param coordinatorID The id of the new coordinator.
     * @param initiatorID The id of the peer who initiated the election.
     */
    void coordinator(int coordinatorID, int initiatorID) throws RemoteException;
}
