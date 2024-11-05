package product;

import java.util.List;
import java.util.Random;

public enum Product {
    FISH(2),
    SALT(1),
    BOARS(3);

    private final int price;

    Product(int price) {
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    private static final List<Product> VALUES = List.of(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static Product pickRandomProduct() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
