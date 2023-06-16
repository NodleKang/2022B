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

        try {
            testOnCompletableFuture(processNo, threadCount);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            testOnCompletableFuture2(processNo, threadCount);
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

    private static void testOnCompletableFuture(int processNo, int threadCount) throws ExecutionException, InterruptedException {
        List<CompletableFuture<String>> completableFutureList = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
                return "Process [" + processNo + "] Thread: " + Thread.currentThread().getName();
            });
            completableFutureList.add(completableFuture);
        }
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[0]))
                .thenAccept((voidValue) -> {
                    completableFutureList.forEach((completableFuture) -> {
                        try {
                            System.out.println(completableFuture.get());
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    });
                });
    }

    private static void testOnCompletableFuture2(int processNo, int threadCount) throws ExecutionException, InterruptedException {
        List<CompletableFuture<String>> completableFutureList = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
                return "Process [" + processNo + "] Thread: " + Thread.currentThread().getName();
            });
            completableFutureList.add(completableFuture);
        }
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[0]))
                .join();
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<String> completableFuture = completableFutureList.get(i);
            try {
                System.out.println(completableFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}
