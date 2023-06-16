package provide.gson;

public class WorkerCall {
    long processId;

    long threadId;

    int queueNo;

    long timestamp;

    String value;

    public WorkerCall(long processId, long threadId, int queueNo, long timestamp, String value) {
        this.processId = processId;
        this.threadId = threadId;
        this.queueNo = queueNo;
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getProcessId() {
        return this.processId;
    }

    public long getThreadId() {
        return this.threadId;
    }

    public int getQueueNo() {
        return this.queueNo;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getValue() {
        return this.value;
    }
}
