package test;

import java.util.*;

public class ThreadTest {
	
	static public List<Integer> arrList;
	
	public ThreadTest() {
		arrList = new ArrayList<Integer>();
	}
	
	public void start() {

		System.out.println("ThreadTest.start()");
		Runnable putWorker = new PutWorker();
		Runnable getWorker = new GetWorker();
		
		Thread pThread = new Thread(putWorker);
		Thread gThread = new Thread(getWorker);
		
		pThread.start();
		gThread.start();
		System.out.println("ThreadTest.end()");
	}
	
	public static void main(String[] args) {
		new ThreadTest().start();
	}
	
	class PutWorker implements Runnable {
		
		public void run() {
			
			try {
				Thread.sleep(1000L);
				ThreadTest.arrList.add(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	class GetWorker implements Runnable {
		public void run() {
			try {
				Thread.sleep(2000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}
}
