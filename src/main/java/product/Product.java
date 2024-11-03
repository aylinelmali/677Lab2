package product;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Product {
    FISH,
    SALT,
    BOARS;

    private static final List<Product> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static Product pickRandomProduct() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
