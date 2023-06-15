package test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RunManager {

	public static void main(String[] args) {
		try {
			testOnHttp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testOnHttp() throws Exception {

		//MyClient client = new MyClient();
		HttpClient client = new HttpClient();
		client.start();
		Request request = client.newRequest("http://127.0.0.1:8080/queueInfo")
				.method(HttpMethod.GET)
				.timeout(0, TimeUnit.MINUTES)
				.idleTimeout(0, TimeUnit.MINUTES);
		ContentResponse response = request.send();
		String queueInfoText = "";
		if (response.getStatus() == 200) {
			queueInfoText = response.getContentAsString();
		}
		//String queueInfoText = client.get("http://127.0.0.1:8080/queueInfo");
		Gson gson = new Gson();
		JsonObject queueInfoJO = gson.fromJson(queueInfoText, JsonObject.class);
		int queueCount = queueInfoJO.get("inputQueueCount").getAsInt();
		LinkedList<String> inputQueueURIs = MyJson.convertJsonArrayToStringList(queueInfoJO.get("inputQueueURIs").getAsJsonArray());
		String outputQueueURI = queueInfoJO.get("outputQueueURI").getAsString();

		HashMap<Integer, Worker> workerHashMap = new HashMap<>();

		for (int i = 0; i < inputQueueURIs.size(); i++) {
			int queueNo = i;
			if (!workerHashMap.containsKey(i)) {
				workerHashMap.put(i, new Worker(i));
			}
			Thread thread = new Thread(() -> {
				String threadName = Thread.currentThread().getName() + " [" + queueNo + "] "+ inputQueueURIs.get(queueNo);
				try {
					while(true) {
						Request req = client.newRequest(inputQueueURIs.get(queueNo))
								.method(HttpMethod.GET)
								.timeout(0, TimeUnit.MINUTES)
								.idleTimeout(0, TimeUnit.MINUTES);
						ContentResponse resp = req.send();
						String inputStr = "";
						if (resp.getStatus() == 200) {
							inputStr = resp.getContentAsString();
						}
						System.out.println(threadName + " " + inputStr);
						JsonObject inputJson = MyJson.convertStringToJsonObject(inputStr);

						// �޾ƿ� �����͸� �̿��� Worker ����
						int timestamp = inputJson.get("timestamp").getAsInt();
						String value = inputJson.get("value").getAsString();
						String result = workerHashMap.get(queueNo).run(timestamp, value);
						System.out.println(threadName + " result from Worker: " + result);

						// Worker ���� ��� ��ȯ
						if (result != null) {
							System.out.println(threadName + " to " + outputQueueURI);
							JsonObject outputJson = new JsonObject();
							outputJson.addProperty("result", result);
							req = client.newRequest(outputQueueURI)
									.method(HttpMethod.POST)
									.timeout(0, TimeUnit.MINUTES)
									.idleTimeout(0, TimeUnit.MINUTES)
									.content(new StringContentProvider(outputJson.toString()));
							resp = req.send();
							String outputStr = "";
							if (resp.getStatus() == 200) {
								outputStr = resp.getContentAsString();
							}
							//String outputStr = client.post(outputQueueURI, MyJson.convertJsonObjectToString(outputJson));
							System.out.println(threadName + " output : " + outputStr);
						}
					}

				} catch (Exception e) {
					System.out.println(threadName + " ERROR ");
					e.printStackTrace();
				}
			});
			thread.start();
		}

		Thread.sleep(20000);

		client.stop();

//		ExecutorService executorService = Executors.newFixedThreadPool(inputQueueURIs.size());

//		// URL ����� ��ȸ�ϸ鼭 ��û�� �����ϴ�.
//		List<Future<String>> futures = new ArrayList<>();
//		for (int i = 0; i < inputQueueURIs.size(); i++) {
//			String url = inputQueueURIs.get(i);
//			int queueNo = i;
//			if (!workerHashMap.containsKey(i)) {
//				workerHashMap.put(i, new Worker(i));
//			}
//			Future<String> future = executorService.submit(() -> {
//				try {
//					// �Է� ������ ��û
//					String threadName = Thread.currentThread().getName() + " [" + queueNo + "] ";
//					System.out.println(threadName + " url : " + url);
//					Request req = client.newRequest(url)
//							.method(HttpMethod.GET)
//							.timeout(0, TimeUnit.MINUTES)
//							.idleTimeout(0, TimeUnit.MINUTES);
//					ContentResponse resp = req.send();
//					//String inputStr = client.get(url);
//					String inputStr = "";
//					if (resp.getStatus() == 200) {
//						inputStr = resp.getContentAsString();
//					}
//					System.out.println(threadName + " input : " + inputStr);
//					JsonObject inputJson = MyJson.convertStringToJsonObject(inputStr);
//
//					// �޾ƿ� �����͸� �̿��� Worker ����
//					int timestamp = inputJson.get("timestamp").getAsInt();
//					String value = inputJson.get("value").getAsString();
//					String result = workerHashMap.get(queueNo).run(timestamp, value);
//					System.out.println(threadName + " result from Worker: " + result);
//
//					// Worker ���� ��� ��ȯ
//					if (result != null) {
//						JsonObject outputJson = new JsonObject();
//						outputJson.addProperty("result", result);
//						req = client.newRequest(outputQueueURI)
//								.method(HttpMethod.POST)
//								.timeout(0, TimeUnit.MINUTES)
//								.idleTimeout(0, TimeUnit.MINUTES);
//						resp = req.send();
//						String outputStr = "";
//						if (resp.getStatus() == 200) {
//							outputStr = resp.getContentAsString();
//						}
//						//String outputStr = client.post(outputQueueURI, MyJson.convertJsonObjectToString(outputJson));
//						System.out.println(threadName + " output : " + outputStr);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				} finally {
//					return null;
//				}
//			});
//			futures.add(future);
//		}
//
//		// ��� ��û�� ����� ��ٸ��ϴ�.
//		for (Future<String> future : futures) {
//			String r = null;
//			try {
//				System.out.println("future: "+ future.toString());
//				r = future.get();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			System.out.println(r);
//		}
//
//		// URL ����� ��ȸ�ϸ鼭 ��û�� �����ϴ�.
//		List<CompletableFuture<String>> futures = inputQueueURIs.stream()
//				.map(url -> CompletableFuture.supplyAsync(() -> {
//					try {
//						while (true) {
//							System.out.println(Thread.currentThread().getName() + " : url : " + url);
//							String inputStr = client.get(url);
//							System.out.println(Thread.currentThread().getName() + " : input : " + inputStr);
//							JsonObject inputJson = MyJson.convertStringToJsonObject(inputStr);
//							JsonObject outputJson = new JsonObject();
//							outputJson.addProperty("result", inputJson.get("value").getAsString());
//							String outputStr = client.post(outputQueueURI, MyJson.convertJsonObjectToString(outputJson));
//							System.out.println(Thread.currentThread().getName() + " : output : " + outputStr);
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					} finally {
//						return "";
//					}
//				}, executorService))
//				.collect(Collectors.toList());

		// ��� ��û�� ����� ��ٸ��ϴ�.
		// ���� ����� �߿��� �� �ƴϰ�, ��������� ������ �ʵ��� �ϴ� ����
//		for (CompletableFuture<String> future : futures) {
//			String response = future.get();
//			System.out.println(response);
//		}

//		int port = 8080;
//		MyServer server = MyServer.getInstance(port);
//		while (true) {
//			Thread.sleep(1000L);
//		}

		//executorService.shutdown();
		//client.stop();
	}

	public static void testOnConsole() {
		HashMap<Integer, Worker> workerHashMap = new HashMap<>();

		Scanner sc = new Scanner(System.in);
		while (true) {
			String line = sc.nextLine();
			if (line.equals("exit")) {
				break;
			} else {
				String[] commands = MyString.splitToStringArray(line, " ", true);
				long timestamp = Integer.parseInt(commands[0]);
				int queueNo = Integer.parseInt(commands[1]);
				String value = commands[2];
				if (!workerHashMap.containsKey(queueNo)) {
					workerHashMap.put(queueNo, new Worker(queueNo));
				}
				String result = workerHashMap.get(queueNo).run(timestamp, value);
				if (result != null) {
					System.out.println(result);
				}
			}
		}
	}
}
