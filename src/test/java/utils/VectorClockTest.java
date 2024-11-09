package utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VectorClockTest {

    private static final int[] BASELINE_TIMESTAMP = {0, 0, 2};
    private static final int[] CONCURRENT_TIMESTAMP = {0, 2, 1};
    private static final int[] HIGHER_TIMESTAMP = {0,0,3};
    private static final int[] LOWER_TIMESTAMP = {0, 0, 1};

    @Test
    public void testConcurrent() {
        Assertions.assertTrue(VectorClock.isConcurrent(BASELINE_TIMESTAMP, CONCURRENT_TIMESTAMP));
        Assertions.assertFalse(VectorClock.isConcurrent(BASELINE_TIMESTAMP, HIGHER_TIMESTAMP));
        Assertions.assertFalse(VectorClock.isConcurrent(BASELINE_TIMESTAMP, LOWER_TIMESTAMP));
    }

    @Test
    public void testSmallerThan() {
        Assertions.assertTrue(VectorClock.isSmallerThan(BASELINE_TIMESTAMP, HIGHER_TIMESTAMP));
        Assertions.assertTrue(VectorClock.isSmallerThan(LOWER_TIMESTAMP, BASELINE_TIMESTAMP));
        Assertions.assertFalse(VectorClock.isSmallerThan(BASELINE_TIMESTAMP, CONCURRENT_TIMESTAMP));
        Assertions.assertFalse(VectorClock.isSmallerThan(BASELINE_TIMESTAMP, BASELINE_TIMESTAMP));
    }

    @Test
    public void testSmallerOrEqualThan() {
        Assertions.assertTrue(VectorClock.isSmallerOrEqualThan(BASELINE_TIMESTAMP, HIGHER_TIMESTAMP));
        Assertions.assertTrue(VectorClock.isSmallerOrEqualThan(LOWER_TIMESTAMP, BASELINE_TIMESTAMP));
        Assertions.assertFalse(VectorClock.isSmallerOrEqualThan(BASELINE_TIMESTAMP, CONCURRENT_TIMESTAMP));
        Assertions.assertTrue(VectorClock.isSmallerOrEqualThan(BASELINE_TIMESTAMP, BASELINE_TIMESTAMP));
    }

    @Test
    public void testMerge() {
        int[] merged1 = VectorClock.merge(BASELINE_TIMESTAMP, BASELINE_TIMESTAMP);
        int[] merged2 = VectorClock.merge(BASELINE_TIMESTAMP, CONCURRENT_TIMESTAMP);
        int[] merged3 = VectorClock.merge(BASELINE_TIMESTAMP, HIGHER_TIMESTAMP);
        int[] merged4 = VectorClock.merge(BASELINE_TIMESTAMP, LOWER_TIMESTAMP);
        Assertions.assertArrayEquals(new int[] {0, 0, 2}, merged1);
        Assertions.assertArrayEquals(new int[] {0, 2, 2}, merged2);
        Assertions.assertArrayEquals(new int[] {0, 0, 3}, merged3);
        Assertions.assertArrayEquals(new int[] {0, 0, 2}, merged4);
    }
}
