package test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;

import java.util.*;
import java.util.concurrent.*;

public class RunManager {

	public static void main(String[] args) {
		try {
			testOnHttp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Thread를 사용한 멀티스레드 비동기 실행
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
		String queueInfoText = "";
		if (response.getStatus() == 200) {
			queueInfoText = response.getContentAsString();
		}

		// String 변수로 받은 Response 분문을 JsonObject 변수에 담기
		Gson gson = new Gson();
		JsonObject queueInfoJO = gson.fromJson(queueInfoText, JsonObject.class);

		// 원격 서버에서 제공해준 정보를 변수들에 나눠담기
		int queueCount = queueInfoJO.get("inputQueueCount").getAsInt();
		LinkedList<String> inputQueueURIs = MyJson.convertJsonArrayToStringList(queueInfoJO.get("inputQueueURIs").getAsJsonArray());
		String outputQueueURI = queueInfoJO.get("outputQueueURI").getAsString();

		// 프로그램에서 사용할 Worker(들)을 담을 변수 선언
		HashMap<Integer, Worker> workerHashMap = new HashMap<>();

		// 입력용 큐 URI 별 작업 처리
		for (int i = 0; i < inputQueueURIs.size(); i++) {

			// Worker(들)을 담을 변수에 Worker 인스턴스 생성해서 담기
			int queueNo = i;
			if (!workerHashMap.containsKey(i)) {
				workerHashMap.put(i, new Worker(i));
			}

			// Thread 변수 선언
			// 스레드 안에서 할 작업 내용은 람다식(Lambda expression)으로 작성됨
			Thread thread = new Thread(() -> {

				// 스레드 이름
				String threadName = Thread.currentThread().getName() + " [" + queueNo + "] "+ inputQueueURIs.get(queueNo);

				try {

					// 무한하게 실행
					while(true) {

						// HttpClient로 보낼 GET Request 작성, timeout 없음
						Request req = client.newRequest(inputQueueURIs.get(queueNo))
								.method(HttpMethod.GET)
								.timeout(0, TimeUnit.MINUTES)
								.idleTimeout(0, TimeUnit.MINUTES);

						// Request 보내고 응답받기
						ContentResponse resp = req.send();

						// Response 본문을 String 변수로 받기
						String inputStr = "";
						if (resp.getStatus() == 200) {
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
							resp = req.send();
							// 정상 응답을 받은 경우
							if (resp.getStatus() == 200) {
								// 응답받은 본문을 화면에 출력
								System.out.println(resp.getContentAsString());
							}

						}
					}

				} catch (Exception e) {
					System.out.println(threadName + " ERROR ");
					e.printStackTrace();
				}
			});

			// 스레드 시작 - 비동기(Async)로 시작됨
			thread.start();
		}

		// 프로그램 종료까지 20초 대기
		Thread.sleep(20000);

		// HttpClient 정지
		client.stop();

	}

}

