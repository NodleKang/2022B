package provide.gson;

public class InputQueueData {
    long timestamp;

    String value;

    public InputQueueData(long timestamp, String value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getValue() {
        return this.value;
    }
}
