* ��Ƽ������ �񵿱�(asynchronous) ���α׷� ����


* HTTP ������ ��û�ؼ� Input Queue �Է¹ޱ�

request)
GET http://127.0.0.1:8080/queueInfo

response ��)
{ "processCount":2, �� Process ����
  "threadCount":2,  �� Thread ����
  "outputQueueBatchSize":2, �� Output Queue �ϰ� ó�� ����
  "inputQueueCount":3,                             �� Input Queue ����
  "inputQueueURIs":["http://127.0.0.1:8010/input", �� Input Queue(0)�� URI
  "http://127.0.0.1:8011/input",                   �� Input Queue(1)�� URI
  "http://127.0.0.1:8012/input"],                  �� Input Queue(2)�� URI
  "outputQueueURI":"http://127.0.0.1:9010/output"  �� Output Queue�� URI
}

* Input Queue ������ ���� Worker�� ��Ƽ ���μ���, ��Ƽ ������� ����

processCount: 2    �� 2���� ���μ����� ����
threadCount: 2     �� �� ���μ��� �ȿ� 2���� ������� ����
inputQueueCount: 7 �� 7���� Worker �����ؼ� ���μ���/������ ���� ���ư��鼭 ������� �ϳ��� �Ҵ�

---------- Process #0 --------------  ---------- Process #1 --------------
|-- Thread #0--|  |-- Thread #1 --|   |-- Thread #0--|  |-- Thread #1 --|
|   Worker #0  |  |   Worker #1   |   |   Worker #2  |  |   Worker #3   |
|   Worker #4  |  |   Worker #5   |   |   Worker #6  |  |               |

Worker�� �ڽ��� �Ҵ�� Process/Thread���� ����Ǿ�� �ϸ�, �ٸ� Process/Thread���� ����Ǹ� �ȵ�
Worker�� �������� �ʴ� ���� Ŭ������ �����Ӱ� ���� ����


* HTTP ������ Output Queue ����ϱ�

���� ���μ����� �Ҵ�� ��� Worker���� ���� ����� outputQueueBatchSize(2) ��ŭ ���̸�
Output Queue�� HTTP POST ��û�� ������ ���
��, ���� �� 2�ʸ� �ʰ��ϵ��� ������ Worker�� ���� ����� �����ϸ� ���� ������� ��� Worker���� ���� ����� ���
Output Queue�� HTTP POST ��û�� ������ ���

request)
POST http://127.0.0.1:9010/output
request ����)
{ "result":["Worker(0):Matched AD3",
            "Worker(1):Matched AD2"] }


         |                            | Process #0                         | Process #1                         | Process #2
 �ó�����  | Controller ����             | Thread #0 | Thread #1  | Thread #2 | Thread #0 | Thread #1  | Thread #2 | Thread #0 | Thread #1  | Thread #2 |
---------|----------------------------|------------|-----------|-----------|------------|-----------|-----------| -----------|-----------|-----------|
 1       | Process ����: 2             | Worker(0)  | Worker(1) |           | Worker(2)  | Worker ����|           |            |           |           |
         | Thread ����: 2              |            |           |           |            |           |           |            |           |           |
         | Input Queue ����: 3         |            |           |           |            |           |           |            |           |           |
         | Output Queue ����: ?        |            |           |           |            |           |           |            |           |           |
         | Output Queue Batch Size: ? |            |           |           |            |           |           |            |           |           |
---------|----------------------------|------------|-----------|-----------|------------|-----------|-----------| -----------|-----------|-----------|
 2       | Process ����: 1             | Worker(0)  | Worker(1) | Worker(2) |            |           |           |            |           |           |
         | Thread ����: 3              | Worker(3)  | Worker(4) | Worker(5) |            |           |           |            |           |           |
         | Input Queue ����: 6         |            |           |           |            |           |           |            |           |           |
         | Output Queue ����: ?        |            |           |           |            |           |           |            |           |           |
         | Output Queue Batch Size: ? |            |           |           |            |           |           |            |           |           |
---------|----------------------------|------------|-----------|-----------|------------|-----------|-----------| -----------|-----------|-----------|
 3       | Process ����: 2             | Worker(0)  | Worker(1) | Worker(2) | Worker(3)  | Worker(4) | Worker(5) |           |           |            |
         | Thread ����: 3              | Worker(6)  | Worker(7) | Worker(8) | Worker(9)  |           |           |            |           |           |
         | Input Queue ����: 10        |            |           |           |            |           |           |            |           |           |
         | Output Queue ����: 1        |            |           |           |            |           |           |            |           |           |
         | Output Queue Batch Size: 2 |            |           |           |            |           |           |            |           |           |
---------|----------------------------|------------|-----------|-----------|------------|-----------|-----------| -----------|-----------|-----------|
 3       | Process ����: 3             | Worker(0)  | Worker(1) |           | Worker(2)  | Worker(3) |           | Worker(4)  | Worker(5) |           |
         | Thread ����: 2              | Worker(6)  | Worker(7) |           | Worker(8)  | Worker(9) |           |            |           |           |
         | Input Queue ����: 10        |            |           |           |            |           |           |            |           |           |
         | Output Queue ����: 1        |            |           |           |            |           |           |            |           |           |
         | Output Queue Batch Size: 2 |            |           |           |            |           |           |            |           |           |


* Worker ����

package test;

public class Worker extends AbstractWorker {

	/*
	 * �� Worker ����
	 * - <Queue ��ȣ>�� �Ķ���ͷ� �Ͽ� Worker �ν��Ͻ� ����
	 */
	public Worker(int queueNo);

	/*
	 * �� Worker ����
	 * - Input Queue �����Ͱ� �ԷµǸ� <Timestamp>, <Value>�� �Ķ���ͷ� �Ͽ� <Queue ��ȣ>�� �ش��ϴ� Worker ����
	 */
	public String run(long timestamp, String value);

	/*
	 * �� ����� Store �׸� ����
	 * - Worker ���� �� �Էµ� Timestamp�� �������� ����ð�(3000)�� �ʰ��� �׸��� Store���� ����
	 */
	public void removeExpiredStoreItems(long timestamp, List<String> store);
}
