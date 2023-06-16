package test;

import com.google.gson.Gson;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWorker {
    private static final String WORKER_CALL_URI = "http://127.0.0.1:9999/workerCall";

    private Gson gson = new Gson();

    private int queueNo;

    private List<String> store;

    public AbstractWorker(int queueNo) {
        this.queueNo = queueNo;
        this.store = new ArrayList<>();
    }

    public AbstractWorker(int queueNo, List<String> store) {
        this.queueNo = queueNo;
        this.store = new ArrayList<>(store);
    }

    public String run(long timestamp, String value) {
        writeLog(timestamp, value);
        String result = null;
        try {
            removeExpiredStoreItems(timestamp, this.store);
            String valueTopic = value.split("_")[0];
            String valueValue = value.split("_")[1];
            for (String data : this.store) {
                String storeTopic = data.split("#")[1].split("_")[0];
                String storeValue = data.split("#")[1].split("_")[1];
                if ("CLICK".equals(valueTopic) &&
                        "VIEW".equals(storeTopic) &&
                        valueValue.equals(storeValue))
                    result = String.format("Worker(%d):Matched %s", new Object[] { Integer.valueOf(this.queueNo), storeValue });
            }
            addToStore(timestamp, value);
        } catch (Exception e) {
            result = null;
            addToStore(timestamp, value);
        }
        return result;
    }

    private void addToStore(long timestamp, String value) {
        this.store.add(String.valueOf(timestamp) + "#" + value);
    }

    protected abstract void removeExpiredStoreItems(long paramLong, List<String> paramList);

    private void writeLog(long timestamp, String value) {
        try {
            request("http://127.0.0.1:9999/workerCall", HttpMethod.POST, this.gson.toJson(new WorkerCall(getPid(), Thread.currentThread().getId(), this.queueNo, timestamp, value)));
        } catch (Exception exception) {}
    }

    private String request(String urlString, HttpMethod method, String requestBody) throws Exception {
        String body = null;
        HttpClient httpClient = new HttpClient();
        httpClient.start();
        if (method == HttpMethod.GET) {
            body = httpClient.newRequest(urlString).method(method).send().getContentAsString();
        } else {
            httpClient.newRequest(urlString).method(method).content((ContentProvider)new StringContentProvider(requestBody)).send();
        }
        httpClient.stop();
        return body;
    }

    private class WorkerCall {
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
    }

    private long getPid() {
        return Long.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]).longValue();
    }

    public List<String> getStore() {
        return this.store;
    }
}
