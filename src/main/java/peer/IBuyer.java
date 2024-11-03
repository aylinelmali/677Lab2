package peer;

import product.Product;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBuyer extends Remote {
    /**
     * Sends a buy message to the seller.
     * @param product Product to buy.
     * @param amount Amount of products to buy.
     */
    void buy(Product product, int amount) throws RemoteException;

    /**
     * Acknowledges the buy message.
     * @param product The bought product.
     * @param bought Indicates if the products were successfully bought.
     */
    void ack(Product product, boolean bought) throws RemoteException;

}
