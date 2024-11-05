package peer;

import product.Product;
import utils.TraderState;
import utils.VectorClock;

import java.rmi.RemoteException;
import java.util.Arrays;

public abstract class APeer implements IPeer {

    protected final int peerID;
    protected IPeer[] peers;
    protected int coordinatorID;
    protected int[] timestamp;

    public APeer(int peerID, IPeer[] peers, int coordinatorID) {
        this.peerID = peerID;
        this.peers = peers;
        this.coordinatorID = coordinatorID;

        this.timestamp = new int[peers.length];
        Arrays.fill(timestamp, 0);
    }

    @Override
    public final int getPeerID() throws RemoteException {
        return peerID;
    }

    @Override
    public final void election(int[] tags) throws RemoteException {
        for (int tag : tags) {
            if (tag == peerID) {
                int max = Arrays.stream(tags).max().getAsInt();
                this.coordinatorID = max;
                peers[(peerID + 1) % peers.length].coordinator(max, peerID);
                return;
            }
        }

        for (int i = 1; i <= peers.length; i++) {
            int nextPeer = (i + peerID) % peers.length;
            int[] newTags = getNewTags(tags);
            try {
                peers[nextPeer].election(newTags);
                break;
            } catch (RemoteException e) {
                // TODO: Do logging
            }
        }
    }

    @Override
    public final void coordinator(int coordinatorID, int initiatorID) throws RemoteException {
        this.coordinatorID = coordinatorID;
        if (peerID != initiatorID) {
            peers[(peerID + 1) % peers.length].coordinator(coordinatorID, initiatorID);
        }
    }

    @Override
    public final void discover(Product product, int amount, int[] buyerTimestamp, int buyerID) throws RemoteException {
        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }

        var traderState = TraderState.readTraderState();
        Integer traderAmount = traderState.get(product);
        boolean available = traderAmount != null && traderAmount >= amount;

        peers[buyerID].discoverAck(product, available, this.timestamp);
    }

    @Override
    public final void buy(Product product, int amount, int[] buyerTimestamp, int buyerID) throws RemoteException {
        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }
        if (VectorClock.isSmallerThan(this.timestamp, buyerTimestamp)) {

            // buy event modifies timestamp.
            this.timestamp[this.peerID] += 1;
            VectorClock.mergeRightToLeft(this.timestamp, buyerTimestamp);

            peers[buyerID].buyAck(product, true, this.timestamp);
        } else { // timestamp of this peer is greater or concurrent.
            peers[buyerID].buyAck(product, false, this.timestamp);
        }
    }

    @Override
    public final void offer(Product product, int amount, int[] sellerTimestamp, int sellerID) throws RemoteException {
        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }
        var traderState = TraderState.readTraderState();
        traderState.compute(product, (k, oldAmount) -> oldAmount == null ? amount : oldAmount + amount);
        TraderState.writeTraderState(traderState);

        peers[sellerID].offerAck(this.timestamp);
    }

    protected int[] getNewTags(int[] tags) {
        int[] newSearchPath = new int[tags.length + 1];
        System.arraycopy(tags, 0, newSearchPath, 0, tags.length);
        newSearchPath[tags.length] = peerID;
        return newSearchPath;
    }
}
