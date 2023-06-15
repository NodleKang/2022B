package test;

import java.util.Iterator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue<V> {

    // 메시지를 저장하는 큐 (메시지가 입력된 순서 보장)
    private LinkedBlockingQueue<V> queue;
    // 메시지 아이디와 메시지를 매핑하는 맵
    private ConcurrentHashMap<String, V> idMessageMap;
    // 메시지와 메시지 아이디를 매핑하는 맵
    private ConcurrentHashMap<V, String> messageIdMap;
    // 메시지와 메시지 상태를 매핑하는 맵
    private ConcurrentHashMap<V, MessageStatus> messageStatusMap;
    // 메시지와 메시지 타임아웃을 매핑하는 맵
    private ConcurrentHashMap<V, Long> messageTimeoutMap;
    // 메시지와 메시지 시각을 매핑하는 맵
    private ConcurrentHashMap<V, Long> messageTimestampMap;
    // 메시지와 메시지 실패 횟수를 매핑하는 맵
    private ConcurrentHashMap<V, Integer> messageFailedCountMap;

    // 큐의 용량
    private int capacity = 0;
    // 메시지 실패 횟수의 최대값
    private int MAX_MESSAGE_FAILED_COUNT = 3;

    // 메시지 상태를 나타내는 열거형
    private enum MessageStatus {
        PENDING, IN_PROGRESS, FAILED
    }

    // 생성자(용량을 지정하는 생성자)
    public MessageQueue(int capacity) {
        this.capacity = capacity;
        initialize();
    }

    // 생성자(용량을 지정하지 않는 생성자)
    public MessageQueue() {
        initialize();
    }

    // 맵을 초기화하는 메서드
    private synchronized void initialize() {
        this.queue = new LinkedBlockingQueue<>();
        this.idMessageMap = new ConcurrentHashMap<>();
        this.messageIdMap = new ConcurrentHashMap<>();
        this.messageStatusMap = new ConcurrentHashMap<>();
        this.messageTimeoutMap = new ConcurrentHashMap<>();
        this.messageTimestampMap = new ConcurrentHashMap<>();
        this.messageFailedCountMap = new ConcurrentHashMap<>();
    }

    // 큐의 용량을 설정하는 메서드
    public synchronized void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // 큐의 용량을 반환하는 메서드
    public synchronized int getCapacity() {
        return this.capacity;
    }

    // 큐의 크기를 반환하는 메서드
    public synchronized int size() {
        return queue.size();
    }

    // 메시지를 큐에 추가하는 메서드
    public void addMessage(V message) {

        // 큐의 용량이 0보다 크고 큐의 크기가 용량보다 크거나 같으면 예외 발생
        if (capacity > 0 && queue.size() >= capacity) {
            throw new IllegalStateException("Queue is full");
        }
        // 큐에 메시지를 추가하고 메시지와 메시지 아이디를 매핑하는 맵에 메시지와 메시지 아이디를 추가
        if (queue.offer(message)) {
            String id = UUID.randomUUID().toString().substring(0, 8);
            idMessageMap.put(id, message);
            messageIdMap.put(message, id);
            messageStatusMap.put(message, MessageStatus.PENDING);
            messageTimeoutMap.put(message, 0L);
            messageTimestampMap.put(message, 0L);
            messageFailedCountMap.put(message, 0);
        }
    }

    // 큐에서 메시지를 가져오는 메서드
    public synchronized V getMessage() {

        // 메시지 가져오기 로직을 동기화 블록으로 감싸기
        synchronized (this) {

            Iterator<V> iterator = queue.iterator();
            while (iterator.hasNext()) {

                V message = iterator.next();
                MessageStatus messageStatus = messageStatusMap.get(message);

                if (messageStatus == MessageStatus.PENDING) {
                    messageStatusMap.put(message, MessageStatus.IN_PROGRESS);
                    messageTimeoutMap.put(message, 0L);
                    messageTimestampMap.put(message, System.currentTimeMillis());
                    return message;
                } else if (messageStatus == MessageStatus.IN_PROGRESS) {
                    isMessageTimeout(message);
                } else if (messageStatus == MessageStatus.FAILED) {
                    if (messageFailedCountMap.get(message) < MAX_MESSAGE_FAILED_COUNT) {
                        messageStatusMap.put(message, MessageStatus.IN_PROGRESS);
                        messageTimeoutMap.put(message, 0L);
                        messageTimestampMap.put(message, System.currentTimeMillis());
                        return message;
                    } else {
                        removeMessage(message);
                    }
                }
            }

        }

        // 큐에 메시지가 없으면 null 반환
        return null;
    }

    public synchronized void restoreMessageById(String id) {
        V message = getMessageById(id);
        if (message != null) {
            messageStatusMap.put(message, MessageStatus.PENDING);
            messageTimeoutMap.put(message, 0L);
            messageTimestampMap.put(message, 0L);
        }
    }

    // 메시지를 기반으로 메시지 아이디를 반환하는 메서드
    public String getMessageId(V message) {
        return messageIdMap.get(message);
    }

    // 메시지 아이디를 기반으로 메시지를 큐에서 가져오는 메서드
    public V getMessageById(String id) {
        return idMessageMap.get(id);
    }

    // 메시지 성공시 메시지를 큐에서 제거하는 메서드
    public void handleMessageSuccess(V message) {
        removeMessage(message);
    }

    // 메시지 성공시 메시지 아이디를 기반으로 메시지를 큐에서 제거하는 메서드
    public void handleMessageSuccessById(String id) {
        V message = getMessageById(id);
        if (message != null) {
            removeMessage(message);
        }
    }

    // 메시지 실패 처리 메서드
    // 메시지 실패 횟수를 가져오고 메시지 실패 횟수가 최대값보다 작으면 메시지 상태를 PENDING으로 변경하고 메시지 실패 횟수를 1 증가시킨다.
    public synchronized void handleMessageFailure(V message) {
        int messageFailedCount = messageFailedCountMap.getOrDefault(message, 0);
        if (messageFailedCount < MAX_MESSAGE_FAILED_COUNT) {
            messageStatusMap.put(message, MessageStatus.PENDING);
            messageFailedCountMap.put(message, messageFailedCount + 1);
        }
    }

    // 메시지 실패시 메시지 아이디를 기반으로 실패 처리 메서드
    public synchronized void handleMessageFailureById(String id) {
        V message = getMessageById(id);
        if (message != null) {
            handleMessageFailure(message);
        }
    }

    // 메시지 상태가 PENDING인지 확인하는 메서드
    public synchronized boolean isMessagePending(V message) {
        return messageStatusMap.getOrDefault(message, MessageStatus.PENDING) == MessageStatus.PENDING;
    }

    // 메시지 상태가 IN_PROGRESS인지 확인하는 메서드
    public synchronized boolean isMessageInProgress(V message) {
        return messageStatusMap.getOrDefault(message, MessageStatus.IN_PROGRESS) == MessageStatus.IN_PROGRESS;
    }

    // 메시지 상태가 FAILED인지 확인하는 메서드
    public synchronized boolean isMessageFailed(V message) {
        return messageStatusMap.getOrDefault(message, MessageStatus.FAILED) == MessageStatus.FAILED;
    }

    // 메시지 타임아웃이 지났는지 확인하고 메시지 실패 처리하는 메서드
    public synchronized boolean isMessageTimeout(V message) {
        long messageTimeout = messageTimeoutMap.getOrDefault(message, 0L);
        long messageTimestamp = messageTimestampMap.getOrDefault(message, 0L);
        if (messageTimestamp > 0L
                && messageTimeout > 0L
                && System.currentTimeMillis() - messageTimestamp > messageTimeout) {
            handleMessageFailure(message);
            return true;
        }
        return false;
    }

    // 메시지 실패 횟수가 최대값을 초과했는지 확인하는 메서드
    public synchronized boolean isMessageFailedCountExceeded(V message) {
        int failedCount = messageFailedCountMap.getOrDefault(message, 0);
        return failedCount >= MAX_MESSAGE_FAILED_COUNT;
    }

    // 메시지를 큐에서 제거하는 메서드
    public synchronized void removeMessage(V message) {
        queue.remove(message);
        String id = messageIdMap.get(message);
        idMessageMap.remove(id);
        messageIdMap.remove(message);
        messageStatusMap.remove(message);
        messageTimeoutMap.remove(message);
        messageTimestampMap.remove(message);
        messageFailedCountMap.remove(message);
    }

    // 메시지 아이디를 기반으로 메시지를 큐에서 제거하는 메서드
    public synchronized void removeMessageById(String id) {
        V message = idMessageMap.get(id);
        if (message != null) {
            removeMessage(message);
        }
    }

    // 메시지 실패 횟수를 기반으로 메시지를 큐에서 제거하는 메서드
    public synchronized void removeMessageByFailedCount(int count) {
        Iterator<V> iterator = queue.iterator();
        while (iterator.hasNext()) {
            V message = iterator.next();
            int failedCount = messageFailedCountMap.getOrDefault(message, 0);
            if (failedCount > count) {
                iterator.remove();
                removeMessage(message);
            }
        }
    }

}
