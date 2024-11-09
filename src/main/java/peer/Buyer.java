package peer;

import product.Product;
import utils.Logger;
import utils.Messages;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Buyer extends APeer{

    private Product product;
    private int amount;

    public Buyer(int peerID, int peersAmt, int coordinatorID) throws RemoteException {
        super(peerID, peersAmt, coordinatorID);
        product = Product.pickRandomProduct();
        amount = (int) (Math.random() * 5) + 1;
    }

    @Override
    public void start() throws RemoteException {
        Logger.log("Peer " + peerID + " (Buyer)");
    }

    @Override
    public void discoverAck(Product product, boolean available, int[] traderTimestamp) throws RemoteException {

    }

    @Override
    public void buyAck(Product product, boolean bought, int[] traderTimestamp) throws RemoteException {

    }

    @Override
    public void offerAck(int[] sellerTimestamp) throws RemoteException {

    }

    @Override
    public void pay(int price, int[] traderTimestamp) throws RemoteException {

    }

    /**
     * Picks a random new product.
     */
    private void buyNewProduct() throws RemoteException {
        product = Product.pickRandomProduct();
        amount = (int) (Math.random() * 5) + 1;
    }
}
