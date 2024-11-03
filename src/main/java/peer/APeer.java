package peer;

import java.rmi.RemoteException;
import java.util.Arrays;

public class APeer implements IPeer {

    private final int peerID;
    private IPeer[] peers;
    private int coordinatorID;

    public APeer(int peerID, IPeer[] peers, int coordinatorID) {
        this.peerID = peerID;
        this.peers = peers;
        this.coordinatorID = coordinatorID;
    }

    @Override
    public void start() throws RemoteException {

    }

    @Override
    public int getPeerID() throws RemoteException {
        return peerID;
    }

    @Override
    public void election(int[] tags) throws RemoteException {
        for (int tag : tags) {
            if (tag == peerID) {
                int max = Arrays.stream(tags).max().getAsInt();
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
    public void coordinator(int coordinatorID, int initiatorID) throws RemoteException {
        this.coordinatorID = coordinatorID;
        if (peerID != initiatorID) {
            peers[(peerID + 1) % peers.length].coordinator(coordinatorID, initiatorID);
        }
    }

    protected int[] getNewTags(int[] tags) {
        int[] newSearchPath = new int[tags.length + 1];
        System.arraycopy(tags, 0, newSearchPath, 0, tags.length);
        newSearchPath[tags.length] = peerID;
        return newSearchPath;
    }
}
