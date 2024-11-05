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

public class TraderState {

    private Map<Product, Integer> products;
    private Map<Product, List<Integer>> sellerQueues;

    private TraderState(Map<Product, Integer> products, Map<Product, List<Integer>> sellerQueues) {
        this.products = products;
        this.sellerQueues = sellerQueues;
    }

    public boolean productAvailable(Product product, int amount) {
        Integer stock = this.products.get(product);
        return stock != null && stock >= amount;
    }

    public List<Integer> takeOutOfStock(Product product, int amount) {
        if (!productAvailable(product, amount)) {
            return List.of();
        }
        // TODO: Implement this
    }

    public Map<Product, Integer> getProducts() {
        return this.products;
    }

    public Map<Product, List<Integer>> getSellerQueues() {
        return this.sellerQueues;
    }

    public static final Path FILE_PATH = Paths.get("trader_state.txt");

    public static synchronized void writeTraderState(TraderState traderState) {
        StringBuilder sb = new StringBuilder();
        for (Product product : traderState.products.keySet()) {
            sb
                    .append(product.toString())
                    .append(":")
                    .append(traderState.products.get(product))
                    .append(":")
                    .append(listToString(traderState.sellerQueues.get(product)))
                    .append("\n");
        }
        createFile(sb.toString());
    }

    private static String listToString(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (int j : list) {
            sb.append(j).append(",");
        }
        return sb.toString();
    }

    public static synchronized TraderState readTraderState() {
        String text = readFile();

        Map<Product, Integer> products = new HashMap<>();
        Map<Product, List<Integer>> sellerQueues = new HashMap<>();
        if (text == null) {
            for (Product product : Product.values()) {
                products.put(product, 0);
                sellerQueues.put(product, new ArrayList<>());
            }
        } else {
            String[] lines = text.split("\n");
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length != 3) {
                    continue;
                }
                Product product = Product.valueOf(parts[0]);
                int amount = Integer.parseInt(parts[1]);
                String[] sellers = parts[2].split(",");
                List<Integer> sellerList = new ArrayList<>();
                for (String seller : sellers) {
                    sellerList.add(Integer.parseInt(seller));
                }
                products.put(product, amount);
                sellerQueues.put(product, sellerList);
            }
        }

        return new TraderState(products, sellerQueues);
    }

    private static void createFile(String content) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(TraderState.FILE_PATH, StandardCharsets.UTF_8);
            writer.write(content);
            writer.close();
        } catch (IOException ignored) {}
    }

    private static String readFile() {

        String text = "";

        try {
            BufferedReader reader = Files.newBufferedReader(TraderState.FILE_PATH, StandardCharsets.UTF_8);

            for (String line = reader.readLine(); line != null; line = reader.readLine())
                text += line + "\n";

            reader.close();
        } catch (IOException e) {
            return null;
        }

        return text;
    }
}
