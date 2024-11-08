package peer;

import product.Product;
import utils.VectorClock;

import java.rmi.RemoteException;

public class Seller extends APeer {

    int money;

    public Seller(int peerID, int peersAmt, int coordinatorID) {
        super(peerID, peersAmt, coordinatorID);
        money = 0;
    }

    @Override
    public void start() throws RemoteException {

    }

    @Override
    public void discoverAck(Product product, boolean available, int[] traderTimestamp) throws RemoteException {
        // Do nothing. This peer is not a buyer!
    }

    @Override
    public void buyAck(Product product, boolean bought, int[] traderTimestamp) throws RemoteException {
        // Do nothing. This peer is not a buyer!
    }

    @Override
    public void offerAck(int[] traderTimestamp) throws RemoteException {
        this.timestamp = VectorClock.merge(this.timestamp, traderTimestamp);
    }

    @Override
    public void pay(int price, int[] traderTimestamp) throws RemoteException {
        money += price;
        this.timestamp = VectorClock.merge(this.timestamp, traderTimestamp);
    }
}
