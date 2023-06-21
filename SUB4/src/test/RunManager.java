package test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

	// CompletableFuture를 사용한 비동기 실행
	public static void testOnHttp() throws Exception {

		// HttpClient 작성과 시작
		HttpClient client = new HttpClient();
		client.start();

		// HttpClient로 보낼 GET Request 작성, timeout 없음
		Request request = client.newRequest("http://127.0.0.1:8080/queueInfo")
				.method(HttpMethod.GET)
				.timeout(0, TimeUnit.MINUTES)
				.idleTimeout(0, TimeUnit.MINUTES);

		// Request 보내고 응답받기
		ContentResponse response = request.send();

		// Response 본문을 String 변수로 받기
		String configText = "";
		if (response.getStatus() == 200) {
			configText = response.getContentAsString();
		}

		// String 변수로 받은 Response 분문을 JsonObject 변수에 담기
		Gson gson = new Gson();
		JsonObject configJo = gson.fromJson(configText, JsonObject.class);

		// 원격 서버에서 제공해준 정보를 변수들에 나눠담기
		int processCount = configJo.get("processCount").getAsInt();
		int threadCount = configJo.get("threadCount").getAsInt();
		int outputQueueBatchSize = configJo.get("outputQueueBatchSize").getAsInt();
		int inputQueueCount = configJo.get("inputQueueCount").getAsInt();
		int queueCount = configJo.get("inputQueueCount").getAsInt();
		LinkedList<String> inputQueueURIs = MyJson.convertJsonArrayToStringList(configJo.get("inputQueueURIs").getAsJsonArray());
		String outputQueueURI = configJo.get("outputQueueURI").getAsString();

		// 프로세스 > 스레드 > Worker 순으로 리스트를 만들어서 각각에 포함되어야 할 Worker 번호 할당
		int workerNo = 0;
		LinkedHashMap<Integer, Object> processMap = new LinkedHashMap();
		for (int processNo = 0; processNo < processCount; processNo++) {
			LinkedHashMap<Integer, Object> threadMap = new LinkedHashMap<>();
			processMap.put(processNo, threadMap);
			for (int threadNo = 0; threadNo < threadCount; threadNo++) {
				LinkedList<Object> workerList = new LinkedList<>();
				threadMap.put(threadNo, workerList);
				if (workerNo < inputQueueCount) {
					workerList.add(workerNo);
					workerNo++;
					continue;
				}
			}
		}

		String basePath = Paths.get("").toAbsolutePath().toString();
		basePath = basePath + "/out/production/SUB4";
		System.out.println("Current Path: " + basePath);

		// 프로세스를 실행할 ProcessBuilder를 담을 LinkedList 변수 선언
		LinkedList<ProcessBuilder> processBuilderLinkedList = new LinkedList<>();
		// 프로세스 실행을 위한 ProcessBuilder 생성 및 설정
		for (int processNo = 0; processNo < processMap.size(); processNo++) {
			// 프로세스 실행을 위한 ProcessBuilder 생성
			ProcessBuilder processBuilder = new ProcessBuilder();
			// 프로세스 실행을 위한 ProcessBuilder 설정
			processBuilder.directory(new File(basePath));
			processBuilder.redirectErrorStream(true);
			// 프로세스 실행을 위한 ProcessBuilder에 전달할 파라미터 생성
			String param = gson.toJson(processMap.get(processNo)).toString();
			// 프로세스 실행을 위한 ProcessBuilder에 전달할 파라미터 설정
			processBuilder.command("java", "-cp", basePath, "test/MyProcessWorker", Integer.toString(processNo), param);
			// 프로세스 실행을 위한 ProcessBuilder를 담을 LinkedList 변수에 담기
			processBuilderLinkedList.add(processBuilder);

			// 프로세스 실행
			try {
				// 프로세스 실행
				Process process = processBuilder.start();
				// 프로세스 실행 후 출력 스트림을 통해 출력되는 내용을 읽어서 출력
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

		Thread.sleep(1000 * 60 * 60 * 24);

		// 프로그램에서 사용할 Worker(들)을 담을 변수 선언
		HashMap<Integer, Worker> workerHashMap = new HashMap<>();

		// CompletableFuture 생성
		List<CompletableFuture<Void>> futures = new ArrayList<>();

		// 입력용 큐 URI 별 작업 처리
		for (int i = 0; i < inputQueueURIs.size(); i++) {

			// Worker(들)을 담을 변수에 Worker 인스턴스 생성해서 담기
			int queueNo = i;
			if (!workerHashMap.containsKey(i)) {
				workerHashMap.put(i, new Worker(i));
			}

			// CompletableFuture 변수 선언
			// 람다식(Lambda expression)으로 작성
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {

				while (true) {

					// HttpClient로 보낼 GET Request 작성, timeout 없음
					Request req = client.newRequest(inputQueueURIs.get(queueNo))
							.method(HttpMethod.GET)
							.timeout(0, TimeUnit.MINUTES)
							.idleTimeout(0, TimeUnit.MINUTES);

					// Request 보내고 응답받기
					ContentResponse resp = null;
					try {
						resp = req.send();
					} catch (Exception e) {
						e.printStackTrace();
					}

					// Response 본문을 String 변수로 받기
					String inputStr = "";
					if (resp != null && resp.getStatus() == 200) {
						inputStr = resp.getContentAsString();
					}

					// String 변수로 받은 Response 분문을 JsonObject 변수에 담기
					JsonObject inputJson = MyJson.convertStringToJsonObject(inputStr);

					// JsonObject 변수에 담긴 데이터를 이용해서 Worker 인스턴스에 작업을 시킴
					int timestamp = inputJson.get("timestamp").getAsInt();
					String value = inputJson.get("value").getAsString();
					String result = workerHashMap.get(queueNo).run(timestamp, value);

					// Worker 인스턴스로부터 받은 작업 결과가 있을 때만 output 용도의 Request 보냄
					if (result != null) {

						// output 용도로 보낼 Json을 담을 JsonObject 변수 생성
						JsonObject outputJson = new JsonObject();
						// JsonObject 변수에 result 속성 추가
						outputJson.addProperty("result", result);
						// JsonObject 변수를 request 본문에 담을 수 있게 StringContentProvider 변수에 담기
						StringContentProvider outputStr = new StringContentProvider(outputJson.toString());
						// output 용도로 HttpClient로 보낼 POST Request 작성
						// timeout 없으며 앞서 작성한 StringContentProvider를 본문에 담음
						req = client.newRequest(outputQueueURI)
								.method(HttpMethod.POST)
								.timeout(0, TimeUnit.MINUTES)
								.idleTimeout(0, TimeUnit.MINUTES)
								.content(outputStr);
						// Request 보내고 응답받기
						resp = null;
						try {
							resp = req.send();
						} catch (Exception e) {
							e.printStackTrace();
						}
						// 정상 응답을 받은 경우
						if (resp != null && resp.getStatus() == 200) {
							// 응답받은 본문을 화면에 출력
							System.out.println(resp.getContentAsString());
						}
					}

				}
			});

			// CompletableFuture를 futures 리스트에 추가
			futures.add(future);
		}

		// CompletableFuture를 모두 조합해서 실행
		// 모든 스레드들이 끝날 떄까지 대기
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		// HttpClient 정지
		client.stop();

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
