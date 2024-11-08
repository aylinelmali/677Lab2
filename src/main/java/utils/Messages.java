package utils;

import product.Product;

import java.util.List;

public class Messages {

    public static String getBoughtMessage(int sellerID, Product product, int stock) {
        return "Bought " + product + " from seller " + sellerID + ". Remaining stock: " + stock;
    }
}
