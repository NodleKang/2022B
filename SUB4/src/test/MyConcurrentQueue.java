package test;

import java.util.concurrent.ConcurrentLinkedQueue;

// ConcurrentLinkedQueue는 메시지의 순서를 보장하지 않습니다.
public class MyConcurrentQueue {

    private ConcurrentLinkedQueue<Object> queue;

    // ConcurrentLinkedQueue의 생성자는 capacity를 인자로 받지 않습니다.
    public MyConcurrentQueue() {
        queue = new ConcurrentLinkedQueue<>();
    }

    // size(현재 큐에 포함된 요소 수)를 확인하고 싶을 때 사용합니다.
    public int getSize() {
        return queue.size();
    }

    // 큐에 메시지를 추가합니다.
    public void addMessage(Object message) {
        queue.add(message);
    }

    // 큐에서 메시지를 가져옵니다.
    public Object getNextMessage() {
        return queue.poll();
    }

    // 큐가 비어있는지 확인합니다.
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
