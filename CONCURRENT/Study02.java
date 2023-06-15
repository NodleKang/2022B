import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Study02 {

    /*
     * Thread 주요 기능
     *
     * sleep : 현재 스레드 멈춰두기
     * - 다른 스레드가 일할 수 있게 기회를 주지만 Lock을 놔주진 않는다. (데드락 조심!)
     * interrupt : 다른 스레드 깨우기
     * - 다른 스레드를 깨워서 InterruptedException을 발생시킨다. InterruptedException이 발생했을 떄 할 일은 직접 정의해야 한다.
     * - 스레드를 종료할지, 계속 하던 일을 할지 등
     * join : 다른 스레드가 끝날 때까지 기다리기
     */

    /*
     * 테스트 출력 결과
     * [18:17:53.66] Main Thread: main
     * [18:17:53.66] second Thread : Thread-1 started
     * [18:17:53.66] first Thread on run : Thread-0
     * [18:17:54.68] first Thread on run : Thread-0
     * [18:17:55.69] first Thread on run : Thread-0
     * [18:17:56.68] second Thread : Thread-1 finished
     * [18:17:56.69] Thread[Thread-0,5,main] main thread is finished
     * [18:17:56.69] first Thread exit!
     */

    public static void main(String[] args) {
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");

        /*
         * 쓰레드 사용법 1 (과거 방식)
         * - 문제점: 쓰레드가 늘어날 때마다 인터럽트 처리가 점점 늘어나고 복잡해진다.
         *   => 수집, 수백개의 쓰레드를 코딩으로 직접 관리하는 것인 어렵다.
         */
        // Thread 인터페이스를 구현해서 스레드를 시작하는 방식
        //
        Thread thread = new Thread(() -> {
            while(true) {
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] first Thread on run : "+ Thread.currentThread().getName());
                try {
                    Thread.sleep(1000L); // 1초 동안 쓰레드 sleep = 즉, 1초 동안 다른 스레드가 먼저 일할 수 있게 한다.
                } catch (InterruptedException e) { // sleep하는 동안이라도 다른 스레드가 이 스레드를 깨우면 발생
                    // 스레드가 interrupt 됐을 때 작업 처리
                    // 스레드를 종료하거나 다른 일을 하게 할 수 있다.
                    System.out.println("[" + LocalDateTime.now().format(formatter) + "] first Thread exit!");
                    return ; // 스레드 종료
                }
            }
        });
        thread.start();

        /*
         * 쓰레드 사용법 2 - Thread 클래스 상속해서 사용하는 방법
         */
        MyThread myThread = new MyThread();
        myThread.start();

        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Main Thread: " + Thread.currentThread().getName());

        try {
            // 다른 스레드를 대기시킨다 = 즉, main 스레드는 이 스레드가 끝날 때까지 대기해야 한다.
            myThread.join();
        } catch (InterruptedException e) {
            // main 스레드가 interrupt 됐을 때 작업 처리
            e.printStackTrace();
        }

        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + thread + " main thread is finished");

        // 맨 앞에 만들었던 스레드 interrupt 시키기 = 스레드 깨우기
        thread.interrupt();
    }

    // Thread 클래스를 상속해서 스레드를 구현하는 방식
    static class MyThread extends Thread {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");
        @Override
        public void run() {
            try {
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] second Thread : "+ Thread.currentThread().getName() + " started");
                Thread.sleep(3000L); // 3초
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] second Thread : "+ Thread.currentThread().getName() + " finished");
        }
    }
}
