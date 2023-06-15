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
