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
        boolean greater = false, less = false;
        for (int i = 0; i < vc1.length; i++) {
            if (vc1[i] > vc2[i]) greater = true;
            if (vc1[i] < vc2[i]) less = true;
        }
        return greater && less; // Concurrent if neither is fully ahead of the other
    }
}

