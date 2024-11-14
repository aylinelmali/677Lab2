package peer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

public class TraderElectionTest {

    @Test
    public void electionTest() throws RemoteException {
        APeer peer1 = new Buyer(0, 4);
        APeer peer2 = new Buyer(1, 4);
        APeer peer3 = new Buyer(2, 4);
        APeer peer4 = new Buyer(3, 4);
        APeer[] peers = {peer1, peer2, peer3, peer4};
        peer1.setPeers(peers);
        peer2.setPeers(peers);
        peer3.setPeers(peers);
        peer4.setPeers(peers);

        Assertions.assertEquals(0, peer1.coordinatorID);
        Assertions.assertEquals(0, peer2.coordinatorID);
        Assertions.assertEquals(0, peer3.coordinatorID);
        Assertions.assertEquals(0, peer4.coordinatorID);

        peer1.election(new int[]{});

        Assertions.assertEquals(3, peer1.coordinatorID);
        Assertions.assertEquals(3, peer2.coordinatorID);
        Assertions.assertEquals(3, peer3.coordinatorID);
        Assertions.assertEquals(3, peer4.coordinatorID);
    }

    @Test
    public void concurrentElectionTest() throws RemoteException, InterruptedException {
        APeer peer1 = new Buyer(0, 4);
        APeer peer2 = new Buyer(1, 4);
        APeer peer3 = new Buyer(2, 4);
        APeer peer4 = new Buyer(3, 4);
        APeer[] peers = {peer1, peer2, peer3, peer4};
        peer1.setPeers(peers);
        peer2.setPeers(peers);
        peer3.setPeers(peers);
        peer4.setPeers(peers);

        Assertions.assertEquals(0, peer1.coordinatorID);
        Assertions.assertEquals(0, peer2.coordinatorID);
        Assertions.assertEquals(0, peer3.coordinatorID);
        Assertions.assertEquals(0, peer4.coordinatorID);

        Thread t1 = new Thread(() -> {
            try {
                peer1.election(new int[]{});
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        t1.start();

        Thread t2 = new Thread(() -> {
            try {
                peer3.election(new int[]{});
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        t2.start();

        t1.join();
        t2.join();

        Assertions.assertEquals(3, peer1.coordinatorID);
        Assertions.assertEquals(3, peer2.coordinatorID);
        Assertions.assertEquals(3, peer3.coordinatorID);
        Assertions.assertEquals(3, peer4.coordinatorID);
    }

    @Test
    public void reelectionTest() throws RemoteException {
        APeer peer1 = new Buyer(0, 4);
        APeer peer3 = new Buyer(2, 4);
        APeer[] peers = {peer1, null, peer3, null};
        peer1.setPeers(peers);
        peer3.setPeers(peers);

        peer1.coordinatorID = 3;
        peer3.coordinatorID = 3;

        Assertions.assertEquals(3, peer1.coordinatorID);
        Assertions.assertEquals(3, peer3.coordinatorID);

        peer1.election(new int[]{});

        Assertions.assertEquals(2, peer1.coordinatorID);
        Assertions.assertEquals(2, peer3.coordinatorID);
    }
}
