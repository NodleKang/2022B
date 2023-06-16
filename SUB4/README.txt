* 멀티스레드 비동기(asynchronous) 프로그램 구현


* HTTP 서버에 요청해서 Input Queue 입력받기

request)
GET http://127.0.0.1:8080/queueInfo

response 예)
{ "processCount":2, → Process 개수
  "threadCount":2,  → Thread 개수
  "outputQueueBatchSize":2, → Output Queue 일괄 처리 개수
  "inputQueueCount":3,                             → Input Queue 개수
  "inputQueueURIs":["http://127.0.0.1:8010/input", → Input Queue(0)의 URI
  "http://127.0.0.1:8011/input",                   → Input Queue(1)의 URI
  "http://127.0.0.1:8012/input"],                  → Input Queue(2)의 URI
  "outputQueueURI":"http://127.0.0.1:9010/output"  → Output Queue의 URI
}

* Input Queue 정보에 따라 Worker를 멀티 프로세스, 멀티 스레드로 생성

processCount: 2    → 2개의 프로세스로 생성
threadCount: 2     → 각 프로세스 안에 2개의 스레드로 생성
inputQueueCount: 7 → 7개의 Worker 생성해서 프로세스/스레드 마다 돌아가면서 순서대로 하나씩 할당

---------- Process #0 --------------  ---------- Process #1 --------------
|-- Thread #0--|  |-- Thread #1 --|   |-- Thread #0--|  |-- Thread #1 --|
|   Worker #0  |  |   Worker #1   |   |   Worker #2  |  |   Worker #3   |
|   Worker #4  |  |   Worker #5   |   |   Worker #6  |  |               |

Worker는 자신이 할당된 Process/Thread에서 실행되어야 하며, 다른 Process/Thread에서 실행되면 안됨
Worker를 포함하지 않는 보조 클래스는 자유롭게 구현 가능


* HTTP 서버로 Output Queue 출력하기

단일 프로세스에 할당된 모든 Worker들의 실행 결과가 outputQueueBatchSize(2) 만큼 모이면
Output Queue에 HTTP POST 요청을 보내서 출력
단, 생성 후 2초를 초과하도록 지연된 Worker의 실행 결과가 존재하면 전송 대기중인 모든 Worker들의 실행 결과를 즉시
Output Queue에 HTTP POST 요청을 보내서 출력

request)
POST http://127.0.0.1:9010/output
request 본문)
{ "result":["Worker(0):Matched AD3",
            "Worker(1):Matched AD2"] }


         |                            | Process #0                         | Process #1                         | Process #2
 시나리오  | Controller 수신             | Thread #0 | Thread #1  | Thread #2 | Thread #0 | Thread #1  | Thread #2 | Thread #0 | Thread #1  | Thread #2 |
---------|----------------------------|------------|-----------|-----------|------------|-----------|-----------| -----------|-----------|-----------|
 1       | Process 개수: 2             | Worker(0)  | Worker(1) |           | Worker(2)  | Worker 없음|           |            |           |           |
         | Thread 개수: 2              |            |           |           |            |           |           |            |           |           |
         | Input Queue 개수: 3         |            |           |           |            |           |           |            |           |           |
         | Output Queue 개수: ?        |            |           |           |            |           |           |            |           |           |
         | Output Queue Batch Size: ? |            |           |           |            |           |           |            |           |           |
---------|----------------------------|------------|-----------|-----------|------------|-----------|-----------| -----------|-----------|-----------|
 2       | Process 개수: 1             | Worker(0)  | Worker(1) | Worker(2) |            |           |           |            |           |           |
         | Thread 개수: 3              | Worker(3)  | Worker(4) | Worker(5) |            |           |           |            |           |           |
         | Input Queue 개수: 6         |            |           |           |            |           |           |            |           |           |
         | Output Queue 개수: ?        |            |           |           |            |           |           |            |           |           |
         | Output Queue Batch Size: ? |            |           |           |            |           |           |            |           |           |
---------|----------------------------|------------|-----------|-----------|------------|-----------|-----------| -----------|-----------|-----------|
 3       | Process 개수: 2             | Worker(0)  | Worker(1) | Worker(2) | Worker(3)  | Worker(4) | Worker(5) |           |           |            |
         | Thread 개수: 3              | Worker(6)  | Worker(7) | Worker(8) | Worker(9)  |           |           |            |           |           |
         | Input Queue 개수: 10        |            |           |           |            |           |           |            |           |           |
         | Output Queue 개수: 1        |            |           |           |            |           |           |            |           |           |
         | Output Queue Batch Size: 2 |            |           |           |            |           |           |            |           |           |
---------|----------------------------|------------|-----------|-----------|------------|-----------|-----------| -----------|-----------|-----------|
 3       | Process 개수: 3             | Worker(0)  | Worker(1) |           | Worker(2)  | Worker(3) |           | Worker(4)  | Worker(5) |           |
         | Thread 개수: 2              | Worker(6)  | Worker(7) |           | Worker(8)  | Worker(9) |           |            |           |           |
         | Input Queue 개수: 10        |            |           |           |            |           |           |            |           |           |
         | Output Queue 개수: 1        |            |           |           |            |           |           |            |           |           |
         | Output Queue Batch Size: 2 |            |           |           |            |           |           |            |           |           |


* Worker 구현

package test;

public class Worker extends AbstractWorker {

	/*
	 * ※ Worker 생성
	 * - <Queue 번호>를 파라미터로 하여 Worker 인스턴스 생성
	 */
	public Worker(int queueNo);

	/*
	 * ※ Worker 실행
	 * - Input Queue 데이터가 입력되면 <Timestamp>, <Value>를 파라미터로 하여 <Queue 번호>에 해당하는 Worker 실행
	 */
	public String run(long timestamp, String value);

	/*
	 * ※ 만료된 Store 항목 제거
	 * - Worker 실행 시 입력된 Timestamp를 기준으로 만료시간(3000)을 초과한 항목을 Store에서 제거
	 */
	public void removeExpiredStoreItems(long timestamp, List<String> store);
}
