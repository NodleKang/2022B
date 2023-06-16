package test;

import java.io.IOException;
import java.nio.file.Paths;

public class RunManager {

	public static void main(String[] args) {

		System.out.println("RunManager.main()");
		System.out.println("Current Process ID: " + ProcessHandle.current().pid());
		System.out.println("Current Process Name: " + ProcessHandle.current().info().command().orElse("unknown"));


		try {
			test();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void test() throws Exception {

		int processCount = 2;
		int threadCount = 3;
		
		String currentPath = Paths.get("").toAbsolutePath().toString();

		for (int i=0; i < processCount; i++) {
			try {
				//String command = "javac "+currentPath+ "\\src\\test\\" + "ThreadTest.java";
				String command = "java -version";
				Process process = new ProcessBuilder("java", "-version").start();
				System.out.println(process.info());
				System.out.println(process.toString());
				System.out.println(process.isAlive());
				process.destroy();
				System.out.println(process.exitValue());
				process.info().command().ifPresent(System.out::println);
				Thread.sleep(1000);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
