package utils;

import java.util.Arrays;

public class VectorClock {

    // Compare two vector clocks
    public static boolean isConcurrent(int[] vc1, int[] vc2) {
        return !isSmallerThan(vc1, vc2) && !isSmallerThan(vc2, vc1);
    }

    public static boolean isSmallerOrEqualThan(int[] vc1, int[] vc2) {
        boolean smallerOrEqual = true;

        for (int i = 0; i < vc1.length; i++) {
            if (vc1[i] > vc2[i]) {
                smallerOrEqual = false;
                break;
            }
        }
        return smallerOrEqual;
    }

    public static boolean isSmallerThan(int[] vc1, int[] vc2) {
        if (Arrays.equals(vc1, vc2)) return false;
        return isSmallerOrEqualThan(vc1, vc2);
    }

    /**
     * Merges second timestamp with fist timestamp.
     * @param vc1 First vector clock timestamp.
     * @param vc2 Second vector clock timestamp.
     */
    public static int[] merge(int[] vc1, int[] vc2) {
        int[] merged = new int[vc1.length];
        for (int i = 0; i < vc1.length; i++) {
            merged[i] = Math.max(vc1[i], vc2[i]);
        }
        return merged;
    }
}

