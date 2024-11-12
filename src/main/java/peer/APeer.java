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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class APeer extends UnicastRemoteObject implements IPeer {

    public static int REGISTRY_ID = 1099;

    public final int peerID;
    public IPeer[] peers;
    public int coordinatorID;
    public int[] timestamp;

    // crash functionality
    public boolean crashIfCoordinator;
    public boolean crashed;

    public APeer(int peerID, int peersAmt, int coordinatorID) throws RemoteException {
        super();
        this.peerID = peerID;
        this.coordinatorID = coordinatorID;

        this.timestamp = new int[peersAmt];
        Arrays.fill(timestamp, 0);

        this.peers = new IPeer[peersAmt];

        crashIfCoordinator = true;
        crashed = false;
    }

    @Override
    public void start() throws RemoteException {
        // TODO: Crash behavior
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

        // method
        for (int tag : tags) {
            if (tag == peerID) {
                int max = Arrays.stream(tags).max().getAsInt();
                this.coordinatorID = max;
                Logger.log(Messages.getElectionDoneMessage(max));
                peers[(peerID + 1) % peers.length].coordinator(max, peerID);
                return;
            }
        }

        int[] newTags = getNewTags(tags);
        Logger.log(Messages.getPeerDoingElectionMessage(peerID, newTags));
        for (int i = 1; i <= peers.length; i++) {
            int nextPeer = (i + peerID) % peers.length;
            try {
                peers[nextPeer].election(newTags);
                break;
            } catch (Exception e) {
                Logger.log(Messages.getPeerDoesNotRespondMessage(nextPeer));
            }
        }
    }

    @Override
    public final void coordinator(int coordinatorID, int initiatorID) throws RemoteException {
        // simulate crash
        simulateCrash();

        // method
        Logger.log(Messages.getPeerUpdatesCoordinatorMessage(this.peerID, coordinatorID));
        this.coordinatorID = coordinatorID;
        if (peerID != initiatorID) {
            peers[(peerID + 1) % peers.length].coordinator(coordinatorID, initiatorID);
        }
    }

    @Override
    public final synchronized void discover(Product product, int amount, int[] buyerTimestamp, int buyerID) throws RemoteException {
        // simulate crash
        simulateCrash();

        // method
        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }

        TraderState traderState = TraderState.readTraderState();
        boolean available = traderState.productAvailable(product, amount);
        if (available) {
            Logger.log(Messages.getProductAvailableMessage(amount, product, buyerID));
        } else {
            Logger.log(Messages.getProductUnavailableMessage(amount, product, buyerID));
        }

        this.timestamp[this.peerID] += 1;
        this.timestamp = VectorClock.merge(this.timestamp, buyerTimestamp);

        peers[buyerID].discoverAck(product, available, this.timestamp);
    }

    @Override
    public final synchronized void buy(Product product, int amount, int[] buyerTimestamp, int buyerID) throws RemoteException {
        // simulate crash
        simulateCrash();

        // method
        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }
        TraderState traderState = TraderState.readTraderState();
        if (VectorClock.isSmallerThan(this.timestamp, buyerTimestamp) && traderState.productAvailable(product, amount)) {
            this.timestamp[this.peerID] += 1;
            this.timestamp = VectorClock.merge(this.timestamp, buyerTimestamp);

            peers[buyerID].buyAck(product, true, this.timestamp);

            List<Integer> sellers = traderState.takeOutOfStock(product, amount);
            TraderState.writeTraderState(traderState);
            Logger.log(Messages.getBoughtMessage(amount, product, this.peerID, buyerID));
            for (Integer sellerID : sellers) {
                peers[sellerID].pay(product.getPrice(), this.timestamp);
            }
        } else { // timestamp of this peer is greater or concurrent.
            Logger.log(Messages.getBuyFailedMessage(buyerID, this.peerID));
            peers[buyerID].buyAck(product, false, this.timestamp);
        }
    }

    @Override
    public final synchronized void offer(Product product, int amount, int[] sellerTimestamp, int sellerID) throws RemoteException {
        // simulate crash
        simulateCrash();

        // method
        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }

        TraderState traderState = TraderState.readTraderState();
        traderState.putIntoStock(product, amount, sellerID);
        TraderState.writeTraderState(traderState);
        Logger.log(Messages.getAddedToStockMessage(amount, product, sellerID, this.peerID));

        this.timestamp[this.peerID] += 1;
        this.timestamp = VectorClock.merge(this.timestamp, sellerTimestamp);
        peers[sellerID].offerAck(this.timestamp);
    }

    private void simulateCrash() throws RemoteException {
        if (crashed) {
            throw new RemoteException();
        }
    }

    protected int[] getNewTags(int[] tags) {
        int[] newSearchPath = new int[tags.length + 1];
        System.arraycopy(tags, 0, newSearchPath, 0, tags.length);
        newSearchPath[tags.length] = peerID;
        return newSearchPath;
    }
}
