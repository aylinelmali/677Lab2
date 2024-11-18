package utils;

import product.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains all the messages for the logging.
 */
public class Messages {

    private static List<Double> meanDeltas = new ArrayList<>();

    public static String getElectionDoneMessage(int coordinatorID) {
        return "Election done. New coordinator is peer " + coordinatorID + ".";
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

    public static String getPeerCouldNotConnectMessage(int peerID, int coordinatorID) {
        return "Peer " + peerID + " could not connect with coordinator peer " + coordinatorID + ". Starting election.";
    }

    public static String getDiscardingLookupMessage(int peerID) {
        return "Peer " + peerID + " is new coordinator. Discarding lookup.";
    }

    public static String getDiscardingBuyMessage(int peerID) {
        return "Peer " + peerID + " is new coordinator. Discarding buy.";
    }

    public static String getStatisticsMessage(int peerID, List<Long> deltas) {
        double delta = deltas.stream().mapToLong(Long::longValue).average().orElse(0);
        return "Peer " + peerID + " has an average response time of " + delta + ". Deltas: " + deltas;
    }
}
