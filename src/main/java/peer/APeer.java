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
import java.util.concurrent.ExecutorService;
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
            int initialDelay = 10;
            int period = 10;

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
            }, initialDelay, period, TimeUnit.SECONDS);
        }

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

        executorService.submit(() -> {
            try {
                for (int tag : tags) {
                    if (tag == peerID) {
                        int max = Arrays.stream(tags).max().getAsInt();
                        Logger.log(Messages.getElectionDoneMessage(max));
                        coordinator(max, tags);
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
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public final void coordinator(int coordinatorID, int[] tags) throws RemoteException {
        // simulate crash
        simulateCrash();

        executorService.submit(() -> {
            try {
                Logger.log(Messages.getPeerUpdatesCoordinatorMessage(this.peerID, coordinatorID));
                this.coordinatorID = coordinatorID;
                int tagIndex = getPeerTagIndex(peerID, tags);
                if (tagIndex != -1 && tagIndex < tags.length-1) {
                    peers[tags[tagIndex + 1]].coordinator(coordinatorID, tags);
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public final void discover(Product product, int amount, int[] buyerTimestamp, int buyerID) throws RemoteException {
        // simulate crash
        simulateCrash();

        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }

        executorService.submit(() -> {
            try {
                boolean available;

                synchronized (this) {
                    // method
                    if (this.peerID != this.coordinatorID) {
                        return;
                    }

                    TraderState traderState = TraderState.readTraderState();
                    available = traderState.productAvailable(product, amount);
                    if (available) {
                        Logger.log(Messages.getProductAvailableMessage(amount, product, buyerID));
                    } else {
                        Logger.log(Messages.getProductUnavailableMessage(amount, product, buyerID));
                    }

                    this.timestamp[this.peerID] += 1;
                    this.timestamp = VectorClock.merge(this.timestamp, buyerTimestamp);
                }

                peers[buyerID].discoverAck(product, available, this.timestamp);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public final void buy(Product product, int amount, int[] buyerTimestamp, int buyerID) throws RemoteException {
        // simulate crash
        simulateCrash();

        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }

        executorService.submit(() -> {
            try {
                boolean bought = false;
                synchronized (this) {
                    // method
                    if (this.peerID != this.coordinatorID) {
                        return;
                    }

                    TraderState traderState = TraderState.readTraderState();
                    if (VectorClock.isSmallerThan(this.timestamp, buyerTimestamp) && traderState.productAvailable(product, amount)) {
                        this.timestamp[this.peerID] += 1;
                        this.timestamp = VectorClock.merge(this.timestamp, buyerTimestamp);

                        bought = true;

                        List<Integer> sellers = traderState.takeOutOfStock(product, amount);
                        TraderState.writeTraderState(traderState);
                        Logger.log(Messages.getBoughtMessage(amount, product, this.peerID, buyerID));
                        for (Integer sellerID : sellers) {
                            peers[sellerID].pay(product.getPrice(), this.timestamp);
                        }
                    } else { // timestamp of this peer is greater or concurrent.
                        Logger.log(Messages.getBuyFailedMessage(buyerID, this.peerID));
                    }
                }
                peers[buyerID].buyAck(product, bought, this.timestamp);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public final void offer(Product product, int amount, int[] sellerTimestamp, int sellerID) throws RemoteException {
        // simulate crash
        simulateCrash();

        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }

        executorService.submit(() -> {
            try {
                synchronized (this) {
                    // method
                    if (this.peerID != this.coordinatorID) {
                        return;
                    }

                    TraderState traderState = TraderState.readTraderState();
                    traderState.putIntoStock(product, amount, sellerID);
                    TraderState.writeTraderState(traderState);
                    Logger.log(Messages.getAddedToStockMessage(amount, product, sellerID, this.peerID));

                    this.timestamp[this.peerID] += 1;
                    this.timestamp = VectorClock.merge(this.timestamp, sellerTimestamp);
                }
                peers[sellerID].offerAck(this.timestamp);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
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

    private int getPeerTagIndex(int peerID, int[] tags) {
        for (int i = 0; i < tags.length; i++) {
            if (tags[i] == peerID) {
                return i;
            }
        }
        return -1;
    }

    public void setPeers(IPeer[] peers) {
        this.peers = peers;
    }
}
