하기 내용을 반드시 숙지하고 시험 응시 바랍니다. 

package test;

public class Worker extends AbstractWorker {
	
	/*
	 * ※ Worker 생성
	 * - <Queue 번호>를 파라미터로 하여 Worker 인스턴스 생성
	 */
	public Worker(int queueNo);

	/*
	 * ※ Worker 생성
	 * - <Queue 번호>와 백업된 <Store>를 파라미터로 하여 Worker 인스턴스 생성
	 */
	public Worker(int queueNo, List<String> store);

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
	
	/*
	 * ※ Worker의 Store 추출
	 * - 백업을 위해 Worker의 Store 추출
	 */
	public List<String> getStore();
}

+-----------------------------------------------------------------------------------+
|                                        주의                                       |
+-----------------------------------------------------------------------------------+

※ 5번 소문항의 Worker.java와 4번 소문항의 Worker.java의 내용이 상이하므로,
   removeExpiredStoreItems() 메소드의 내용만 복사하여 사용하세요.

※ 이미 Worker.java 파일을 4번 소문항의 Worker.java 파일로 overwrite한 경우
   5번 소문항 홈의 'Worker백업' 폴더 내의 Worker.java 파일로 원복하세요.
