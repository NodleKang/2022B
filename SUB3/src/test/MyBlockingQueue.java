package test;

import java.util.concurrent.LinkedBlockingQueue;

// LinkedBlockingQueue를 사용하여 메시지를 저장하는 큐를 구현합니다.
// LinkedBlockingQueue는 내부적으로 ReentrantLock을 사용하여 스레드 안전성을 보장합니다.

// FIFO(First-In-First-Out) 순서를 보장합니다.
// 메시지의 순서를 유지해야 하는 멀티스레드 환경에서는 LinkedBlockingQueue를 사용하는 것이 가장 적합합니다.

// LinkedBlockingQueue 사용이 이로울 때
// 요소를 추가하려는 스레드가 큐가 가득 찬 경우
// 요소를 가져오려는 스레드가 큐가 비어 있는 경우
// 기다려야 하는 블로킹 동작이 필요한 경우 LinkedBlockingQueue를 선택하는 것이 적절합니다.
// 이는 생산자와 소비자가 효율적으로 동기화되고 작업을 조정하는 데 도움이 됩니다.
public class MyBlockingQueue {
    private LinkedBlockingQueue<Object> queue;

    // LinkedBlockingQueue의 생성자는 capacity를 인자로 받습니다.
    public MyBlockingQueue(int capacity) {
        queue = new LinkedBlockingQueue<>(capacity);
    }

    // capacity를 지정하지 않으면 Integer.MAX_VALUE로 지정됩니다.
    public MyBlockingQueue() {
        queue = new LinkedBlockingQueue<>();
    }

    // capacity(큐가 저장할수 있는 최대 요소 수)를 변경하고 싶을 때 사용합니다.
    public void setCapacity(int capacity) {
        queue = new LinkedBlockingQueue<>(capacity);
    }

    // capacity(큐가 저장할수 있는 최대 요소 수)를 확인하고 싶을 때 사용합니다.
    public int getCapacity() {
        return queue.remainingCapacity();
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
    // 큐에서 가장 오래된 요소를 가져오고, 해당 요소는 큐에서 제거됩니다.
    // 큐가 비어있으면 null을 반환합니다.
    public Object getMessage() {
        // return queue.take(); // 큐가 비어있으면 스레드가 블록됩니다.
        return queue.poll();
    }

    // 큐에서 메시지를 확인합니다.
    // 큐에서 가장 오래된 요소를 가져오지만, 큐에는 여전히 남아 있습니다.
    public Object checkMessage() {
        return queue.peek();
    }

    // 큐가 비어있는지 확인합니다.
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
