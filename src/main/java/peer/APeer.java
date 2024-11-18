package peer;

import product.Product;
import utils.Logger;
import utils.Messages;
import utils.TraderState;
import utils.VectorClock;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public abstract class APeer extends UnicastRemoteObject implements IPeer {

    public static final int REGISTRY_ID = 1099;
    public static final int CRASH_PERIOD = 10000;

    public final int peerID;
    public IPeer[] peers;
    public volatile int coordinatorID;
    public int[] timestamp;

    // crash functionality
    public boolean crashIfCoordinator;
    public boolean crashed;

    protected ExecutorService executorService;

    public APeer(int peerID, int peersAmt) throws RemoteException {
        super();
        this.peerID = peerID;

        this.timestamp = new int[peersAmt];
        Arrays.fill(timestamp, 0);

        this.peers = new IPeer[peersAmt];

        crashIfCoordinator = true;
        crashed = false;

        executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public void start() throws RemoteException {
        if (this.peerID == peers.length-1 && crashIfCoordinator && peers.length > 3) { // This is the highest coordinator
            int period = new Random().nextInt(CRASH_PERIOD/2, CRASH_PERIOD);

            // simulate periodic crash and recovery functionality
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(() -> {
                crashed = !crashed;
                if (!crashed) {
                    try {
                        election(new int[] {});
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, this.peers.length * 200L + period, period, TimeUnit.MILLISECONDS);
        }

        // get all other peers from the registry
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", REGISTRY_ID);
        for (int i = 0; i < this.peers.length; i++) {
            try {
                peers[i] = (IPeer) registry.lookup("" + i);
            } catch (NotBoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public final int getPeerID() throws RemoteException {
        return peerID;
    }

    @Override
    public final void election(int[] tags) throws RemoteException {
        // simulate crash
        simulateCrash();

        // add job to thread pool
        executorService.submit(() -> {
            try {
                // check if election has reached every peer
                for (int tag : tags) {
                    if (tag == peerID) { // election has reached every peer
                        int max = Arrays.stream(tags).max().getAsInt(); // find max peer
                        Logger.log(Messages.getElectionDoneMessage(max));
                        coordinator(max, tags);
                        return;
                    }
                }

                // election has not reached every peer, forward election to next peer that is alive.
                int[] newTags = getNewTags(tags);
                Logger.log(Messages.getPeerDoingElectionMessage(peerID, newTags));
                for (int i = 1; i <= peers.length; i++) {
                    int nextPeer = (i + peerID) % peers.length;
                    try { // check if next peer is alive, else try next peer.
                        peers[nextPeer].election(newTags);
                        break;
                    } catch (Exception e) {
                        Logger.log(Messages.getPeerDoesNotRespondMessage(nextPeer));
                    }
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public final void coordinator(int coordinatorID, int[] tags) throws RemoteException {
        // simulate crash
        simulateCrash();

        // add job to thread pool
        executorService.submit(() -> {
            // forward coordinator message to next peer in the tags array.
            try {
                Logger.log(Messages.getPeerUpdatesCoordinatorMessage(this.peerID, coordinatorID));
                this.coordinatorID = coordinatorID; // update coordinator
                int tagIndex = getPeerTagIndex(tags);
                if (tagIndex != -1 && tagIndex < tags.length-1) {
                    peers[tags[tagIndex + 1]].coordinator(coordinatorID, tags); // forward message
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // checks if the product and amount is available at the trader.
    @Override
    public final void discover(Product product, int amount, int[] buyerTimestamp, int buyerID) throws RemoteException {
        // simulate crash
        simulateCrash();

        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }

        // add job to thread pool
        executorService.submit(() -> {
            try {
                boolean available;

                synchronized (this) {
                    // method
                    if (this.peerID != this.coordinatorID) {
                        return;
                    }

                    // check if product available
                    TraderState traderState = TraderState.readTraderState();
                    available = traderState.productAvailable(product, amount);
                    if (available) {
                        Logger.log(Messages.getProductAvailableMessage(amount, product, buyerID));
                    } else {
                        Logger.log(Messages.getProductUnavailableMessage(amount, product, buyerID));
                    }

                    // update timestamp
                    this.timestamp[this.peerID] += 1;
                    this.timestamp = VectorClock.merge(this.timestamp, buyerTimestamp);
                }

                // send acknowledgement
                peers[buyerID].discoverAck(product, amount, available, this.timestamp);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // buy the product from the trader
    @Override
    public final void buy(Product product, int amount, int[] buyerTimestamp, int buyerID, long timeInitiated) throws RemoteException {
        // simulate crash
        simulateCrash();

        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }

        // add job to thread pool
        executorService.submit(() -> {
            try {
                boolean bought = false;
                synchronized (this) {
                    if (this.peerID != this.coordinatorID) {
                        return;
                    }

                    TraderState traderState = TraderState.readTraderState();
                    // check that timestamp of buyer is valid and that product is available
                    if (VectorClock.isSmallerThan(this.timestamp, buyerTimestamp) && traderState.productAvailable(product, amount)) {

                        // update timestamp
                        this.timestamp[this.peerID] += 1;
                        this.timestamp = VectorClock.merge(this.timestamp, buyerTimestamp);

                        bought = true;

                        // take out of stock and pay sellers
                        List<Integer> sellers = traderState.takeOutOfStock(product, amount);
                        TraderState.writeTraderState(traderState);
                        Logger.log(Messages.getBoughtMessage(amount, product, this.peerID, buyerID));
                        for (Integer sellerID : sellers) {
                            peers[sellerID].pay(product.getPrice(), this.timestamp); // pay sellers
                        }
                    } else { // timestamp of this peer is greater or concurrent.
                        Logger.log(Messages.getBuyFailedMessage(buyerID, this.peerID));
                    }
                }
                // send acknowledgement
                peers[buyerID].buyAck(product, amount, bought, this.timestamp, timeInitiated);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // called when seller offers product to trader.
    @Override
    public final void offer(Product product, int amount, int[] sellerTimestamp, int sellerID) throws RemoteException {
        // simulate crash
        simulateCrash();

        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }

        // add job to thread pool
        executorService.submit(() -> {
            try {
                synchronized (this) {
                    if (this.peerID != this.coordinatorID) {
                        return;
                    }

                    // add products to stock
                    TraderState traderState = TraderState.readTraderState();
                    traderState.putIntoStock(product, amount, sellerID);
                    TraderState.writeTraderState(traderState);
                    Logger.log(Messages.getAddedToStockMessage(amount, product, sellerID, this.peerID));

                    // update timestamp
                    this.timestamp[this.peerID] += 1;
                    this.timestamp = VectorClock.merge(this.timestamp, sellerTimestamp);
                }

                // send acknowledge message
                peers[sellerID].offerAck(this.timestamp);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Simulates trader crash.
     */
    private void simulateCrash() throws RemoteException {
        if (crashed) {
            throw new RemoteException();
        }
    }

    /**
     * Adds this peer id to tags array.
     * @param tags Old tags array not containing this peer id.
     * @return New tags array containing this peer id.
     */
    protected int[] getNewTags(int[] tags) {
        int[] newSearchPath = new int[tags.length + 1];
        System.arraycopy(tags, 0, newSearchPath, 0, tags.length);
        newSearchPath[tags.length] = peerID;
        return newSearchPath;
    }

    /**
     * Retrieves index of this peer id in tags array.
     * @param tags Tags array containing all peer indices.
     * @return Index of this peer id in tags array.
     */
    private int getPeerTagIndex(int[] tags) {
        for (int i = 0; i < tags.length; i++) {
            if (tags[i] == this.peerID) {
                return i;
            }
        }
        return -1;
    }

    public void setPeers(IPeer[] peers) {
        this.peers = peers;
    }

    /**
     * Waits until coordinator id changes or timeout occurs.
     * @param oldCoordinatorID Old coordinator id.
     * @param timeout Timeout for maximum wait.
     */
    public void waitForCoordinatorChangeWithTimeout(int oldCoordinatorID, long timeout) {
        long startTime = System.currentTimeMillis();
        long currentTime;
        do {
            currentTime = System.currentTimeMillis();
        } while(this.coordinatorID == oldCoordinatorID && currentTime - startTime < timeout);
    }
}
