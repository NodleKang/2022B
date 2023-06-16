package test;

import java.io.*;

public class ExeRunner {
	
	public static void main(String[] args) {

		try {
			test();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void test() {
		String path = MyFile.getCurrentDirectoryFullPath();
		path = path + "\\SUB4\\src";

		ProcessBuilder pb = new ProcessBuilder("javac", "-cp", ".", "test/ThreadTest.java");
		pb.directory(new File(path)); // 명령어가 실행될 경로
		pb.redirectErrorStream(true); // 명령어 실행 도중에 오류가 발생하면 getInputStream()에서 오류 메시지도 함께 읽어온다.

		try {
			Process process = pb.start();

			InputStream inputStream = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void test2() {

		try {
			String path = MyFile.getCurrentDirectoryFullPath();
			path = "C:\\sp_workspace\\2022B\\SUB4\\src\\test";
			System.out.println("Current Path: " + path);

			//ProcessBuilder pb = new ProcessBuilder("실행파일", "아규먼트");
			/*
			C:\sp_workspace\2022B\SUB4\src>java -cp . test/ThreadTest.java
			ThreadTest.start()
			ThreadTest.end()
			 */
			ProcessBuilder pb = new ProcessBuilder("javac", "ThreadTest.java");
			pb = new ProcessBuilder("javac", "-cp", ".", "test/ThreadTest.java");
			pb.directory(new File(path)); // 명령어가 실행될 경로
			pb.redirectErrorStream(true); // 명령어 실행 도중에 오류가 발생하면 getInputStream()에서 오류 메시지도 함께 읽어온다.
			Process process = pb.start();

			// 프로세스 정보 출력
			System.out.println(process.info());

			// 프로세스에서 데이터 읽어오기
			// process.getInputStream() : 프로세스에서 출력하는 데이터를 읽어오는 스트림
			InputStream inputStream = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			int complieResult = -1;
			try {
				complieResult = process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (complieResult == 0) {
				System.out.println("Compile Success");
				path = MyFile.getParentDirectoryFullPath(path);
				pb.directory(new File(path)); // 명령어가 실행될 경로
				pb.command("java", "test.ThreadTest");
				process = pb.start();
			} else {
				System.out.println("Compile Fail");
			}

			// 프로세스 정보 출력
			System.out.println(process.info());

			inputStream = process.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream, "MS949"));
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			// 프로세스 종료까지 대기
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			/*
			// 프로세스 종료
			if (process.isAlive()) {
				process.destroy();
			}
			// 프로세스로 데이터 보내기
			// process.getOutputStream() : 프로세스로 데이터를 보내는 스트림
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			writer.write("보내는 문자열");
			writer.flush();
			*/

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
