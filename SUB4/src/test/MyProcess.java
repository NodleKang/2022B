package test;

public class MyProcess {

    public static void main(String[] args) {
        int processNo = Integer.parseInt(args[0]);
        int threadCount = Integer.parseInt(args[1]);

        testOnThread(processNo, threadCount);
    }

    private static void testOnThread(int processNo, int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Process ["+processNo+"] Thread: " + Thread.currentThread().getName());
                }
            });
            thread.start();
        }
    }

}
