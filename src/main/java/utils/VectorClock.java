package utils;

import java.util.Arrays;

public class VectorClock {
    private int[] timestampVector;

    public VectorClock(int numPeers) {
        this.timestampVector = new int[numPeers];
        Arrays.fill(this.timestampVector, 0);
    }

    // Increment the local peer's clock
    public void increment(int peerIndex) {
        timestampVector[peerIndex]++;
    }

    // Update the vector clock when a message is received
    public void update(int[] receivedTimestamp, int peerIndex) {
        // Update own clock to max of each element
        for (int i = 0; i < timestampVector.length; i++) {
            timestampVector[i] = Math.max(timestampVector[i], receivedTimestamp[i]);
        }
        timestampVector[peerIndex]++; // Increment local clock after update
    }

    // Get the vector clock
    public int[] getTimestampVector() {
        return this.timestampVector;
    }

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
}

