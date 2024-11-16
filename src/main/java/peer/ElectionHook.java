package peer;

import java.rmi.RemoteException;

@FunctionalInterface
public interface ElectionHook {
    /**
     * Computes a result, or throws an exception if unable to do so.
     */
    void call() throws RemoteException;
}
