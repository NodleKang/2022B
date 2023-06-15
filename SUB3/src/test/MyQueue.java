package test;

import java.util.Iterator;

public class MyQueue<V> {

    private java.util.LinkedList<MyQueueElement<V>> queue;
    private String name;
    private int capacity;

    public MyQueue(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        this.queue = new java.util.LinkedList<>();
    }

    public synchronized V get() {
        while (queue.size() <= 0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        MyQueueElement<V> element = queue.getFirst();
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - element.getTimestamp();

        if (elapsedTime >= element.getTimeout()) {
            queue.removeFirst();
            return null;
        }

        return queue.removeFirst().getElement();
    }

    public synchronized V getNoWait() {

        if (queue.size() > 0) {

            Iterator<MyQueueElement<V>> iterator = queue.iterator();
            while (iterator.hasNext()) {
                MyQueueElement<V> element = iterator.next();
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - element.getTimestamp();

                if (elapsedTime >= element.getTimeout()) {
                    iterator.remove(); // 유효하지 않은 요소는 큐에서 제거
                } else {
                    return element.getElement();
                }
            }
        }

        return null;
    }

    public synchronized V peek() {
        if (queue.size() <= 0) {
            return null;
        }
        MyQueueElement<V> element = queue.getFirst();
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - element.getTimestamp();

        if (elapsedTime >= element.getTimeout()) {
            queue.removeFirst();
            return null;
        }

        return element.getElement();
    }

    public synchronized boolean put(V element) {
        if (capacity <= 0 || queue.size() < capacity) {
            queue.add(new MyQueueElement<>(element));
            notifyAll();
            return true;
        } else {
            notify();
            return false;
        }
    }

    public synchronized boolean put(String id, V element, long timeout) {
        if (capacity <= 0 || queue.size() < capacity) {
            queue.add(new MyQueueElement<>(id, element, 0));
            notifyAll();
            return true;
        } else {
            notify();
            return false;
        }
    }

    public synchronized MyQueueElement<V> findElementById(String id) {
        for (MyQueueElement<V> element : queue) {
            if (element.getId().equals(id)) {
                return element;
            }
        }
        return null;
    }

    public synchronized void removeElementById(String id) {
        MyQueueElement<V> element = findElementById(id);
        if (element != null) {
            queue.remove(element);
        }
    }

    public synchronized void clear() {
        queue.clear();
    }

    public int size() {
        return queue.size();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int size) {
        this.capacity = size;
    }

}
