package utils;

import product.Product;

import java.util.Arrays;

public class Messages {
    public static String getElectionDoneMessage(int coordinatorID) {
        return "Election done. New coordinator is " + coordinatorID + ".";
    }

    public static String getPeerDoingElectionMessage(int peerID, int[] newTags) {
        return "Peer " + peerID + " is doing election. Tags: " + Arrays.toString(newTags) + ".";
    }

    public static String getPeerDoesNotRespondMessage(int peerID) {
        return "Peer " + peerID + " doesn't respond.";
    }

    public static String getPeerUpdatesCoordinatorMessage(int peerID, int coordinatorID) {
        return "Peer " + peerID + " sets coordinator to " + coordinatorID + ".";
    }

    public static String getBoughtMessage(int amount, Product product, int peerID, int buyerID) {
        return amount + " piece(s) of " + product + " bought from coordinator " + peerID + " by peer " + buyerID + ".";
    }

    public static String getBuyFailedMessage(int buyerID, int coordinatorID) {
        return "Peer " + buyerID + " couldn't buy product from coordinator " + coordinatorID + ". Timestamp is invalid.";
    }

    public static String getAddedToStockMessage(int amount, Product product, int sellerID, int peerID) {
        return amount + " piece(s) of " + product + " offered from " + sellerID + " added to stock by coordinator " + peerID + ".";
    }

    public static String getDiscoveryMessage(int peerID, int amount, Product product) {
        return "Peer " + peerID + " is looking for " + amount + " piece(s) of " + product + ".";
    }

    public static String getBuyMessage(int peerID, int amount, Product product) {
        return "Peer " + peerID + " is buying " + amount + " piece(s) of " + product + ".";
    }

    public static String getPayMoney(int peerID, int price) {
        return "Seller " + peerID + " received " + price + " money.";
    }

    public static String getOfferMessage(int peerID, int amount, Product product) {
        return "Seller " + peerID + " offers " + amount + " piece(s) of " + product + ".";
    }

    public static String getProductAvailableMessage(int amount, Product product, int buyerID) {
        return amount + " piece(s) of " + product + " is available for peer " + buyerID + ".";
    }

    public static String getProductUnavailableMessage(int amount, Product product, int buyerID) {
        return amount + " piece(s) of " + product + " is not available for peer " + buyerID + ".";
    }
}
