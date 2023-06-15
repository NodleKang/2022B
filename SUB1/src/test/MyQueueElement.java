package test;

public class MyQueueElement<V> {
    private String id;
    private boolean isRemoved;
    private V element;
    private long timeout;
    private long timestamp;
    private int failedCount;

    public MyQueueElement(String id, V element, long timeout) {
        this.id = id;
        this.isRemoved = false;
        this.element = element;
        this.timeout = timeout;
        this.timestamp = System.currentTimeMillis();
        this.failedCount = 0;
    }

    public MyQueueElement(V element) {
        this.id = generateId();
        this.isRemoved = false;
        this.element = element;
        this.timeout = 0L;
        this.timestamp = System.currentTimeMillis();
        this.failedCount = 0;
    }

    public void setRemoved() {
        this.isRemoved = true;
    }

    public void resetRemoved() {
        this.isRemoved = false;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setElement(V element) {
        this.element = element;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
        this.timestamp = System.currentTimeMillis();
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public String getId() {
        return id;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public V getElement() {
        return element;
    }

    public long getTimeout() {
        return timeout;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void increaseFailedCount() {
        this.failedCount++;
    }

    private String generateId() {
        return String.valueOf(System.currentTimeMillis());
    }
}
