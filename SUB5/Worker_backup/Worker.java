package test;

import com.lgcns.test.AbstractWorker;

import java.util.List;

/* ----------------------------------------------------------------------------
 * 
 * Worker.java - removeExpiredStoreItems() 구현, 그 외 변경 금지
 * 
 * ----------------------------------------------------------------------------
 */
public class Worker extends AbstractWorker {
	
	/*
	 * ※ Worker 생성
	 * - <Queue 번호>를 파라미터로 하여 Worker 인스턴스 생성
	 */
	public Worker(int queueNo) {
		super(queueNo);
	}
	
	/*
	 * ※ Worker 생성
	 * - <Queue 번호>와 백업된 <Store>를 파라미터로 하여 Worker 인스턴스 생성
	 */
	public Worker(int queueNo, List<String> store) {
		super(queueNo, store);
	}
	
	/*
	 * ※ 만료된 Store Item 제거
	 * - 입력된 Timestamp와 Store Item의 Timestamp간의 차이가 만료시간(3000)을 초과하면 Store에서 제거
	 */
	public void removeExpiredStoreItems(long timestamp, List<String> store) {
		// 아래 라인을 지우고 만료된 Store Item 제거 기능을 구현하세요.
		throw new UnsupportedOperationException("removeExpiredStoreItems()를 4번 문항에서 복사하세요.");
	}
}
