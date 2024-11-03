package peer;

import product.Product;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISeller extends Remote {

    void offer(Product product, int amount) throws RemoteException;

    void receiveMoney(Product boughtProduct, int amountBought, int money) throws RemoteException;
}
