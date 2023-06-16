package provide.gson;

import java.util.List;

public class Configuration {
    int processCount;

    int threadCount;

    int outputQueueBatchSize;

    int inputQueueCount;

    List<String> inputQueueURIs;

    String outputQueueURI;

    public int getProcessCount() {
        return this.processCount;
    }

    public int getThreadCount() {
        return this.threadCount;
    }

    public int getOutputQueueBatchSize() {
        return this.outputQueueBatchSize;
    }

    public int getInputQueueCount() {
        return this.inputQueueCount;
    }

    public List<String> getInputQueueURIs() {
        return this.inputQueueURIs;
    }

    public String getOutputQueueURI() {
        return this.outputQueueURI;
    }

    public String toString() {
        return "Configuration [inputQueueCount=" + this.inputQueueCount + ", inputQueueURIs=" + this.inputQueueURIs +
                ", outputQueueURI=" + this.outputQueueURI + "]";
    }
}
