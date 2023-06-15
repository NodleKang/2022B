import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/*
 * Callable
 * - Runnable과 유사하지만 작업의 결과를 받을 수 있습니다.
 * - Runnable은 리턴 값이 없기 때문에 스레드가 수행한 작업의 결과를 이용한 처리를 할 수 없습니다.
 */
/*
 * Future 예제
 * - 비동기적인 작업의 현재 상태를 조회하거나 결과를 가져올 수 있습니다.
 *
 * get()
 *  - 결과를 받을 때까지 해당 위치지에 대기합니다.
 *  - timeout을 설정할 수 있습니다.
 * isDone()
 *   - 작업 상태를 확인할 수 있습니다. (완료: true, 미완료: false)
 * cancel()
 *   - 취소에 성공하면 true, 실패하면 false를 반환합니다.
 *   - parameter로 true를 전달하면 현재 진행중인 스레드를 interrupt 하고, 아니면 작업이 끝날 때까지 기다립니다.
 * invokeAll()
 *   - 여러 작업을 동시에 실행합니다.
 *   - 가장 오래 걸리는 작업이 끝날때까지 기다립니다.
 * invokeAny()
 *   - 여러 작업을 동시에 실행합니다.
 *   - 가장 먼저 끝나는 작업이 끝날때까지 기다립니다.
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

        // 앞에서 Executor 스레드가 2개이기 때문에 Blocking Queue에서 대기함 (hello가 완료되어야 작업 수행 가능)
        Callable<String> dev = () -> {
            Thread.sleep(1000L);
            return "Dev";
        };

        // invokeAll(): Callable을 뭉쳐서 여러 작업을 동시애 수행할 수 있습니다.
        // 모든 스레드가 끝나야 값을 가져올 수 있습니다.
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

        // invokeAny(): Callable을 뭉쳐서 여러 작업을 동시에 수행할 수 있습니다.
        // 한 스레드라도 응답이 오면 값을 가져올 수 있습니다.
        // 블록킹 콜 입니다.
        try {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-- invokeAny() ---------------");
            String s = executorService.invokeAny(Arrays.asList(hello, java, dev));
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] "+ s);
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-------------------------------");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Callable이 반환하는 값은 Future로 받을 수 있습니다.
        Future<String> helloFuture = executorService.submit(hello);
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-- return Future --------------");
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + helloFuture.isDone()); // 실행 중인 작업이 끝났으면 true, 아니면 false 반환
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-------------------------------");

        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "Main Thread Started!");

        /*
        // 현재 진행 중인 스레드를 interrupt 하면서 종료
        helloFuture.cancel(true);
        */
        /*
        // 현재 진행 중인 스레드가 끝나길 기다렸다가 종료 (== graceful)
        // - 작업 완료를 기다려도 cancel이 호출되면 Future에서 값을 꺼내는 것은 불가능하다
        //   이미 취소한 작업에서 값을 꺼내려고 하면 CancellationException 발생
        // - cancel을 하면 상태(isDone)은 무조건 true가 된다.
        //   이 때 true는 작업이 완료되어 값을 꺼낼 수 있다는 의미가 아니며, cancel로 인해 종료된 것 뿐이다.
        helloFuture.cancel(false);
        */

        try {
            // get()은 Blocking Call 이기 때문에 결과를 반환받을 때까지 대기한다.
            String s = helloFuture.get(); // get()을 이용해서 Future의 값을 받아온다.
            System.out.println(helloFuture.isDone()); // 작업이 끝났으면 true, 아니면 false
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
