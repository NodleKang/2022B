import java.util.*;

public class Study01 {

    public static void main(String[] args) {

        System.out.println("hello : " + Thread.currentThread().getName());

        // 쓰레드 구현 방법 1 - Thread 클래스를 상속받은 클래스를 이용한 쓰레드 구현 방법
        HelloThread helloThread = new HelloThread();
        helloThread.start();

        // 쓰레드 구현 방법 2 - Runnable 인터페이스를 구현한 클래스를 이용한 쓰레드 구현 방법
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("world sub2: " + Thread.currentThread().getName());
            }
        });
        thread.start();

        // 쓰레드 구현 방법 3 - 람다식을 이용한 쓰레드 구현 방법
        new Thread(() -> {
            System.out.println("world sub3: " + Thread.currentThread().getName());
        }).start();

    }
}

// Thread 클래스를 상속받아서 run 메소드를 오버라이딩
class HelloThread extends Thread {
    @Override
    public void run() {
        System.out.println("world sub1: " + Thread.currentThread().getName());
    }
}
