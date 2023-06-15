import com.sun.tools.javac.Main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/*
 * java에서 비동기(async) 프로그래밍하기
 * - Future를 사용해서도 어드 정도 가능했지만 힘든 일들이 많았다.
 *
 * Future로는 하기 힘든 일
 * - Future는 외부에서 완료시킬 수 없다.
 *   : 취소하거나 get()에 타임아웃을 설정할 수는 있음
 * - 블록킹 코드(get())을 사용하지 않고서는 작업이 끝났을 때 콜백을 실행할 수는 없다.
 * - 여러 Future를 조합할 수는 없다.
 *   : 예) Event 정보를 가져온 후에 Event에 참석하는 회원 목록 가져오기
 * - 예외 처리용 API를 제공하지 않는다.
 */
/*
 * CompletableFuture
 *
 * - 외부에서 명시적으로 Complete 시킬 수 있다.
 *   : 몇 초 이내에 응답이 오지 않으면 기본값으로 설정
 * - 명시적으로 Executor(스레드풀)을 선언해서 사용하지 않아도 된다.
 * - main 스레드 입장에서는 get()을 사용해야 CompletableFuture에 정의한 동작이 수행된다.
 *   : main 스레드에서 sleep() 혹은 get() 메소드를 사용하지 않으면 그 작업을 기다리지 않고
 *     바로 끝나서 해당 Future 작업을 볼 수 없다.
 *   : 하지만 ForkJoinPool에서 가져온 스레드는 sleep() 혹은 get()이 없어도 CompletableFuture에 정의된 코드를 실행한다.
 * - ForkJoinPool을 사용해서 Executor(스레드풀)을 따로 정의하지 않고도 스레드를 사용할 수 있다.
 *   : Executor(스레드풀)을 구현한 구현체(Dequeue를 사용함)
 *   : 자기 스레드가 할 일이 없으면 직접 Dequeue에서 할 일을 가져와서 처리하는 방식의 프레임워크임
 *   : 작업 단위를 자기가 파생시킨 서브 태스크가 있다면 서브 태스크들을 잘게 쪼개서
 *     다른 스레드에 분산시켜서 작업을 처리하고 모아서 결과 값을 도출한다.
 * - Implements Future
 * - Implements CompletionStage
 */
/*
 * 비동기로 작업 실행하기
 * - 반환값이 없는 경우: runAsync()
 * - 반환값이 있는 경우: supplyAsync()
 * - 필요하다면 원하는 Executor(스레드풀)을 사용해서 실행할 수도 있다. (기본은 commonPool()) *
 */
/*
 * 콜백 제공하기
 * - thenApply(Function): 반환값을 받아서 다른 값으로 바꾸는 콜백
 * - thenAccept(Consumer): 반환값을 받아서 다른 작업을 처리하는 콜랙(반환 없음)
 * - thenRun(Runnable): 반환값을 리턴받지 않고, 다른 작업을 처리하는 콜백
 * - 콜백 자체를 또 다른 스레드에서 실행할 수 있다.
 */
/*
 * 조합하기
 * thenCompose(): 두 작업을 서로 이어서 실행하도록 조합
 * thenCombine(): 두 작업을 각각 독립적으로 실행하고 둘 다 종료됐을 때 콜백 실행
 * allOf(): 여러 작업을 모두 실행하고 모든 작업 결과에 콜백 실행
 * anyOf(): 여러 작업 중에 가장 빨리 끝난 하나의 결과에 콜백 실행
 */
/*
 * 예외처리
 * - exceptionally(Function): 예외가 발생하는 콜백 실행
 * - handle(BiFunction)
 */
public class Study04 {

    public static void main(String[] args) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");

        /*
         * 기존의 Future 사용 방식
         */
        // 기존 Future의 문제점
        // - Future에서 get()하기 전까지는 Future의 결과를 이용한 작업을 할 수 없다.
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        Callable<String> hello = () -> {
            Thread.sleep(1000L);
            return "Hello";
        };

        Future<String> future1 = executorService.submit(hello);
        try {
            // Future의 값을 이용한 로직은 future.get() 이후에 사용할 수 있다.
            future1.get();
            executorService.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "Future Ended!");

        /*
         * CompletableFuture 사용 방식 1
         * - 외부에서 명시적으로 Complete 할 수 있다.
         */
        CompletableFuture<String> completableFuture1 = new CompletableFuture<>();
        completableFuture1.complete("Dev History"); // Future의 기본값 설정과 동시에 작업 완료 처리가 된다.
        System.out.println(completableFuture1.isDone()); // 상태 true 출력
        try {
            System.out.println(completableFuture1.get()); // Future 가 종료된 후에 결과값을 가져와서 출력 가능
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture 사용 방식 2
         * - 외부에서 명시적으로 Complete 할 수 있다. (위의 결과와 동일함)
         */
        CompletableFuture<String> completableFuture2 = CompletableFuture.completedFuture("Dev History");
        System.out.println(completableFuture2.isDone()); // 상태 true 출력
        try {
            System.out.println(completableFuture2.get()); // Future 가 종료된 후에 결과값을 가져와서 출력 가능
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + "-----------");

        /*
         * CompletableFuture 사용 방식 3 - 1 runAsync()
         * - 비동기로 작업 실행하기
         * - runAsync(): 반환값이 없는 경우
         * - supplyAsync(): 반환값이 있는 경우
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
         * CompletableFuture 사용 방식 3 - 2 supplyAsync()
         * - 비동기로 작업 실행하기
         * - runAsync(): 반환값이 없는 경우
         * - supplyAsync(): 반환값이 있는 경우
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
         * CompletableFuture 사용 방식 4 - 1 thenApply()
         * - CompletableFuture는 Future와 달리 콜백을 주는 것이 가능하다.
         * - 그리고 get() 이전에 처리 로직을 작성하는 것이 가능해졌다.
         * - thenApply(Function): 반환 값을 받아서 다른 값으로 바꾸는 콜백
         * - thenAccept(Consumer): 반환 값으로 또 다른 작업을 처리하는 콜백(반환 없이)
         * - thenRun(Runnable): 반환 값을 다른 스레의 작업에서 사용하는 콜백
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
         * CompletableFuture 사용 방식 4 - 1 thenAccept()
         * - CompletableFuture는 Future와 달리 콜백을 주는 것이 가능하다.
         * - 그리고 get() 이전에 처리 로직을 작성하는 것이 가능해졌다.
         * - thenApply(Function): 반환 값을 받아서 다른 값으로 바꾸는 콜백
         * - thenAccept(Consumer): 반환 값으로 또 다른 작업을 처리하는 콜백(반환 없이)
         * - thenRun(Runnable): 반환 값을 다른 스레의 작업에서 사용하는 콜백
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
         * CompletableFuture 사용 방식 4 - 2 thenRun()
         * - CompletableFuture는 Future와 달리 콜백을 주는 것이 가능하다.
         * - 그리고 get() 이전에 처리 로직을 작성하는 것이 가능해졌다.
         * - thenApply(Function): 반환 값을 받아서 다른 값으로 바꾸는 콜백
         * - thenAccept(Consumer): 반환 값으로 또 다른 작업을 처리하는 콜백(반환 없이)
         * - thenRun(Runnable): 반환 값을 다른 스레의 작업에서 사용하는 콜백
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
         * CompletableFuture 사용 방식 5 - 콜백 + 스레드풀 변경
         * CompletableFuture에서 스레드풀(Executor)를 변경해서 실행하면 사용되는 스레드가 달라진다.
         */
        ExecutorService executorService2 = Executors.newFixedThreadPool(4);

        CompletableFuture<Void> completableFuture8 = CompletableFuture.supplyAsync(() -> {
            // supplyAsync()는 executorService2 스레드풀 내의 1번 스레드 사용
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] supplyAsync() " + Thread.currentThread().getName());
            return "Hello";
        }, executorService2).thenRun(() -> {
            // thenRun()은 executorService2 스레드풀 내의 1번 스레드 혹은 main 스레드 사용
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenRun() " + Thread.currentThread().getName());
        }).thenRunAsync(() -> {
            // thenRunAsync()는 executorService2 스레드풀 내의 2번 스레드 사용
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenRunAsync() " + Thread.currentThread().getName());
        }, executorService2).thenRun(() -> {
            // thenRun()은 executorService2 스레드풀 내의 2번 스레드 사용
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenRun() " + Thread.currentThread().getName());
        }).thenRunAsync(() -> {
            // thenRun()은 executorService2 스레드풀 내의 3번 스레드 사용
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] thenRunAsync() " + Thread.currentThread().getName());
        }, executorService2).thenRunAsync(() -> {
            // thenRun()은 executorService2 스레드풀 내의 4번 스레드 사용
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
         * CompletableFuture 사용 방식 6 비동기 작업 조합 - 1 thenCompose()
         * - thenCompose(): 두 작업이 서로 이어서 실행하도록 조립(CompletableFuture 두 개를 연결해서 처리한 하나의 CompltableFuture가 나온다)
         * - thenCombine(): 두 작업을 독립적으로 실행하고 둘 다 종료했을 때 콜백 실행
         * - allOf(): 여러 작업을 모두 실행하고 모든 작업 결과에 콜백 실행
         * - anyOf(): 여러 작업 중에 가장 빨리 끝난 하나의 결과에 콜백 실행
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
         * CompletableFuture 사용 방식 6 비동기 작업 조합 - 2 thenCombine()
         * - thenCompose(): 두 작업이 서로 이어서 실행하도록 조립(CompletableFuture 두 개를 연결해서 처리한 하나의 CompltableFuture가 나온다)
         * - thenCombine(): 두 작업을 독립적으로 실행하고 둘 다 종료했을 때 콜백 실행
         * - allOf(): 여러 작업을 모두 실행하고 모든 작업 결과에 콜백 실행
         * - anyOf(): 여러 작업 중에 가장 빨리 끝난 하나의 결과에 콜백 실행
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
         * CompletableFuture 사용 방식 7 비동기 작업 조합 - 3 allOf()
         * n개 이상인 서브 태스크들을 합쳐서 처리하는 방법
         * - allOf()에 넘긴 모든 태스크들이 다 끝났을 때 모든 작업 결과에 콜백을 실행한다.
         * - 문제는 여러 태스크들의 결과가 모두 동일한 타입임을 보장할 수 없기 때문에 처리가 어렵다.
         */
        // join과 get은 똑같은데, 예외처리방식에서 다르다.
        // get은 checked Exception, join은 unchecked Exception이 발생한다.
        // 결과를 아래와 같이 콜렉션으로 처리하면 블록킹이 발생하지 않는다.
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
         * CompletableFuture 사용 방식 7 비동기 작업 조합 - 3 anyOf()
         * n개 이상인 서브 태스크들을 합쳐서 처리하는 방법
         * - anyOf()에 넘긴 테스크 중에 빨리 끝나는 작업 하나의 결과에 대해 콜백을 실행한다.
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
         * CompletableFuture 사용 방식 8 - 에러 발생시 콜백 실행 - 1 anyOf()
         * - exceptionally(Function): 에러 발생시 콜백 실행
         * - handle(BiFunction): 정상적으로 종료되는 경우와 에러가 발생했을 종료되는 경우 모두에서 사용 가능
         *   첫 번째 파라미터는 정상적으로 종료되었을 경우의 값, 두 번째 파라미터는 에러가 발생한 경우의 값
         */
        boolean throwError = true;
        CompletableFuture<String> banana = CompletableFuture.supplyAsync(() -> {
            if (throwError) {
                throw new IllegalArgumentException();
            }
            // 바로 에러 처리로 넘어가기 떄문에 Banana가 반환되는 일이 없음
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
         * CompletableFuture 사용 방식 8 - 에러 발생시 콜백 실행 - 2 handle()
         * - exceptionally(Function): 에러 발생시 콜백 실행
         * - handle(BiFunction): 정상적으로 종료되는 경우와 에러가 발생했을 종료되는 경우 모두에서 사용 가능
         *   첫 번째 파라미터는 정상적으로 종료되었을 경우의 값, 두 번째 파라미터는 에러가 발생한 경우의 값
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
