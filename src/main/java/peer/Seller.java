package peer;

import product.Product;
import utils.VectorClock;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Seller extends APeer {

    int money;

    public Seller(int peerID, int peersAmt, int coordinatorID) {
        super(peerID, peersAmt, coordinatorID);
        this.money = 0;
    }

    @Override
    public void start() throws RemoteException {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        int initialDelay = new Random().nextInt(1,11);
        int period = new Random().nextInt(7,11);

        executor.scheduleAtFixedRate(() -> {
            // only sell something if not coordinator
            if (this.peerID == this.coordinatorID) {
                return;
            }
            Product product = Product.pickRandomProduct();
            int amount = new Random().nextInt(6);
            try {
                this.peers[this.coordinatorID].offer(product, amount, this.timestamp, this.peerID);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }, initialDelay, period, TimeUnit.SECONDS);
        super.start();
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
        // this method only exists for keeping the seller timestamp up to date.
        this.timestamp = VectorClock.merge(this.timestamp, traderTimestamp);
    }

    @Override
    public void pay(int price, int[] traderTimestamp) throws RemoteException {
        money += price;
        // keep the seller timestamp up to date.
        this.timestamp = VectorClock.merge(this.timestamp, traderTimestamp);
    }
}
