package test;

import java.io.IOException;

public class RunManager {

	public static void main(String[] args) {

		System.out.println("RunManager.main()");
		System.out.println("Current Process ID: " + ProcessHandle.current().pid());
		System.out.println("Current Process Name: " + ProcessHandle.current().info().command().orElse("unknown"));


		try {
			testOnHttp();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void testOnHttp() throws Exception {

		int processCount = 2;
		int threadCount = 3;

		for (int i=0; i < processCount; i++) {
			try {
				Process process = new ProcessBuilder("notepad").start();
				process.info().command().ifPresent(System.out::println);
				Thread.sleep(1000);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
