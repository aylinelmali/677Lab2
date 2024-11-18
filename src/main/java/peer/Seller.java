package peer;

import product.Product;
import utils.Logger;
import utils.Messages;
import utils.VectorClock;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Seller extends APeer {

    public static void main(String[] args) throws RemoteException {
        int peerID = Integer.parseInt(args[0]);
        int peersAmt = Integer.parseInt(args[1]);

        Registry registry = LocateRegistry.getRegistry("127.0.0.1", REGISTRY_ID);
        registry.rebind("" + peerID, new Seller(peerID, peersAmt));
    }

    public static final int PERIOD = 5000;

    // Amount of money earned through sales
    public int money;

    public Seller(int peerID, int peersAmt) throws RemoteException {
        super(peerID, peersAmt);
        this.money = 0;
    }

    // starts seller
    @Override
    public void start() throws RemoteException {
        super.start();
        Logger.log("Peer " + peerID + " (Seller)");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        int initialDelay = new Random().nextInt(0, PERIOD);
        int delay = new Random().nextInt(PERIOD/2, PERIOD);


        executor.scheduleAtFixedRate(() -> {
            // only sell something if not coordinator
            if (this.peerID == this.coordinatorID) {
                return;
            }
            Product product = Product.pickRandomProduct();
            int amount = (int) (Math.random() * 5) + 1;
            try {
                initiateOffer(product, amount);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }, this.peers.length * 200L + initialDelay, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void discoverAck(Product product, int amount, boolean available, int[] traderTimestamp) throws RemoteException {
        // Do nothing. This peer is not a buyer!
    }

    @Override
    public void buyAck(Product product, int amount, boolean bought, int[] traderTimestamp, long timeInitiated) throws RemoteException {
        // Do nothing. This peer is not a buyer!
    }

    // Handles coordinator acknowledgement seller's offer
    @Override
    public void offerAck(int[] traderTimestamp) throws RemoteException {
        // add job to thread pool
        executorService.submit(() -> {
            // this method only exists for keeping the seller timestamp up to date.
            synchronized (this) {
                this.timestamp[this.peerID] += 1;
                this.timestamp = VectorClock.merge(this.timestamp, traderTimestamp);
            }
        });
    }

    // Handles payment from coordinator
    @Override
    public void pay(int price, int[] traderTimestamp) throws RemoteException {
        // add job to thread pool
        executorService.submit(() -> {
            synchronized (this) {
                money += price; // pay the seller

                // keep the seller timestamp up to date.
                this.timestamp[this.peerID] += 1;
                this.timestamp = VectorClock.merge(this.timestamp, traderTimestamp);
            }

            Logger.log(Messages.getPayMoney(this.peerID, price));
        });
    }

    // Send offer to coordinator for product and amount
    public void initiateOffer(Product product, int amount) throws RemoteException {
        // add job to thread pool
        executorService.submit(() -> {
            try {
                synchronized (this) {
                    this.timestamp[this.peerID] += 1;
                }
                Logger.log(Messages.getOfferMessage(peerID, amount, product));
                this.peers[this.coordinatorID].offer(product, amount, this.timestamp, this.peerID);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
