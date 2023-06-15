import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Study02 {

    /*
     * Thread �ֿ� ���
     *
     * sleep : ���� ������ ����α�
     * - �ٸ� �����尡 ���� �� �ְ� ��ȸ�� ������ Lock�� ������ �ʴ´�. (����� ����!)
     * interrupt : �ٸ� ������ �����
     * - �ٸ� �����带 ������ InterruptedException�� �߻���Ų��. InterruptedException�� �߻����� �� �� ���� ���� �����ؾ� �Ѵ�.
     * - �����带 ��������, ��� �ϴ� ���� ���� ��
     * join : �ٸ� �����尡 ���� ������ ��ٸ���
     */

    /*
     * �׽�Ʈ ��� ���
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
         * ������ ���� 1 (���� ���)
         * - ������: �����尡 �þ ������ ���ͷ�Ʈ ó���� ���� �þ�� ����������.
         *   => ����, ���鰳�� �����带 �ڵ����� ���� �����ϴ� ���� ��ƴ�.
         */
        // Thread �������̽��� �����ؼ� �����带 �����ϴ� ���
        //
        Thread thread = new Thread(() -> {
            while(true) {
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] first Thread on run : "+ Thread.currentThread().getName());
                try {
                    Thread.sleep(1000L); // 1�� ���� ������ sleep = ��, 1�� ���� �ٸ� �����尡 ���� ���� �� �ְ� �Ѵ�.
                } catch (InterruptedException e) { // sleep�ϴ� �����̶� �ٸ� �����尡 �� �����带 ����� �߻�
                    // �����尡 interrupt ���� �� �۾� ó��
                    // �����带 �����ϰų� �ٸ� ���� �ϰ� �� �� �ִ�.
                    System.out.println("[" + LocalDateTime.now().format(formatter) + "] first Thread exit!");
                    return ; // ������ ����
                }
            }
        });
        thread.start();

        /*
         * ������ ���� 2 - Thread Ŭ���� ����ؼ� ����ϴ� ���
         */
        MyThread myThread = new MyThread();
        myThread.start();

        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Main Thread: " + Thread.currentThread().getName());

        try {
            // �ٸ� �����带 ����Ų�� = ��, main ������� �� �����尡 ���� ������ ����ؾ� �Ѵ�.
            myThread.join();
        } catch (InterruptedException e) {
            // main �����尡 interrupt ���� �� �۾� ó��
            e.printStackTrace();
        }

        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + thread + " main thread is finished");

        // �� �տ� ������� ������ interrupt ��Ű�� = ������ �����
        thread.interrupt();
    }

    // Thread Ŭ������ ����ؼ� �����带 �����ϴ� ���
    static class MyThread extends Thread {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");
        @Override
        public void run() {
            try {
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] second Thread : "+ Thread.currentThread().getName() + " started");
                Thread.sleep(3000L); // 3��
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] second Thread : "+ Thread.currentThread().getName() + " finished");
        }
    }
}
