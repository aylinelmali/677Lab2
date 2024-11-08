package utils;

import product.Product;

import java.util.List;

public class Messages {

    public static String getBoughtMessage(int sellerID, Product product, int stock) {
        return "Bought " + product + " from seller " + sellerID + ". Remaining stock: " + stock;
    }

    public static String getNewCoordinatorMessage(int coordID) {
        return "Dear buyers and sellers, my ID is " + coordID + " and I am the new coordinator";
    }

    public static String getNotEnoughStockMessage(int sellerID, Product product) {
        return "Seller " + sellerID + " does not have enough stock for product " + product;
    }
}
