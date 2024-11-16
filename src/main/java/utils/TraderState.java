package utils;

import product.Product;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains all the logic for managing the items offered by the trader,
 */
public class TraderState {

    // Allows trader to keep track of sellers and deposited items
    private final Map<Product, List<Integer>> sellerQueues;

    private TraderState(Map<Product, List<Integer>> sellerQueues) {
        this.sellerQueues = sellerQueues;
    }

    /**
     * Checks if a specified amount of product is available.
     * @param product Product to check.
     * @param amount Amount to check
     * @return Product available or not.
     */
    public boolean productAvailable(Product product, int amount) {
        List<Integer> stock = this.sellerQueues.get(product);
        return stock != null && stock.size() >= amount;
    }

    /**
     * Removes specified amount of product from stock and returns list of seller IDs corresponding to these units.
     * @param product Product to take out.
     * @param amount Amount to take out.
     * @return List of sellers to pay.
     */
    public List<Integer> takeOutOfStock(Product product, int amount) {
        if (!productAvailable(product, amount)) {
            return List.of();
        }

        List<Integer> queue = sellerQueues.get(product);
        if (queue == null) {
            return List.of();
        }

        List<Integer> first = queue.subList(0, amount); // sellers to pay
        List<Integer> last = queue.subList(amount, queue.size()); // remaining sellers

        sellerQueues.put(product, last);

        return first;
    }

    /**
     * Adds specified amount of product to queue for a given seller.
     * @param product Product to add.
     * @param amount Amount to add.
     * @param sellerID ID of the seller.
     */
    public void putIntoStock(Product product, int amount, int sellerID) {
        List<Integer> queue = sellerQueues.computeIfAbsent(product, k -> new ArrayList<>());

        for (int i = 0; i < amount; i++) {
            queue.add(sellerID);
        }
    }

    // static functions

    public static final Path FILE_PATH = Paths.get("trader_state.txt");

    // Writes current TraderState to text file
    public static synchronized void writeTraderState(TraderState traderState) {
        StringBuilder sb = new StringBuilder();
        for (Product product : traderState.sellerQueues.keySet()) {
            sb
                    .append(product.toString())
                    .append(":")
                    .append(listToString(traderState.sellerQueues.get(product)))
                    .append("\n");
        }
        createFile(sb.toString());
    }

    /**
     * Reads saved state from text file.
     * @return the trader state read from file.
     */
    public static synchronized TraderState readTraderState() {
        String text = readFile();

        Map<Product, List<Integer>> sellerQueues = new HashMap<>();
        if (text == null) { // file contains no data: create new trader state and return it.
            for (Product product : Product.values()) {
                sellerQueues.put(product, new ArrayList<>());
            }
        } else { // file contains data: parse data and return it.
            String[] lines = text.split("\n");
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length != 2) {
                    continue;
                }
                Product product = Product.valueOf(parts[0]);
                String[] sellers = parts[1].split(",");
                List<Integer> sellerList = new ArrayList<>();
                for (String seller : sellers) {
                    sellerList.add(Integer.parseInt(seller));
                }
                sellerQueues.put(product, sellerList);
            }
        }

        return new TraderState(sellerQueues);
    }

    /**
     * Clears contents of text file to reset the marketplace state.
     */
    public static void resetTraderState() {
        try {
            BufferedWriter writer = Files.newBufferedWriter(TraderState.FILE_PATH, StandardCharsets.UTF_8);
            writer.write("");
            writer.close();
        } catch (IOException ignored) {}
    }

    /**
     * Converts list of seller IDs to a comma separated string for writing.
     * @param list List to convert.
     * @return String representation of list.
     */
    private static String listToString(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (int j : list) {
            sb.append(j).append(",");
        }
        return sb.toString();
    }

    /**
     * Writes a string to text file to save trader's current state
     * @param content Content to write to file.
     */
    private static void createFile(String content) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(TraderState.FILE_PATH, StandardCharsets.UTF_8);
            writer.write(content);
            writer.close();
        } catch (IOException ignored) {}
    }

    /**
     * Reads the contexts of text file into string.
     * @return Content of the file.
     */
    private static String readFile() {

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader reader = Files.newBufferedReader(TraderState.FILE_PATH, StandardCharsets.UTF_8);

            for (String line = reader.readLine(); line != null; line = reader.readLine())
                text.append(line).append("\n");

            reader.close();
        } catch (IOException e) {
            return null;
        }

        return text.toString();
    }
}
