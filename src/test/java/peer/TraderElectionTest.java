package peer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

public class TraderElectionTest {

    @Test
    public void testWaitNoTimeout() throws RemoteException {
        APeer peer1 = new Buyer(0, 2);
        APeer peer2 = new Buyer(1, 2);
        APeer[] peers = {peer1, peer2};
        peer1.setPeers(peers);
        peer2.setPeers(peers);

        int oldCoordinatorID = 0;
        peer1.election(new int[] {});
        long timeBefore = System.currentTimeMillis();
        peer1.waitForCoordinatorChangeWithTimeout(oldCoordinatorID,5000);
        peer2.waitForCoordinatorChangeWithTimeout(oldCoordinatorID,5000);
        long timeAfter = System.currentTimeMillis();

        Assertions.assertEquals(1, peer1.coordinatorID);
        Assertions.assertEquals(1, peer2.coordinatorID);
        Assertions.assertTrue(timeAfter - timeBefore < 1000);
    }

    @Test
    public void testWaitTimeout() throws RemoteException {
        APeer peer1 = new Buyer(0, 2);
        APeer peer2 = new Buyer(1, 2);
        APeer[] peers = {peer1, peer2};
        peer1.setPeers(peers);
        peer2.setPeers(peers);

        int oldCoordinatorID = 0;
        long timeBefore = System.currentTimeMillis();
        peer1.waitForCoordinatorChangeWithTimeout(oldCoordinatorID,100);
        long timeAfter = System.currentTimeMillis();

        Assertions.assertEquals(0, peer1.coordinatorID);
        Assertions.assertEquals(0, peer2.coordinatorID);
        Assertions.assertTrue(timeAfter - timeBefore >= 100);
    }

    @Test
    public void electionTest() throws RemoteException, InterruptedException {
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

        peer1.election(new int[] {});
        Thread.sleep(100);

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

        peer1.election(new int[] {});
        peer3.election(new int[] {});

        Thread.sleep(100);

        Assertions.assertEquals(3, peer1.coordinatorID);
        Assertions.assertEquals(3, peer2.coordinatorID);
        Assertions.assertEquals(3, peer3.coordinatorID);
        Assertions.assertEquals(3, peer4.coordinatorID);
    }

    @Test
    public void reelectionTest() throws RemoteException, InterruptedException {

        APeer peer1 = new Buyer(0, 4);
        APeer peer3 = new Buyer(2, 4);
        APeer[] peers = {peer1, null, peer3, null};
        peer1.setPeers(peers);
        peer3.setPeers(peers);

        Assertions.assertEquals(0, peer1.coordinatorID);
        Assertions.assertEquals(0, peer3.coordinatorID);

        peer1.election(new int[] {});
        Thread.sleep(100);

        Assertions.assertEquals(2, peer1.coordinatorID);
        Assertions.assertEquals(2, peer3.coordinatorID);
    }
}
