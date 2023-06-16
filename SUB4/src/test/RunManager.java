package test;

import java.io.IOException;

public class RunManager {

	public static void main(String[] args) {

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
				Thread.sleep(1000);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
