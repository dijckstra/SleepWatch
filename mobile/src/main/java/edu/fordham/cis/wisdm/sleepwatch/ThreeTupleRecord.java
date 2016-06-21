package edu.fordham.cis.wisdm.sleepwatch;

import java.io.Serializable;

/**
 * Class to hold SensorEvent data. Created because references to SensorEvent instances can be lost.
 */
public class ThreeTupleRecord implements Serializable {
    private long timestamp;
    private float x;
    private float y;
    private float z;

    private static final long serialVersionUID = 1L;

    public ThreeTupleRecord(long timestamp, float x, float y, float z) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    // Format to write files
    @Override
    public String toString() {
        return timestamp + "," + x + "," + y + "," + z;
    }
}
