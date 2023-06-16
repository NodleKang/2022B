package test;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWorker {
    private int queueNo;

    private List<String> store;

    public AbstractWorker(int queueNo) {
        this.queueNo = queueNo;
        this.store = new ArrayList<>();
    }

    public String run(long timestamp, String value) {
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
        } catch (Exception e) {
            result = null;
        }
        addToStore(timestamp, value);
        return result;
    }

    private void addToStore(long timestamp, String value) {
        this.store.add(String.valueOf(timestamp) + "#" + value);
    }

    protected abstract void removeExpiredStoreItems(long paramLong, List<String> paramList);
}
