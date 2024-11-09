package utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.Product;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class TraderStateTest {

    @BeforeEach
    public void setUp() {
        try {
            BufferedWriter writer = Files.newBufferedWriter(TraderState.FILE_PATH, StandardCharsets.UTF_8);
            writer.write("");
            writer.close();
        } catch (IOException ignored) {}
    }

    @Test
    public void testTraderState() {
        TraderState traderState = TraderState.readTraderState();
        traderState.putIntoStock(Product.FISH, 3, 0);
        traderState.putIntoStock(Product.FISH, 3, 1);
        traderState.putIntoStock(Product.BOARS, 3, 2);
        traderState.putIntoStock(Product.BOARS, 3, 3);
        TraderState.writeTraderState(traderState);

        traderState = TraderState.readTraderState();

        Assertions.assertTrue(traderState.productAvailable(Product.FISH, 6));
        Assertions.assertTrue(traderState.productAvailable(Product.BOARS, 6));
        Assertions.assertFalse(traderState.productAvailable(Product.FISH, 7));
        Assertions.assertFalse(traderState.productAvailable(Product.BOARS, 7));

        Assertions.assertEquals(List.of(0,0), traderState.takeOutOfStock(Product.FISH, 2));
        Assertions.assertEquals(List.of(0,1), traderState.takeOutOfStock(Product.FISH, 2));
        Assertions.assertEquals(List.of(1), traderState.takeOutOfStock(Product.FISH, 1));
        Assertions.assertEquals(List.of(), traderState.takeOutOfStock(Product.FISH, 2));
        Assertions.assertEquals(List.of(1), traderState.takeOutOfStock(Product.FISH, 1));

        Assertions.assertFalse(traderState.productAvailable(Product.FISH, 1));
        Assertions.assertTrue(traderState.productAvailable(Product.BOARS, 6));
    }
}
