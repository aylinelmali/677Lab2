package peer;

import product.Product;
import utils.Logger;
import utils.Messages;
import utils.VectorClock;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Buyer extends APeer {

    private Product product;
    private int amount;

    public Buyer(int peerID, int peersAmt, int coordinatorID) throws RemoteException {
        super(peerID, peersAmt, coordinatorID);
        pickRandomProduct();
    }

    @Override
    public void start() throws RemoteException {
        super.start();
        Logger.log("Peer " + peerID + " (Buyer)");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        int initialDelay = new Random().nextInt(1,11);
        int period = new Random().nextInt(7,11);

        executor.scheduleAtFixedRate(() -> {
            // only buy something if not coordinator
            if (this.peerID == this.coordinatorID) {
                return;
            }
            try {
                initiateDiscovery();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }, initialDelay, period, TimeUnit.SECONDS);
    }

    @Override
    public void discoverAck(Product product, boolean available, int[] traderTimestamp) throws RemoteException {
        // check if ack is valid
        this.timestamp[this.peerID] += 1;
        this.timestamp = VectorClock.merge(this.timestamp, traderTimestamp);
        if (this.product == product && available) {
            initiateBuy();
        } else {
            pickRandomProduct();
        }
    }

    @Override
    public void buyAck(Product product, boolean bought, int[] traderTimestamp) throws RemoteException {
        // check if ack is valid
        this.timestamp[this.peerID] += 1;
        this.timestamp = VectorClock.merge(this.timestamp, traderTimestamp);
        if (this.product == product && bought) {
            pickRandomProduct();
        }
    }

    @Override
    public void offerAck(int[] sellerTimestamp) throws RemoteException {
        // Do nothing. This peer is not a Seller
    }

    @Override
    public void pay(int price, int[] traderTimestamp) throws RemoteException {
        // Do nothing. This peer is not a Seller
    }

    public void initiateDiscovery() throws RemoteException {
        Logger.log(Messages.getDiscoveryMessage(peerID, amount, product));
        this.timestamp[this.peerID] += 1;
        this.peers[this.coordinatorID].discover(this.product, this.amount, this.timestamp, this.peerID);
    }

    public void initiateBuy() throws RemoteException {
        Logger.log(Messages.getBuyMessage(peerID, amount, product));
        this.timestamp[this.peerID] += 1;
        peers[coordinatorID].buy(this.product, this.amount, this.timestamp, this.peerID);
    }

    /**
     * Picks a random new product.
     */
    public void pickRandomProduct() {
        this.product = Product.pickRandomProduct();
        this.amount = (int) (Math.random() * 5) + 1;
    }

    /**
     * Picks a new product.
     * @param product product to pick.
     * @param amount amount to pick.
     */
    public void pickProduct(Product product, int amount) {
        this.product = product;
        this.amount = amount;
    }
}
