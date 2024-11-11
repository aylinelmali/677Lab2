package peer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.Product;
import utils.TraderState;

import java.rmi.RemoteException;

public class CommunicationTest {

    @BeforeEach
    public void setUp() {
        TraderState.resetTraderState();
    }

    @Test
    public void testOffer() throws RemoteException {
        Seller seller = new Seller(0,2,1);
        APeer coordinator = new APeer(1, 2, 1) {
            @Override
            public void discoverAck(Product product, boolean available, int[] traderTimestamp) throws RemoteException {}

            @Override
            public void buyAck(Product product, boolean bought, int[] traderTimestamp) throws RemoteException {}

            @Override
            public void offerAck(int[] sellerTimestamp) throws RemoteException {}

            @Override
            public void pay(int price, int[] traderTimestamp) throws RemoteException {}
        };
        IPeer[] peers = new IPeer[] { seller, coordinator };
        seller.setPeers(peers);
        coordinator.setPeers(peers);
        seller.initiateOffer(Product.BOARS, 5);
        Assertions.assertArrayEquals(new int[] { 2, 1 }, seller.timestamp);
        Assertions.assertArrayEquals(new int[] { 1, 1 }, coordinator.timestamp);
        seller.initiateOffer(Product.FISH, 3);

        TraderState traderState = TraderState.readTraderState();
        Assertions.assertTrue(traderState.productAvailable(Product.BOARS, 5));
        Assertions.assertTrue(traderState.productAvailable(Product.FISH, 3));
        Assertions.assertFalse(traderState.productAvailable(Product.BOARS, 6));
        Assertions.assertFalse(traderState.productAvailable(Product.FISH, 4));

        Assertions.assertArrayEquals(new int[] { 4, 2 }, seller.timestamp);
        Assertions.assertArrayEquals(new int[] { 3, 2 }, coordinator.timestamp);
    }

    @Test
    public void testSuccessfulBuy() throws RemoteException {
        Buyer buyer = new Buyer(0,3,2);
        Seller seller = new Seller(1,3,2);
        APeer coordinator = new APeer(2, 3, 2) {
            @Override
            public void discoverAck(Product product, boolean available, int[] traderTimestamp) throws RemoteException {}

            @Override
            public void buyAck(Product product, boolean bought, int[] traderTimestamp) throws RemoteException {}

            @Override
            public void offerAck(int[] sellerTimestamp) throws RemoteException {}

            @Override
            public void pay(int price, int[] traderTimestamp) throws RemoteException {}
        };
        IPeer[] peers = new IPeer[] { buyer, seller, coordinator };
        buyer.setPeers(peers);
        seller.setPeers(peers);
        coordinator.setPeers(peers);
        // put items in stock
        seller.initiateOffer(Product.BOARS, 5);
        Assertions.assertArrayEquals(new int[] { 0, 0, 0 }, buyer.timestamp);
        Assertions.assertArrayEquals(new int[] { 0, 2, 1 }, seller.timestamp);
        Assertions.assertArrayEquals(new int[] { 0, 1, 1 }, coordinator.timestamp);

        buyer.pickProduct(Product.BOARS, 3);
        buyer.initiateDiscovery();

        TraderState traderState = TraderState.readTraderState();
        Assertions.assertFalse(traderState.productAvailable(Product.BOARS, 3));
        Assertions.assertTrue(traderState.productAvailable(Product.BOARS, 2));
        Assertions.assertEquals(9, seller.money);

        Assertions.assertArrayEquals(new int[] { 4, 1, 3 }, buyer.timestamp);
        Assertions.assertArrayEquals(new int[] { 3, 5, 3 }, seller.timestamp);
        Assertions.assertArrayEquals(new int[] { 3, 1, 3 }, coordinator.timestamp);
    }

    @Test
    public void testFailedBuyInvalidTimestamp() throws RemoteException {
        Buyer buyer = new Buyer(0,3,2);
        Seller seller = new Seller(1,3,2);
        APeer coordinator = new APeer(2, 3, 2) {
            @Override
            public void discoverAck(Product product, boolean available, int[] traderTimestamp) throws RemoteException {}

            @Override
            public void buyAck(Product product, boolean bought, int[] traderTimestamp) throws RemoteException {}

            @Override
            public void offerAck(int[] sellerTimestamp) throws RemoteException {}

            @Override
            public void pay(int price, int[] traderTimestamp) throws RemoteException {}
        };
        IPeer[] peers = new IPeer[] { buyer, seller, coordinator };
        buyer.setPeers(peers);
        seller.setPeers(peers);
        coordinator.setPeers(peers);
        // put items in stock
        seller.initiateOffer(Product.BOARS, 5);
        Assertions.assertArrayEquals(new int[] { 0, 0, 0 }, buyer.timestamp);
        Assertions.assertArrayEquals(new int[] { 0, 2, 1 }, seller.timestamp);
        Assertions.assertArrayEquals(new int[] { 0, 1, 1 }, coordinator.timestamp);

        buyer.pickProduct(Product.BOARS, 3);
        buyer.initiateBuy();

        TraderState traderState = TraderState.readTraderState();
        Assertions.assertTrue(traderState.productAvailable(Product.BOARS, 5));
        Assertions.assertEquals(0, seller.money);

        Assertions.assertArrayEquals(new int[] { 2, 1, 1 }, buyer.timestamp);
        Assertions.assertArrayEquals(new int[] { 0, 2, 1 }, seller.timestamp);
        Assertions.assertArrayEquals(new int[] { 0, 1, 1 }, coordinator.timestamp);
    }

    @Test
    public void testFailedBuyLowStock() throws RemoteException {
        Buyer buyer = new Buyer(0,3,2);
        Seller seller = new Seller(1,3,2);
        APeer coordinator = new APeer(2, 3, 2) {
            @Override
            public void discoverAck(Product product, boolean available, int[] traderTimestamp) throws RemoteException {}

            @Override
            public void buyAck(Product product, boolean bought, int[] traderTimestamp) throws RemoteException {}

            @Override
            public void offerAck(int[] sellerTimestamp) throws RemoteException {}

            @Override
            public void pay(int price, int[] traderTimestamp) throws RemoteException {}
        };
        IPeer[] peers = new IPeer[] { buyer, seller, coordinator };
        buyer.setPeers(peers);
        seller.setPeers(peers);
        coordinator.setPeers(peers);
        // put items in stock
        seller.initiateOffer(Product.BOARS, 5);
        Assertions.assertArrayEquals(new int[] { 0, 0, 0 }, buyer.timestamp);
        Assertions.assertArrayEquals(new int[] { 0, 2, 1 }, seller.timestamp);
        Assertions.assertArrayEquals(new int[] { 0, 1, 1 }, coordinator.timestamp);

        buyer.pickProduct(Product.BOARS, 6);
        buyer.initiateDiscovery();

        TraderState traderState = TraderState.readTraderState();
        Assertions.assertFalse(traderState.productAvailable(Product.BOARS, 6));
        Assertions.assertTrue(traderState.productAvailable(Product.BOARS, 5));
        Assertions.assertEquals(0, seller.money);

        Assertions.assertArrayEquals(new int[] { 2, 1, 2 }, buyer.timestamp);
        Assertions.assertArrayEquals(new int[] { 0, 2, 1 }, seller.timestamp);
        Assertions.assertArrayEquals(new int[] { 1, 1, 2 }, coordinator.timestamp);

        buyer.pickProduct(Product.BOARS, 6);
        buyer.initiateBuy();
        Assertions.assertFalse(traderState.productAvailable(Product.BOARS, 6));
        Assertions.assertTrue(traderState.productAvailable(Product.BOARS, 5));
        Assertions.assertEquals(0, seller.money);
        Assertions.assertArrayEquals(new int[] { 4, 1, 2 }, buyer.timestamp);
        Assertions.assertArrayEquals(new int[] { 0, 2, 1 }, seller.timestamp);
        Assertions.assertArrayEquals(new int[] { 1, 1, 2 }, coordinator.timestamp);
    }
}
