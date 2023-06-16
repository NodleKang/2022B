package test;

import java.util.ArrayList;
import java.util.List;

public class Worker {
    private int queueNo;

    private List<String> store;

    public Worker(int queueNo) {
        this.queueNo = queueNo;
        this.store = new ArrayList<>();
    }

    public String run(String value) {
        String result = null;
        try {
            String valueTopic = value.split("_")[0];
            String valueValue = value.split("_")[1];
            for (String data : this.store) {
                String storeTopic = data.split("_")[0];
                String storeValue = data.split("_")[1];
                if ("CLICK".equals(valueTopic) &&
                        "VIEW".equals(storeTopic) &&
                        valueValue.equals(storeValue))
                    result = String.format("Worker(%d):Matched %s", new Object[] { Integer.valueOf(this.queueNo), storeValue });
            }
        } catch (Exception e) {
            result = null;
        }
        addToStore(value);
        return result;
    }

    private void addToStore(String value) {
        this.store.add(value);
    }
}
