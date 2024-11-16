package peer;

import product.Product;
import utils.Logger;
import utils.Messages;
import utils.VectorClock;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Buyer extends APeer {

    public static void main(String[] args) throws RemoteException {
        int peerID = Integer.parseInt(args[0]);
        int peersAmt = Integer.parseInt(args[1]);

        Registry registry = LocateRegistry.getRegistry("127.0.0.1", REGISTRY_ID);
        registry.rebind("" + peerID, new Buyer(peerID, peersAmt));
    }

    public static final int PERIOD = 4;

    private Product product;
    private int amount;

    public Buyer(int peerID, int peersAmt) throws RemoteException {
        super(peerID, peersAmt);
        pickRandomProduct();
    }

    // starts buyer
    @Override
    public void start() throws RemoteException {
        super.start();
        Logger.log("Peer " + peerID + " (Buyer)");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        int initialDelay = new Random().nextInt(1,11);

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
        }, initialDelay, PERIOD, TimeUnit.SECONDS);
    }

    // Handles acknowledgement from coordinator about product availability
    @Override
    public void discoverAck(Product product, boolean available, int[] traderTimestamp) throws RemoteException {
        // add job to thread pool
        executorService.submit(() -> {
            try {
                // check if ack is valid
                synchronized(this) {
                    this.timestamp[this.peerID] += 1;
                    this.timestamp = VectorClock.merge(this.timestamp, traderTimestamp);
                }
                // Check if product is available and if so, trigger buy request
                if (this.product == product && available) {
                    initiateBuy();
                } else {
                    pickRandomProduct();
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Handle acknowledgment of successful or failed purchase
    @Override
    public void buyAck(Product product, boolean bought, int[] traderTimestamp) throws RemoteException {
        // add job to thread pool
        executorService.submit(() -> {
            // check if ack is valid
            synchronized(this) {
                this.timestamp[this.peerID] += 1;
                this.timestamp = VectorClock.merge(this.timestamp, traderTimestamp);
            }
            // Check if product was bought successfully and if so, pick new product
            if (this.product == product && bought) {
                pickRandomProduct();
            }
        });
    }

    @Override
    public void offerAck(int[] sellerTimestamp) throws RemoteException {
        // Do nothing. This peer is not a Seller
    }

    @Override
    public void pay(int price, int[] traderTimestamp) throws RemoteException {
        // Do nothing. This peer is not a Seller
    }

    // Initiate discovery request to coordinator
    public void initiateDiscovery() throws RemoteException {
        // add job to thread pool
        executorService.submit(() -> {
            try {
                Logger.log(Messages.getDiscoveryMessage(peerID, amount, product));
                // updating timestamp and try discovery
                synchronized(this) {
                    this.timestamp[this.peerID] += 1;
                }
                this.peers[this.coordinatorID].discover(this.product, this.amount, this.timestamp, this.peerID);
            } catch (RemoteException e) {
                try {
                    Logger.log(Messages.getPeerCouldNotConnectMessage(peerID, coordinatorID));

                    // coordinator crashed, start election
                    int oldCoordinatorID = this.coordinatorID;
                    election(new int[] {});
                    waitForCoordinatorChangeWithTimeout(oldCoordinatorID, 5000);

                    // election done, retry.
                    if (this.peerID != this.coordinatorID) { // if this peer is a coordinator, discard discovery.
                        initiateDiscovery(); // retry discovery after election
                    } else {
                        Logger.log(Messages.getDiscardingLookupMessage(this.peerID));
                    }
                } catch (RemoteException f) {
                    throw new RuntimeException(f);
                }
            }
        });
    }

    // Initiate a purchase request to coordinator
    public void initiateBuy() throws RemoteException {
        // add job to thread pool
        executorService.submit(() -> {
            try {
                Logger.log(Messages.getBuyMessage(peerID, amount, product));
                synchronized (this) {
                    this.timestamp[this.peerID] += 1;
                }
                peers[coordinatorID].buy(this.product, this.amount, this.timestamp, this.peerID);
            } catch (RemoteException e) {
                try {
                    Logger.log(Messages.getPeerCouldNotConnectMessage(peerID, coordinatorID));
                    // coordinator crashed, start election
                    int oldCoordinatorID = this.coordinatorID;
                    election(new int[] {});
                    waitForCoordinatorChangeWithTimeout(oldCoordinatorID, 5000);

                    // election done, retry.
                    if (this.peerID != this.coordinatorID) { // if this peer is a coordinator, discard discovery.
                        initiateBuy(); // retry buy after election
                    } else {
                        Logger.log(Messages.getDiscardingBuyMessage(this.peerID));
                    }
                } catch (RemoteException f) {
                    throw new RuntimeException(f);
                }
            }
        });
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
