package test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MyProcess {

    public static void main(String[] args) {
        int processNo = Integer.parseInt(args[0]);
        int threadCount = Integer.parseInt(args[1]);

        testOnThread(processNo, threadCount);

        try {
            testOnCallable(processNo, threadCount);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Process ["+processNo+"] End");
    }

    private static void testOnThread(int processNo, int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(
                    () -> System.out.println("Process ["+processNo+"] Thread: " + Thread.currentThread().getName())
            );
            thread.start();
        }
    }

    private static void testOnCallable(int processNo, int threadCount) throws ExecutionException, InterruptedException {
        List<Callable<String>> callableList = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Callable<String> callable = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "Process [" + processNo + "] Thread: " + Thread.currentThread().getName();
                }
            };
            callableList.add(callable);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<String>> futures = executorService.invokeAll(callableList);
        for (Future<String> future : futures) {
            System.out.println(future.get());
        }
        executorService.shutdown();
    }

}
