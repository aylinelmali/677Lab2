package peer;

import product.Product;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

public interface IPeer extends Remote {
    /**
     * Initializes and starts the peer.
     */
    void start() throws RemoteException;

    /**
     * @return The ID of the peer.
     */
    int getPeerID() throws RemoteException;

    // Coordination

    /**
     * Sends an election message to the next peer.
     * @param tags Contains the ID's of the election.
     */
    void election(int[] tags) throws RemoteException;

    /**
     * Sends a coordinator to the next peer to tell the new coordinator.
     * @param coordinatorID The id of the new coordinator.
     * @param tags Contains the ID's of the election.
     */
    void coordinator(int coordinatorID, int[] tags) throws RemoteException;

    // Trading

    /**
     * Sens a discover message to the trader.
     * @param product Product to discover.
     * @param amount Amount to discover.
     * @param buyerTimestamp Timestamp of the buyer.
     * @param buyerID ID of the buyer who initiated the discovery.
     */
    void discover(Product product, int amount, int[] buyerTimestamp, int buyerID) throws RemoteException;

    /**
     * Acknowledges the discover message.
     * @param product Discovered product.
     * @param amount Amount discovered.
     * @param available Product available.
     * @param traderTimestamp Timestamp of trader.
     */
    void discoverAck(Product product, int amount, boolean available, int[] traderTimestamp) throws RemoteException;

    /**
     * Sends a buy message to the trader.
     * @param product Product to buy.
     * @param amount Amount of products to buy.
     * @param buyerTimestamp Timestamp of the buyer.
     * @param buyerID ID of the buyer who initiated the buy.
     * @param timeInitiated Time when buy was initiated. Used for statistics.
     */
    void buy(Product product, int amount, int[] buyerTimestamp, int buyerID, long timeInitiated) throws RemoteException;

    /**
     * Acknowledges the buy message.
     * @param product The bought product.
     * @param amount The amount of product bought.
     * @param bought Indicates if the products were successfully bought.
     * @param traderTimestamp Timestamp of the trader.
     * @param timeInitiated Time when buy was initiated. Used for statistics.
     */
    void buyAck(Product product, int amount, boolean bought, int[] traderTimestamp, long timeInitiated) throws RemoteException;

    /**
     * Sends an offer message to the trader.
     * @param product Product to offer.
     * @param amount Amounts of products to offer.
     * @param sellerTimestamp Timestamp of the seller.
     * @param sellerID ID of the seller who initiated the offer.
     */
    void offer(Product product, int amount, int[] sellerTimestamp, int sellerID) throws RemoteException;

    /**
     * Acknowledges the offer message.
     * @param sellerTimestamp Timestamp of the seller.
     */
    void offerAck(int[] sellerTimestamp) throws RemoteException;

    /**
     * Pays the seller.
     * @param price Amount of money paid.
     * @param traderTimestamp Timestamp of the trader.
     */
    void pay(int price, int[] traderTimestamp) throws RemoteException;
}
