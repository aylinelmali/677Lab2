package peer;

import product.Product;
import utils.TraderState;
import utils.VectorClock;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

public abstract class APeer implements IPeer {

    public final int peerID;
    public IPeer[] peers;
    public int coordinatorID;
    public int[] timestamp;

    public APeer(int peerID, int peersAmt, int coordinatorID) {
        this.peerID = peerID;
        this.coordinatorID = coordinatorID;

        this.timestamp = new int[peersAmt];
        Arrays.fill(timestamp, 0);
    }

    @Override
    public final int getPeerID() throws RemoteException {
        return peerID;
    }

    @Override
    public final void setPeers(IPeer[] peers) {
        this.peers = peers;
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
            } catch (Exception e) {
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

        TraderState traderState = TraderState.readTraderState();
        boolean available = traderState.productAvailable(product, amount);

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
            this.timestamp = VectorClock.merge(this.timestamp, buyerTimestamp);

            peers[buyerID].buyAck(product, true, this.timestamp);

            TraderState traderState = TraderState.readTraderState();
            List<Integer> sellers = traderState.takeOutOfStock(product, amount);
            TraderState.writeTraderState(traderState);
            for (Integer sellerID : sellers) {
                peers[sellerID].pay(product.getPrice(), this.timestamp);
            }
        } else { // timestamp of this peer is greater or concurrent.
            peers[buyerID].buyAck(product, false, this.timestamp);
        }
    }

    @Override
    public final void offer(Product product, int amount, int[] sellerTimestamp, int sellerID) throws RemoteException {
        if (this.peerID != this.coordinatorID) {
            throw new RemoteException();
        }


        TraderState traderState = TraderState.readTraderState();
        traderState.putIntoStock(product, amount, sellerID);
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
