import java.util.*;

public class Study01 {

    public static void main(String[] args) {

        System.out.println("hello : " + Thread.currentThread().getName());

        // ������ ���� ��� 1 - Thread Ŭ������ ��ӹ��� Ŭ������ �̿��� ������ ���� ���
        HelloThread helloThread = new HelloThread();
        helloThread.start();

        // ������ ���� ��� 2 - Runnable �������̽��� ������ Ŭ������ �̿��� ������ ���� ���
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("world sub2: " + Thread.currentThread().getName());
            }
        });
        thread.start();

        // ������ ���� ��� 3 - ���ٽ��� �̿��� ������ ���� ���
        new Thread(() -> {
            System.out.println("world sub3: " + Thread.currentThread().getName());
        }).start();

    }
}

// Thread Ŭ������ ��ӹ޾Ƽ� run �޼ҵ带 �������̵�
class HelloThread extends Thread {
    @Override
    public void run() {
        System.out.println("world sub1: " + Thread.currentThread().getName());
    }
}
