import com.sun.tools.javac.Main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/*
 * java���� �񵿱�(async) ���α׷����ϱ�
 * - Future�� ����ؼ��� ��� ���� ���������� ���� �ϵ��� ���Ҵ�.
 *
 * Future�δ� �ϱ� ���� ��
 * - Future�� �ܺο��� �Ϸ��ų �� ����.
 *   : ����ϰų� get()�� Ÿ�Ӿƿ��� ������ ���� ����
 * - ���ŷ �ڵ�(get())�� ������� �ʰ��� �۾��� ������ �� �ݹ��� ������ ���� ����.
 * - ���� Future�� ������ ���� ����.
 *   : ��) Event ������ ������ �Ŀ� Event�� �����ϴ� ȸ�� ��� ��������
 * - ���� ó���� API�� �������� �ʴ´�.
 */
/*
 * CompletableFuture
 *
 * - �ܺο��� ��������� Complete ��ų �� �ִ�.
 *   : �� �� �̳��� ������ ���� ������ �⺻������ ����
 * - ��������� Executor(������Ǯ)�� �����ؼ� ������� �ʾƵ� �ȴ�.
 * - main ������ ���忡���� get()�� ����ؾ� CompletableFuture�� ������ ������ ����ȴ�.
 *   : main �����忡�� sleep() Ȥ�� get() �޼ҵ带 ������� ������ �� �۾��� ��ٸ��� �ʰ�
 *     �ٷ� ������ �ش� Future �۾��� �� �� ����.
 *   : ������ ForkJoinPool���� ������ ������� sleep() Ȥ�� get()�� ��� CompletableFuture�� ���ǵ� �ڵ带 �����Ѵ�.
 * - ForkJoinPool�� ����ؼ� Executor(������Ǯ)�� ���� �������� �ʰ� �����带 ����� �� �ִ�.
 *   : Executor(������Ǯ)�� ������ ����ü(Dequeue�� �����)
 *   : �ڱ� �����尡 �� ���� ������ ���� Dequeue���� �� ���� �����ͼ� ó���ϴ� ����� �����ӿ�ũ��
 *   : �۾� ������ �ڱⰡ �Ļ���Ų ���� �½�ũ�� �ִٸ� ���� �½�ũ���� �߰� �ɰ���
 *     �ٸ� �����忡 �л���Ѽ� �۾��� ó���ϰ� ��Ƽ� ��� ���� �����Ѵ�.
 * - Implements Future
 * - Implements CompletionStage
 */
/*
 * �񵿱�� �۾� �����ϱ�
 * - ��ȯ���� ���� ���: runAsync()
 * - ��ȯ���� �ִ� ���: supplyAsync()
 * - �ʿ��ϴٸ� ���ϴ� Executor(������Ǯ)�� ����ؼ� ������ ���� �ִ�. (�⺻�� commonPool()) *
 */
/*
 * �ݹ� �����ϱ�
 * - thenApply(Function): ��ȯ���� �޾Ƽ� �ٸ� ������ �ٲٴ� �ݹ�
 * - thenAccept(Consumer): ��ȯ���� �޾Ƽ� �ٸ� �۾��� ó���ϴ� �ݷ�(��ȯ ����)
 * - thenRun(Runnable): ��ȯ���� ���Ϲ��� �ʰ�, �ٸ� �۾��� ó���ϴ� �ݹ�
 * - �ݹ� ��ü�� �� �ٸ� �����忡�� ������ �� �ִ�.
 */
/*
 * �����ϱ�
 * thenCompose(): �� �۾��� ���� �̾ �����ϵ��� ����
 * thenCombine(): �� �۾��� ���� ���������� �����ϰ� �� �� ������� �� �ݹ� ����
 * allOf(): ���� �۾��� ��� �����ϰ� ��� �۾� ����� �ݹ� ����
 * anyOf(): ���� �۾� �߿� ���� ���� ���� �ϳ��� ����� �ݹ� ����
 */
/*
 * ����ó��
 * - exceptionally(Function): ���ܰ� �߻��ϴ� �ݹ� ����
 * - handle(BiFunction)
 */
public class Study04 {

    public static void main(String[] args) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");

        /*
         * ������ Future ��� ���
         */
        // ���� Future�� ������
        // - Future���� get()�ϱ� �������� Future�� ����� �̿��� �۾��� �� �� ����.
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        Callable<String> hello = () -> {
            Thread.sleep(1000L);
            return "Hello";
        };

        Future<String> future1 = executorService.submit(hello);
        try {
            // Future�� ���� �̿��� ������ future.get() ���Ŀ� ����� �� �ִ�.
            future1.get();
            executorService.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "Future Ended!");

        /*
         * CompletableFuture ��� ��� 1
         * - �ܺο��� ��������� Complete �� �� �ִ�.
         */
        CompletableFuture<String> completableFuture1 = new CompletableFuture<>();
        completableFuture1.complete("Dev History"); // Future�� �⺻�� ������ ���ÿ� �۾� �Ϸ� ó���� �ȴ�.
        System.out.println(completableFuture1.isDone()); // ���� true ���
        try {
            System.out.println(completableFuture1.get()); // Future �� ����� �Ŀ� ������� �����ͼ� ��� ����
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 2
         * - �ܺο��� ��������� Complete �� �� �ִ�. (���� ����� ������)
         */
        CompletableFuture<String> completableFuture2 = CompletableFuture.completedFuture("Dev History");
        System.out.println(completableFuture2.isDone()); // ���� true ���
        try {
            System.out.println(completableFuture2.get()); // Future �� ����� �Ŀ� ������� �����ͼ� ��� ����
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 3 - 1 runAsync()
         * - �񵿱�� �۾� �����ϱ�
         * - runAsync(): ��ȯ���� ���� ���
         * - supplyAsync(): ��ȯ���� �ִ� ���
         */
        CompletableFuture<Void> completableFuture3 = CompletableFuture.runAsync(() -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "runAsync() " + Thread.currentThread().getName());
        });
        try {
            System.out.println(completableFuture3.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 3 - 2 supplyAsync()
         * - �񵿱�� �۾� �����ϱ�
         * - runAsync(): ��ȯ���� ���� ���
         * - supplyAsync(): ��ȯ���� �ִ� ���
         */
        CompletableFuture<String> completableFuture4 = CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "supplyAsync() " + Thread.currentThread().getName());
            return "Hello";
        });
        try {
            System.out.println(completableFuture4.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");


        /*
         * CompletableFuture ��� ��� 4 - 1 thenApply()
         * - CompletableFuture�� Future�� �޸� �ݹ��� �ִ� ���� �����ϴ�.
         * - �׸��� get() ������ ó�� ������ �ۼ��ϴ� ���� ����������.
         * - thenApply(Function): ��ȯ ���� �޾Ƽ� �ٸ� ������ �ٲٴ� �ݹ�
         * - thenAccept(Consumer): ��ȯ ������ �� �ٸ� �۾��� ó���ϴ� �ݹ�(��ȯ ����)
         * - thenRun(Runnable): ��ȯ ���� �ٸ� ������ �۾����� ����ϴ� �ݹ�
         */
        CompletableFuture<String> completableFuture5 = CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "Hello supplyAsync() " + Thread.currentThread().getName());
            return "Hello";
        }).thenApply((s) -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenApply() " +Thread.currentThread().getName());
            return s.toUpperCase();
        });
        try {
            System.out.println(completableFuture5.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 4 - 1 thenAccept()
         * - CompletableFuture�� Future�� �޸� �ݹ��� �ִ� ���� �����ϴ�.
         * - �׸��� get() ������ ó�� ������ �ۼ��ϴ� ���� ����������.
         * - thenApply(Function): ��ȯ ���� �޾Ƽ� �ٸ� ������ �ٲٴ� �ݹ�
         * - thenAccept(Consumer): ��ȯ ������ �� �ٸ� �۾��� ó���ϴ� �ݹ�(��ȯ ����)
         * - thenRun(Runnable): ��ȯ ���� �ٸ� ������ �۾����� ����ϴ� �ݹ�
         */
        CompletableFuture<Void> completableFuture6 = CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "Hello supplyAsync() " + Thread.currentThread().getName());
            return "Hello";
        }).thenAccept((s) -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenAccept() " +Thread.currentThread().getName());
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenAccept() " + s.toLowerCase());
        });
        try {
            System.out.println(completableFuture6.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 4 - 2 thenRun()
         * - CompletableFuture�� Future�� �޸� �ݹ��� �ִ� ���� �����ϴ�.
         * - �׸��� get() ������ ó�� ������ �ۼ��ϴ� ���� ����������.
         * - thenApply(Function): ��ȯ ���� �޾Ƽ� �ٸ� ������ �ٲٴ� �ݹ�
         * - thenAccept(Consumer): ��ȯ ������ �� �ٸ� �۾��� ó���ϴ� �ݹ�(��ȯ ����)
         * - thenRun(Runnable): ��ȯ ���� �ٸ� ������ �۾����� ����ϴ� �ݹ�
         */
        CompletableFuture<Void> completableFuture7 = CompletableFuture.supplyAsync( () -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Hello supplyAsync() " + Thread.currentThread().getName());
            return "Hello";
        }).thenRun(() -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenRun() " + Thread.currentThread().getName());
        });
        try {
            completableFuture7.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 5 - �ݹ� + ������Ǯ ����
         * CompletableFuture���� ������Ǯ(Executor)�� �����ؼ� �����ϸ� ���Ǵ� �����尡 �޶�����.
         */
        ExecutorService executorService2 = Executors.newFixedThreadPool(4);

        CompletableFuture<Void> completableFuture8 = CompletableFuture.supplyAsync(() -> {
            // supplyAsync()�� executorService2 ������Ǯ ���� 1�� ������ ���
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] supplyAsync() " + Thread.currentThread().getName());
            return "Hello";
        }, executorService2).thenRun(() -> {
            // thenRun()�� executorService2 ������Ǯ ���� 1�� ������ Ȥ�� main ������ ���
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenRun() " + Thread.currentThread().getName());
        }).thenRunAsync(() -> {
            // thenRunAsync()�� executorService2 ������Ǯ ���� 2�� ������ ���
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenRunAsync() " + Thread.currentThread().getName());
        }, executorService2).thenRun(() -> {
            // thenRun()�� executorService2 ������Ǯ ���� 2�� ������ ���
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenRun() " + Thread.currentThread().getName());
        }).thenRunAsync(() -> {
            // thenRun()�� executorService2 ������Ǯ ���� 3�� ������ ���
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenRunAsync() " + Thread.currentThread().getName());
        }, executorService2).thenRunAsync(() -> {
            // thenRun()�� executorService2 ������Ǯ ���� 4�� ������ ���
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenRunAsync() " + Thread.currentThread().getName());
        }, executorService2);

        try {
            completableFuture8.get();
            executorService2.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 6 �񵿱� �۾� ���� - 1 thenCompose()
         * - thenCompose(): �� �۾��� ���� �̾ �����ϵ��� ����(CompletableFuture �� ���� �����ؼ� ó���� �ϳ��� CompltableFuture�� ���´�)
         * - thenCombine(): �� �۾��� ���������� �����ϰ� �� �� �������� �� �ݹ� ����
         * - allOf(): ���� �۾��� ��� �����ϰ� ��� �۾� ����� �ݹ� ����
         * - anyOf(): ���� �۾� �߿� ���� ���� ���� �ϳ��� ����� �ݹ� ����
         */
        CompletableFuture<String> firstFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] first " + Thread.currentThread().getName());
            return "first";
        });
        try {
            CompletableFuture<String> secondFuture = firstFuture.thenCompose(Study04::getWorld);
            System.out.println("[" + LocalDateTime.now().format(formatter) +"] " + secondFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 6 �񵿱� �۾� ���� - 2 thenCombine()
         * - thenCompose(): �� �۾��� ���� �̾ �����ϵ��� ����(CompletableFuture �� ���� �����ؼ� ó���� �ϳ��� CompltableFuture�� ���´�)
         * - thenCombine(): �� �۾��� ���������� �����ϰ� �� �� �������� �� �ݹ� ����
         * - allOf(): ���� �۾��� ��� �����ϰ� ��� �۾� ����� �ݹ� ����
         * - anyOf(): ���� �۾� �߿� ���� ���� ���� �ϳ��� ����� �ݹ� ����
         */
        CompletableFuture<String> friendFuture1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] John " + Thread.currentThread().getName());
            return "John";
        });
        CompletableFuture<String> friendFuture2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Jane " + Thread.currentThread().getName());
            return "Jane";
        });
        CompletableFuture<String> completableFuture9 = friendFuture1.thenCombine(friendFuture2, (f1, f2) -> {
            return f1 + " and " + f2;
        });
        try {
            System.out.println("[" + LocalDateTime.now().format(formatter) +"] " + completableFuture9.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 7 �񵿱� �۾� ���� - 3 allOf()
         * n�� �̻��� ���� �½�ũ���� ���ļ� ó���ϴ� ���
         * - allOf()�� �ѱ� ��� �½�ũ���� �� ������ �� ��� �۾� ����� �ݹ��� �����Ѵ�.
         * - ������ ���� �½�ũ���� ����� ��� ������ Ÿ������ ������ �� ���� ������ ó���� ��ƴ�.
         */
        // join�� get�� �Ȱ�����, ����ó����Ŀ��� �ٸ���.
        // get�� checked Exception, join�� unchecked Exception�� �߻��Ѵ�.
        // ����� �Ʒ��� ���� �ݷ������� ó���ϸ� ���ŷ�� �߻����� �ʴ´�.
        List<CompletableFuture<String>> futureList = Arrays.asList(friendFuture1, friendFuture2);
        CompletableFuture[] futureArray = futureList.toArray(new CompletableFuture[futureList.size()]);

        CompletableFuture<List<String>> allCompletableFuture = CompletableFuture.allOf(futureArray)
                .thenApply(v -> futureList.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));

        try {
            allCompletableFuture.get().forEach(System.out::println);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 7 �񵿱� �۾� ���� - 3 anyOf()
         * n�� �̻��� ���� �½�ũ���� ���ļ� ó���ϴ� ���
         * - anyOf()�� �ѱ� �׽�ũ �߿� ���� ������ �۾� �ϳ��� ����� ���� �ݹ��� �����Ѵ�.
         */
        CompletableFuture<Void> anyCompletableFuture =
                CompletableFuture.anyOf(friendFuture1, friendFuture2)
                        .thenAccept(System.out::println);

        try {
            anyCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 8 - ���� �߻��� �ݹ� ���� - 1 anyOf()
         * - exceptionally(Function): ���� �߻��� �ݹ� ����
         * - handle(BiFunction): ���������� ����Ǵ� ���� ������ �߻����� ����Ǵ� ��� ��ο��� ��� ����
         *   ù ��° �Ķ���ʹ� ���������� ����Ǿ��� ����� ��, �� ��° �Ķ���ʹ� ������ �߻��� ����� ��
         */
        boolean throwError = true;
        CompletableFuture<String> banana = CompletableFuture.supplyAsync(() -> {
            if (throwError) {
                throw new IllegalArgumentException();
            }
            // �ٷ� ���� ó���� �Ѿ�� ������ Banana�� ��ȯ�Ǵ� ���� ����
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Banana " + Thread.currentThread().getName());
            return "Banana";
        }).exceptionally(ex -> {
            System.out.println(ex);
            return "Error!";
        });
        try {
            System.out.println(banana.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture ��� ��� 8 - ���� �߻��� �ݹ� ���� - 2 handle()
         * - exceptionally(Function): ���� �߻��� �ݹ� ����
         * - handle(BiFunction): ���������� ����Ǵ� ���� ������ �߻����� ����Ǵ� ��� ��ο��� ��� ����
         *   ù ��° �Ķ���ʹ� ���������� ����Ǿ��� ����� ��, �� ��° �Ķ���ʹ� ������ �߻��� ����� ��
         */
        CompletableFuture<String> grape = CompletableFuture.supplyAsync(() -> {
            if (throwError) {
                throw new IllegalArgumentException();
            }
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Grape " + Thread.currentThread().getName());
            return "Grape";
        }).handle((result, ex) -> {
            if (ex != null) {
                System.out.println(ex);
                return "Error!";
            } else {
                return result;
            }
        });
        try {
            System.out.println(grape.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

    }

    private static CompletableFuture<String> getWorld(String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] World " + Thread.currentThread().getName());
            return "World";
        });
    }
}
