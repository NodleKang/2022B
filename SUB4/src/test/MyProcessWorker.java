package test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.LinkedList;

public class MyProcessWorker {

    public int processNo;
    public LinkedList<Worker> workers = new LinkedList<>();

    public MyProcessWorker(int processNo, LinkedList<String> workers) {
        this.processNo = processNo;
    }

    public static void main(String[] args) {
        // 프로그램 실행 시 인자로 프로세스 번호와 스레드별 Worker 정보를 받는다.
        int processNo = Integer.parseInt(args[0]);
        String threadWorkers = args[1];

        Gson gson = new Gson();
        JsonObject jo = gson.fromJson(threadWorkers, JsonObject.class);

        // 프로세스 번호와 스레드 개수를 이용하여 MyProcessWorker 객체를 생성한다.
        MyProcessWorker myProcessWorker = new MyProcessWorker(processNo, null);

        System.out.println("Process ["+processNo+"] Thread Workers: " + threadWorkers);
    }
}
