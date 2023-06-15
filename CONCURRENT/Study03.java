import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/*
 * Callable
 * - Runnable�� ���������� �۾��� ����� ���� �� �ֽ��ϴ�.
 * - Runnable�� ���� ���� ���� ������ �����尡 ������ �۾��� ����� �̿��� ó���� �� �� �����ϴ�.
 */
/*
 * Future ����
 * - �񵿱����� �۾��� ���� ���¸� ��ȸ�ϰų� ����� ������ �� �ֽ��ϴ�.
 *
 * get()
 *  - ����� ���� ������ �ش� ��ġ���� ����մϴ�.
 *  - timeout�� ������ �� �ֽ��ϴ�.
 * isDone()
 *   - �۾� ���¸� Ȯ���� �� �ֽ��ϴ�. (�Ϸ�: true, �̿Ϸ�: false)
 * cancel()
 *   - ��ҿ� �����ϸ� true, �����ϸ� false�� ��ȯ�մϴ�.
 *   - parameter�� true�� �����ϸ� ���� �������� �����带 interrupt �ϰ�, �ƴϸ� �۾��� ���� ������ ��ٸ��ϴ�.
 * invokeAll()
 *   - ���� �۾��� ���ÿ� �����մϴ�.
 *   - ���� ���� �ɸ��� �۾��� ���������� ��ٸ��ϴ�.
 * invokeAny()
 *   - ���� �۾��� ���ÿ� �����մϴ�.
 *   - ���� ���� ������ �۾��� ���������� ��ٸ��ϴ�.
 */
public class Study03 {

    public static void main(String[] args) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<String> hello = () -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "Callable hello Start!");
            Thread.sleep(2000L);
            return "Hello";
        };

        Callable<String> java = () -> {
            Thread.sleep(3000L);
            return "Java";
        };

        // �տ��� Executor �����尡 2���̱� ������ Blocking Queue���� ����� (hello�� �Ϸ�Ǿ�� �۾� ���� ����)
        Callable<String> dev = () -> {
            Thread.sleep(1000L);
            return "Dev";
        };

        // invokeAll(): Callable�� ���ļ� ���� �۾��� ���þ� ������ �� �ֽ��ϴ�.
        // ��� �����尡 ������ ���� ������ �� �ֽ��ϴ�.
        try {
            List<Future<String>> futures = executorService.invokeAll(Arrays.asList(hello, java, dev));
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-- invokeAll() ---------------");
            for (Future<String> f: futures) {
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + f.get());
            }
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-------------------------------");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // invokeAny(): Callable�� ���ļ� ���� �۾��� ���ÿ� ������ �� �ֽ��ϴ�.
        // �� ������� ������ ���� ���� ������ �� �ֽ��ϴ�.
        // ���ŷ �� �Դϴ�.
        try {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-- invokeAny() ---------------");
            String s = executorService.invokeAny(Arrays.asList(hello, java, dev));
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] "+ s);
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-------------------------------");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Callable�� ��ȯ�ϴ� ���� Future�� ���� �� �ֽ��ϴ�.
        Future<String> helloFuture = executorService.submit(hello);
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-- return Future --------------");
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + helloFuture.isDone()); // ���� ���� �۾��� �������� true, �ƴϸ� false ��ȯ
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-------------------------------");

        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "Main Thread Started!");

        /*
        // ���� ���� ���� �����带 interrupt �ϸ鼭 ����
        helloFuture.cancel(true);
        */
        /*
        // ���� ���� ���� �����尡 ������ ��ٷȴٰ� ���� (== graceful)
        // - �۾� �ϷḦ ��ٷ��� cancel�� ȣ��Ǹ� Future���� ���� ������ ���� �Ұ����ϴ�
        //   �̹� ����� �۾����� ���� �������� �ϸ� CancellationException �߻�
        // - cancel�� �ϸ� ����(isDone)�� ������ true�� �ȴ�.
        //   �� �� true�� �۾��� �Ϸ�Ǿ� ���� ���� �� �ִٴ� �ǹ̰� �ƴϸ�, cancel�� ���� ����� �� ���̴�.
        helloFuture.cancel(false);
        */

        try {
            // get()�� Blocking Call �̱� ������ ����� ��ȯ���� ������ ����Ѵ�.
            String s = helloFuture.get(); // get()�� �̿��ؼ� Future�� ���� �޾ƿ´�.
            System.out.println(helloFuture.isDone()); // �۾��� �������� true, �ƴϸ� false
            System.out.println(s);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "Main Thread Ended!");
        executorService.shutdown();
    }

    /*public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> helloFuture = executorService.submit(() -> {
            Thread.sleep(2000L);
            return "Callable";
        });
        System.out.println("Hello");

        try {
            String result = helloFuture.get();
            System.out.println(result);
        } catch (ExecutionException | InterruptedException e) {
            return ;
        }
        executorService.shutdown();
    }*/

}
